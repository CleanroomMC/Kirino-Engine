# JMH regression helper

[jmh-regression.gradle](jmh-regression.gradle) makes performance testing easier:
it runs JMH benchmarks, saves a history and reports changes against earlier runs.

For now it lives in Kirino. It should move into Cleanroom in the future so other
modules can use the same tooling.

Run from the Cleanroom repository root, where the script is already applied:

```powershell
.\gradlew.bat jmhRegression
```

For a quick check of a single benchmark:

```powershell
.\gradlew.bat jmhRegression -Pjmh.quick "-Pjmh.include=EntityFlushRealisticBenchmark"
```

Quick runs have a separate history and are useful for smoke checks. Use full runs
to assess performance changes. Results go to `benchmark-results/`; the latest
report is `reports/full/latest.md` (or `reports/quick/latest.md`).

Use `-Pjmh.tag=<name>` to keep separate histories for different machines or settings.
The default change threshold is 5%, with non-overlapping confidence intervals.
Extra JMH options go through `"-Pjmh.args=-prof gc"`; leave `-rf` and `-rff` to the script.

`jmhRun` only writes the current JMH JSON. `jmhCompare` runs JMH, then saves and
compares the results; `jmhRegression` is the convenience entry point for both.
