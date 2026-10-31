from fastapi import APIRouter, HTTPException
from emulators.devices import REGISTRY

router = APIRouter()

@router.get("")
async def list_families():
    return sorted(REGISTRY.keys())

@router.get("/{family}")
async def get_declaration(family: str):
    if family not in REGISTRY:
        raise HTTPException(404, "unknown family")
    dev = REGISTRY[family](serial="0")
    return dev.declaration()
