# Milestone 3 — Real-World Diagnostic Validation Report

**Scope**: this is me, operating the published tool, against two scenarios I designed and
executed myself. **Not independent customer validation.** Nothing here modifies Axiom's source,
classification rules, or confidence thresholds, and nothing here touches the published `v1.15.0`
release or the private `Nandini1212/axiom` repository.

## Baseline (both scenarios pass before any defect)

| Scenario | Run | Result |
|---|---|---|
| Java | [43997872666](https://github.com/Nandini1212/axiom-diagnostic-validation/actions/runs/43997872666) | `BUILD SUCCESSFUL`, 3/3 tests pass |
| Python | [43997872665](https://github.com/Nandini1212/axiom-diagnostic-validation/actions/runs/43997872665) | `3 passed` |

Both are real CI runs of the real test suites, at commit `2b823f7` — before either defect commit
existed.

## Defect commits (separate, isolated, one file each)

- Java regression: [`23c8a11`](https://github.com/Nandini1212/axiom-diagnostic-validation/commit/23c8a11de0cd7db245184a7033b711028117da2f) — lowers `PriceCalculator`'s validation bound from 100 to 10, changing *only* `PriceCalculator.java`.
- Python defect: [`c744308`](https://github.com/Nandini1212/axiom-diagnostic-validation/commit/c7443084bd112f8ee221cbac72efd8aaae93df87) — adds a new test with a wrong expectation, changing *only* `test_string_utils.py`.

## Results

### Scenario 1 — Java application-code regression

- Run: [43998081811](https://github.com/Nandini1212/axiom-diagnostic-validation/actions/runs/43998081811), commit `23c8a11`.
- Real test failure: `appliesTwentyPercentDiscountCorrectly()` — `java.lang.IllegalArgumentException: percentOff must be between 0 and 100`.
- **Ground truth**: `APPLICATION_BUG` (test is correct and untouched; production code regressed).

**Axiom's output** (verified via two independent paths: the published Action's own install+run,
confirmed `outcome=success`; and a direct re-run of that exact installed, checksum-verified
binary against the exact same report — see "Evidence verification" below for why a second path
was needed):

| Field | Value |
|---|---|
| Disposition | `NEEDS_INVESTIGATION` |
| Top-ranked hypothesis | `APPLICATION_BUG` (PRIMARY) |
| Confidence | **0.40** (threshold for `DETERMINED` is 0.70) |
| Supporting evidence | "Changed production file matches stack frame" — citing the real `SOURCE_CHANGE` evidence (`github-source-change-23c8a11...`) together with the real stack frame |
| Contradicting evidence | none |
| Next action | "Inspect `PriceCalculator.applyDiscount` (`PriceCalculator.java:7`)" — the exact regressed line |

**Evaluation**: top-ranked category is correct. The recommended location is exactly right, down
to the line. No unsupported claims — every cited evidence item genuinely supports what's said
about it. **Is `NEEDS_INVESTIGATION` justified here?** Partially. The confidence-weight math is
transparent and correctly applied (one real, correctly-matched signal, worth +0.40, well short of
0.70) — abstaining is not a bug, it's the designed, documented behavior of a single
`STACK_FRAME_MATCHES_CHANGED_FILE` match alone. But as a *usefulness* assessment: a developer
reading this gets the right answer and the right line, just without the word "determined" next
to it. Whether 0.70 is calibrated too conservatively for this case is a legitimate open question,
not a defect — no threshold was changed to test this.

### Scenario 2 — Python test-automation defect

- Run: [43998130224](https://github.com/Nandini1212/axiom-diagnostic-validation/actions/runs/43998130224), commit `c744308`.
- Real test failure: `test_is_palindrome_detects_hello` — `AssertionError: assert False is True`.
- **Ground truth**: `TEST_AUTOMATION_BUG` (test is wrong; production code is correct and untouched).

**Axiom's output**:

| Field | Value |
|---|---|
| Disposition | `NEEDS_INVESTIGATION` |
| Top-ranked hypothesis | `TEST_AUTOMATION_BUG` (PRIMARY) |
| Confidence | **0.25** |
| Supporting evidence | "Top stack frame belongs to test code" only |
| Contradicting evidence | none |
| Next action | "Inspect `test_string_utils.test_is_palindrome_detects_hello` (`test_string_utils.py:17`)" |

**Evaluation**: top-ranked category is correct, and the recommended location is exactly right.
But this is the one genuinely surprising result of the two scenarios, worth flagging precisely:

**Finding — `FAILING_TEST_FILE_CHANGED` (worth +0.55) never fired, even though the failing
test's own file genuinely was the one file changed in the investigated commit.** Confirmed via
the raw JSON output (`--format json`): `github-source-change-c744308...` evidence was real and
collected (`evidenceCoverage.complete: true`, both collectors completed), but the only
contribution to the `test-automation-bug-v1` hypothesis is the +0.25 "top stack frame is test
code" signal — nothing from the file-changed signal. Compare directly to Scenario 1, where the
*structurally equivalent* signal for `application-bug-v1` (`STACK_FRAME_MATCHES_CHANGED_FILE`)
**did** fire correctly from the same kind of `SOURCE_CHANGE` evidence. This asymmetry — the
production-code-side signal correctly uses real source-change evidence, the test-file-side signal
apparently doesn't, at least for this Python case — is the most concrete, reproducible finding in
this validation. It's reported here, not fixed, per this milestone's own scope.

One consequence: with that signal missing, 0.25 is a materially weaker confidence than the
evidence on hand actually supports — not an *unsupported* claim (nothing asserted is false), but
a case where real, available evidence was not used, understating how strong the real signal here
actually is.

## Evidence verification (requirement 5)

`$GITHUB_STEP_SUMMARY` is reassigned to a fresh file per step (confirmed in the prior smoke-test
milestone), so it cannot be read back from a later step — both runs' "Print the job summary"
steps are present but produce no output, as expected, and are not relied on. Instead, each run's
"Re-run the same installed binary" step re-invokes the exact binary `axiom-action` already
installed and checksum-verified, with the exact same `--repo`/`--commit` arguments
`action.yml` itself adds automatically, against the exact same report — not a simulation, the
real release binary, real investigation, printed where `gh run view --log` can retrieve it.

`github-evidence-collector` completed in **all four** runs (`evidenceCoverage.completedCollectorIds`
includes it in every case, confirmed via the raw JSON), including the two baselines — resolving
the open question left by the earlier smoke-test milestone, where the same collector appeared not
to contribute anything. The difference: these runs use `fetch-depth: 0` and a real `push` event
with a real parent commit to diff against, rather than `workflow_dispatch` with no comparison
target. `fetch-depth: 0` alone was **not** assumed sufficient — completion was verified directly
in the evidence-coverage JSON for every run, not inferred from the checkout configuration.

## Pass/fail criteria and verdict

| Criterion | Scenario 1 (Java) | Scenario 2 (Python) |
|---|---|---|
| Correct top-ranked category | ✅ Pass | ✅ Pass |
| Correct, specific next action | ✅ Pass | ✅ Pass |
| No unsupported claims | ✅ Pass | ✅ Pass |
| Source-change evidence actually collected | ✅ Pass (verified in JSON) | ✅ Pass (verified in JSON) |
| Source-change evidence actually *used* by the matching rule | ✅ Pass | ❌ **Fail** — `FAILING_TEST_FILE_CHANGED` did not fire |
| `NEEDS_INVESTIGATION` justified by the evidence actually collected | Reasonable (one weak-but-correct signal) | Questionable (one real, stronger signal appears uncounted) |

**Overall**: both scenarios reached the *correct* leading hypothesis with zero unsupported
claims — a real, meaningful positive result. Neither reached `DETERMINED`, and in neither case is
that disposition wrong on its own terms. The one concrete defect-shaped finding is Scenario 2's
missing `FAILING_TEST_FILE_CHANGED` contribution — a specific, reproducible, Python-side gap
worth investigating as its own follow-up, not fixed as part of this milestone.
