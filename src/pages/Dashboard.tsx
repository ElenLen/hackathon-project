// src/pages/Dashboard.tsx
import { useEffect, useRef, useState } from "react";
import { useSmartHomeStore } from "../store/useSmartHomeStore";
import { Droplets, Lightbulb, Thermometer, ShieldAlert, Gauge, Wifi, WifiOff, RefreshCw } from "lucide-react";

const ICONS: Record<string, any> = {
  leak: Droplets, light: Lightbulb, thermostat: Thermometer, barrier: ShieldAlert, counter: Gauge,
};

export default function Dashboard() {
  const { devices, sendSignal, logs, isLoaded, applyDifference } = useSmartHomeStore();
  const [selected, setSelected] = useState<string | null>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    applyDifference(); // первый запрос с dt=0 — грузит всё
    pollRef.current = setInterval(applyDifference, 3000); // затем дельта каждые 3 сек
    return () => { if (pollRef.current) clearInterval(pollRef.current); };
  }, []);

  const rooms = devices.reduce((acc, d) => {
    if (!acc[d.room]) acc[d.room] = [];
    acc[d.room].push(d);
    return acc;
  }, {} as Record<string, typeof devices>);

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-cyan-400">Умный дом</h1>
        <div className="flex items-center gap-2 text-sm">
          {!isLoaded
            ? <><RefreshCw className="w-4 h-4 animate-spin text-cyan-400" /><span className="text-slate-400">Загрузка...</span></>
            : <><Wifi className="w-4 h-4 text-green-400" /><span className="text-green-400">Ujin OS</span></>
          }
        </div>
      </div>

      <div className="bg-slate-800/50 rounded-lg p-4 mb-6 border border-slate-700">
        <h3 className="text-sm font-semibold text-slate-400 mb-2">📡 Логи</h3>
        <div className="h-24 overflow-y-auto text-xs font-mono text-green-400 space-y-1">
          {logs.slice(-10).map((log, i) => <div key={i}>{log}</div>)}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {Object.entries(rooms).map(([room, roomDevices]) => (
          <div key={room} className="bg-slate-800 rounded-xl p-4 border border-slate-700">
            <h2 className="text-lg font-semibold mb-3 text-slate-200">{room}</h2>
            <div className="space-y-2">
              {roomDevices.map((device) => {
                const Icon = ICONS[device.type] || Lightbulb;
                const isAlert = device.signals.leak?.value === 1 || device.signals.leak?.value === "1";
                return (
                  <div key={device.id} onClick={() => setSelected(device.id)}
                    className={`flex items-center gap-3 p-3 rounded-lg cursor-pointer transition-all ${
                      isAlert ? "bg-red-900/50 border border-red-500 animate-pulse"
                              : "bg-slate-700 hover:bg-slate-600 border border-transparent"}`}>
                    <Icon className="w-5 h-5 text-cyan-400 shrink-0" />
                    <div className="flex-1">
                      <div className="font-medium text-sm">{device.name}</div>
                      <div className="text-xs text-slate-400">
                        {!device.isOnline ? "Ожидание данных..."
                          : isAlert ? "⚠️ Тревога!"
                          : `${Object.keys(device.signals).length} сигналов`}
                      </div>
                    </div>
                    {!device.isOnline && <WifiOff className="w-3 h-3 text-slate-500" />}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {selected && (
        <DeviceModal deviceId={selected} onClose={() => setSelected(null)} onSend={sendSignal} />
      )}
    </div>
  );
}

function DeviceModal({ deviceId, onClose, onSend }: {
  deviceId: string;
  onClose: () => void;
  onSend: (id: string, signal: string, value: any) => Promise<void>;
}) {
  const device = useSmartHomeStore((s) => s.devices.find((d) => d.id === deviceId));
  const [pending, setPending] = useState<string | null>(null);
  if (!device) return null;

  const toggle = async (name: string, val: any) => {
    setPending(name);
    await onSend(device.id, name, val === 1 || val === "1" ? 0 : 1);
    setPending(null);
  };

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-slate-800 rounded-xl p-6 w-full max-w-md border border-slate-600" onClick={(e) => e.stopPropagation()}>
        <h2 className="text-xl font-bold mb-1 text-cyan-400">🎛 {device.name}</h2>
        <p className="text-xs text-slate-500 mb-4">SN: {device.sn}</p>
        <div className="space-y-3">
          {Object.entries(device.signals).map(([name, signal]) => (
            <div key={name} className="bg-slate-700/50 rounded-lg p-3">
              <div className="flex justify-between mb-1">
                <span className="text-sm font-mono text-slate-300">{name}</span>
                <span className="text-xs text-slate-500 bg-slate-800 px-2 py-0.5 rounded">{signal.mgmt}</span>
              </div>
              {signal.mgmt === "rw" ? (
                <button disabled={pending === name} onClick={() => toggle(name, signal.value)}
                  className={`w-full py-2 rounded-lg font-semibold text-sm transition-all disabled:opacity-50 ${
                    signal.value === 1 || signal.value === "1"
                      ? "bg-red-600 hover:bg-red-700" : "bg-green-600 hover:bg-green-700"}`}>
                  {pending === name ? "Отправка..." : signal.value === 1 || signal.value === "1" ? `Выключить (${signal.value})` : `Включить (${signal.value})`}
                </button>
              ) : (
                <div className="text-slate-400 text-sm">Значение: <span className="text-white font-mono">{signal.value}</span></div>
              )}
            </div>
          ))}
          {Object.keys(device.signals).length === 0 && (
            <p className="text-slate-500 text-sm text-center py-4">Сигналы загружаются...</p>
          )}
        </div>
        <button onClick={onClose} className="mt-4 w-full py-2 bg-slate-600 hover:bg-slate-500 rounded-lg font-semibold text-sm">Закрыть</button>
      </div>
    </div>
  );
}
