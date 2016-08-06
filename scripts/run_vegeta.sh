#!/usr/bin/env bash
echo "GET http://localhost:8082/" | ./vegeta attack -header Accept-Encoding:gzip, deflate -duration=5s | tee results.bin | ./vegeta report