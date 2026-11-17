import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchCharacterCount, fetchScales, type ScaleDirectory } from "../api";

const DEFAULT_TITLE = "Default Scale";
const DEFAULT_CRITERION = "Overall power";

function matchesDefault(search: string): boolean {
  const value = search.trim().toLowerCase();
  if (!value) {
    return true;
  }
  return (
    DEFAULT_TITLE.toLowerCase().includes(value) ||
    DEFAULT_CRITERION.toLowerCase().includes(value)
  );
}

export function BoardsPage() {
  const [directory, setDirectory] = useState<ScaleDirectory | null>(null);
  const [searchInput, setSearchInput] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);
  const [defaultCount, setDefaultCount] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDirectory = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setDirectory(await fetchScales(search, page));
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load scales");
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => {
    void loadDirectory();
  }, [loadDirectory]);

  useEffect(() => {
    fetchCharacterCount()
      .then(setDefaultCount)
      .catch(() => setDefaultCount(null));
  }, []);

  function handleSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(0);
    setSearch(searchInput);
  }

  function clearSearch() {
    setSearchInput("");
    setSearch("");
    setPage(0);
  }

  const showDefault = matchesDefault(search);
  const scales = directory?.content ?? [];
  const showEmpty = !loading && !showDefault && scales.length === 0;

  return (
    <section className="boards-page">
      <header className="page-header">
        <h1>Scales</h1>
        <Link className="button" to="/create">
          New scale
        </Link>
      </header>

      <form className="board-search" onSubmit={handleSearch}>
        <input
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder="Search"
          aria-label="Search scales"
        />
        <button className="button" type="submit">
          Search
        </button>
        {search && (
          <button className="button button-ghost" type="button" onClick={clearSearch}>
            Clear
          </button>
        )}
      </form>

      {error && <div className="alert">{error}</div>}
      {loading && <div className="card placeholder">Loading...</div>}

      {!loading && (
        <div className="board-grid">
          {showDefault && (
            <Link className="card board-card" to="/default">
              <h2>Default Scale</h2>
              <p className="board-criterion">{DEFAULT_CRITERION} · Elo</p>
              <div className="board-footer">
                <span>{defaultCount == null ? "Roster" : `${defaultCount} items`}</span>
                <span>Vote</span>
              </div>
            </Link>
          )}

          {scales.map((scale) => (
            <Link className="card board-card" key={scale.id} to={`/s/${scale.slug}`}>
              <h2>{scale.title}</h2>
              <p className="board-criterion">
                {scale.criterion} · {scale.rankingMethod === "POINTS" ? "Points" : "Elo"}
              </p>
              <div className="board-footer">
                <span>{scale.itemCount} items</span>
                <span>{scale.voteCount} votes</span>
              </div>
            </Link>
          ))}

          <Link className="card board-card board-create" to="/create">
            <h2>New scale</h2>
            <p className="board-criterion">Create a custom ranking</p>
          </Link>
        </div>
      )}

      {showEmpty && (
        <div className="card empty-state">
          <h2>No scales found</h2>
          <Link className="button" to="/create">
            New scale
          </Link>
        </div>
      )}

      {directory && directory.totalPages > 1 && (
        <div className="pagination">
          <button
            className="button button-ghost"
            disabled={page === 0}
            onClick={() => setPage((current) => Math.max(0, current - 1))}
          >
            Previous
          </button>
          <span className="muted">
            {directory.page + 1} / {directory.totalPages}
          </span>
          <button
            className="button button-ghost"
            disabled={directory.page + 1 >= directory.totalPages}
            onClick={() => setPage((current) => current + 1)}
          >
            Next
          </button>
        </div>
      )}
    </section>
  );
}
