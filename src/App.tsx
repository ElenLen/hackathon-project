import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import ScenarioEditor from "./pages/ScenarioEditor";

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-slate-900 text-slate-100">
        <nav className="p-4 bg-slate-800 flex gap-4 border-b border-slate-700">
          <Link to="/" className="hover:text-cyan-400">
            Дашборд
          </Link>
          <Link to="/editor" className="hover:text-cyan-400">
            Редактор сценариев
          </Link>
        </nav>
        <main className="p-6">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/editor" element={<ScenarioEditor />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
