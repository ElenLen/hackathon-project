import { useState } from "react";
import { authenticate } from "../api/ujin";
import { LogIn, Loader2, ShieldCheck, AlertCircle } from "lucide-react";

interface LoginProps {
  onLoginSuccess: (profile: any) => void;
}

export default function Login({ onLoginSuccess }: LoginProps) {
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const apiUser = await authenticate(login, password);

      if (!apiUser) {
        setError(
          "Неверный логин или пароль. Проверьте данные и попробуйте снова.",
        );
        setIsLoading(false);
        return;
      }

      const apt = apiUser.apartment?.[0];
      const fullName = apiUser.user_fullname || "Пользователь";
      const initials = fullName
        .split(" ")
        .map((n: string) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);

      const profile = {
        name: fullName,
        role: "Управляющая компания",
        apartmentTitle: apt?.title || "Квартира не указана",
        complexTitle: apt?.complex_title || "ЖК не указан",
        phone: apiUser.user_phone || "",
        initials,
        token: apiUser.token,
      };

      localStorage.setItem("ujin_user_v3", JSON.stringify(profile));
      onLoginSuccess(profile);
    } catch (err) {
      console.error(err);
      setError("Ошибка сервера. Попробуйте позже.");
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900 px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-cyan-400">Ujin OS</h1>
          <p className="text-sm text-slate-400 mt-1">
            BMS Panel для управляющей компании
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-slate-800 rounded-xl p-6 border border-slate-700 shadow-xl"
        >
          <h2 className="text-xl font-semibold text-white mb-4">
            Вход в систему
          </h2>

          <div className="mb-4">
            <label className="block text-sm font-medium text-slate-300 mb-1">
              Телефон или логин
            </label>
            <input
              type="text"
              value={login}
              onChange={(e) => setLogin(e.target.value)}
              placeholder="79908724352"
              required
              autoComplete="username"
              className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-slate-300 mb-1">
              Пароль
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
              autoComplete="current-password"
              className="w-full bg-slate-900 border border-slate-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-900/30 border border-red-700 rounded-lg flex items-start gap-2">
              <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
              <span className="text-sm text-red-300">{error}</span>
            </div>
          )}

          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-2.5 bg-cyan-600 hover:bg-cyan-500 disabled:bg-slate-600 rounded-lg font-medium text-white flex items-center justify-center gap-2 transition-colors"
          >
            {isLoading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <LogIn className="w-4 h-4" />
            )}
            {isLoading ? "Проверка..." : "Войти"}
          </button>

          <div className="mt-4 flex items-center gap-2 text-xs text-slate-500">
            <ShieldCheck className="w-4 h-4 text-emerald-500" />
            <span>Пароль не сохраняется, только токен сессии</span>
          </div>
        </form>

        <p className="text-center text-xs text-slate-600 mt-4">
          Хакатон «Код. Город. Будущее.» · Команда «хихи-хахакеры»
        </p>
      </div>
    </div>
  );
}
