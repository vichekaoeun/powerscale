import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  fetchScale,
  fetchScaleLeaderboard,
  type Scale,
  type ScaleLeaderboard,
} from "../api";
import { BoardTabs } from "../components/BoardTabs";
import { CharacterAvatar } from "../components/CharacterAvatar";

export function ScaleRankingsPage() {
  const { slug = "" } = useParams();
  const [scale, setScale] = useState<Scale | null>(null);
  const [leaderboard, setLeaderboard] = useState<ScaleLeaderboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const nextScale = await fetchScale(slug);
      setScale(nextScale);
      setLeaderboard(await fetchScaleLeaderboard(nextScale.id));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load the rankings");
    } finally {
      setLoading(false);
    }
  }, [slug]);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading) {
    return <div className="card placeholder">Loading...</div>;
  }

  if (!scale) {
    return (
      <div className="card empty-state">
        <h2>Scale not found</h2>
        <Link className="button" to="/">
          Back to scales
        </Link>
      </div>
    );
  }

  const entries = leaderboard?.entries ?? [];

  return (
    <section className="scale-page">
      <header className="page-header">
        <h1>{scale.title}</h1>
      </header>

      <BoardTabs rankingsTo={`/s/${slug}/rankings`} voteTo={`/s/${slug}`} />

      {error && <div className="alert">{error}</div>}

      {entries.length === 0 ? (
        <div className="card empty-state">
          <h2>No rankings yet</h2>
          <Link className="button" to={`/s/${slug}`}>
            Vote
          </Link>
        </div>
      ) : (
        <div className="card leaderboard-list">
          <div className="leaderboard-row leaderboard-head">
            <span>Rank</span>
            <span>Name</span>
            <span>Score</span>
            <span>W-L</span>
            <span>Win %</span>
          </div>
          {entries.map((entry) => (
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
