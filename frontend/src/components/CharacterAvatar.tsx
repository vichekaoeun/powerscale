import { useEffect, useState } from "react";
import { resolveAssetUrl } from "../api";

interface AvatarSubject {
  name: string;
  imageUrl: string | null;
}

interface CharacterAvatarProps {
  character: AvatarSubject;
  size?: "md" | "lg";
}

function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("");
}

function isImage(url: string | null): url is string {
  return url != null && (url.startsWith("/api/uploads/") || /^https?:\/\//i.test(url));
}

export function CharacterAvatar({ character, size = "md" }: CharacterAvatarProps) {
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    setFailed(false);
  }, [character.imageUrl]);

  if (isImage(character.imageUrl) && !failed) {
    return (
      <img
        className={`avatar avatar-${size}`}
        src={resolveAssetUrl(character.imageUrl) ?? character.imageUrl}
        alt={character.name}
        loading="lazy"
        onError={() => setFailed(true)}
      />
    );
  }

  return (
    <div className={`avatar avatar-${size} avatar-fallback`} aria-hidden="true">
      {initials(character.name) || "?"}
    </div>
  );
}
