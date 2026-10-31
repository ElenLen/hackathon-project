import uuid

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from db.models import get_db
from emulators.devices import REGISTRY
from integrations.ujin_adapter import adapter
from schemas.device import DeviceOut
from services import device_service, signal_service

router = APIRouter()


class DeviceIn(BaseModel):
    title: str
    family: str
    apartment_id: str = ""


async def _present(d: dict) -> dict:
    latest = await signal_service.latest_per_signal(d["id"])
    return {**d, "last_values": {k: v["value"] for k, v in latest.items()}}


@router.get("", response_model=list[DeviceOut])
async def list_devices():
    return [await _present(d) for d in await device_service.list_devices()]


@router.post("", response_model=DeviceOut, status_code=201)
async def add_device(dev: DeviceIn):
    if dev.family not in REGISTRY:
        raise HTTPException(400, f"unknown family: {dev.family}")
    ext_guid = f"khikhikhakhakery_{dev.family}_{uuid.uuid4().hex[:8]}"
    try:
        created = await adapter.create_device(
            model="parkcloud_device",
            title=dev.title,
            ext_guid=ext_guid,
            apartment_id=dev.apartment_id or None,
        )
    except Exception as e:
        raise HTTPException(502, f"ujin create failed: {e}")
    sn = str((created or {}).get("sn") or (created or {}).get("serial") or "")
    row = {
        "id": ext_guid,
        "ujin_sn": sn,
        "ext_guid": ext_guid,
        "model": "parkcloud_device",
        "title": dev.title,
        "family": dev.family,
        "apartment_id": dev.apartment_id,
    }
    db = await get_db()
    try:
        await db.execute(
            """INSERT INTO devices(id, ujin_sn, ext_guid, model, title, family, apartment_id)
               VALUES (?,?,?,?,?,?,?)""",
            (row["id"], row["ujin_sn"], row["ext_guid"], row["model"],
             row["title"], row["family"], row["apartment_id"]),
        )
        await db.commit()
    finally:
        await db.close()
    return await _present({**row, "last_seen_at": None})


@router.get("/{device_id}", response_model=DeviceOut)
async def get_device(device_id: str):
    d = next((x for x in await device_service.list_devices() if x["id"] == device_id), None)
    if not d:
        raise HTTPException(404, "device not found")
    return await _present(d)


@router.get("/{device_id}/declaration")
async def get_device_declaration(device_id: str):
    d = next((x for x in await device_service.list_devices() if x["id"] == device_id), None)
    if not d:
        raise HTTPException(404, "device not found")
    family = d.get("family")
    if family not in REGISTRY:
        raise HTTPException(404, f"unknown family: {family}")
    return REGISTRY[family](serial=d.get("ujin_sn") or "0").declaration()