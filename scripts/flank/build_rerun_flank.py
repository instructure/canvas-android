#!/usr/bin/env python3
"""
Generate a Flank yaml config scoped to a specific list of failed test methods.

Reads the original flank.yml plus a rerun-tests file (one "class#method" per
line, as produced by detect_rerun.py) and writes a new yaml to stdout that:
  - keeps everything from the original (devices, orchestrator, timeouts,
    env vars, num-flaky-test-attempts, etc.)
  - replaces test-targets with explicit per-method entries
  - reduces testShards so we don't over-shard a tiny rerun

Usage: build_rerun_flank.py <original_flank.yml> <rerun-tests.txt>

If rerun-tests.txt contains the "__MISSING_SHARDS__" sentinel, exits 3
(caller should fall back to a full-suite rerun instead).
"""
import sys
import yaml


def main(original_path: str, rerun_tests_path: str) -> int:
    with open(original_path) as f:
        cfg = yaml.safe_load(f)

    with open(rerun_tests_path) as f:
        lines = [ln.strip() for ln in f if ln.strip()]

    if '__MISSING_SHARDS__' in lines:
        print("ERROR: missing-shards sentinel present; scoped rerun not "
              "possible", file=sys.stderr)
        return 3

    methods = [ln for ln in lines if '#' in ln]
    if not methods:
        print("ERROR: no failed tests in rerun list", file=sys.stderr)
        return 1

    # Flank's `test-targets` accepts AndroidJUnitRunner-style filters.
    # "class <fqcn>#<method>" runs exactly that method.
    new_targets = [f"class {m}" for m in methods]
    cfg.setdefault('gcloud', {})['test-targets'] = new_targets

    # Right-size shards. Over-sharding a small rerun just adds device-allocation
    # overhead per shard; min(original, len) keeps the same parallelism ceiling.
    flank_block = cfg.setdefault('flank', {})
    original_shards = int(flank_block.get('testShards', 1))
    flank_block['testShards'] = max(1, min(original_shards, len(methods)))
    flank_block['testRuns'] = 1

    yaml.dump(cfg, sys.stdout, sort_keys=False, default_flow_style=False)
    return 0


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: build_rerun_flank.py <original_flank.yml> "
              "<rerun-tests.txt>", file=sys.stderr)
        sys.exit(2)
    sys.exit(main(sys.argv[1], sys.argv[2]))
