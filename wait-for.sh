#!/bin/sh
# wait-for.sh: wait for host:port pairs to become available, then run the command after --
# Usage: wait-for.sh host1 port1 [host2 port2 ...] -- command args...

set -e

if [ "$#" -lt 3 ]; then
  echo "Usage: $0 host1 port1 [host2 port2 ...] -- command args..."
  exit 1
fi

# collect host/port pairs until --
hosts=()
ports=()
while [ $# -gt 0 ]; do
  case "$1" in
    --)
      shift
      break
      ;;
    *)
      hosts+=("$1")
      shift
      if [ $# -eq 0 ]; then
        echo "Missing port for host ${hosts[-1]}"
        exit 1
      fi
      ports+=("$1")
      shift
      ;;
  esac
done

# remaining args are command
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

# Loop through hosts
i=0
while [ $i -lt ${#hosts[@]} ]; do
  wait_for "${hosts[$i]}" "${ports[$i]}" &
  i=$((i+1))
done

# wait for all background checkers
wait

# Execute the command
exec $CMD

