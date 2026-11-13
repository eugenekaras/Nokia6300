# Nokia 6300 (2007) — Java ME app

This repository builds the supplied menu app for a classic Nokia 6300 using Java ME bytecode settings (CLDC 1.1 / MIDP 2.0).

## Build it online with GitHub Actions

1. Sign in to GitHub and create a **new empty repository**.
2. Upload the contents of this ZIP to the repository (keep the `.github` folder).
   - On GitHub, use **Add file → Upload files**.
   - Make sure `.github/workflows/build.yml` is included.
3. Open the repository's **Actions** tab.
4. Select **Build Nokia 6300 Java ME app**.
5. If GitHub asks, enable workflows, then click **Run workflow**.
   - A push to `main` or `master` also starts a build automatically.
6. When the run finishes, open it and find **Artifacts** at the bottom.
7. Download `Nokia6300-installation-files`. Inside are:
   - `NokiaApp.jar`
   - `NokiaApp.jad`

## Install on the phone

Copy both files to the phone (or its memory card) and open the `.jad` file in the phone's file manager/browser to start installation. Keep the JAR and JAD together in the same folder.

If the phone reports an invalid application, check that both files came from the same Actions run. The JAD contains the exact JAR file size.

## Notes

- Target: MIDP 2.0 / CLDC 1.1.
- The build uses the open-source MicroEmulator API stubs only for compilation; those API classes are **not** packaged into the app.
- A successful cloud build confirms compilation and packaging. It does not guarantee testing on the physical handset. Actual device installation is the final compatibility check.
- This starter project contains the menu app from the supplied Java source.
