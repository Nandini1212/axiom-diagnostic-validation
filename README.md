# Axiom Diagnostic Validation (Milestone 3)

Real-world diagnostic validation of the published [`Nandini1212/axiom-action@v1.15.0`](https://github.com/Nandini1212/axiom-action)
against two independent, ground-truth-known CI failure scenarios. This repository contains no
confidential information and no Axiom source code.

- **`java-app-regression/`** — a correct, passing test; a later commit regresses the
  *production* code it exercises. Ground truth: `APPLICATION_BUG`.
- **`python-test-defect/`** — correct, unchanged production code; a later commit adds a *test*
  with a wrong expectation. Ground truth: `TEST_AUTOMATION_BUG`.

Each scenario's workflow (`.github/workflows/`) runs the real test suite on every push to that
scenario's own directory, then investigates the result with the published Action — no custom
`--rules`, no fabricated evidence, no manual `--changes` (relying on the Action's own automatic
`--repo`/`--commit` derivation).

See the validation report (added once both scenarios have real run results) for the full
comparison against ground truth.
