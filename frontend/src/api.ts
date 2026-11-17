export type Difficulty = "LOW" | "MID" | "HIGH";
export type CharacterSource = "JIKAN" | "ANILIST" | "CUSTOM";

export interface CharacterSummary {
  id: string;
  source: CharacterSource;
  externalId: number;
  malId: number | null;
  name: string;
  imageUrl: string | null;
}

export interface Matchup {
  id: string;
  characterA: CharacterSummary;
  characterB: CharacterSummary;
  createdAt: string;
}

export interface Vote {
  id: string;
  matchupId: string;
  winner: CharacterSummary;
  loser: CharacterSummary;
  difficulty: Difficulty;
  createdAt: string;
  updatedAt: string;
}

export interface OutcomeStat {
  characterId: string;
  difficulty: Difficulty;
  votes: number;
  percentage: number;
}

export interface MatchupStats {
  matchupId: string;
  characterA: CharacterSummary;
  characterB: CharacterSummary;
  totalVotes: number;
  outcomes: OutcomeStat[];
}

export interface LeaderboardEntry extends CharacterSummary {
  rank: number;
  rating: number;
  wins: number;
  losses: number;
  votes: number;
  winRate: number;
}

export interface Leaderboard {
  entries: LeaderboardEntry[];
  totalCharacters: number;
  totalVotes: number;
}

export interface ScaleItem {
  id: string;
  name: string;
  imageUrl: string | null;
}

export interface ScaleItemInput {
  name: string;
  imageUrl: string | null;
}

export type RankingMethod = "ELO" | "POINTS";

export interface RankingFormula {
  method: RankingMethod;
  baseRating: number;
  kFactor: number;
  lowWeight: number;
  midWeight: number;
  highWeight: number;
  lowPoints: number;
  midPoints: number;
  highPoints: number;
}

export interface Scale {
  id: string;
  slug: string;
  title: string;
  criterion: string;
  description: string | null;
  formula: RankingFormula;
  selections: string[];
  itemCount: number;
  items: ScaleItem[];
  createdAt: string;
}

export interface ScaleSummary {
  id: string;
  slug: string;
  title: string;
  criterion: string;
  rankingMethod: RankingMethod;
  description: string | null;
  itemCount: number;
  voteCount: number;
  createdAt: string;
}

export interface ScaleDirectory {
  content: ScaleSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreatedScale {
  scale: Scale;
  creatorKey: string;
}

export interface ScaleMatchup {
  id: string;
  itemA: ScaleItem;
  itemB: ScaleItem;
  createdAt: string;
}

export interface ScaleOutcomeStat {
  itemId: string;
  difficulty: Difficulty;
  votes: number;
  percentage: number;
}

export interface ScaleMatchupStats {
  matchupId: string;
  itemA: ScaleItem;
  itemB: ScaleItem;
  totalVotes: number;
  outcomes: ScaleOutcomeStat[];
}

export interface ScaleVote {
  id: string;
  matchupId: string;
  winner: ScaleItem;
  loser: ScaleItem;
  difficulty: Difficulty;
  createdAt: string;
  updatedAt: string;
}

export interface ScaleLeaderboardEntry {
  rank: number;
  id: string;
  name: string;
  imageUrl: string | null;
  rating: number;
  wins: number;
  losses: number;
  votes: number;
  winRate: number;
}

export interface ScaleLeaderboard {
  entries: ScaleLeaderboardEntry[];
  totalItems: number;
  totalVotes: number;
}

const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:8080").replace(/\/$/, "");
const USER_ID_KEY = "powerscale.userId";
const SCALE_KEYS_KEY = "powerscale.scaleKeys";

export function getUserId(): string {
  const existing = window.localStorage.getItem(USER_ID_KEY);
  if (existing) {
    return existing;
  }

  const userId = crypto.randomUUID();
  window.localStorage.setItem(USER_ID_KEY, userId);
  return userId;
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("X-User-Id", getUserId());
  if (typeof init.body === "string") {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_URL}${path}`, { ...init, headers });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const problem = (await response.json()) as { detail?: string; title?: string };
      message = problem.detail ?? problem.title ?? message;
    } catch {
      // Keep the fallback message when the body is not JSON.
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function fetchRandomMatchup(): Promise<Matchup> {
  return request<Matchup>("/api/matchups/random");
}

export function fetchMatchupStats(matchupId: string): Promise<MatchupStats> {
  return request<MatchupStats>(`/api/matchups/${matchupId}/stats`);
}

export function submitVote(
  matchupId: string,
  winnerId: string,
  difficulty: Difficulty,
): Promise<Vote> {
  return request<Vote>("/api/votes", {
    method: "POST",
    body: JSON.stringify({ matchupId, winnerId, difficulty }),
  });
}

export function fetchLeaderboard(limit = 100): Promise<Leaderboard> {
  return request<Leaderboard>(`/api/leaderboard?limit=${limit}`);
}

export function resolveAssetUrl(url: string | null): string | null {
  if (!url) {
    return null;
  }
  if (/^https?:\/\//i.test(url) || !url.startsWith("/")) {
    return url;
  }
  return `${API_URL}${url}`;
}

export function uploadImage(file: File): Promise<{ url: string }> {
  const body = new FormData();
  body.append("file", file);
  return request<{ url: string }>("/api/uploads/images", { method: "POST", body });
}

export function addOnePieceCharacters(items: ScaleItemInput[]): Promise<CharacterSummary[]> {
  return request<CharacterSummary[]>("/api/characters/manual", {
    method: "POST",
    body: JSON.stringify({ characters: items }),
  });
}

export async function fetchCharacterCount(): Promise<number> {
  const page = await request<{ totalElements: number }>("/api/characters?size=1");
  return page.totalElements;
}

export function fetchScales(search: string, page = 0, size = 12): Promise<ScaleDirectory> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (search.trim()) {
    params.set("search", search.trim());
  }
  return request<ScaleDirectory>(`/api/scales?${params.toString()}`);
}

export function saveScaleKey(slug: string, key: string): void {
  const keys = readScaleKeys();
  keys[slug] = key;
  window.localStorage.setItem(SCALE_KEYS_KEY, JSON.stringify(keys));
}

export function getScaleKey(slug: string): string | null {
  return readScaleKeys()[slug] ?? null;
}

function readScaleKeys(): Record<string, string> {
  try {
    const raw = window.localStorage.getItem(SCALE_KEYS_KEY);
    return raw ? (JSON.parse(raw) as Record<string, string>) : {};
  } catch {
    return {};
  }
}

export function createScale(input: {
  title: string;
  criterion: string;
  description: string;
  items: ScaleItemInput[];
  formula: RankingFormula;
  selections: string[];
}): Promise<CreatedScale> {
  return request<CreatedScale>("/api/scales", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function fetchScale(slug: string): Promise<Scale> {
  return request<Scale>(`/api/scales/${encodeURIComponent(slug)}`);
}

export function addScaleItems(slug: string, items: ScaleItemInput[]): Promise<Scale> {
  return request<Scale>(`/api/scales/${encodeURIComponent(slug)}/items`, {
    method: "POST",
    body: JSON.stringify({ items }),
  });
}

export function fetchScaleMatchup(slug: string): Promise<ScaleMatchup> {
  return request<ScaleMatchup>(`/api/scales/${encodeURIComponent(slug)}/matchups/random`);
}

export function fetchScaleMatchupStats(slug: string, matchupId: string): Promise<ScaleMatchupStats> {
  return request<ScaleMatchupStats>(
    `/api/scales/${encodeURIComponent(slug)}/matchups/${matchupId}/stats`,
  );
}

export function submitScaleVote(
  slug: string,
  matchupId: string,
  winnerId: string,
  difficulty: Difficulty,
): Promise<ScaleVote> {
  return request<ScaleVote>(`/api/scales/${encodeURIComponent(slug)}/votes`, {
    method: "POST",
    body: JSON.stringify({ matchupId, winnerId, difficulty }),
  });
}

export function fetchScaleLeaderboard(scaleId: string, limit = 100): Promise<ScaleLeaderboard> {
  return request<ScaleLeaderboard>(`/api/scale-leaderboard/${scaleId}?limit=${limit}`);
}
