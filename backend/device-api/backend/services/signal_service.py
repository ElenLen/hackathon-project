from __future__ import annotations
import aiosqlite
from backend.core.config import settings
from backend.db.models import get_db

async def record_signals(device_id: str, signals: list[dict]) -> int:
    SKIP = {"dt_create", "push-connect", "ver"}
    rows = []
    for s in signals:
        name = s.get("name")
        if not name or name in SKIP:
            continue
        value = s.get("value")
        num = value if isinstance(value, (int, float)) else None
        txt = None if num is not None else str(value) if value is not None else None
        rows.append((device_id, name, num, txt))
    if not rows:
        return 0
    db = await get_db()
    try:
        await db.executemany(
            "INSERT INTO signals(device_id, signal_name, value_num, value_str) VALUES (?,?,?,?)",
            rows,
        )
        await db.execute(
            "UPDATE devices SET last_seen_at = datetime('now') WHERE id = ?",
            (device_id,),
        )
        await db.commit()
        return len(rows)
    finally:
        await db.close()

async def history(device_id: str, name: str | None = None,
                  from_ts: str | None = None, to_ts: str | None = None,
                  limit: int = 500) -> list[dict]:
    sql = "SELECT signal_name, value_num, value_str, ts FROM signals WHERE device_id = ?"
    args: list = [device_id]
    if name:
        sql += " AND signal_name = ?"
        args.append(name)
    if from_ts:
        sql += " AND ts >= ?"
        args.append(from_ts)
    if to_ts:
        sql += " AND ts <= ?"
        args.append(to_ts)
    sql += " ORDER BY ts DESC LIMIT ?"
    args.append(limit)
    db = await get_db()
    try:
        cur = await db.execute(sql, args)
        return [dict(r) for r in await cur.fetchall()]
    finally:
        await db.close()

async def latest_per_signal(device_id: str) -> dict[str, dict]:
    db = await get_db()
    try:
        cur = await db.execute(
            """SELECT s.signal_name, s.value_num, s.value_str, s.ts
               FROM signals s
               JOIN (SELECT signal_name, MAX(ts) AS mts FROM signals WHERE device_id = ? GROUP BY signal_name) m
                 ON s.signal_name = m.signal_name AND s.ts = m.mts
               WHERE s.device_id = ?""",
            (device_id, device_id),
        )
        return {r["signal_name"]: {"value": r["value_num"] if r["value_num"] is not None else r["value_str"], "ts": r["ts"]}
                for r in await cur.fetchall()}
    finally:
        await db.close()
