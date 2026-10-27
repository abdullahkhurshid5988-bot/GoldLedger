# Softonic Submission & Store Listing Package — GoldLedger

## 1. Application Metadata
- **Application Name:** GoldLedger — Smart Expense & Budget Manager
- **Short Description (80 characters):**
  Luxury offline expense tracker, multi-wallet ledger, budgets and savings goals.
- **Category:** Finance / Personal Finance / Productivity
- **Content Rating:** Everyone (PEGI 3, ESRB Everyone)
- **Distribution Model:** Free with AdMob Ads & Optional Cosmetic Rewarded Ads
- **Version Name:** 1.0
- **Version Code:** 1
- **Package Name:** `com.aistudio.goldledger.fzqkmv`
- **Minimum Android Version:** Android 8.0 Oreo (API level 26)
- **Target Android Version:** Android 16 (API level 36)

---

## 2. Full Promotional Description
Take total command of your personal finances with **GoldLedger**, the premier offline-first expense tracker, budget planner, and smart wallet manager designed with an exquisite Black & Gold aesthetic.

GoldLedger is engineered for individuals, freelancers, and families who demand privacy, precision, and elegance. Unlike cloud services that compromise your financial records or require persistent connectivity, GoldLedger operates completely offline using a high-performance local SQLite database.

### 🌟 Key Features:
- **Zero Internet Required:** All accounts, transactions, and analytics remain strictly on your physical device.
- **Exact Decimal Arithmetic:** Stored in integer minor units without floating-point calculation drift.
- **Multiple Accounts & Wallets:** Seamlessly track Cash Wallets, Bank Accounts, Mobile Money, and Savings Reserves with real-time balance calculations.
- **Inter-Account Transfers:** Transfer funds between wallets with automatic double-entry adjustments without falsely distorting income or expense statistics.
- **Dynamic Category Budgeting:** Configure weekly or monthly category spending limits with threshold alerts when approaching 80% and 100% consumption.
- **Goal-Oriented Savings Tracker:** Allocate contributions toward target goals (Emergency Fund, Vehicle, Real Estate, Vacation) with instant progress metrics.
- **Automated Recurring Payments:** Schedule recurring rent, utility bills, salaries, and subscriptions via Android WorkManager.
- **Visual Analytics & Reports:** Interactive Donut charts, spending trend line graphs, and comparative income-versus-expense bar visualizers.
- **Export to PDF & CSV:** Generate professional printable PDF financial statements and raw CSV spreadsheets with one-tap Android Share Sheet dispatch.
- **Biometric & PIN Security:** Lock the application behind an encrypted 4-digit PIN with hardware-backed biometric authentication.
- **Local Backup & Restore:** Export encrypted JSON backups to user-selected folders with integrity validation.

---

## 3. Graphical Assets Checklist
- [x] **High-Resolution App Icon:** 512 × 512 PNG (`app/src/main/res/drawable/goldledger_logo_512.png`)
- [x] **Adaptive Launcher Icon:** Foreground vector/layer-list and background hex (`#090909`) in all density buckets (mdpi through xxxhdpi)
- [x] **Hero Graphic:** 16:9 3D gold fintech illustration (`app/src/main/res/drawable/onboarding_hero.jpg`)
- [x] **Color Scheme:** Primary `#090909`, Secondary `#171717`, Card `#202020`, Accent `#D4AF37`, Light Gold `#F3D77A`

---

## 4. Softonic Submission Checklist
1. **Security & Permissions Check:**
   - App requires no dangerous storage permissions (`READ_EXTERNAL_STORAGE` is NOT used; SAF and FileProvider are utilized).
   - Only standard network permissions for AdMob test ads and `POST_NOTIFICATIONS` for local budget warnings.
2. **Build Verification:**
   - APK built via Gradle release configuration.
   - ProGuard/R8 rules preserved Room entities and database structures.
3. **Privacy Compliance:**
   - Public Privacy Policy URL hosted (see `PRIVACY_POLICY.md`).
   - Official AdMob test identifiers used in development.
4. **Testing Verification:**
   - Tested on Android 8.0 (API 26) through Android 16 (API 36).
   - Confirmed offline persistence after complete process termination.
