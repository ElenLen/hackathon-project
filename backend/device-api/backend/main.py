from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from backend.core.config import settings
from backend.db.models import init_db


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    print(f"[startup] db ready: {settings.db_path}")
    yield
    print("[shutdown]")


app = FastAPI(title="hackathon-backend", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_list,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health():
    return {"status": "ok"}
