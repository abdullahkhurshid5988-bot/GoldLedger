# Privacy Policy for GoldLedger

**Last Updated:** October 27, 2026  
**Effective Date:** October 27, 2026  
**Developer:** GoldLedger Development Team  
**Contact Email:** [support@goldledger.app](mailto:support@goldledger.app)  

---

## 1. Introduction
Welcome to **GoldLedger — Smart Expense & Budget Manager** ("we", "our", or "the Application"). We are dedicated to protecting your financial confidentiality and privacy. This Privacy Policy clarifies how GoldLedger operates as an offline-first mobile application and details what data is processed when you use the app on Android devices.

---

## 2. Offline-First Architecture & Data Confidentiality
- **On-Device Storage:** All financial records created by the user—including transactions, income amounts, expenses, notes, categories, bank account names, balances, budgets, savings goals, and schedules—are stored strictly on your local device via a private SQLite database managed through Android Jetpack Room.
- **No Remote Financial Transmission:** GoldLedger does not operate private cloud databases for your financial ledger. Your financial data is never sent, mirrored, sold, or shared with the developer or any third-party analytics company.
- **No Banking Credentials:** The Application does not connect to Open Banking APIs, Plaid, or financial aggregator networks. We never ask for your online banking username, password, credit card CVV, or bank login credentials.

---

## 3. Advertising & Google AdMob
GoldLedger displays advertisements supplied by Google AdMob. 

Google AdMob may collect and process certain pseudonymous device and usage data:
- IP address (used to derive approximate coarse location, e.g., city or region)
- Device identifiers (such as the Google Advertising ID / GAID)
- Ad performance metrics and user interaction signals
- Crash telemetry related to the Google Mobile Ads SDK

For information on how Google collects and processes advertising information, please review Google’s Privacy Policy: [https://policies.google.com/technologies/ads](https://policies.google.com/technologies/ads).

Users may reset or delete their Google Advertising Identifier at any time directly through the Android OS system settings (`Settings > Google > Ads`).

---

## 4. Local Backup, Export & Storage Permissions
- GoldLedger uses the **Android Storage Access Framework (SAF)** and **FileProvider** for exporting PDF reports, CSV spreadsheets, and JSON backup archives.
- The Application does not request broad file access permissions (`READ_EXTERNAL_STORAGE` or `MANAGE_EXTERNAL_STORAGE`). Files are exported solely to locations explicitly chosen by the user in the system file picker.

---

## 5. Security & Biometrics
- **PIN Lock:** If you enable the optional PIN lock, your PIN is securely hashed using SHA-256 with a unique salt on the device. Plaintext PINs are never stored.
- **Biometric Authentication:** GoldLedger uses the official Android Jetpack `BiometricPrompt` framework. Biometric fingerprints or face recognition data remain strictly inside the hardware-backed Trusted Execution Environment (TEE) of your device and are never accessible to the app.

---

## 6. Children's Privacy
GoldLedger does not target or knowingly collect data from children under the age of 13 (or under 16 in the European Economic Area). The app is a general-audience financial tracking utility.

---

## 7. User Data Deletion
You retain complete control over your data:
- To erase all records, navigate to `Settings > Delete All Data` inside the application. This action executes an atomic wipe of all database tables and resets local preferences.
- Uninstalling the application immediately removes all app-private databases and cached reports from your device.

---

## 8. Updates to This Policy
We may periodically update this policy to reflect application enhancements or regulatory requirements. Any modifications will be reflected by the "Last Updated" date at the top of this document and will be made available inside the app's Settings menu.

---

## 9. Contact Us
If you have questions regarding this Privacy Policy, please contact:
- **Email:** support@goldledger.app
- **Application:** GoldLedger (Android)
