import { useCallback, useState, useEffect } from "react";
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  addEdge,
  useNodesState,
  useEdgesState,
} from "reactflow";
import type { Connection, Node } from "reactflow";
import "reactflow/dist/style.css";
import { Plus, Save, Play, Trash2 } from "lucide-react";
import TriggerNode from "../components/nodes/TriggerNode";
import ActionNode from "../components/nodes/ActionNode";
import { useSmartHomeStore } from "../store/useSmartHomeStore";

const nodeTypes = {
  triggerNode: TriggerNode,
  actionNode: ActionNode,
};

export default function ScenarioEditor() {
  const {
    devices,
    scenarios,
    saveScenario,
    addLog,
    triggerScenarioManually,
    applyDifference,
  } = useSmartHomeStore();

  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [scenarioName, setScenarioName] = useState("Новый сценарий");

  // Если зашли сразу в редактор, а устройства пустые — грузим их из Ujin
  useEffect(() => {
    const needFetch =
      devices.length === 0 ||
      devices.every((d) => Object.keys(d.signals).length === 0);
    if (needFetch) {
      addLog("🔄 Загрузка данных из Ujin OS для редактора...");
      applyDifference();
    }
  }, [devices.length, applyDifference, addLog]);

  // Синхронизация данных ноды с React Flow (чтобы списки "залипали")
  const handleNodeChange = useCallback(
    (nodeId: string, patch: any) => {
      setNodes((nds) =>
        nds.map((n) =>
          n.id === nodeId ? { ...n, data: { ...n.data, ...patch } } : n,
        ),
      );
    },
    [setNodes],
  );

  const onConnect = useCallback(
    (params: Connection) =>
      setEdges((eds) => addEdge({ ...params, animated: true }, eds)),
    [setEdges],
  );

  const addTrigger = () => {
    const newNode: Node = {
      id: `trigger_${Date.now()}`,
      type: "triggerNode",
      position: { x: 100, y: 150 },
      data: { deviceId: "", signal: "", value: "", onChange: handleNodeChange },
    };
    setNodes((nds) => [...nds, newNode]);
  };

  const addAction = () => {
    const newNode: Node = {
      id: `action_${Date.now()}`,
      type: "actionNode",
      position: { x: 500, y: 150 },
      data: { deviceId: "", signal: "", value: "", onChange: handleNodeChange },
    };
    setNodes((nds) => [...nds, newNode]);
  };

  const handleSave = () => {
    const id = `scenario_${Date.now()}`;
    saveScenario({
      id,
      name: scenarioName,
      isActive: true,
      nodes,
      edges,
    });
    addLog(`💾 Сценарий «${scenarioName}» сохранён (${nodes.length} нод)`);
    alert(`Сценарий «${scenarioName}» сохранён!`);
  };

  const handleTest = () => {
    const id = `scenario_${Date.now()}`;
    saveScenario({ id, name: scenarioName, isActive: true, nodes, edges });
    setTimeout(() => triggerScenarioManually(id), 100);
  };

  const clearCanvas = () => {
    setNodes([]);
    setEdges([]);
  };

  return (
    <div className="h-[calc(100vh-120px)] flex flex-col">
      {/* Панель управления */}
      <div className="bg-slate-800 p-3 rounded-t-xl border border-slate-700 flex items-center gap-3 flex-wrap">
        <input
          type="text"
          value={scenarioName}
          onChange={(e) => setScenarioName(e.target.value)}
          placeholder="Название сценария"
          className="bg-slate-900 border border-slate-600 rounded px-3 py-1.5 text-sm text-white w-64"
        />
        <button
          onClick={addTrigger}
          className="flex items-center gap-1 px-3 py-1.5 bg-amber-600 hover:bg-amber-500 rounded text-sm font-medium text-white"
        >
          <Plus className="w-4 h-4" /> Триггер
        </button>
        <button
          onClick={addAction}
          className="flex items-center gap-1 px-3 py-1.5 bg-cyan-600 hover:bg-cyan-500 rounded text-sm font-medium text-white"
        >
          <Plus className="w-4 h-4" /> Действие
        </button>
        <div className="flex-1" />
        <button
          onClick={clearCanvas}
          className="flex items-center gap-1 px-3 py-1.5 bg-slate-700 hover:bg-slate-600 rounded text-sm text-white"
        >
          <Trash2 className="w-4 h-4" /> Очистить
        </button>
        <button
          onClick={handleTest}
          className="flex items-center gap-1 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 rounded text-sm font-medium text-white"
        >
          <Play className="w-4 h-4" /> Тест
        </button>
        <button
          onClick={handleSave}
          className="flex items-center gap-1 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 rounded text-sm font-medium text-white"
        >
          <Save className="w-4 h-4" /> Сохранить
        </button>
      </div>

      {/* Холст */}
      <div className="flex-1 bg-slate-950 rounded-b-xl border border-t-0 border-slate-700">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          nodeTypes={nodeTypes}
          fitView
        >
          <Background color="#334155" gap={16} />
          <Controls />
          <MiniMap nodeColor="#06b6d4" maskColor="rgba(0,0,0,0.7)" />
        </ReactFlow>
      </div>

      {/* Список сохранённых сценариев */}
      <div className="bg-slate-800/50 p-3 rounded-xl border border-slate-700 mt-3">
        <h3 className="text-sm font-semibold text-slate-400 mb-2">
          Сохранённые сценарии ({scenarios.length})
        </h3>
        <div className="flex gap-2 flex-wrap">
          {scenarios.map((s) => (
            <div
              key={s.id}
              className="bg-slate-700 px-3 py-1.5 rounded text-xs flex items-center gap-2 text-white"
            >
              <span>{s.name}</span>
              <button
                onClick={() => triggerScenarioManually(s.id)}
                className="text-emerald-400 hover:text-emerald-300"
              >
                ▶
              </button>
            </div>
          ))}
          {scenarios.length === 0 && (
            <span className="text-xs text-slate-500">Пока нет сценариев</span>
          )}
        </div>
      </div>
    </div>
  );
}
