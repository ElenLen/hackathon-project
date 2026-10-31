from fastapi import APIRouter, HTTPException
from schemas.device import DeviceOut
from services import device_service, signal_service

router = APIRouter()

@router.get("", response_model=list[DeviceOut])
async def list_devices():
    devices = await device_service.list_devices()
    out = []
    for d in devices:
        latest = await signal_service.latest_per_signal(d["id"])
        out.append({
            "id": d["id"], "ujin_sn": d["ujin_sn"],
            "title": d["title"], "family": d["family"],
            "apartment_id": d["apartment_id"],
            "last_values": {k: v["value"] for k, v in latest.items()},
            "last_seen_at": d["last_seen_at"],
        })
    return out

@router.get("/{device_id}", response_model=DeviceOut)
async def get_device(device_id: str):
    devices = await device_service.list_devices()
    d = next((x for x in devices if x["id"] == device_id), None)
    if not d:
        raise HTTPException(404, "device not found")
    latest = await signal_service.latest_per_signal(device_id)
    return {**d, "last_values": {k: v["value"] for k, v in latest.items()}}
