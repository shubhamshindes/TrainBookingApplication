#!/bin/sh
# wait-for.sh: wait for host:port pairs to become available, then run the command after --
# Usage: wait-for.sh host1 port1 [host2 port2 ...] -- command args...

set -e

if [ "$#" -lt 3 ]; then
  echo "Usage: $0 host1 port1 [host2 port2 ...] -- command args..."
  exit 1
fi

# collect host/port pairs until --
hosts=""
ports=""

# We parse parameters carefully
while [ $# -gt 0 ]; do
  case "$1" in
    --)
      shift
      break
      ;;
    *)
      if [ -z "$hosts" ]; then
        hosts="$1"
      else
        hosts="$hosts $1"
      fi
      shift
      if [ $# -eq 0 ]; then
        echo "Missing port for host"
        exit 1
      fi
      if [ -z "$ports" ]; then
        ports="$1"
      else
        ports="$ports $1"
      fi
      shift
      ;;
  esac
done

if [ $# -eq 0 ]; then
  echo "No command specified after --"
  exit 1
fi

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

# Wait sequentially for each host-port pair
for i in $(seq 1 $(echo "$hosts" | wc -w)); do
  host=$(echo "$hosts" | cut -d' ' -f$i)
  port=$(echo "$ports" | cut -d' ' -f$i)
  wait_for "$host" "$port"
done

# Execute the command
exec $CMD
