import { Handle, Position } from "reactflow";
import { Zap } from "lucide-react";
import { useSmartHomeStore } from "../../store/useSmartHomeStore";

export default function TriggerNode({ data, id }: { data: any; id: string }) {
  const { devices } = useSmartHomeStore();
  const selectedDevice = devices.find((d) => d.id === data.deviceId);
  const selectedSignal = selectedDevice?.signals[data.signal];

  const update = (patch: any) => {
    if (data.onChange) data.onChange(id, patch);
  };

  // Определяем, является ли сигнал числовым
  const isNumeric =
    data.signal === "target_temp" ||
    data.signal === "brightness" ||
    data.signal === "current_temp";

  return (
    <div className="bg-amber-900/50 border-2 border-amber-500 rounded-lg p-3 min-w-[240px] shadow-lg">
      <Handle
        type="source"
        position={Position.Right}
        className="w-3 h-3 bg-amber-400"
      />
      <div className="flex items-center gap-2 mb-2">
        <Zap className="w-4 h-4 text-amber-400" />
        <span className="text-xs font-bold text-amber-400 uppercase">
          Триггер (ЕСЛИ)
        </span>
      </div>

      {/* Выбор устройства */}
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

      {/* Выбор сигнала */}
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

      {/* Ввод значения (зависит от типа) */}
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
            <span className="text-xs font-mono text-amber-300 w-10 text-right">
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
