class UjinError(Exception):
    """Ошибка от Ujin API (error != 0 в ответе)."""

    def __init__(self, message: str, code: int | str = ""):
        self.message = message
        self.code = code
        super().__init__(message)


class UjinAuthError(UjinError):
    """Проблема аутентификации (ust протух, нет доступа)."""
