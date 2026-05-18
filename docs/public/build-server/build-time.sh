#!/usr/bin/env bash

set -u

if [ "$#" -lt 3 ]; then
  echo "usage: $0 <metadata-file> <recipe-name> <command...>" >&2
  exit 2
fi

metadata_file=$1
recipe_name=$2
shift 2

start_epoch=$(date +%s)
start_iso=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

echo "$recipe_name started at $start_iso"

"$@"
status=$?

end_epoch=$(date +%s)
end_iso=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
duration=$((end_epoch - start_epoch))

mkdir -p "$(dirname "$metadata_file")"
printf "recipe=%s\nstart_utc=%s\nend_utc=%s\nduration_seconds=%s\nstatus=%s\n" \
  "$recipe_name" "$start_iso" "$end_iso" "$duration" "$status" \
  > "$metadata_file"

echo "$recipe_name finished at $end_iso (duration ${duration}s, exit ${status})"
cat "$metadata_file"

exit "$status"
