from pydantic import BaseModel

class DeviceOut(BaseModel):
    id: str
    ujin_sn: str
    title: str
    family: str
    apartment_id: str
    last_values: dict[str, float | int | str | None] = {}
    last_seen_at: str | None = None
