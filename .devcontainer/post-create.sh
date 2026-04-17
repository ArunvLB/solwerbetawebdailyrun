#!/usr/bin/env bash
set -euo pipefail

sudo apt-get update
sudo apt-get install -y chromium

mvn -q -DskipTests dependency:go-offline
