import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import ScenarioEditor from "./pages/ScenarioEditor";
import UserMenu from "./components/UserMenu";
import Login from "./pages/Login";
import { useAuth } from "./hooks/useAuth";
import { Building2 } from "lucide-react";

function AppContent() {
  const { user, logout, isLoading, setUser } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center">
        <div className="text-cyan-400 text-lg">Загрузка...</div>
      </div>
    );
  }

  if (!user) {
    return <Login onLoginSuccess={(profile) => setUser(profile)} />;
  }

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100">
      <nav className="p-4 bg-slate-800 flex gap-4 border-b border-slate-700 items-center">
        <div className="px-4">
          <svg width="109" height="39" viewBox="0 0 109 39" fill="none">
            <path
              d="M67.0355 7.66016H61.9325V25.2375L57.3892 25.2374C54.3562 25.2374 52.2932 23.1703 52.2932 20.1356V11.5853C52.2932 9.4176 50.537 7.66029 48.3706 7.66029H47.1902V20.1356C47.1902 25.7716 51.7564 30.3406 57.3892 30.3406L67.0355 30.3405V7.66016Z"
              fill="white"
            />
            <path
              d="M89.1505 30.3411H94.2535V12.7639H98.7969C101.829 12.7639 103.892 14.8309 103.892 17.8658V26.4159C103.892 28.5837 105.649 30.341 107.815 30.341H108.996V17.8658C108.996 12.2296 104.43 7.66064 98.7969 7.66064L89.1505 7.66078V30.3411Z"
              fill="white"
            />
            <path
              d="M85.2065 5.12456C85.2065 4.45441 85.0744 3.79083 84.818 3.1717C84.5615 2.55256 84.1856 1.99001 83.7118 1.51614C83.2379 1.04228 82.6753 0.666386 82.0562 0.409933C81.4371 0.153479 80.7735 0.0214844 80.1033 0.0214844V5.12456H85.2065Z"
              fill="white"
            />
            <path
              d="M76.1141 32.8653C76.1141 33.5355 75.9821 34.199 75.7257 34.8182C75.4692 35.4373 75.0933 35.9999 74.6195 36.4737C74.1456 36.9476 73.5831 37.3235 72.9639 37.5799C72.3448 37.8364 71.6812 37.9684 71.0111 37.9684L70.9717 7.646H76.0748L76.1141 32.8653Z"
              fill="white"
            />
            <path
              d="M80.0714 7.66016H85.1745V30.3405H80.0714V7.66016Z"
              fill="white"
            />
            <path
              d="M0 5.38774C0 2.41217 2.41217 0 5.38775 0H38.0509V38.0509H5.38774C2.41217 38.0509 0 35.6388 0 32.6632V5.38774Z"
              fill="#00C0C9"
            />
            <path
              d="M31.4285 5.16357H25.0498V26.4878H19.3709C15.5798 26.4878 13.0012 23.8955 13.0012 20.0894V10.0863C13.0012 7.36765 10.8059 5.16374 8.09789 5.16374H6.62244V20.0894C6.62244 27.1579 12.3301 32.888 19.3709 32.888L31.4285 32.8878V5.16357Z"
              fill="white"
            />
          </svg>
        </div>
        <Link to="/" className="hover:text-cyan-400">
          Дашборд
        </Link>
        <Link to="/editor" className="hover:text-cyan-400">
          Редактор сценариев
        </Link>
        <div className="flex-1" />
        <div className="hidden md:flex items-center gap-2 mr-2 px-3 py-1.5 bg-slate-900/60 rounded-lg border border-slate-700">
          <Building2 className="w-4 h-4 text-cyan-400" />
          <div className="flex flex-col">
            <span className="text-[10px] uppercase text-slate-500 tracking-wider">
              Объект
            </span>
            <span className="text-xs font-medium text-cyan-400">
              {user.complexTitle} · {user.apartmentTitle}
            </span>
          </div>
        </div>
        <UserMenu user={user} onLogout={logout} />
      </nav>
      <main className="p-6">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/editor" element={<ScenarioEditor />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}
