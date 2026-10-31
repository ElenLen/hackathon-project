import React, { useState } from 'react';
import { Play, Plus, Zap, ArrowRight, CheckCircle2, ShieldAlert } from 'lucide-react';

export const ScenarioEditor: React.FC = () => {
  const [trigger, setTrigger] = useState('leak_detected');
  const [action, setAction] = useState('close_valves');
  const [isExecuting, setIsExecuting] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);

  const runSimulation = () => {
    setIsExecuting(true);
    setLogs((prev) => [`[${new Date().toLocaleTimeString()}] Сценарий запущен: Триггер (${trigger})`, ...prev]);
    
    setTimeout(() => {
      setLogs((prev) => [`[${new Date().toLocaleTimeString()}] Команда отправлена в Ujin OS Core`, ...prev]);
    }, 600);

    setTimeout(() => {
      setLogs((prev) => [`[${new Date().toLocaleTimeString()}] Эмулятор выполнил действие: (${action}) успешно!`, ...prev]);
      setIsExecuting(false);
    }, 1400);
  };

  return (
    <div className="bg-slate-900 text-white p-6 rounded-xl border border-slate-800 shadow-xl max-w-4xl mx-auto">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-6">
        <div className="flex items-center gap-3">
          <Zap className="w-7 h-7 text-amber-400" />
          <h2 className="text-xl font-bold">Визуальный редактор сценариев (Ujin Rules Engine)</h2>
        </div>
        <button
          onClick={runSimulation}
          disabled={isExecuting}
          className="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-500 disabled:bg-slate-700 text-white font-medium rounded-lg transition shadow-lg"
        >
          <Play className="w-4 h-4" />
          {isExecuting ? 'Выполнение...' : 'Тест сценария'}
        </button>
      </div>

      {/* Блочная схема сценария (No-code) */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center mb-6 bg-slate-950 p-6 rounded-xl border border-slate-800">
        
        {/* Узел 1: Триггер */}
        <div className="bg-slate-900 border border-slate-700 p-4 rounded-lg relative">
          <div className="text-xs text-amber-400 font-semibold uppercase mb-1">Шаг 1: Триггер (ЕСЛИ)</div>
          <select
            value={trigger}
            onChange={(e) => setTrigger(e.target.value)}
            className="w-full bg-slate-950 border border-slate-700 rounded p-2 text-sm text-white focus:outline-none"
          >
            <option value="leak_detected">Датчик протечки: Обнаружена вода</option>
            <option value="motion_detected">Датчик движения: Активность в коридоре</option>
            <option value="temp_high">Термостат: Температура > 28°C</option>
          </select>
        </div>

        <div className="flex justify-center text-slate-500">
          <ArrowRight className="w-6 h-6 hidden md:block" />
          <span className="md:hidden text-sm font-bold">ТО</span>
        </div>

        {/* Узел 2: Действие */}
        <div className="bg-slate-900 border border-slate-700 p-4 rounded-lg">
          <div className="text-xs text-indigo-400 font-semibold uppercase mb-1">Шаг 2: Действие (ТОГДА)</div>
          <select
            value={action}
            onChange={(e) => setAction(e.target.value)}
            className="w-full bg-slate-950 border border-slate-700 rounded p-2 text-sm text-white focus:outline-none"
          >
            <option value="close_valves">Перекрыть шаровые краны ввода воды</option>
            <option value="turn_off_lights">Выключить освещение во всей квартире</option>
            <option value="notify_uk">Отправить экстренный вызов в диспетчерскую УК</option>
          </select>
        </div>

      </div>

      {/* Терминал / Логи выполнения */}
      <div>
        <div className="text-sm font-medium text-slate-400 mb-2 flex items-center gap-2">
          <ShieldAlert className="w-4 h-4 text-emerald-400" /> Логи исполнения сценария в реальном времени:
        </div>
        <div className="bg-slate-950 border border-slate-800 rounded-lg p-4 h-36 overflow-y-auto font-mono text-xs space-y-1.5 text-slate-300">
          {logs.length === 0 ? (
            <span className="text-slate-600">Нажмите «Тест сценария», чтобы проверить связку с Python-эмулятором...</span>
          ) : (
            logs.map((log, idx) => (
              <div key={idx} className="flex items-center gap-2 text-emerald-400">
                <CheckCircle2 className="w-3.5 h-3.5 flex-shrink-0" />
                <span>{log}</span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};