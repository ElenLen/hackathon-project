"""Приём сигналов от эмуляторов и внешних источников.

На UAE-стенде публикации в Ujin (group-update-device-signal) не сохраняются
(blockers R-31/R-32/R-33). Поэтому данные пишем в свою БД здесь, а Ujin
вызываем параллельно (error=0), чтобы сохранить реализм.

Дополнительно: после записи вызываем rule_engine.on_signal, чтобы сценарии
срабатывали на сигналы, пришедшие через ingest (иначе они реагируют только
на поллер, который на UAE не видит этих сигналов).
"""
from fastapi import APIRouter
from pydantic import BaseModel
from services import signal_service, device_service
from services.rule_engine import rule_engine
from integrations.ujin_adapter import adapter

router = APIRouter()


class SignalIn(BaseModel):
    sn: str
    name: str
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
        try:
            row = {"sn": device_id, "s": f"integrations_devices_{s.name}",
                   "v": 0.001 if s.value == 0 else s.value,
                   "c": "integrations_devices"}
            await adapter.publish([row])
        except Exception as e:
            errors.append(f"publish {s.name}: {e}")
        try:
            await rule_engine.on_signal(device_id, s.name, s.value)
        except Exception as e:
            errors.append(f"rule_engine {s.name}: {e}")
    return {"written": written, "errors": errors}
