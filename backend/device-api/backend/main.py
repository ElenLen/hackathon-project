import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

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

app = FastAPI(title="hackathon-backend", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_list,
    allow_methods=["*"], allow_headers=["*"],
)

app.include_router(api_router)

@app.get("/health")
async def health():
    return {"status": "ok"}
