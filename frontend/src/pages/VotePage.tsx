import { useCallback, useEffect, useState } from "react";
import {
  addOnePieceCharacters,
  fetchRandomMatchup,
  submitVote,
  type CharacterSummary,
  type Difficulty,
  type Matchup,
} from "../api";
import { BoardTabs } from "../components/BoardTabs";
import { CharacterAvatar } from "../components/CharacterAvatar";
import { ItemEditor, emptyItem, toItemInputs, type ItemDraft } from "../components/ItemEditor";

const DIFFICULTIES: Difficulty[] = ["LOW", "MID", "HIGH"];

const DIFFICULTY_LABELS: Record<Difficulty, string> = {
  LOW: "Low",
  MID: "Mid",
  HIGH: "High",
};

function Fighter({ character }: { character: CharacterSummary }) {
  return (
    <div className="fighter">
      <CharacterAvatar character={character} size="lg" />
      <h2>{character.name}</h2>
    </div>
  );
}

export function VotePage() {
  const [matchup, setMatchup] = useState<Matchup | null>(null);
  const [loading, setLoading] = useState(true);
  const [itemDrafts, setItemDrafts] = useState<ItemDraft[]>([emptyItem()]);
  const [addingCharacters, setAddingCharacters] = useState(false);
  const [submitting, setSubmitting] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const loadMatchup = useCallback(async () => {
    setLoading(true);
    setError(null);
    setNotice(null);

    try {
      setMatchup(await fetchRandomMatchup());
    } catch (loadError) {
      setMatchup(null);
      setError(loadError instanceof Error ? loadError.message : "Could not load a matchup");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadMatchup();
  }, [loadMatchup]);

  async function handleAddCharacters(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const items = toItemInputs(itemDrafts);
    if (items.length === 0) {
      setError("Add at least one character.");
      return;
    }

    setAddingCharacters(true);
    setError(null);
    setNotice(null);
    try {
      const created = await addOnePieceCharacters(items);
      setItemDrafts([emptyItem()]);
      if (created.length === 0) {
        setNotice("Those characters already exist.");
        return;
      }
      await loadMatchup();
      setNotice(`Added ${created.length} character${created.length === 1 ? "" : "s"}.`);
    } catch (addError) {
      setError(addError instanceof Error ? addError.message : "Could not add characters");
    } finally {
      setAddingCharacters(false);
    }
  }

  async function handleVote(characterId: string, difficulty: Difficulty) {
    if (!matchup) {
      return;
    }

    const key = `${characterId}-${difficulty}`;
    setSubmitting(key);
    setError(null);
    setNotice(null);

    try {
      const vote = await submitVote(matchup.id, characterId, difficulty);
      await loadMatchup();
      setNotice(`Voted ${vote.winner.name}.`);
    } catch (voteError) {
      setError(voteError instanceof Error ? voteError.message : "Could not save your vote");
    } finally {
      setSubmitting(null);
    }
  }

  return (
    <section className="vote-page">
      <header className="page-header">
        <h1>Default Scale</h1>
      </header>

      <BoardTabs rankingsTo="/default/rankings" voteTo="/default" />

      {error && (
        <div className="alert">
          <span>{error}</span>
        </div>
      )}

      {notice && <div className="notice">{notice}</div>}

      {loading && <div className="card placeholder">Loading...</div>}

      {!loading && matchup && (
        <>
          <div className="arena">
            <Fighter character={matchup.characterA} />
            <div className="versus">VS</div>
            <Fighter character={matchup.characterB} />
          </div>

          <div className="vote-grid">
            {[matchup.characterA, matchup.characterB].map((character) => (
              <div className="vote-column" key={character.id}>
                <h2>{character.name}</h2>
                {DIFFICULTIES.map((difficulty) => {
                  const key = `${character.id}-${difficulty}`;

                  return (
                    <button
                      key={difficulty}
                      className="vote-option"
                      onClick={() => void handleVote(character.id, difficulty)}
                      disabled={submitting !== null}
                    >
                      <span className="vote-label">
                        {DIFFICULTY_LABELS[difficulty]}
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

      <section className="card creator-panel">
        <h2>Add characters</h2>
        <form onSubmit={handleAddCharacters}>
          <ItemEditor items={itemDrafts} onChange={setItemDrafts} />
          <div className="creator-actions">
            <button className="button" type="submit" disabled={addingCharacters}>
              {addingCharacters ? "Adding..." : "Add"}
            </button>
          </div>
        </form>
      </section>
    </section>
  );
}
