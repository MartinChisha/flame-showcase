#!/usr/bin/env bash
# Compile and test the backend samples with nothing but a JDK (21+).
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
find src -name '*.java' > out/sources.txt
javac --release 21 -Xlint:all -Werror -d out @out/sources.txt
java -cp out showcase.TestRunner
