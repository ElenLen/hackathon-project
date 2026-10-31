"""Приём сигналов от эмуляторов и внешних источников.

На UAE-стенде публикации в Ujin (group-update-device-signal) не сохраняются
(blockers R-31/R-32/R-33). Поэтому данные пишем в свою БД здесь, а Ujin
вызываем параллельно (error=0), чтобы сохранить реализм.
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from backend.services import signal_service, device_service
from backend.integrations.ujin_adapter import adapter

router = APIRouter()


class SignalIn(BaseModel):
    sn: str              # ujin_sn
    name: str            # короткое имя сигнала, напр. "leak"
    value: float | int | str


class SignalsIn(BaseModel):
    signals: list[SignalIn]


@router.post("/ingest", status_code=201)
async def ingest(payload: SignalsIn):
    written = 0
    errors = []
    for s in payload.signals:
        device_id = await device_service.get_device_id_by_sn(s.sn)
        if not device_id:
            errors.append(f"unknown sn: {s.sn}")
            continue
        n = await signal_service.record_signals(
            device_id, [{"name": s.name, "value": s.value}]
        )
        written += n
        # параллельно публикуем в Ujin (для реализма, error=0)
        try:
            row = {"sn": device_id, "s": f"integrations_devices_{s.name}",
                   "v": 0.001 if s.value == 0 else s.value,
                   "c": "integrations_devices"}
            await adapter.publish([row])
        except Exception as e:
            errors.append(f"publish {s.name}: {e}")
    return {"written": written, "errors": errors}
