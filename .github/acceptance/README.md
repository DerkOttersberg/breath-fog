# Runtime acceptance manifest

`acceptance.json` binds the branch's production-source predecessor and normalized source inputs to the exact runtime/source JAR hashes. Its accepted profiles list installed dependencies, scenario names, screenshots, renderer and PASS marker hashes.

The source predecessor contains the tested production implementation. Later documentation or separate QA-driver changes do not alter those runtime bytes. Text input hashes normalize CRLF to LF; binary assets remain exact. A rebuild is not presumed identical to an already accepted binary.

[TESTING.txt](../../TESTING.txt) explains fixture instrumentation, actual game paths, failed attempts and limits. Full lightweight logs/captures remain in the owner's `qa-artifacts/breath-fog-mc<version>` folder; worlds, game libraries and helper binaries are kept outside Git and source archives. Hosted CI is a separate gate.
