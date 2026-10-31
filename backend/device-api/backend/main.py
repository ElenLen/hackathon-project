import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI, APIRouter, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
from backend.integrations.ujin_adapter import adapter

from backend.core.config import settings
from backend.db.models import init_db
from backend.services.device_service import seed_devices
from backend.integrations.poller import poll_forever
from backend.services.rule_engine import rule_engine
from backend.api import router as api_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    n = await seed_devices(settings.apartment_id)
    print(f"[startup] db ready: {settings.db_path}, devices seeded: {n}")

    await rule_engine.reload()
    print("[startup] rule engine loaded")

    poll_task = None
    if settings.enable_poller:
        poll_task = asyncio.create_task(
            poll_forever(rule_engine=rule_engine, interval=settings.poll_interval))
        print(f"[startup] poller started (interval={settings.poll_interval}s)")

    yield

    if poll_task:
        poll_task.cancel()
        try:
            await poll_task
        except BaseException:
            pass
    print("[shutdown]")

app = FastAPI(
    title="Ujin OS Hackathon API",
    description="Интеграционный бэкенд для управления устройствами и сценариями умного дома",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# Разрешаем CORS (объединяем настройки из вашей ветки и ветки Насти)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_list if hasattr(settings, 'cors_list') else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)

class SignalPayload(BaseModel):
    sn: str
    signal: str
    value: float

class PublishRequest(BaseModel):
    signals: List[SignalPayload]

@app.get("/")
async def root():
    return {"message": "Ujin OS Integration API is running", "swagger_docs": "/docs"}

@app.get("/api/devices", summary="Список устройств и сигналов квартиры")
async def get_devices():
    """Возвращает текущие показания устройств и сигналы из Ujin OS API"""
    res = await adapter.get_apartment_signals()
    return res

@app.get("/api/difference", summary="Получение дельты изменений")
async def get_difference(dt: int = 0):
    """Возвращает сигналы, изменившиеся с момента временной метки dt"""
    return await adapter.difference(dt)

@app.post("/api/signal", summary="Отправка единичного сигнала")
async def send_single_signal(payload: SignalPayload):
    """Отправляет управляющую команду на конкретное устройство по S/N"""
    res = await adapter.publish([payload.dict()])
    return res

@app.post("/api/publish", summary="Пакетная публикация сигналов")
async def publish_signals(payload: PublishRequest):
    """Пакетная отправка сигналов в Ujin OS"""
    res = await adapter.publish([s.dict() for s in payload.signals])
    return res

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8001, reload=True)