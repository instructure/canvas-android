#!/usr/bin/env bash
set -ex

# Open source users: Run this script before building.
rm private-data
cp -R open_source_data private-data
