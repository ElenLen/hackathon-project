<<<<<<< HEAD
"""WS-клиент умного дома по контракту МП (ujin-ios).

Адрес: GET /api/devices/wss/?token=<ust> -> data.wss[0] (на preprod пусто,
см. preprod-evidence/2026-10-14-websocket.md).
Auth после open: {"token":...,"event":"authenticate","signals":true}.
Команда: {"event":"setValue","devices":[{"id":uniq,"sn":serial,
"signals":[{"name":...,"value":"..."}]}]}.
Приём: event!=setValue -> data.devices[].signals обновляют FSM;
event==setValue -> эхо по id (status failed == отказ).
"""
import asyncio
import json
import ssl
import time


def build_auth(token):
    return json.dumps({"token": token, "event": "authenticate", "signals": True},
                      separators=(",", ":"))


def build_set_value(uniq_id, sn, signal_name, value):
    return json.dumps({
        "event": "setValue",
        "devices": [{"id": uniq_id, "sn": int(sn),
                     "signals": [{"name": signal_name, "value": str(value)}]}],
    }, separators=(",", ":"))


def parse_message(text):
    msg = json.loads(text)
    kind = msg.get("event") or "update"
    devices = (msg.get("data") or {}).get("devices") or []
    return kind, devices


class WsDeviceHub:
    """Держател FSM устройств; применяет входящие сигналы, копит эхо setValue."""

    def __init__(self, devices):
        self.devices = {str(d.serial): d for d in devices}
        self.echo = {}
        self.states_seen = {}

    def handle(self, kind, devices):
        for dev in devices:
            sn = str(dev.get("sn"))
            if kind == "setValue":
                self.echo[dev.get("id")] = dev.get("status")
                continue
            if dev.get("state"):
                self.states_seen[sn] = dev["state"]
            device = self.devices.get(sn)
            if not device:
                continue
            for sig in dev.get("signals") or []:
                name, value = sig.get("name"), sig.get("value")
                if name is not None and value is not None:
                    device.apply_ws_signal(name, value)


class WsHomeClient:
    def __init__(self, url, token, hub, insecure=True):
        self.url = url
        self.token = token
        self.hub = hub
        self.insecure = insecure
        self.auth_ok = None
        self._ws = None

    async def run(self, once=False, wait=10):
        import websockets
        ctx = ssl._create_unverified_context() if self.insecure else None
        async with websockets.connect(self.url, ssl=ctx, open_timeout=8,
                                      ping_interval=None) as ws:
            self._ws = ws
            await ws.send(build_auth(self.token))
            deadline = time.time() + wait
            while time.time() < deadline:
                try:
                    text = await asyncio.wait_for(
                        ws.recv(), timeout=max(0.5, deadline - time.time()))
                except asyncio.TimeoutError:
                    break
                kind, devices = parse_message(text)
                if kind == "authenticate":
                    self.auth_ok = devices or "denied"
                    if once:
                        return
                else:
                    self.hub.handle(kind, devices)
                if once:
                    return
            if once:
                return
            while True:
                try:
                    text = await ws.recv()
                except Exception:
                    break
                kind, devices = parse_message(text)
                self.hub.handle(kind, devices)

    async def send(self, signal_name, value, sn, uniq_id=None):
        uniq_id = uniq_id or int(time.time() * 1000) % 65535
        await self._ws.send(build_set_value(uniq_id, sn, signal_name, value))
        return uniq_id


def fetch_wss_address(api_base, token):
    """data.wss[0] из devices/wss/; параметр именно token= (user_token -> гость)."""
    import urllib.request
    url = f"{api_base}/devices/wss/?token={token}"
    with urllib.request.urlopen(url, timeout=20) as r:
        data = json.loads(r.read().decode("utf-8"))
    if data.get("error"):
        return None, data.get("message")
    addrs = [a for a in (data.get("data") or {}).get("wss") or [] if a]
    return (addrs[0] if addrs else None), None
=======
import asyncio
import json
from typing import Dict, Any, Optional
import websockets
from core import BaseDevice, SensorDevice

CONNECTED_CLIENTS = set()
DEVICES: Dict[str, BaseDevice] = {
    "sensor_1": SensorDevice("sensor_1")
}

async def register(websocket):
    CONNECTED_CLIENTS.add(websocket)
    # Отправляем текущее состояние при подключении
    initial_data = [dev.get_telemetry() for dev in DEVICES.values()]
    await websocket.send(json.dumps({"type": "INIT_STATE", "data": initial_data}))

async def unregister(websocket):
    CONNECTED_CLIENTS.remove(websocket)

async def handle_message(websocket, path):
    await register(websocket)
    try:
        async for message in websocket:
            data = json.loads(message)
            msg_type = data.get("type")
            
            if msg_type == "COMMAND":
                device_id = data.get("device_id")
                command = data.get("command")
                payload = data.get("payload", {})
                
                if device_id in DEVICES:
                    result = await DEVICES[device_id].process_command(command, payload)
                    broadcast_message = json.dumps({"type": "DEVICE_UPDATED", "data": result})
                    await asyncio.gather(*[client.send(broadcast_message) for client in CONNECTED_CLIENTS])
    except websockets.ConnectionClosedError:
        pass
    finally:
        await unregister(websocket)

async def telemetry_loop():
    while True:
        await asyncio.sleep(2)
        for dev in DEVICES.values():
            if isinstance(dev, SensorDevice):
                await dev.step()
                msg = json.dumps({"type": "TELEMETRY", "data": dev.get_telemetry()})
                if CONNECTED_CLIENTS:
                    await asyncio.gather(*[client.send(msg) for client in CONNECTED_CLIENTS])

async def main():
    server = await websockets.serve(handle_message, "0.0.0.0", 8080)
    print("WebSocket Emulator running on ws://localhost:8080")
    await asyncio.gather(server.wait_closed(), telemetry_loop())

if __name__ == "__main__":
    asyncio.run(main())
>>>>>>> d58725f (добавила новые файлы)
