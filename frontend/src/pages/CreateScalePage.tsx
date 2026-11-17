import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createScale, saveScaleKey, type RankingFormula, type RankingMethod } from "../api";
import { ItemEditor, emptyItem, toItemInputs, type ItemDraft } from "../components/ItemEditor";

interface SelectionDraft {
  label: string;
  value: number;
}

export function CreateScalePage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [criterion, setCriterion] = useState("");
  const [description, setDescription] = useState("");
  const [method, setMethod] = useState<RankingMethod>("ELO");
  const [baseRating, setBaseRating] = useState(1500);
  const [kFactor, setKFactor] = useState(32);
  const [selections, setSelections] = useState<SelectionDraft[]>([
    { label: "Winner", value: 1 },
  ]);
  const [itemDrafts, setItemDrafts] = useState<ItemDraft[]>([emptyItem(), emptyItem()]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const items = toItemInputs(itemDrafts);

  function updateSelection(index: number, field: keyof SelectionDraft, value: string | number) {
    setSelections((current) =>
      current.map((selection, position) => (
        position === index ? { ...selection, [field]: value } : selection
      )),
    );
  }

  function addSelection() {
    if (selections.length >= 3) {
      return;
    }
    setSelections((current) => [...current, { label: "", value: 1 }]);
  }

  function removeSelection(index: number) {
    if (selections.length <= 1) {
      return;
    }
    setSelections((current) => current.filter((_, position) => position !== index));
  }

  function buildFormula(): RankingFormula {
    const value = (index: number) => selections[index]?.value ?? 1;
    const base = {
      method,
      baseRating,
      kFactor,
      lowWeight: 1,
      midWeight: 1,
      highWeight: 1,
      lowPoints: 1,
      midPoints: 1,
      highPoints: 1,
    };

    if (method === "POINTS") {
      return {
        ...base,
        lowPoints: value(0),
        midPoints: value(1),
        highPoints: value(2),
      };
    }
    return {
      ...base,
      lowWeight: value(0),
      midWeight: value(1),
      highWeight: value(2),
    };
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!title.trim() || !criterion.trim()) {
      setError("Add a title and criterion.");
      return;
    }
    if (items.length < 2) {
      setError("Add at least two items.");
      return;
    }
    if (selections.some((selection) => !selection.label.trim())) {
      setError("Name every selection.");
      return;
    }

    setSubmitting(true);
    try {
      const created = await createScale({
        title: title.trim(),
        criterion: criterion.trim(),
        description: description.trim(),
        items,
        formula: buildFormula(),
        selections: selections.map((selection) => selection.label.trim()),
      });
      saveScaleKey(created.scale.slug, created.creatorKey);
      navigate(`/s/${created.scale.slug}`);
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Could not create the scale");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="create-page">
      <header className="page-header">
        <h1>Create a scale</h1>
        <Link className="button button-ghost" to="/">
          Cancel
        </Link>
      </header>

      {error && <div className="alert">{error}</div>}

      <form className="card form-card" onSubmit={handleSubmit}>
        <div className="form-grid">
          <label className="field">
            <span>Title</span>
            <input
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              placeholder="Best pizza toppings"
              maxLength={120}
            />
          </label>

          <label className="field">
            <span>Criterion</span>
            <input
              value={criterion}
              onChange={(event) => setCriterion(event.target.value)}
              placeholder="Taste"
              maxLength={120}
            />
          </label>
        </div>

        <label className="field">
          <span>Description</span>
          <input
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Optional"
            maxLength={1000}
          />
        </label>

        <fieldset className="formula-panel">
          <legend>Scoring</legend>

          <label className="field">
            <span>Method</span>
            <select
              value={method}
              onChange={(event) => setMethod(event.target.value as RankingMethod)}
            >
              <option value="ELO">Elo rating</option>
              <option value="POINTS">Points</option>
            </select>
          </label>

          {method === "ELO" && (
            <div className="form-grid">
              <label className="field">
                <span>Base rating</span>
                <input
                  type="number"
                  value={baseRating}
                  min={100}
                  max={5000}
                  onChange={(event) => setBaseRating(Number(event.target.value))}
                />
              </label>
              <label className="field">
                <span>K factor</span>
                <input
                  type="number"
                  value={kFactor}
                  min={1}
                  max={200}
                  onChange={(event) => setKFactor(Number(event.target.value))}
                />
              </label>
            </div>
          )}

          <div className="field">
            <span>Selections</span>
            <div className="item-editor">
              {selections.map((selection, index) => (
                <div className="selection-row" key={index}>
                  <input
                    value={selection.label}
                    onChange={(event) => updateSelection(index, "label", event.target.value)}
                    placeholder="Winner"
                    maxLength={40}
                  />
                  <input
                    type="number"
                    value={selection.value}
                    step="0.05"
                    min={0}
                    onChange={(event) =>
                      updateSelection(index, "value", Number(event.target.value))
                    }
                    placeholder={method === "ELO" ? "Weight" : "Points"}
                  />
                  <button
                    className="item-remove"
                    type="button"
                    onClick={() => removeSelection(index)}
                    disabled={selections.length <= 1}
                  >
                    Remove
                  </button>
                </div>
              ))}
              {selections.length < 3 && (
                <button
                  className="button button-ghost item-add"
                  type="button"
                  onClick={addSelection}
                >
                  Add option
                </button>
              )}
            </div>
          </div>
        </fieldset>

        <div className="field">
          <span>Items</span>
          <ItemEditor items={itemDrafts} onChange={setItemDrafts} />
        </div>

        <div className="form-footer">
          <span className="form-count">
            {items.length} item{items.length === 1 ? "" : "s"}
          </span>
          <button className="button" type="submit" disabled={submitting}>
            {submitting ? "Creating..." : "Create"}
          </button>
        </div>
      </form>
    </section>
  );
}
