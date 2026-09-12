# LoanConnect APK

APK-ready Android project for GitHub Actions cloud build.

## Build
Open **Actions** → **Build LoanConnect APK** → **Run workflow**. After the run finishes, download the **LoanConnect-debug-apk** artifact.

No Android Studio is required for the GitHub Actions build.

## Security
The app does not contain a fake CIBIL score. Real CIBIL data requires an authorized TransUnion CIBIL/provider integration and customer consent. Never put CIBIL API keys or owner passwords inside the APK or public repository.
