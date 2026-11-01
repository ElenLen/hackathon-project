import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// Проксируем /api → https://api-uae-test.ujin.tech/api
// чтобы браузер не блокировал CORS при разработке
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      "/api": {
        target: "https://api-uae-test.ujin.tech",
        changeOrigin: true,
        secure: false,
        // /api/bmsdevice/... → https://api-uae-test.ujin.tech/api/bmsdevice/...
        // путь оставляем как есть — префикс /api уже совпадает с базой
      },
    },
  },
});
