import json, time, uuid
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Any
from db.models import get_db
from integrations.ujin_adapter import adapter
from services.rule_engine import rule_engine

router = APIRouter()

OPS = {"eq": "==", "ne": "!=", "gt": ">", "lt": "<", "ge": ">=", "le": "<=",
       "==": "==", "!=": "!=", ">": ">", "<": "<", ">=": ">=", "<=": "<="}

class ScenarioIn(BaseModel):
    name: str = ""
    enabled: bool = True
    rule: dict | None = None
    trigger: dict | None = None
    conditions: list = []
    actions: list = []
    layout: dict | None = None


def _norm_op(op: Any) -> str:
    return OPS.get(str(op).lower(), "==")


def _norm_signal(sig: Any) -> str:
    s = str(sig or "")
    for p in ("integrations_devices_", "integrations_"):
        if s.startswith(p):
            s = s[len(p):]
            break
    return s


def _norm_action(act: dict) -> dict:
    if act.get("type"):
        return act
    return {
        "type": "command",
        "id": act.get("id") or f"action-{uuid.uuid4().hex[:8]}",
        "device_id": act.get("device_id", ""),
        "signal": _norm_signal(act.get("signal")),
        "value": act.get("value", 1),
        "class": act.get("class", "integrations_devices"),
    }


def _norm_rule(s: ScenarioIn) -> dict:
    if s.rule:
        rule = dict(s.rule)
    else:
        t = s.trigger or {}
        rule = {
            "trigger": {
                "device_id": t.get("device_id", ""),
                "signal": _norm_signal(t.get("signal")),
                "op": _norm_op(t.get("operator", t.get("op", "eq"))),
                "value": t.get("value", 0),
            },
            "actions": [_norm_action(a) for a in s.actions],
        }
    trig = dict(rule.get("trigger") or {})
    trig.setdefault("device_id", "")
    trig["signal"] = _norm_signal(trig.get("signal"))
    trig["op"] = _norm_op(trig.get("op", trig.get("operator", "eq")))
    trig.setdefault("value", 0)
    rule["trigger"] = trig
    rule["actions"] = [_norm_action(a) for a in rule.get("actions", [])]
    if s.conditions:
        rule["conditions"] = [
            {
                "device_id": c.get("device_id", ""),
                "signal": _norm_signal(c.get("signal")),
                "op": _norm_op(c.get("operator", c.get("op", "eq"))),
                "value": c.get("value", 0),
            }
            for c in s.conditions
        ]
    if s.layout is not None:
        rule["layout"] = s.layout
    return rule


def _present(sid: str, name: str, enabled: bool, rule: dict, created_at: Any = "") -> dict:
    trig = rule.get("trigger", {})
    return {
        "id": sid,
        "name": name,
        "enabled": bool(enabled),
        "rule": rule,
        "trigger": trig,
        "conditions": rule.get("conditions", []),
        "actions": rule.get("actions", []),
        "layout": rule.get("layout", {"nodes": [], "edges": []}),
        "created_at": str(created_at or ""),
    }


@router.get("")
async def list_scenarios():
    db = await get_db()
    try:
        cur = await db.execute("SELECT id, name, enabled, rule_json, created_at FROM scenarios ORDER BY created_at DESC")
        rows = await cur.fetchall()
    finally:
        await db.close()
    return [_present(r["id"], r["name"], r["enabled"], json.loads(r["rule_json"]), r["created_at"]) for r in rows]


@router.post("", status_code=201)
async def create_scenario(s: ScenarioIn):
    sid = str(uuid.uuid4())
    rule = _norm_rule(s)
    db = await get_db()
    try:
        await db.execute("INSERT INTO scenarios(id, name, enabled, rule_json) VALUES (?,?,?,?)",
                         (sid, s.name, int(s.enabled), json.dumps(rule)))
        await db.commit()
    finally:
        await db.close()
    await rule_engine.reload()
    return _present(sid, s.name, s.enabled, rule)


@router.put("/{sid}")
async def update_scenario(sid: str, s: ScenarioIn):
    rule = _norm_rule(s)
    db = await get_db()
    try:
        cur = await db.execute("UPDATE scenarios SET name=?, enabled=?, rule_json=? WHERE id=?",
                               (s.name, int(s.enabled), json.dumps(rule), sid))
        await db.commit()
        if cur.rowcount == 0:
            raise HTTPException(404, "scenario not found")
    finally:
        await db.close()
    await rule_engine.reload()
    return _present(sid, s.name, s.enabled, rule)


@router.patch("/{sid}")
async def toggle_scenario(sid: str, enabled: bool):
    db = await get_db()
    try:
        cur = await db.execute("UPDATE scenarios SET enabled=? WHERE id=?", (int(enabled), sid))
        await db.commit()
        if cur.rowcount == 0:
            raise HTTPException(404)
    finally:
        await db.close()
    await rule_engine.reload()
    return {"ok": True}


@router.delete("/{sid}", status_code=204)
async def delete_scenario(sid: str):
    db = await get_db()
    try:
        cur = await db.execute("DELETE FROM scenarios WHERE id=?", (sid,))
        await db.commit()
        if cur.rowcount == 0:
            raise HTTPException(404)
    finally:
        await db.close()
    await rule_engine.reload()


async def _load_rule(sid: str) -> dict:
    db = await get_db()
    try:
        cur = await db.execute("SELECT rule_json FROM scenarios WHERE id=?", (sid,))
        row = await cur.fetchone()
    finally:
        await db.close()
    if not row:
        raise HTTPException(404, "scenario not found")
    return json.loads(row["rule_json"])


async def _log_run(sid: str, snapshot: dict, results: list) -> int:
    ok = all(r["ok"] for r in results) if results else False
    db = await get_db()
    try:
        cur = await db.execute(
            "INSERT INTO scenario_runs(scenario_id, trigger_snapshot, actions_json, ok) VALUES (?,?,?,?)",
            (sid, json.dumps(snapshot), json.dumps(results), int(ok)),
        )
        await db.commit()
        return cur.lastrowid
    finally:
        await db.close()


@router.post("/{sid}/test")
async def test_scenario(sid: str):
    rule = await _load_rule(sid)
    trig = rule.get("trigger", {})
    results = []
    for act in rule.get("actions", []):
        if act.get("type", "command") != "command":
            continue
        cls = act.get("class", "integrations_devices")
        row = {"sn": act["device_id"], "s": f"{cls}_{act['signal']}",
               "v": act["value"], "c": cls}
        try:
            await adapter.publish([row])
            results.append({"action": act, "ok": True})
        except Exception as exc:
            results.append({"action": act, "ok": False, "error": str(exc)})
    snapshot = {"manual": True, **{k: trig.get(k) for k in ("device_id", "signal", "op", "value")}}
    run_id = await _log_run(sid, snapshot, results)
    return {
        "id": run_id,
        "scenario_id": sid,
        "triggered_at": time.strftime("%Y-%m-%d %H:%M:%S"),
        "trigger_snapshot": json.dumps(snapshot),
        "actions_json": json.dumps(results),
        "ok": int(all(r["ok"] for r in results)) if results else 0,
    }


@router.get("/{sid}/runs")
async def list_runs(sid: str, limit: int = 50):
    db = await get_db()
    try:
        cur = await db.execute("SELECT * FROM scenario_runs WHERE scenario_id=? ORDER BY id DESC LIMIT ?", (sid, limit))
        return [dict(r) for r in await cur.fetchall()]
    finally:
        await db.close()
