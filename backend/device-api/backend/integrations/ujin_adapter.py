import os
import httpx
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    resident_ust: str = os.getenv("RESIDENT_UST", "ust-2815073-048272461ef140f8b1d680df3bbb88d0")
    apartment_id: int = int(os.getenv("APARTMENT_ID", "2785"))
    ujin_base_url: str = os.getenv("UJIN_BASE_URL", "https://api-uae-test.ujin.tech")

    class Config:
        env_file = ".env"

settings = Settings()

class UjinAdapter:
    def __init__(self):
        self.base_url = settings.ujin_base_url
        self.ust = settings.resident_ust
        self.apartment_id = settings.apartment_id

    async def get_apartment_signals(self):
        """Получение текущего списка устройств и всех сигналов квартиры"""
        url = f"{self.base_url}/api/v1/apartments/{self.apartment_id}/signals"
        params = {"ust": self.ust}
        
        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(url, params=params, timeout=10.0)
                if response.status_code == 200:
                    return response.json()
                return {"status": "error", "code": response.status_code, "data": []}
            except Exception as e:
                return {"status": "error", "message": str(e), "data": []}

    async def difference(self, dt: int = 0):
        """Получение изменившихся сигналов с временной метки dt"""
        url = f"{self.base_url}/api/v1/apartments/{self.apartment_id}/signals/difference"
        params = {"ust": self.ust, "dt": dt}
        
        async with httpx.AsyncClient() as client:
            try:
                response = await client.get(url, params=params, timeout=10.0)
                if response.status_code == 200:
                    return response.json()
                return {"status": "error", "code": response.status_code}
            except Exception as e:
                return {"status": "error", "message": str(e)}

    async def publish(self, signals_payload: list):
        """Публикация управляющих сигналов в Ujin OS"""
        url = f"{self.base_url}/api/v1/apartments/{self.apartment_id}/signals/publish"
        params = {"ust": self.ust}
        
        async with httpx.AsyncClient() as client:
            try:
                response = await client.post(url, params=params, json={"signals": signals_payload}, timeout=10.0)
                if response.status_code == 200:
                    return response.json()
                return {"status": "error", "code": response.status_code}
            except Exception as e:
                return {"status": "error", "message": str(e)}

adapter = UjinAdapter()