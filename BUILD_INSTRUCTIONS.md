# GoldLedger — Build & Release Documentation

## 1. Overview
GoldLedger is a native Android expense and budget manager written in Kotlin with Jetpack Compose and Room Database (SQLite). It is completely offline-first and integrates the official Google Mobile Ads SDK for Android using standard Google test ad units.

## 2. Prerequisites
- **JDK:** OpenJDK 17 or higher
- **Android Studio:** Ladybug (2024.2.1) or newer
- **Android SDK:** Platform 36 (Android 16), Build-Tools 36.0.0
- **Minimum Supported Android Version:** Android 8.0 (API 26)
- **Target SDK:** API 36

## 3. Build Commands

### Clean Project
```bash
./gradlew clean
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output artifact location:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Local Tests (Unit & Robolectric)
```bash
./gradlew :app:testDebugUnitTest
```

### Build Release APK
```bash
./gradlew assembleRelease
```
Output artifact location:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed if credentials are provided)

---

## 4. Release Keystore Generation & Secure Signing

### Step 1: Generate a Private Release Keystore
Run keytool from your terminal:
```bash
keytool -genkey -v -keystore goldledger-release.jks \
  -alias goldledger \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=GoldLedger, OU=Finance, O=GoldLedger, L=Karachi, S=Sindh, C=PK"
```

### Step 2: Configure Environment Variables
Never hardcode passwords in `build.gradle.kts`. Export the following environment variables on your CI/CD runner or workstation:
```bash
export KEYSTORE_PATH="/path/to/goldledger-release.jks"
export STORE_PASSWORD="your_secure_keystore_password"
export KEY_PASSWORD="your_secure_key_password"
```

### Step 3: Generate Signed APK
```bash
./gradlew assembleRelease
```
The resulting signed and zipalign-optimized APK will be located at:
`app/build/outputs/apk/release/app-release.apk`
