# Ujin Smart Home — Редактор сценариев

Инструмент для создания автоматизаций умного дома на базе платформы **Ujin OS**.  
Позволяет описывать устройства, настраивать правила взаимодействия и тестировать сценарии на эмуляторах — без реального оборудования.

## Демо-сценарий

```
Датчик протечки → обнаружена вода
       ↓
  Шлагбаум → открыть (для аварийных служб)
```

## Стек технологий

| Слой | Технологии |
|------|-----------|
| Frontend | React, TypeScript, Vite, Zustand, React Flow, Tailwind CSS |
| Backend | FastAPI (Python), SQLite |
| Эмуляторы | Python, WebSocket, HTTP |
| Интеграция | Ujin OS REST API, WebSocket |

## Быстрый старт

### Frontend
```bash
npm install
npm run dev
Открой [http://localhost:5173](http://localhost:5173).
```

### Backend
```bash
cd backend/device-api
pip install -r requirements.txt   
uvicorn backend.main:app --reload
```

### Эмуляторы устройств
```bash
cd emulators
python main.py
```

## Структура проекта

```
├── src/                    # React frontend
│   ├── api/                # Ujin OS API client
│   ├── components/         # DeviceBuilder, ScenarioEditor, nodes
│   ├── pages/              # Dashboard, Login, ScenarioEditor
│   ├── store/              # Zustand state (devices, scenarios, logs)
│   └── types/              # TypeScript types
├── backend/
│   └── device-api/
│       ├── backend/        # FastAPI app
│       └── emulators/      # Device emulator library
│           └── devices/    # leak, valve, barrier, thermostat, light…
├── emulators/              # Standalone emulator runner
└── ROADMAP.md
```

## Демо

_(ссылка на видео будет здесь)_

## Команда

- [Елена] — Frontend Developer
- [Анастасия] — Backend Developer
- [Екатерина] — Дизайнер, аналитик и презентация
- [Андрей] — Мобильная разработка
- [Екатерина] — Backend Developer 2

## Быстрый старт

Для запуска вам понадобятся **два окна терминала** (или две вкладки в VS Code).

### 1. Запуск Бэкенда (FastAPI)
Откройте первый терминал и перейдите в папку бэкенда:

```bash
cd backend/device-api
pip install -r requirements.txt   
cp .env.example .env
ЗАПУСК СЕРВЕРА (Специальная команда):
Из-за особенностей путей в Windows стандартная команда uvicorn backend.main:app --reload может выдать ошибку ModuleNotFoundError. Используйте эту проверенную команду, которая принудительно добавляет нужные папки в пути поиска Python:

bash
py -c "import sys; sys.path.append('backend'); import uvicorn; uvicorn.run('backend.main:app', port=8001)"
Признак успеха: В консоли появится INFO: Application startup complete и Uvicorn running on http://0.0.0.0:8001.
(Ошибки API returned error object: 404 про Ujin — это нормально, бэкенд работает в локальном режиме Ujin=off).

2. Запуск Веб-интерфейса (Frontend)
Не закрывая первый терминал, откройте второй терминал в корне проекта:

bash
npm install --legacy-peer-deps
npm run dev
Откройте в браузере http://localhost:5173.