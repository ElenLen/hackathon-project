import React, { useState } from 'react';
import { Plus, Trash2, Cpu, Save, Code } from 'lucide-react';

interface DeviceCapability {
  id: string;
  name: string;
  type: 'property' | 'action' | 'event';
  dataType: 'boolean' | 'number' | 'string';
  unit?: string;
}

interface CustomDevice {
  id: string;
  name: string;
  category: string;
  capabilities: DeviceCapability[];
}

export const DeviceBuilder: React.FC = () => {
  const [deviceName, setDeviceName] = useState('');
  const [category, setCategory] = useState('sensors');
  const [capabilities, setCapabilities] = useState<DeviceCapability[]>([]);
  const [showJson, setShowJson] = useState(false);

  const addCapability = () => {
    const newCap: DeviceCapability = {
      id: `cap_${Date.now()}`,
      name: '',
      type: 'property',
      dataType: 'boolean',
    };
    setCapabilities([...capabilities, newCap]);
  };

  const removeCapability = (id: string) => {
    setCapabilities(capabilities.filter((c) => c.id !== id));
  };

  const updateCapability = (id: string, field: keyof DeviceCapability, value: any) => {
    setCapabilities(
      capabilities.map((c) => (c.id === id ? { ...c, [field]: value } : c))
    );
  };

  const generatedSchema: CustomDevice = {
    id: `dev_type_${deviceName.toLowerCase().replace(/\s+/g, '_') || 'custom'}`,
    name: deviceName || 'Новое устройство',
    category,
    capabilities,
  };

  return (
    <div className="bg-slate-900 text-white p-6 rounded-xl border border-slate-800 shadow-xl max-w-4xl mx-auto">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-6">
        <div className="flex items-center gap-3">
          <Cpu className="w-7 h-7 text-indigo-400" />
          <h2 className="text-xl font-bold">Конструктор профилей устройств (Ujin Spec)</h2>
        </div>
        <button
          onClick={() => setShowJson(!showJson)}
          className="flex items-center gap-2 px-3 py-1.5 text-sm bg-slate-800 hover:bg-slate-700 rounded-lg transition"
        >
          <Code className="w-4 h-4" />
          {showJson ? 'Скрыть JSON' : 'Показать JSON'}
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div>
          <label className="block text-sm font-medium text-slate-400 mb-1">
            Название типа устройства
          </label>
          <input
            type="text"
            placeholder="Например: Датчик протечки воды"
            value={deviceName}
            onChange={(e) => setDeviceName(e.target.value)}
            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-400 mb-1">Категория</label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-indigo-500"
          >
            <option value="sensors">Датчики и безопасность</option>
            <option value="climate">Климат и Вентиляция</option>
            <option value="lighting">Освещение и Электрика</option>
            <option value="valves">Запорная арматура / Приводы</option>
            <option value="meters">Счетчики ресурсов</option>
          </select>
        </div>
      </div>

      <div className="mb-6">
        <div className="flex justify-between items-center mb-3">
          <h3 className="text-md font-semibold text-slate-300">
            Возможности (Properties / Actions / Events)
          </h3>
          <button
            onClick={addCapability}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white text-sm rounded-lg transition"
          >
            <Plus className="w-4 h-4" /> Добавить атрибут
          </button>
        </div>

        {capabilities.length === 0 ? (
          <div className="text-center py-8 text-slate-500 border border-dashed border-slate-800 rounded-lg">
            Нажмите «Добавить атрибут», чтобы описать свойства или действия устройства
          </div>
        ) : (
          <div className="space-y-3">
            {capabilities.map((cap) => (
              <div
                key={cap.id}
                className="flex items-center gap-3 bg-slate-800/60 p-3 rounded-lg border border-slate-700/50"
              >
                <input
                  type="text"
                  placeholder="Имя (например, water_leak)"
                  value={cap.name}
                  onChange={(e) => updateCapability(cap.id, 'name', e.target.value)}
                  className="flex-1 bg-slate-900 border border-slate-700 rounded px-2.5 py-1 text-sm text-white focus:outline-none"
                />

                <select
                  value={cap.type}
                  onChange={(e) =>
                    updateCapability(cap.id, 'type', e.target.value as any)
                  }
                  className="bg-slate-900 border border-slate-700 rounded px-2.5 py-1 text-sm text-slate-300"
                >
                  <option value="property">Свойство (Property)</option>
                  <option value="action">Команда (Action)</option>
                  <option value="event">Событие (Event)</option>
                </select>

                <select
                  value={cap.dataType}
                  onChange={(e) =>
                    updateCapability(cap.id, 'dataType', e.target.value as any)
                  }
                  className="bg-slate-900 border border-slate-700 rounded px-2.5 py-1 text-sm text-slate-300"
                >
                  <option value="boolean">Boolean (Вкл/Выкл)</option>
                  <option value="number">Number (Число)</option>
                  <option value="string">String (Строка)</option>
                </select>

                {cap.dataType === 'number' && (
                  <input
                    type="text"
                    placeholder="Ед.изм (ед/°C/% )"
                    value={cap.unit || ''}
                    onChange={(e) => updateCapability(cap.id, 'unit', e.target.value)}
                    className="w-24 bg-slate-900 border border-slate-700 rounded px-2 py-1 text-sm text-white"
                  />
                )}

                <button
                  onClick={() => removeCapability(cap.id)}
                  className="p-1.5 text-slate-400 hover:text-red-400 transition"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {showJson && (
        <div className="mb-6">
          <div className="text-xs font-mono text-slate-400 mb-1">Генерируемая Ujin-спецификация:</div>
          <pre className="bg-slate-950 p-4 rounded-lg border border-slate-800 text-xs font-mono text-emerald-400 overflow-x-auto">
            {JSON.stringify(generatedSchema, null, 2)}
          </pre>
        </div>
      )}

      <div className="flex justify-end gap-3 pt-4 border-t border-slate-800">
        <button
          onClick={() => alert('Профиль устройства сохранен и готов к эмуляции!')}
          className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-medium transition"
        >
          <Save className="w-4 h-4" /> Сохранить в реестре устройств
        </button>
      </div>
    </div>
  );
};