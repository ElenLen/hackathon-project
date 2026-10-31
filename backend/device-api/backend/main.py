# backend/device-api/backend/main.py
from fastapi import FastAPI, APIRouter, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
from backend.integrations.ujin_adapter import adapter

app = FastAPI(
    title="Ujin OS Hackathon API",
    description="Интеграционный бэкенд для управления устройствами и сценариями умного дома",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Разрешаем CORS для React приложения (Vite)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

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