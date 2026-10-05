# Version branch workflow

The repository keeps one branch per exact Minecraft version: `26.3`, `1.21.1`, and `1.20.1`. Each contains all supported loader modules. `26.2` preserves the historical Fabric MVP and provides the default page's version index. Permanent loader-specific branches are unnecessary.

Base fixes and pull requests on the affected version branch. Update each port through its real Minecraft APIs, metadata, assets and toolchain; widening a version range does not establish compatibility. Quilt is outside the maintained matrix.

Run the chosen branch's `clean check build`, then the relevant isolated production-profile gates. Record exact final runtime hashes and limitations in `TESTING.txt` and `.github/acceptance/acceptance.json`. Keep failed attempts alongside passing evidence. QA helpers and development JARs are not gameplay releases.

The build workflow uses that branch's catalog, checks every loader's packaged JAR and uploads runtime/source JARs. Older branches compile Minecraft with Java 17 or 21 while Java 25 hosts Gradle. Hosted Actions results and local acceptance are separate; a refused or blocked CI job is never a passing check.

Commit source and documentation together after the applicable gates pass. Push only the intended version branch without force. Preserve the default branch, existing history, licenses and published artifacts. A binary release, visibility change, branch retirement or default-branch cutover needs its own explicit owner request.
