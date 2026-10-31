export type DeviceState = {
    device_id: string;
    type: string;
    state: {
      power: boolean;
      status: string;
      temperature?: number;
      humidity?: number;
      [key: string]: any;
    };
  };
  
  type MessageHandler = (data: { type: string; data: any }) => void;
  
  export class EmulatorWSClient {
    private socket: WebSocket | null = null;
    private handlers: Set<MessageHandler> = new Set();
  
    constructor(private url: string) {}
  
    connect() {
      this.socket = new WebSocket(this.url);
  
      this.socket.onopen = () => {
        console.log('Connected to Emulator WS');
      };
  
      this.socket.onmessage = (event) => {
        try {
          const parsed = JSON.parse(event.data);
          this.handlers.forEach((handler) => handler(parsed));
        } catch (e) {
          console.error('Failed to parse WS message', e);
        }
      };
  
      this.socket.onclose = () => {
        console.log('WS Connection closed, retrying in 3s...');
        setTimeout(() => this.connect(), 3000);
      };
    }
  
    subscribe(handler: MessageHandler) {
      this.handlers.add(handler);
      return () => this.handlers.delete(handler);
    }
  
    sendCommand(deviceId: string, command: string, payload: Record<string, any> = {}) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(
          JSON.stringify({
            type: 'COMMAND',
            device_id: deviceId,
            command,
            payload,
          })
        );
      }
    }
  }
  
  export const wsClient = new EmulatorWSClient('ws://localhost:8080');