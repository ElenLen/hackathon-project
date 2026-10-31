import asyncio
import sys

if sys.platform == "win32":
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from core.config import settings
from db.models import init_db
from services.device_service import seed_devices
from integrations.poller import poll_forever
from services.rule_engine import rule_engine
from api import router as api_router


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


app = FastAPI(
    title="Ujin OS Hackathon API",
    description="Интеграционный бэкенд для управления устройствами и сценариями умного дома",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_list if hasattr(settings, "cors_list") else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)


@app.get("/")
async def root():
    return {"message": "Ujin OS Integration API is running", "swagger_docs": "/docs"}


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.get("/api/health")
async def api_health():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="127.0.0.1", port=settings.app_port, reload=False)