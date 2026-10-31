import React, { useState } from 'react';
import { Plus, Cpu, Save, Shield, Droplets, Zap } from 'lucide-react';
import { useSmartHomeStore } from '../store/useSmartHomeStore';

export const DeviceBuilder: React.FC = () => {
  const { addDevice } = useSmartHomeStore();
  const [name, setName] = useState('');
  const [type, setType] = useState<'leak' | 'barrier' | 'boiler' | 'counter' | 'relay'>('leak');
  const [sn, setSn] = useState('');
  const [signals, setSignals] = useState<{ name: str; code: str }[]>([
    { name: 'Протечка', code: 'leak_status' }
  ]);

  const handleAddSignal = () => {
    setSignals([...signals, { name: '', code: '' }]);
  };

  const handleSaveDevice = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !sn) return;

    const newDevice = {
      id: `dev_${Date.now()}`,
      name,
      type,
      sn,
      status: 'online' as const,
      value: type === 'barrier' ? 1 : 0
    };

    addDevice(newDevice);
    setName('');
    setSn('');
    alert('Устройство успешно добавлено в схему!');
  };

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 backdrop-blur-xl shadow-xl">
      <div className="flex items-center gap-3 mb-6">
        <Cpu className="w-6 h-6 text-cyan-400" />
        <h2 className="text-xl font-bold text-slate-100">Конструктор устройств Ujin OS</h2>
      </div>

      <form onSubmit={handleSaveDevice} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
            Название устройства
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Например: Задвижка вводная (Кухня)"
            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-100 focus:outline-none focus:border-cyan-500 transition-colors"
            required
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Тип устройства
            </label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value as any)}
              className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-100 focus:outline-none focus:border-cyan-500 transition-colors"
            >
              <option value="leak">Датчик протечки</option>
              <option value="barrier">Запорный кран / Барьер</option>
              <option value="boiler">Котел / Нагреватель</option>
              <option value="counter">Счетчик импульсов</option>
              <option value="relay">Силовое реле</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Серийный номер (S/N)
            </label>
            <input
              type="text"
              value={sn}
              onChange={(e) => setSn(e.target.value)}
              placeholder="SN-998234-UJ"
              className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-100 focus:outline-none focus:border-cyan-500 transition-colors"
              required
            />
          </div>
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Сигналы устройства (Ujin Signal Protocol)
            </label>
            <button
              type="button"
              onClick={handleAddSignal}
              className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1"
            >
              <Plus className="w-3.5 h-3.5" /> Добавить сигнал
            </button>
          </div>

          <div className="space-y-2">
            {signals.map((sig, idx) => (
              <div key={idx} className="flex gap-2">
                <input
                  type="text"
                  placeholder="Имя сигнала (напр. Затопление)"
                  value={sig.name}
                  onChange={(e) => {
                    const copy = [...signals];
                    copy[idx].name = e.target.value;
                    setSignals(copy);
                  }}
                  className="flex-1 bg-slate-800 border border-slate-700 rounded-lg px-3 py-1.5 text-xs text-slate-100"
                />
                <input
                  type="text"
                  placeholder="Код (s_leak)"
                  value={sig.code}
                  onChange={(e) => {
                    const copy = [...signals];
                    copy[idx].code = e.target.value;
                    setSignals(copy);
                  }}
                  className="flex-1 bg-slate-800 border border-slate-700 rounded-lg px-3 py-1.5 text-xs text-slate-100 font-mono"
                />
              </div>
            ))}
          </div>
        </div>

        <button
          type="submit"
          className="w-full bg-cyan-600 hover:bg-cyan-500 text-white font-semibold py-2.5 rounded-lg flex items-center justify-center gap-2 transition-colors shadow-lg shadow-cyan-600/20"
        >
          <Save className="w-4 h-4" /> Сохранить в схему и Ujin
        </button>
      </form>
    </div>
  );
};