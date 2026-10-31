from pydantic import BaseModel

class SignalPoint(BaseModel):
    signal_name: str
    value_num: float | None = None
    value_str: str | None = None
    ts: str
