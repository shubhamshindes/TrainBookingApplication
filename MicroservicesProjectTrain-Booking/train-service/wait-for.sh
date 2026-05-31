#!/bin/sh
set -e
hosts=""
ports=""
while [ $# -gt 0 ]; do
  case "$1" in
    --)
      shift
      break
      ;;
    *)
      if [ -z "$hosts" ]; then hosts="$1"; else hosts="$hosts $1"; fi
      shift
      if [ -z "$ports" ]; then ports="$1"; else ports="$ports $1"; fi
      shift
      ;;
  esac
done
CMD="$@"
wait_for() {
  host="$1"
  port="$2"
  echo "Waiting for $host:$port..."
  while ! nc -z "$host" "$port"; do
    sleep 1
  done
  echo "$host:$port is available"
}
for i in $(seq 1 $(echo "$hosts" | wc -w)); do
  host=$(echo "$hosts" | cut -d' ' -f$i)
  port=$(echo "$ports" | cut -d' ' -f$i)
  wait_for "$host" "$port"
done
exec $CMD
