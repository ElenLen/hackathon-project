from __future__ import annotations
from backend.db.models import get_db

SEED_DEVICES = [
    {"id": "khikhikhakhakery_leak_01", "ujin_sn": "17048769610018002",
     "title": "Протечка", "family": "leak", "model": "parkcloud_device"},
    {"id": "khikhikhakhakery_barrier_01", "ujin_sn": "17037033610008585",
     "title": "Шлагбаум", "family": "barrier", "model": "parkcloud_device"},
    {"id": "khikhikhakhakery_thermostat_01", "ujin_sn": "17036961610007336",
     "title": "Термостат", "family": "thermostat", "model": "parkcloud_device"},
    {"id": "khikhikhakhakery_counter_01", "ujin_sn": "17048805610016831",
     "title": "Счётчик", "family": "counter", "model": "parkcloud_device"},
    {"id": "khikhikhakhakery_light_01", "ujin_sn": "17048841600011160",
     "title": "Свет", "family": "light", "model": "parkcloud_device"},
]

async def seed_devices(apartment_id: str) -> int:
    db = await get_db()
    try:
        for d in SEED_DEVICES:
            await db.execute(
                """INSERT INTO devices(id, ujin_sn, ext_guid, model, title, family, apartment_id)
                   VALUES (?,?,?,?,?,?,?)
                   ON CONFLICT(ext_guid) DO NOTHING""",
                (d["id"], d["ujin_sn"], d["id"], d["model"], d["title"], d["family"], apartment_id),
            )
        await db.commit()
        cur = await db.execute("SELECT COUNT(*) as n FROM devices")
        return (await cur.fetchone())["n"]
    finally:
        await db.close()

async def get_device_id_by_sn(sn: str) -> str | None:
    db = await get_db()
    try:
        cur = await db.execute("SELECT id FROM devices WHERE ujin_sn = ?", (sn,))
        row = await cur.fetchone()
        return row["id"] if row else None
    finally:
        await db.close()

async def list_devices() -> list[dict]:
    db = await get_db()
    try:
        cur = await db.execute("SELECT * FROM devices ORDER BY id")
        return [dict(r) for r in await cur.fetchall()]
    finally:
        await db.close()
