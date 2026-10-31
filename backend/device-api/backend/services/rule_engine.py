from __future__ import annotations
import asyncio, json, logging, time
from typing import Any
from db.models import get_db
from integrations.ujin_adapter import adapter

log = logging.getLogger(__name__)

def _cmp(value: Any, op: str, target: Any) -> bool:
    try:
        v, t = float(value), float(target)
    except (TypeError, ValueError):
        v, t = str(value), str(target)
    return {"==": v == t, "!=": v != t, ">": v > t, "<": v < t, ">=": v >= t, "<=": v <= t}.get(op, False)

class RuleEngine:
    def __init__(self) -> None:
        self._cache: dict[str, dict] = {}
        self._last_fire: dict[str, float] = {}
        self._locks: dict[str, asyncio.Lock] = {}

    async def reload(self) -> None:
        db = await get_db()
        try:
            cur = await db.execute("SELECT id, rule_json FROM scenarios WHERE enabled = 1")
            rows = await cur.fetchall()
            self._cache = {r["id"]: json.loads(r["rule_json"]) for r in rows}
            log.info("rule engine loaded %d scenarios", len(self._cache))
        finally:
            await db.close()

    async def on_signal(self, device_id: str, name: str, value: Any) -> None:
        for sid, rule in self._cache.items():
            trig = rule.get("trigger", {})
            if trig.get("device_id") != device_id or trig.get("signal") != name:
                continue
            if not _cmp(value, trig.get("op", "=="), trig.get("value")):
                continue
            lock = self._locks.setdefault(sid, asyncio.Lock())
            if lock.locked():
                continue
            async with lock:
                cooldown = rule.get("cooldown_sec", 0)
                if cooldown and time.time() - self._last_fire.get(sid, 0) < cooldown:
                    continue
                await self._fire(sid, rule, device_id, name, value)

    async def _fire(self, sid: str, rule: dict, device_id: str, name: str, value: Any) -> None:
        results = []
        for act in rule.get("actions", []):
            if act.get("type") != "command":
                continue
            row = {"sn": act["device_id"],
                   "s": f"{act.get('class', 'integrations_devices')}_{act['signal']}",
                   "v": act["value"],
                   "c": act.get("class", "integrations_devices")}
            try:
                await adapter.publish([row])
                results.append({"action": act, "ok": True})
            except Exception as exc:
                results.append({"action": act, "ok": False, "error": str(exc)})
                log.exception("action failed: %s", exc)
        self._last_fire[sid] = time.time()
        await self._log_run(sid, {"device_id": device_id, "signal": name, "value": value}, results)

    async def _log_run(self, sid: str, snapshot: dict, results: list) -> None:
        ok = all(r["ok"] for r in results) if results else False
        db = await get_db()
        try:
            await db.execute(
                "INSERT INTO scenario_runs(scenario_id, trigger_snapshot, actions_json, ok) VALUES (?,?,?,?)",
                (sid, json.dumps(snapshot), json.dumps(results), int(ok)),
            )
            await db.commit()
        finally:
            await db.close()

rule_engine = RuleEngine()
