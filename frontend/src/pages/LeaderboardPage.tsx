import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchLeaderboard, type Leaderboard } from "../api";
import { BoardTabs } from "../components/BoardTabs";
import { CharacterAvatar } from "../components/CharacterAvatar";

export function LeaderboardPage() {
  const [leaderboard, setLeaderboard] = useState<Leaderboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    fetchLeaderboard(100)
      .then((result) => {
        if (active) {
          setLeaderboard(result);
        }
      })
      .catch((loadError: unknown) => {
        if (active) {
          setError(loadError instanceof Error ? loadError.message : "Could not load the rankings");
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <section>
      <header className="page-header">
        <h1>Default Scale</h1>
      </header>

      <BoardTabs rankingsTo="/default/rankings" voteTo="/default" />

      {error && <div className="alert">{error}</div>}
      {loading && <div className="card placeholder">Loading...</div>}

      {!loading && leaderboard && leaderboard.entries.length === 0 && (
        <div className="card empty-state">
          <h2>No rankings yet</h2>
          <Link className="button" to="/default">
            Vote
          </Link>
        </div>
      )}

      {!loading && leaderboard && leaderboard.entries.length > 0 && (
        <div className="card leaderboard-list">
          <div className="leaderboard-row leaderboard-head">
            <span>Rank</span>
            <span>Name</span>
            <span>Score</span>
            <span>W-L</span>
            <span>Win %</span>
          </div>
          {leaderboard.entries.map((entry) => (
            <div className={`leaderboard-row${entry.rank <= 3 ? " is-top" : ""}`} key={entry.id}>
              <span className="leaderboard-rank">{entry.rank}</span>
              <span className="leaderboard-fighter">
                <CharacterAvatar character={entry} />
                <strong>{entry.name}</strong>
              </span>
              <span className="leaderboard-rating">
                <span className="rating-value">{entry.rating.toFixed(1)}</span>
              </span>
              <span className="leaderboard-record">
                {entry.wins}-{entry.losses}
              </span>
              <span className="leaderboard-winrate">
                {entry.votes === 0 ? "—" : `${entry.winRate.toFixed(1)}%`}
              </span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
