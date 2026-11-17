#!/usr/bin/env sh
set -e

if ! docker compose exec -T postgres psql -U powerscale -d postgres -tAc \
    "SELECT 1 FROM pg_database WHERE datname='powerscale_ranking'" | grep -q 1; then
  docker compose exec -T postgres psql -U powerscale -d postgres \
    -c "CREATE DATABASE powerscale_ranking OWNER powerscale"
fi
