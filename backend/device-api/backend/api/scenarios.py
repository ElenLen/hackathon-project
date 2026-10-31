import json, time, uuid
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from db.models import get_db
from integrations.ujin_adapter import adapter
from services.rule_engine import rule_engine

router = APIRouter()

class ScenarioIn(BaseModel):
    name: str
    enabled: bool = True
    rule: dict

@router.get("")
async def list_scenarios():
    db = await get_db()
    try:
        cur = await db.execute("SELECT id, name, enabled, rule_json, created_at FROM scenarios ORDER BY created_at DESC")
        return [{**dict(r), "rule": json.loads(r["rule_json"])} for r in await cur.fetchall()]
    finally:
        await db.close()

@router.post("", status_code=201)
async def create_scenario(s: ScenarioIn):
    sid = str(uuid.uuid4())
    db = await get_db()
    try:
        await db.execute("INSERT INTO scenarios(id, name, enabled, rule_json) VALUES (?,?,?,?)",
                         (sid, s.name, int(s.enabled), json.dumps(s.rule)))
        await db.commit()
    finally:
        await db.close()
    await rule_engine.reload()
    return {"id": sid, **s.model_dump()}

@router.put("/{sid}")
async def update_scenario(sid: str, s: ScenarioIn):
    db = await get_db()
    try:
        cur = await db.execute("UPDATE scenarios SET name=?, enabled=?, rule_json=? WHERE id=?",
                               (s.name, int(s.enabled), json.dumps(s.rule), sid))
        await db.commit()
        if cur.rowcount == 0:
            raise HTTPException(404, "scenario not found")
    finally:
        await db.close()
    await rule_engine.reload()
    return {"id": sid, **s.model_dump()}

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
        if act.get("type") != "command":
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