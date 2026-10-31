import json, uuid
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from backend.db.models import get_db
from backend.services.rule_engine import rule_engine

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

@router.get("/{sid}/runs")
async def list_runs(sid: str, limit: int = 50):
    db = await get_db()
    try:
        cur = await db.execute("SELECT * FROM scenario_runs WHERE scenario_id=? ORDER BY id DESC LIMIT ?", (sid, limit))
        return [dict(r) for r in await cur.fetchall()]
    finally:
        await db.close()
