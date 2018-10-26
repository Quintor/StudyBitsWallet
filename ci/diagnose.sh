#!/bin/bash

while true; do
    echo "DIAGNOSE.SH"
    ps aux --sort -rss | head -10
    sleep 30
done
