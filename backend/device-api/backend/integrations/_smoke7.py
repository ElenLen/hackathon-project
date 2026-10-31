import asyncio
from backend.integrations.ujin_adapter import adapter

async def main():
    row = {"sn": "khikhikhakhakery_leak_01",
           "s": "integrations_devices_leak", "v": 1,
           "c": "integrations_devices"}
    print("publish ...")
    resp = await adapter.publish([row])
    print("publish error:", resp.get("error"))

    print("wait 3s ...")
    await asyncio.sleep(3)

    devices = await adapter.get_apartment_signals()
    for d in devices:
        if d["sn"] == "17048769610018002":
            for s in d["signals"]:
                if "leak" in (s["name"] or ""):
                    print(f"  {s['name']} = {s['value']}  ← должно быть 1")

asyncio.run(main())
