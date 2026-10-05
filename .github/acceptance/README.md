# Runtime acceptance manifest

`acceptance.json` binds the branch's production-source predecessor and normalized source inputs to the exact runtime/source JAR hashes. Its accepted profiles list installed dependencies, scenario names, screenshots, renderer and PASS marker hashes.

The source predecessor contains the tested production implementation. Later documentation or separate QA-driver changes do not alter those runtime bytes. The manifest records its source-hash normalization; packaged binary assets and license notices are checked against the exact build inputs. A rebuild is not presumed identical to an already accepted binary.

[TESTING.txt](../../TESTING.txt) explains fixture instrumentation, actual game paths, failed attempts and limits. Full lightweight logs/captures remain in the owner's `qa-artifacts/breath-fog-1.0.1-mc<version>` folder; worlds, game libraries and helper binaries are kept outside Git and source archives. Hosted CI is a separate gate.

The full 1.0.0 acceptance and the earlier 1.0.1 default/icon update acceptance are preserved in `docs/historical`. The current manifest records final package revalidation separately, including byte comparisons that establish unchanged code and assets after the owner policy integration. Prior PASS markers do not certify changed runtime hashes.
