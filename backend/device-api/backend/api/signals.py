from fastapi import APIRouter, Query
from backend.schemas.signal import SignalPoint
from backend.services import signal_service

router = APIRouter()

@router.get("", response_model=list[SignalPoint])
async def list_signals(
    device_id: str = Query(...),
    name: str | None = None,
    from_ts: str | None = Query(None, alias="from"),
    to_ts: str | None = Query(None, alias="to"),
    limit: int = 500,
):
    return await signal_service.history(device_id, name, from_ts, to_ts, limit)
