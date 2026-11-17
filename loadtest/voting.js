import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<500"],
  },
};

export default function () {
  const matchupResponse = http.get(`${BASE_URL}/api/matchups/random`);
  const matchupOk = check(matchupResponse, {
    "matchup fetched": (response) => response.status === 200,
  });
  if (!matchupOk) {
    sleep(0.2);
    return;
  }

  const matchup = matchupResponse.json();
  const userId = `load-${__VU}-${__ITER}`;
  const payload = JSON.stringify({
    matchupId: matchup.id,
    winnerId: matchup.characterA.id,
    difficulty: ["LOW", "MID", "HIGH"][__ITER % 3],
  });

  const voteResponse = http.post(`${BASE_URL}/api/votes`, payload, {
    headers: {
      "Content-Type": "application/json",
      "X-User-Id": userId,
    },
  });

  check(voteResponse, {
    "vote accepted": (response) => response.status === 200,
  });

  sleep(0.1);
}
