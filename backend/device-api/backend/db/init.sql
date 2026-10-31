CREATE TABLE IF NOT EXISTS devices(
    id TEXT PRIMARY KEY,
    ujin_sn TEXT NOT NULL,
    ext_guid TEXT UNIQUE NOT NULL,
    model TEXT,
    title TEXT,
    family TEXT,
    apartment_id TEXT,
    created_at TEXT DEFAULT (datetime('now')),
    last_seen_at TEXT
);

CREATE TABLE IF NOT EXISTS signals(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT NOT NULL,
    signal_name TEXT NOT NULL,
    value_num REAL,
    value_str TEXT,
    ts TEXT DEFAULT (datetime('now')),
    FOREIGN KEY(device_id) REFERENCES devices(id)
);
CREATE INDEX IF NOT EXISTS ix_signals_dev_ts ON signals(device_id, ts);
CREATE INDEX IF NOT EXISTS ix_signals_dev_name_ts ON signals(device_id, signal_name, ts);

CREATE TABLE IF NOT EXISTS device_tasks(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_sn TEXT NOT NULL,
    signal_name TEXT NOT NULL,
    task_dt TEXT NOT NULL,
    applied_at TEXT DEFAULT (datetime('now')),
    UNIQUE(device_sn, signal_name, task_dt)
);

CREATE TABLE IF NOT EXISTS scenarios(
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    enabled INTEGER DEFAULT 1,
    rule_json TEXT NOT NULL,
    created_at TEXT DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS scenario_runs(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id TEXT NOT NULL,
    triggered_at TEXT DEFAULT (datetime('now')),
    trigger_snapshot TEXT,
    actions_json TEXT,
    ok INTEGER
);
