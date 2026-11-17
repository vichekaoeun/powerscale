import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  addScaleItems,
  fetchScale,
  fetchScaleMatchup,
  submitScaleVote,
  type Difficulty,
  type Scale,
  type ScaleMatchup,
} from "../api";
import { BoardTabs } from "../components/BoardTabs";
import { CharacterAvatar } from "../components/CharacterAvatar";
import { ItemEditor, emptyItem, toItemInputs, type ItemDraft } from "../components/ItemEditor";

const DIFFICULTIES: Difficulty[] = ["LOW", "MID", "HIGH"];

export function ScalePage() {
  const { slug = "" } = useParams();
  const [scale, setScale] = useState<Scale | null>(null);
  const [matchup, setMatchup] = useState<ScaleMatchup | null>(null);
  const [itemDrafts, setItemDrafts] = useState<ItemDraft[]>([emptyItem()]);
  const [loading, setLoading] = useState(true);
  const [matchupLoading, setMatchupLoading] = useState(false);
  const [submitting, setSubmitting] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const loadMatchup = useCallback(async () => {
    setMatchupLoading(true);
    setError(null);
    try {
      setMatchup(await fetchScaleMatchup(slug));
    } catch (loadError) {
      setMatchup(null);
      setError(loadError instanceof Error ? loadError.message : "Could not load a matchup");
    } finally {
      setMatchupLoading(false);
    }
  }, [slug]);

  const loadScale = useCallback(async () => {
    setLoading(true);
    setError(null);
    setNotice(null);
    try {
      const nextScale = await fetchScale(slug);
      setScale(nextScale);
      if (nextScale.items.length >= 2) {
        await loadMatchup();
      }
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : "Could not load the scale");
    } finally {
      setLoading(false);
    }
  }, [loadMatchup, slug]);

  useEffect(() => {
    void loadScale();
  }, [loadScale]);

  async function handleVote(itemId: string, difficulty: Difficulty) {
    if (!matchup) {
      return;
    }

    const key = `${itemId}-${difficulty}`;
    setSubmitting(key);
    setError(null);
    setNotice(null);

    try {
      const vote = await submitScaleVote(slug, matchup.id, itemId, difficulty);
      await loadMatchup();
      setNotice(`Voted ${vote.winner.name}.`);
    } catch (voteError) {
      setError(voteError instanceof Error ? voteError.message : "Could not save your vote");
    } finally {
      setSubmitting(null);
    }
  }

  async function handleAddItems(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const items = toItemInputs(itemDrafts);
    if (items.length === 0) {
      setError("Add at least one item.");
      return;
    }

    setAdding(true);
    setError(null);
    setNotice(null);
    try {
      const updated = await addScaleItems(slug, items);
      setScale(updated);
      setItemDrafts([emptyItem()]);
      setNotice(`Added ${items.length} item${items.length === 1 ? "" : "s"}.`);
      if (updated.items.length >= 2) {
        await loadMatchup();
      }
    } catch (addError) {
      setError(addError instanceof Error ? addError.message : "Could not add items");
    } finally {
      setAdding(false);
    }
  }

  async function copyShareLink() {
    try {
      await navigator.clipboard.writeText(`${window.location.origin}/s/${slug}`);
      setNotice("Link copied.");
    } catch {
      setNotice("Could not copy the link.");
    }
  }

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

  return (
    <section className="scale-page">
      <header className="page-header">
        <h1>{scale.title}</h1>
        <div className="header-actions">
          <button className="button button-ghost" onClick={() => void copyShareLink()}>
            Copy link
          </button>
        </div>
      </header>

      <BoardTabs rankingsTo={`/s/${slug}/rankings`} voteTo={`/s/${slug}`} />

      {error && <div className="alert">{error}</div>}
      {notice && <div className="notice">{notice}</div>}

      {scale.items.length < 2 && (
        <div className="card empty-state">
          <h2>Add at least two characters to start voting</h2>
        </div>
      )}

      {scale.items.length >= 2 && (
        <>
          {matchupLoading && <div className="card placeholder">Loading...</div>}

          {!matchupLoading && matchup && (
            <>
              <div className="arena">
                <div className="fighter">
                  <CharacterAvatar character={matchup.itemA} size="lg" />
                  <h2>{matchup.itemA.name}</h2>
                </div>
                <div className="versus">VS</div>
                <div className="fighter">
                  <CharacterAvatar character={matchup.itemB} size="lg" />
                  <h2>{matchup.itemB.name}</h2>
                </div>
              </div>

              <div className="vote-grid">
                {[matchup.itemA, matchup.itemB].map((item) => (
                  <div className="vote-column" key={item.id}>
                    <h2>{item.name}</h2>
                    {scale.selections.map((label, index) => {
                      const difficulty = DIFFICULTIES[index];
                      const key = `${item.id}-${difficulty}`;

                      return (
                        <button
                          key={difficulty}
                          className="vote-option"
                          onClick={() => void handleVote(item.id, difficulty)}
                          disabled={submitting !== null}
                        >
                          <span className="vote-label">
                            {label}
                            {submitting === key ? " · saving" : ""}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                ))}
              </div>
            </>
          )}
        </>
      )}

      <section className="card creator-panel">
        <h2>Add characters</h2>
        <form onSubmit={handleAddItems}>
          <ItemEditor items={itemDrafts} onChange={setItemDrafts} />
          <div className="creator-actions">
            <button className="button" type="submit" disabled={adding}>
              {adding ? "Adding..." : "Add"}
            </button>
          </div>
        </form>
      </section>
    </section>
  );
}
