from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from integrations.ujin_adapter import adapter
from core.config import settings
import re

router = APIRouter()
EXT_RE = re.compile(r"^[a-z0-9_]+$")

class CommandIn(BaseModel):
    device_id: str
    signal: str
    value: float | int | str

def _safe(v):
    if isinstance(v, (int, float)) and float(v) == 0.0:
        return settings.signal_epsilon
    return v

@router.post("", status_code=201)
async def send_command(cmd: CommandIn):
    if not EXT_RE.fullmatch(cmd.device_id):
        raise HTTPException(400, "invalid ext_guid")
    row = {"sn": cmd.device_id,
           "s": f"{settings.publish_class}_{cmd.signal}",
           "v": _safe(cmd.value),
           "c": settings.publish_class}
    try:
        return await adapter.publish([row])
    except Exception as e:
        raise HTTPException(502, str(e))
