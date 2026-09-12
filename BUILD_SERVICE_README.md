# LoanConnect — one-click APK build

This project is prepared for cloud Android build services.

## Easiest option: GitHub Actions

1. Create a new GitHub repository.
2. Extract this ZIP and upload **all files/folders** to the repository root.
3. Commit/push to `main`.
4. Open the repository's **Actions** tab.
5. Select **Build LoanConnect APK**.
6. Click **Run workflow** (or push to `main` to trigger it automatically).
7. When the workflow finishes, open the run and download the artifact named **LoanConnect-debug-apk**.
8. Inside it is `app-debug.apk`.

No Android Studio is required on your computer.

## Codemagic

The included `codemagic.yaml` is ready for a Codemagic Android workflow. Connect the repository and start the workflow; the APK is collected as an artifact.

## Important

- This produces a **debug APK** suitable for testing/installation.
- A Play Store release needs a properly signed **release APK/AAB** and secure signing credentials.
- Do not put CIBIL API keys, admin passwords, session secrets, or other production secrets inside the APK or source repository.
- The app's real CIBIL functionality still requires an authorized CIBIL/provider integration and backend configuration. The project does not fabricate a CIBIL score.
