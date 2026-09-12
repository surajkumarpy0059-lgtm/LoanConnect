# LoanConnect — APK-ready Android project

This is an Android Studio project wrapping the existing LoanConnect customer/admin web UI in a native Android WebView.

## Build APK
1. Install the latest Android Studio.
2. Open this folder: `LoanConnect_APK_Ready`.
3. Let Gradle sync.
4. Select **Build > Build APK(s)** for a test APK, or **Build > Generate Signed Bundle / APK** for a signed release APK.
5. The generated APK will be under `app/build/outputs/apk/`.

Android requires release APKs to be digitally signed. See the official Android release guidance.

## Important: real CIBIL
The app does **not** generate or display a fake CIBIL score. The CIBIL button is only a frontend request to the authorized backend endpoint `/api/cibil/report`.

For production, deploy the Node/Express backend from the previous LoanConnect package on HTTPS and connect it to an authorized TransUnion CIBIL/member/provider API. Do not put CIBIL API secrets inside the APK.

## Current limitation
Because the APK contains the web UI as a local asset, customer features that need a server work only after the backend URL/API routing is configured. The supplied UI currently calls `/api/...`; for a production build, change the web app to use your HTTPS backend base URL or host the frontend on your domain.

Never put `ADMIN_PASSWORD`, `SESSION_SECRET`, `CIBIL_API_TOKEN`, or other secrets into this Android project.

## Cloud APK build

For a no-Android-Studio build, see `BUILD_SERVICE_README.md`. A GitHub Actions workflow is included at `.github/workflows/build-apk.yml`, and a Codemagic configuration is included as `codemagic.yaml`.
