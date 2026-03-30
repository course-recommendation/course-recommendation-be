#!/bin/bash

# Configuration
SITE1="api.courserecom.site"
SITE2="ai.courserecom.site"
INTERVAL=240 # seconds
DURATION=7200 # seconds
END_TIME=$((SECONDS + DURATION))

echo "Monitoring $SITE1 and $SITE2."
echo "----------------------------------------------------"

while [ $SECONDS -lt $END_TIME ]; do
    (
        if ping -c 1 "$SITE1" > /dev/null 2>&1; then
            echo "$(date): [SUCCESS] $SITE1"
        else
            echo "$(date): [FAILED]  $SITE1"
        fi
    ) &

    (
        if ping -c 1 "$SITE2" > /dev/null 2>&1; then
            echo "$(date): [SUCCESS] $SITE2"
        else
            echo "$(date): [FAILED]  $SITE2"
        fi
    ) &

    wait

    sleep $INTERVAL
done

echo "----------------------------------------------------"
echo "Done"