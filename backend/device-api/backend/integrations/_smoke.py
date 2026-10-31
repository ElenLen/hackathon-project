import asyncio
from emulators.ujin_client import UjinClient
from core.config import settings


async def main():
    client = UjinClient(
        base_url=settings.ujin_base_url,
        dry_run=False,
        cls=settings.publish_class,
        signal_prefix=settings.publish_class + "_",
        ust=settings.resident_ust,
        apartment_id=settings.apartment_id,
    )
    resp = await asyncio.to_thread(
        client.get_device_info, settings.resident_ust, "17048769610018002")
    print("error:", resp.get("error"), "message:", resp.get("message"))
    signals = (resp.get("data") or {}).get("signals", [])
    print("signals:", len(signals))
    for s in signals[:8]:
        print(" ", s.get("name"), "=", s.get("value"))


if __name__ == "__main__":
    asyncio.run(main())
