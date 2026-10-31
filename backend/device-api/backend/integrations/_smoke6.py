import asyncio
from backend.integrations.ujin_adapter import adapter

async def main():
    devices = await adapter.get_apartment_signals()
    print("devices:", len(devices))
    for d in devices:
        print(f"  sn={d['sn']!r} title={d['title']!r} signals={len(d['signals'])}")
        for s in d["signals"][:4]:
            print(f"    {s['name']} = {s['value']}")

asyncio.run(main())
