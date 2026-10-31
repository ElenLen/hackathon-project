from pathlib import Path

import aiosqlite

from backend.core.config import settings


async def get_db() -> aiosqlite.Connection:
    """Создаёт НОВОЕ соединение. Вызывающий закрывает его сам."""
    Path(settings.db_path).parent.mkdir(parents=True, exist_ok=True)
    db = await aiosqlite.connect(settings.db_path)
    db.row_factory = aiosqlite.Row
    return db


async def init_db() -> None:
    sql = (Path(__file__).parent / "init.sql").read_text(encoding="utf-8")
    db = await get_db()
    try:
        await db.executescript(sql)
        await db.commit()
    finally:
        await db.close()
