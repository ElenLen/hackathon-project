from __future__ import annotations
import asyncio
import logging
from core.config import settings
from integrations.ujin_adapter import adapter
from services import device_service, signal_service

log = logging.getLogger(__name__)

async def poll_once(rule_engine=None) -> int:
    devices = await adapter.get_apartment_signals()
    
    # Если API вернул не список (например, строку с ошибкой или словарь), пропускаем
    if not isinstance(devices, list):
        if isinstance(devices, dict):
            log.warning("API returned error object: %s", devices)
        else:
            log.warning("API returned unexpected type: %s (value: %s)", type(devices), devices)
        return 0

    total = 0
    for d in devices:
        # Если элемент списка не словарь, пропускаем
        if not isinstance(d, dict):
            continue
            
        sn = d.get("sn")
        if not sn:
            continue
        device_id = await device_service.get_device_id_by_sn(sn)
        if not device_id:
            continue
        n = await signal_service.record_signals(device_id, d.get("signals", []))
        total += n
        if rule_engine is not None:
            for s in d.get("signals", []):
                if not isinstance(s, dict):
                    continue
                name = s.get("name")
                if not name:
                    continue
                await rule_engine.on_signal(device_id, name, s.get("value"))
    return total

async def poll_forever(rule_engine=None, interval: float = None) -> None:
    interval = interval or settings.poll_interval
    log.info("poller started, interval=%.1fs", interval)
    while True:
        try:
            n = await poll_once(rule_engine)
            log.debug("polled %d signals", n)
        except asyncio.CancelledError:
            log.info("poller cancelled")
            raise
        except Exception as exc:
            log.exception("poll error: %s", exc)
        await asyncio.sleep(interval)