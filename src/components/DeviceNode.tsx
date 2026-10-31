import React from 'react';
import { Handle, Position } from 'reactflow';
import { 
  Power, 
  Droplet, 
  ShieldAlert, 
  Activity, 
  Flame, 
  Zap,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';
import { useSmartHomeStore } from '../store/useSmartHomeStore';

interface DeviceNodeData {
  id: string;
  name: string;
  type: 'leak' | 'barrier' | 'boiler' | 'counter' | 'relay';
  status: 'online' | 'offline' | 'warning' | 'error';
  value?: number | string;
  sn?: string;
}

export const DeviceNode: React.FC<{ data: DeviceNodeData }> = ({ data }) => {
  const { sendSignal } = useSmartHomeStore();

  const getIcon = () => {
    switch (data.type) {
      case 'leak':
        return <Droplet className="w-6 h-6 text-cyan-400" />;
      case 'barrier':
        return <ShieldAlert className="w-6 h-6 text-amber-400" />;
      case 'boiler':
        return <Flame className="w-6 h-6 text-orange-400" />;
      case 'counter':
        return <Activity className="w-6 h-6 text-emerald-400" />;
      default:
        return <Zap className="w-6 h-6 text-blue-400" />;
    }
  };

  const handleToggle = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const isChecked = e.target.checked;
    await sendSignal(data.id, 'open', isChecked ? 1 : 0);
  };

  const handleSliderChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    await sendSignal(data.id, 'value', val);
  };

  return (
    <div className="bg-slate-800/90 border border-slate-700 rounded-xl p-4 min-w-[220px] shadow-lg backdrop-blur-md hover:border-slate-500 transition-all">
      <Handle type="target" position={Position.Top} className="w-3 h-3 !bg-cyan-500" />
      
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          {getIcon()}
          <div>
            <h4 className="font-semibold text-sm text-slate-100">{data.name}</h4>
            <span className="text-[10px] text-slate-400 font-mono">{data.sn || data.id}</span>
          </div>
        </div>
        <span className={`w-2.5 h-2.5 rounded-full ${
          data.status === 'online' ? 'bg-emerald-500 shadow-emerald-500/50 shadow-sm' :
          data.status === 'warning' ? 'bg-amber-500 shadow-amber-500/50 shadow-sm' : 'bg-rose-500'
        }`} />
      </div>

      <div className="space-y-3 pt-2 border-t border-slate-700/50 text-xs">
        {data.type === 'barrier' && (
          <div className="flex items-center justify-between">
            <span className="text-slate-400">Состояние:</span>
            <label className="relative inline-flex items-center cursor-pointer">
              <input 
                type="checkbox" 
                defaultChecked={data.value === 1 || data.value === '1'} 
                onChange={handleToggle}
                className="sr-only peer" 
              />
              <div className="w-9 h-5 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-cyan-600"></div>
            </label>
          </div>
        )}

        {data.type === 'boiler' && (
          <div className="space-y-1">
            <div className="flex justify-between text-slate-400">
              <span>Мощность:</span>
              <span className="font-mono text-cyan-400">{data.value ?? 0}%</span>
            </div>
            <input 
              type="range" 
              min="0" 
              max="100" 
              defaultValue={Number(data.value) || 0} 
              onMouseUp={handleSliderChange}
              onTouchEnd={handleSliderChange}
              className="w-full accent-cyan-500 bg-slate-700 h-1.5 rounded-lg appearance-none cursor-pointer"
            />
          </div>
        )}

        {(data.type === 'leak' || data.type === 'counter') && (
          <div className="flex justify-between items-center text-slate-300">
            <span>Значение:</span>
            <span className="font-mono font-bold text-slate-100">
              {data.value !== undefined ? String(data.value) : '0'}
            </span>
          </div>
        )}
      </div>

      <Handle type="source" position={Position.Bottom} className="w-3 h-3 !bg-cyan-500" />
    </div>
  );
};