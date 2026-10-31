from __future__ import annotations
import asyncio
import logging
from backend.core.config import settings
from backend.integrations.ujin_adapter import adapter
from backend.services import device_service, signal_service

log = logging.getLogger(__name__)

async def poll_once(rule_engine=None) -> int:
    devices = await adapter.get_apartment_signals()
    total = 0
    for d in devices:
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
