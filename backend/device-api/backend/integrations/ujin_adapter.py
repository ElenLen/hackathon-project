"""Async-обёртка над emulators.ujin_client.UjinClient.

Чтение — через apartment/get-device-signals (под ust резидента) — единственный
путь, работающий на uae-test (get-device-info требует роль сотрудника УК).
Публикация — через UjinClient.publish_group (uae-режим: ust + apartment_id в query).
"""
from __future__ import annotations

import asyncio
import json
import urllib.parse
import urllib.request

from emulators.ujin_client import UjinClient

from core.config import settings


class UjinError(Exception):
    def __init__(self, message: str, code=None):
        super().__init__(message)
        self.message = message
        self.code = code


class UjinAdapter:
    def __init__(self) -> None:
        self._client = UjinClient(
            base_url=settings.ujin_base_url,
            device_token="",
            dry_run=False,
            cls=settings.publish_class,
            signal_prefix=settings.publish_class + "_",
            ust=settings.resident_ust,
            apartment_id=str(settings.apartment_id),
        )

    async def get_apartment_signals(self) -> list[dict]:
        raw = await asyncio.to_thread(
            self._get_raw,
            "/api/apartment/get-device-signals/",
            {"token": settings.resident_ust},
        )
        if raw.get("error", 0) != 0:
            raise UjinError(raw.get("message", "get-device-signals failed"),
                            raw.get("error_code"))
        devices = (raw.get("data") or {}).get("devices", [])
        return [self._normalize_device(d) for d in devices]

    async def difference(self, dt: int = 0) -> dict:
        raw = await asyncio.to_thread(
            self._get_raw,
            "/api/devices/difference/",
            {"token": settings.resident_ust, "dt": dt},
        )
        if raw.get("error", 0) != 0:
            raise UjinError(raw.get("message", "difference failed"),
                            raw.get("error_code"))
        return raw.get("data") or {}

    async def publish(self, rows: list[dict], new_short: list = ()) -> dict:
        resp = await asyncio.to_thread(self._client.publish_group, rows, new_short)
        if resp.get("error", 0) != 0:
            raise UjinError(resp.get("message", "publish failed"),
                            resp.get("error_code"))
        return resp

    async def create_device(self, *, model: str, title: str,
                            ext_guid: str, apartment_id: str | None = None) -> dict:
        resp = await asyncio.to_thread(
            self._client.create_update_device,
            model=model,
            title=title,
            device_ext_id=ext_guid,
            apartment_id=apartment_id or settings.apartment_id,
        )
        if resp.get("error", 0) != 0:
            raise UjinError(resp.get("message", "create failed"),
                            resp.get("error_code"))
        return resp

    def _get_raw(self, path: str, params: dict) -> dict:
        url = f"{settings.ujin_base_url.rstrip('/')}{path}?{urllib.parse.urlencode(params)}"
        with urllib.request.urlopen(url, timeout=15) as r:
            return json.loads(r.read().decode())

    @staticmethod
    def _normalize_device(d: dict) -> dict:
        sn = d.get("id") or ""
        title = d.get("title") or d.get("device_model_title") or ""
        signals = []
        for s in d.get("signals") or []:
            signals.append({
                "name": s.get("name"),
                "title": s.get("title"),
                "value": s.get("value"),
                "dt": s.get("dt"),
                "signal_id": s.get("signal_id"),
                "type_control": s.get("type_control"),
            })
        return {"sn": str(sn), "title": title,
                "model": d.get("device_model_name") or "", "signals": signals}


adapter = UjinAdapter()
