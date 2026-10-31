from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # extra="ignore" позволяет игнорировать переменные в .env, которых нет в этом классе
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ujin_base_url: str = "https://api-uae-test.ujin.tech"
    resident_ust: str = ""
    resident_login: str = ""
    resident_password: str = ""
    apartment_id: str = ""
    publish_class: str = "integrations_devices"
    signal_epsilon: float = 0.001
    db_path: str = "./db/app.sqlite3"
    enable_poller: bool = False
    poll_interval: float = 2.0
    app_port: int = 8001
    cors_origins: str = "http://localhost:5173"

    @property
    def cors_list(self) -> list[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]


settings = Settings()
