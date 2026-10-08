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
    markers = ("What went wrong", "e: ", "w: ", "> Task", "FAILED", "Exception", "Caused by", "error:", "ERROR")
    important = [line for line in lines if any(marker in line for marker in markers)]
    if important:
        emit("Gradle errors", "\n".join(important[-80:]))
    emit("Gradle log tail", "\n".join(lines[-60:]))


def main() -> int:
    junit_count = report_junit()
    if len(sys.argv) > 1:
        report_gradle(sys.argv[1])
    print(f"reported {junit_count} failing test case(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
