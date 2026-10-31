from fastapi import APIRouter
from . import devices, signals, commands, scenarios, declarations, signals_ingest

router = APIRouter(prefix="/api")
router.include_router(devices.router, prefix="/devices", tags=["devices"])
router.include_router(signals.router, prefix="/signals", tags=["signals"])
router.include_router(signals_ingest.router, prefix="/signals", tags=["signals"])
router.include_router(commands.router, prefix="/commands", tags=["commands"])
router.include_router(scenarios.router, prefix="/scenarios", tags=["scenarios"])
router.include_router(declarations.router, prefix="/declarations", tags=["declarations"])
