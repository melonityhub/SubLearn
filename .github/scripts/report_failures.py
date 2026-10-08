#!/usr/bin/env python3
"""Turns Gradle, compiler, lint and JUnit failures into GitHub error annotations.

Raw job logs are not always downloadable, but check-run annotations are, so a failed step writes its
key findings there. GitHub keeps at most 10 error annotations per step, so this script emits at most
five annotations, each holding a block of lines (see docs/PROGRESS.md, "How CI results are read").
"""
import glob
import sys
import xml.etree.ElementTree as ET

MAX_CHARS = 7000
WORKSPACE = "/home/runner/work/SubLearn/SubLearn/"


def escape(text: str) -> str:
    return text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def emit(title: str, lines: list[str]) -> None:
    if not lines:
        return
    message = "\n".join(lines)
    if len(message) > MAX_CHARS:
        message = message[:MAX_CHARS] + "\n… (truncated)"
    print(f"::error title={title}::{escape(message)}")


def lint_lines() -> list[str]:
    out: list[str] = []
    for path in sorted(glob.glob("**/build/reports/lint-results-*.xml", recursive=True)):
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for issue in root.iter("issue"):
            if issue.get("severity") not in ("Error", "Fatal"):
                continue
            location = issue.find("location")
            file_name = ((location.get("file") if location is not None else "") or "").replace(WORKSPACE, "")
            line = location.get("line") if location is not None else "?"
            out.append(f"{issue.get('id')}: {issue.get('message')} [{file_name}:{line}]")
    return out


def junit_lines() -> list[str]:
    out: list[str] = []
    for path in sorted(glob.glob("**/build/test-results/**/*.xml", recursive=True)):
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for case in root.iter("testcase"):
            for failure in list(case.findall("failure")) + list(case.findall("error")):
                name = f"{case.get('classname')}.{case.get('name')}"
                message = (failure.get("message") or "")[:300]
                out.append(f"TEST {name}: {message}")
    return out


def gradle_lines(path: str) -> tuple[list[str], list[str], list[str], list[str]]:
    try:
        lines = open(path, encoding="utf-8", errors="replace").read().splitlines()
    except OSError:
        return [], [], [], [f"missing log: {path}"]
    summary = [l for l in lines if "tests completed" in l or l.startswith("BUILD ") or " FAILED" in l]
    compile_errors = [l for l in lines if l.startswith("e: ") or l.startswith("w: file") and "error" in l.lower()]
    went_wrong: list[str] = []
    for index, line in enumerate(lines):
        if "What went wrong" in line:
            went_wrong.extend(l for l in lines[index + 1:index + 12] if l.strip())
    tail = [l for l in lines[-25:] if l.strip()]
    return summary, compile_errors, went_wrong, tail


def main() -> int:
    log = sys.argv[1] if len(sys.argv) > 1 else None
    summary, compile_errors, went_wrong, tail = gradle_lines(log) if log else ([], [], [], [])
    lint = lint_lines()
    tests = junit_lines()
    emit("Lint errors", lint)
    emit("Compile errors", compile_errors)
    emit("Test failures", tests)
    emit("Build summary and what went wrong", summary + went_wrong)
    if not (lint or compile_errors or tests or went_wrong):
        emit("Log tail", tail)
    print(f"reported {len(lint)} lint error(s), {len(compile_errors)} compile error(s), {len(tests)} failing test(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
