#!/bin/sh

JSC_GMAIL_STATS_PATH="/app/jsc-gmail-stats"

cd "${JSC_GMAIL_STATS_PATH}" || return

echo "${JSC_CREDENTIALS}" > "${JSC_GMAIL_STATS_PATH}"/credentials.json

./bin/jsc-gmail-stats