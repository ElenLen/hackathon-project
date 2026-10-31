import asyncio, json, urllib.request, urllib.parse, time
from backend.core.config import settings

BASE = settings.ujin_base_url.rstrip("/")

def http_post_json(path, payload):
    url = f"{BASE}{path}"
    data = json.dumps(payload).encode()
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=15) as r:
        return json.loads(r.read().decode())

def http_get(path, params):
    url = f"{BASE}{path}?{urllib.parse.urlencode(params)}"
    with urllib.request.urlopen(url, timeout=15) as r:
        return json.loads(r.read().decode())

async def main():
    # 1. Пробуем получить employee-ust
    print("=== 1. employee auth ===")
    try:
        resp = await asyncio.to_thread(
            http_post_json, "/api/auth/authenticate/",
            {"login": "79906330238", "password": "Hk6PO5T-wgj!"})
        print("error:", resp.get("error"), "message:", resp.get("message"))
        emp_ust = (resp.get("data") or {}).get("token")
        print("employee ust:", (emp_ust or "NONE"))
    except Exception as e:
        print("EXC:", e)
        emp_ust = None

    # 2. get-device-info под employee-ust
    if emp_ust:
        print("\n=== 2. get-device-info (employee) ===")
        try:
            resp = await asyncio.to_thread(
                http_get, "/api/bmsdevice/get-device-info/",
                {"token": emp_ust, "serialnumber": "17048769610018002", "mts": 1})
            print("error:", resp.get("error"), "message:", resp.get("message"))
            signals = (resp.get("data") or {}).get("signals", [])
            print("signals:", len(signals))
            for s in signals[:6]:
                print("  ", s.get("name"), "=", s.get("value"))
        except Exception as e:
            print("EXC:", e)

    # 3. get-device-info под ust резидента (для сравнения)
    print("\n=== 3. get-device-info (resident) ===")
    try:
        resp = await asyncio.to_thread(
            http_get, "/api/bmsdevice/get-device-info/",
            {"token": settings.resident_ust,
             "serialnumber": "17048769610018002", "mts": 1})
        print("error:", resp.get("error"), "message:", resp.get("message"))
    except Exception as e:
        print("EXC:", e)

    # 4. devices/difference под ust резидента
    print("\n=== 4. devices/difference (resident) ===")
    dt = int(time.time()) - 3600
    try:
        resp = await asyncio.to_thread(
            http_get, "/api/devices/difference/",
            {"token": settings.resident_ust, "dt": dt})
        print("error:", resp.get("error"), "message:", resp.get("message"))
        data = resp.get("data") or {}
        if isinstance(data, dict):
            print("data keys:", list(data.keys())[:5])
        else:
            print("data type:", type(data).__name__, "len:", len(data) if hasattr(data, "__len__") else "?")
    except Exception as e:
        print("EXC:", e)

    # 5. apartment/get-device-signals под ust резидента
    print("\n=== 5. apartment/get-device-signals (resident) ===")
    try:
        resp = await asyncio.to_thread(
            http_get, "/api/apartment/get-device-signals/",
            {"token": settings.resident_ust})
        print("error:", resp.get("error"), "message:", resp.get("message"))
        data = resp.get("data") or {}
        if isinstance(data, dict):
            print("data keys:", list(data.keys())[:5])
    except Exception as e:
        print("EXC:", e)

asyncio.run(main())
