#!/usr/bin/env python3
"""Turns Gradle and JUnit failures into GitHub error annotations.

Raw job logs are not always downloadable, but check-run annotations are. Running this after a
failed step makes the important lines readable through the API (see docs/PROGRESS.md, "How CI
failures are read").
"""
import glob
import sys
import xml.etree.ElementTree as ET


def escape(text: str) -> str:
    return text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def emit(title: str, message: str) -> None:
    print(f"::error title={title}::{escape(message[:6000])}")


def report_junit(limit: int = 30) -> int:
    emitted = 0
    for path in sorted(glob.glob("**/build/test-results/**/*.xml", recursive=True)):
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for case in root.iter("testcase"):
            for failure in list(case.findall("failure")) + list(case.findall("error")):
                emitted += 1
                name = f"{case.get('classname')}.{case.get('name')}"
                body = (failure.get("message") or "")[:800] + "\n" + (failure.text or "")[:2500]
                emit("Test failed", f"{name}\n{body}")
                if emitted >= limit:
                    return emitted
    return emitted


def report_gradle(path: str) -> None:
    try:
        lines = open(path, encoding="utf-8", errors="replace").read().splitlines()
    except OSError:
        emit("Gradle log missing", path)
        return
    summary = [line for line in lines if "tests completed" in line or "BUILD " in line or "FAILED" in line]
    compile_errors = [line for line in lines if line.startswith("e: ")]
    what_went_wrong = []
    for index, line in enumerate(lines):
        if "What went wrong" in line:
            what_went_wrong.extend(lines[index:index + 8])
    chunks = [("Build summary", summary), ("Compile errors", compile_errors), ("What went wrong", what_went_wrong)]
    for title, block in chunks:
        if not block:
            continue
        for start in range(0, len(block), 25):
            emit(title, "\n".join(block[start:start + 25]))
    emit("Gradle log tail", "\n".join(lines[-40:]))


def main() -> int:
    junit_count = report_junit()
    if len(sys.argv) > 1:
        report_gradle(sys.argv[1])
    print(f"reported {junit_count} failing test case(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
