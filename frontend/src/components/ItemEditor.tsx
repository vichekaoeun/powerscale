import { useState } from "react";
import { resolveAssetUrl, uploadImage, type ScaleItemInput } from "../api";

export interface ItemDraft {
  name: string;
  imageUrl: string;
}

export function emptyItem(): ItemDraft {
  return { name: "", imageUrl: "" };
}

export function toItemInputs(items: ItemDraft[]): ScaleItemInput[] {
  const seen = new Set<string>();
  const result: ScaleItemInput[] = [];

  for (const item of items) {
    const name = item.name.trim();
    if (!name) {
      continue;
    }
    const key = name.toLowerCase();
    if (seen.has(key)) {
      continue;
    }
    seen.add(key);

    const image = item.imageUrl.trim();
    result.push({
      name,
      imageUrl:
        image && (image.startsWith("/api/uploads/") || /^https?:\/\//i.test(image))
          ? image
          : null,
    });
  }

  return result;
}

interface ItemEditorProps {
  items: ItemDraft[];
  onChange: (items: ItemDraft[]) => void;
}

export function ItemEditor({ items, onChange }: ItemEditorProps) {
  const [uploadingIndex, setUploadingIndex] = useState<number | null>(null);
  const [uploadError, setUploadError] = useState<string | null>(null);

  function update(index: number, name: string) {
    onChange(items.map((item, current) => (
      current === index ? { ...item, name } : item
    )));
  }

  function remove(index: number) {
    if (items.length <= 1) {
      return;
    }
    onChange(items.filter((_, current) => current !== index));
  }

  async function handleFile(index: number, file: File | undefined) {
    if (!file) {
      return;
    }

    setUploadingIndex(index);
    setUploadError(null);
    try {
      const uploaded = await uploadImage(file);
      onChange(items.map((item, current) => (
        current === index ? { ...item, imageUrl: uploaded.url } : item
      )));
    } catch (error) {
      setUploadError(error instanceof Error ? error.message : "Could not upload the image");
    } finally {
      setUploadingIndex(null);
    }
  }

  return (
    <div className="item-editor">
      {items.map((item, index) => (
        <div className="item-row" key={index}>
          <input
            value={item.name}
            onChange={(event) => update(index, event.target.value)}
            placeholder="Name"
            aria-label={`Item ${index + 1} name`}
          />
          <div className="item-image">
            {item.imageUrl ? (
              <img src={resolveAssetUrl(item.imageUrl) ?? undefined} alt="" />
            ) : (
              <span>No image</span>
            )}
            <label className="item-upload">
              {uploadingIndex === index ? "Uploading..." : "Image"}
              <input
                type="file"
                accept="image/png,image/jpeg,image/webp,image/gif"
                onChange={(event) => void handleFile(index, event.target.files?.[0])}
                hidden
              />
            </label>
          </div>
          <button
            className="item-remove"
            type="button"
            onClick={() => remove(index)}
            disabled={items.length <= 1}
          >
            Remove
          </button>
        </div>
      ))}
      {uploadError && <div className="item-error">{uploadError}</div>}
      <button
        className="button button-ghost item-add"
        type="button"
        onClick={() => onChange([...items, emptyItem()])}
      >
        Add item
      </button>
    </div>
  );
}
