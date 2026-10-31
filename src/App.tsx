import React, { useEffect } from 'react';
import ReactFlow, { Background, Controls } from 'reactflow';
import type { Node, Edge } from 'reactflow';
import 'reactflow/dist/style.css';
import { wsClient } from './api/websocket';
import { useDeviceStore } from './store/deviceStore';
import { DeviceNode } from './components/DeviceNode';

const nodeTypes = {
  deviceNode: DeviceNode,
};

const initialNodes: Node[] = [
  {
    id: '1',
    type: 'deviceNode',
    position: { x: 250, y: 100 },
    data: { deviceId: 'sensor_1', label: 'Датчик температуры' },
  },
];

const initialEdges: Edge[] = [];

export const App: React.FC = () => {
  const setDevices = useDeviceStore((state) => state.setDevices);
  const updateDevice = useDeviceStore((state) => state.updateDevice);

  useEffect(() => {
    wsClient.connect();

    const unsubscribe = wsClient.subscribe((msg) => {
      if (msg.type === 'INIT_STATE') {
        setDevices(msg.data);
      } else if (msg.type === 'TELEMETRY' || msg.type === 'DEVICE_UPDATED') {
        updateDevice(msg.data);
      }
    });

    return () => unsubscribe();
  }, [setDevices, updateDevice]);

  return (
    <div className="w-screen h-screen bg-slate-50">
      <ReactFlow nodes={initialNodes} edges={initialEdges} nodeTypes={nodeTypes}>
        <Background />
        <Controls />
      </ReactFlow>
    </div>
  );
};

export default App;