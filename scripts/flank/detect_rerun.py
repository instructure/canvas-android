#!/usr/bin/env python3
"""
Walk a Flank results directory and emit the set of test methods that need rerun.

A test needs rerun when:
  - its JUnit testcase has <failure> or <error> in the initial run; OR
  - its containing shard has no parseable JUnit XML at all (FTL accepted the
    shard but it never reported results — the "shard started, then nothing"
    timeout case).

Output (stdout):
  - For each failed test: a line "fully.qualified.ClassName#methodName".
  - If any shard reported no results at all, an additional sentinel line
    "__MISSING_SHARDS__" so the caller can decide between a scoped rerun
    (only failed methods) and a full-suite rerun (missing-shard fallback,
    since we can't recover which tests were in a vanished shard without
    a pre-execution dump).

Exit code is always 0 on a well-formed results dir; the caller checks the
output. Errors reading the dir go to stderr and exit 2.
"""
import sys

# Python's stdlib XML parsers (Expat) are only hardened against XXE and
# "billion laughs"/"quadratic blowup" DoS attacks on Python 3.11+ (Expat 2.6+).
# Refuse to run on older interpreters where xml.etree.ElementTree is unsafe.
if sys.version_info < (3, 11):
    sys.exit("Python 3.11 or newer is required (secure XML parsing).")

import xml.etree.ElementTree as ET
from pathlib import Path


def latest_run_dir(results_root: Path):
    """Return the most recent run subdirectory, or None if there isn't one."""
    subs = [p for p in results_root.iterdir() if p.is_dir()]
    if not subs:
        return None
    return max(subs, key=lambda p: p.stat().st_mtime)


def iter_shard_dirs(run_dir: Path):
    """Flank lays out shard results as subdirectories of the run dir.
    Their naming has varied across versions ("shard_0", "matrix-0", device-name
    based dirs). We treat every immediate subdirectory that contains at least
    one .xml as a shard, and every immediate subdirectory with no .xml as a
    missing shard."""
    for child in sorted(run_dir.iterdir()):
        if not child.is_dir():
            continue
        yield child


def parse_testcases(xml_path: Path):
    """Yield (classname, name, status) for each <testcase>.
    status is one of 'pass', 'fail' (failure or error).
    Skipped tests are ignored — they're neither failures nor candidates for rerun."""
    try:
        tree = ET.parse(xml_path)
    except (ET.ParseError, OSError):
        return
    root = tree.getroot()
    suites = root.findall('.//testsuite') or ([root] if root.tag == 'testsuite' else [])
    for suite in suites:
        for tc in suite.findall('testcase'):
            classname = tc.get('classname', '').strip()
            name = tc.get('name', '').strip()
            if not classname or not name:
                continue
            if tc.find('skipped') is not None:
                continue
            failed = tc.find('failure') is not None or tc.find('error') is not None
            yield classname, name, ('fail' if failed else 'pass')


def main(results_root_str: str) -> int:
    results_root = Path(results_root_str)
    if not results_root.exists():
        # Flank produced no output at all (e.g. crashed before any matrix started).
        # Signal full-rerun fallback rather than dying with a non-actionable error.
        print(f"WARN: results dir does not exist: {results_root}", file=sys.stderr)
        print("__MISSING_SHARDS__")
        return 0

    run_dir = latest_run_dir(results_root)
    if run_dir is None:
        print(f"WARN: no run subdirectories under {results_root}", file=sys.stderr)
        print("__MISSING_SHARDS__")
        return 0

    failed = set()
    passed = set()
    missing_shards = []

    for shard in iter_shard_dirs(run_dir):
        xmls = list(shard.rglob('*.xml'))
        # Only consider JUnit-shaped result files. Flank writes
        # "test_result_*.xml" per device/shard and sometimes other XML
        # (e.g. logcat reports). We don't try to distinguish here; if a
        # file isn't valid JUnit, parse_testcases yields nothing.
        if not xmls:
            missing_shards.append(shard.name)
            continue

        shard_had_any_testcase = False
        for x in xmls:
            for classname, name, status in parse_testcases(x):
                shard_had_any_testcase = True
                key = f"{classname}#{name}"
                if status == 'fail':
                    failed.add(key)
                else:
                    passed.add(key)

        if not shard_had_any_testcase:
            missing_shards.append(shard.name)

    # A test that passed in any retry and failed in another counts as passed
    # (we already paid for the retry; no need to run it again).
    final_failed = sorted(failed - passed)

    if missing_shards:
        print(f"WARN: {len(missing_shards)} shard(s) without results: "
              f"{missing_shards}", file=sys.stderr)
        print("__MISSING_SHARDS__")

    for t in final_failed:
        print(t)

    return 0


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Usage: detect_rerun.py <results_dir>", file=sys.stderr)
        sys.exit(2)
    sys.exit(main(sys.argv[1]))
