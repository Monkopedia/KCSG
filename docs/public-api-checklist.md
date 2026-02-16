# Public API Checklist

This checklist tracks public API coverage status for library modules.

## Update Rule

- Any change to public API in `kcsg` or `kcsg-dsl` must update this file in the same PR.
- Mark each symbol with one of: `missing`, `partial`, `covered`.
- Add or update test references for each symbol as coverage is implemented.

## Status Legend

- `missing`: no direct assertion tests for this symbol.
- `partial`: tested indirectly or only in subset scenarios.
- `covered`: direct tests cover nominal and relevant error/edge behavior.

## Module: kcsg

| Symbol | Status | Tests |
| --- | --- | --- |
| _Populate in Phase 1 inventory task_ | missing | n/a |

## Module: kcsg-dsl

| Symbol | Status | Tests |
| --- | --- | --- |
| _Populate in Phase 1 inventory task_ | missing | n/a |
