import { Navigate, NavLink, Route, Routes } from "react-router-dom";
import { BoardsPage } from "./pages/BoardsPage";
import { CreateScalePage } from "./pages/CreateScalePage";
import { LeaderboardPage } from "./pages/LeaderboardPage";
import { ScalePage } from "./pages/ScalePage";
import { ScaleRankingsPage } from "./pages/ScaleRankingsPage";
import { VotePage } from "./pages/VotePage";

const navClassName = ({ isActive }: { isActive: boolean }) =>
  isActive ? "nav-link active" : "nav-link";

export function App() {
  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/">
          <span className="brand-mark">PS</span>
          <span className="brand-title">Powerscale</span>
        </NavLink>

        <nav className="site-nav">
          <NavLink className={navClassName} to="/" end>
            Scales
          </NavLink>
          <NavLink className={navClassName} to="/create">
            Create
          </NavLink>
        </nav>
      </header>

      <main className="site-main">
        <Routes>
          <Route element={<BoardsPage />} path="/" />
          <Route element={<VotePage />} path="/default" />
          <Route element={<LeaderboardPage />} path="/default/rankings" />
          <Route element={<Navigate replace to="/default/rankings" />} path="/leaderboard" />
          <Route element={<CreateScalePage />} path="/create" />
          <Route element={<ScalePage />} path="/s/:slug" />
          <Route element={<ScaleRankingsPage />} path="/s/:slug/rankings" />
          <Route element={<Navigate replace to="/" />} path="*" />
        </Routes>
      </main>
    </div>
  );
}
