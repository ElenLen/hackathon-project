import { Handle, Position } from "reactflow";
import { Play } from "lucide-react";
import { useSmartHomeStore } from "../../store/useSmartHomeStore";

export default function ActionNode({ data, id }: { data: any; id: string }) {
  const { devices } = useSmartHomeStore();
  const selectedDevice = devices.find((d) => d.id === data.deviceId);

  const update = (patch: any) => {
    if (data.onChange) data.onChange(id, patch);
  };

  const isNumeric =
    data.signal === "target_temp" || data.signal === "brightness";

  return (
    <div className="bg-cyan-900/50 border-2 border-cyan-500 rounded-lg p-3 min-w-[240px] shadow-lg">
      <Handle
        type="target"
        position={Position.Left}
        className="w-3 h-3 bg-cyan-400"
      />
      <div className="flex items-center gap-2 mb-2">
        <Play className="w-4 h-4 text-cyan-400" />
        <span className="text-xs font-bold text-cyan-400 uppercase">
          Действие (ТОГДА)
        </span>
      </div>

      <select
        value={data.deviceId || ""}
        onChange={(e) =>
          update({ deviceId: e.target.value, signal: "", value: "" })
        }
        className="w-full bg-slate-800 border border-slate-600 rounded px-2 py-1 text-xs text-white mb-1"
      >
        <option value="">Выбери устройство...</option>
        {devices.map((d) => (
          <option key={d.id} value={d.id}>
            {d.name}
          </option>
        ))}
      </select>

      {selectedDevice && (
        <select
          value={data.signal || ""}
          onChange={(e) => update({ signal: e.target.value, value: "" })}
          className="w-full bg-slate-800 border border-slate-600 rounded px-2 py-1 text-xs text-white mb-1"
        >
          <option value="">Выбери сигнал...</option>
          {Object.keys(selectedDevice.signals).map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      )}

      {data.signal &&
        (isNumeric ? (
          <div className="flex items-center gap-2 mt-1">
            <input
              type="range"
              min={0}
              max={data.signal === "target_temp" ? 35 : 100}
              value={Number(data.value) || 0}
              onChange={(e) => update({ value: Number(e.target.value) })}
              className="flex-1"
            />
            <span className="text-xs font-mono text-cyan-300 w-10 text-right">
              {data.value || 0}
              {data.signal === "target_temp" ? "°C" : "%"}
            </span>
          </div>
        ) : (
          <select
            value={data.value ?? ""}
            onChange={(e) => update({ value: e.target.value })}
            className="w-full bg-slate-800 border border-slate-600 rounded px-2 py-1 text-xs text-white mt-1"
          >
            <option value="">Выбери состояние...</option>
            <option value="1">Включить (1)</option>
            <option value="0">Выключить (0)</option>
          </select>
        ))}
    </div>
  );
}
