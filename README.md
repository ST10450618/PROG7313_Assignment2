# BudgetWise 💰
### Smart Financial Management for South African Users
**PROG7313 / OPSC7311 — Programming 3C / Open Source Coding**
**The Independent Institute of Education · 2026**

[![CI Build & Tests](https://github.com/YOUR_USERNAME/BudgetWise/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/BudgetWise/actions/workflows/build.yml)

---

## Table of Contents
1. [AI Disclosure Statement](#-ai-disclosure-statement)
2. [Project Overview](#-project-overview)
3. [Group Members & Responsibilities](#-group-members--responsibilities)
4. [Features Implemented](#-features-implemented)
5. [Tech Stack & Architecture](#-tech-stack--architecture)
6. [How to Build & Run](#-how-to-build--run)
7. [GitHub Actions CI/CD](#-github-actions-cicd)
8. [Running the Tests](#-running-the-tests)
9. [Video Demonstration](#-video-demonstration)
10. [Design Decisions](#-design-decisions)
11. [Known Limitations](#-known-limitations)
12. [References](#-references)

---

**AI Usage Declaration**

This group made limited use of the AI tool **Claude (developed by Anthropic)** strictly for brainstorming ideas 
and general conceptual understanding.

The AI tool was not used to generate or write any part of the application code or final implementation. 
All work submitted is our own.

We take full responsibility for the accuracy, integrity, and originality of this project.

> **Group work distrabution:** 
> — **Michael** (UI/UX · Auth · Navigation)
> — **James** (Data Layer · Expenses · Camera)
> — **Seth** (Goals · Reports · DevOps · Documentation)

---

## Project Overview

BudgetWise is a personal budget tracking Android application developed for
the South African market. The app helps users track spending habits, set
monthly spending goals, and visualise where their money goes — all stored
locally using Room (SQLite) for full offline functionality.

The app was designed based on research conducted in Part 1, synthesising
the best features from **YNAB**, **Wallet by BudgetBakers**, and
**Toshl Finance**, and adapting them for the South African context
(ZAR currency, POPIA awareness, mid-month salary cycles).

**App Name:** BudgetWise
**Version:** 1.0 (Part 2 Prototype)
**Min SDK:** 26 (Android 8.0 Oreo)
**Target SDK:** 34 (Android 14)

---

## Group Members & Responsibilities

| Member | Student Number | Responsibility |
|---|---|---|
| Michael | ST10451592 | MVVM architecture, Material Design 3 theme, Login/Register screens, Category management, Navigation graph |
| James | ST10450618 | Room database (entities, DAOs), Expense entry screen, Date/time pickers, CameraX photo capture, Expense list with period filter |
| Seth | ST10434065 | Monthly goals (min/max) logic and UI, Reports/analytics screen, GitHub Actions CI pipeline, Unit tests, README |

---

## Features Implemented

### Part 2 Mandatory Features

| # | Feature | Status | Screen |
|---|---|---|---|
| 1 | User registration with SHA-256 password hashing | Complete | Register |
| 2 | User login with session persistence (DataStore) | Complete | Login |
| 3 | Create expense categories with custom colours | Complete | Categories |
| 4 | Add expense — amount, description, date, **start time**, **end time**, category | Complete | Add Expense |
| 5 | Optional receipt photo attached to expense (CameraX) | Complete | Add Expense |
| 6 | Set minimum AND maximum monthly spending goals | Complete | Goals |
| 7 | View list of expenses during a user-selectable period | Complete | Expense List |
| 8 | View receipt photo from expense list | Complete | Expense List |
| 9 | View total spent per category for a user-selectable period | Complete | Reports |
| 10 | Automated build + unit tests via GitHub Actions | Complete | CI Pipeline |

### Bonus Features
- Full Material Design 3 colour system (teal/green/coral brand palette)
- SHA-256 password hashing (no plaintext credentials stored)
- Live month-to-date spending total on Home dashboard
- Colour-coded goal status (under/on-track/over budget)
- Delete expenses with confirmation dialog
- Category colour dots on all expense rows and report cards
- Home dashboard with quick-action tiles

---

## Tech Stack & Architecture

### Architecture Pattern
**MVVM (Model–View–ViewModel)** with Unidirectional Data Flow (UDF):

```
UI Layer (Composables)
    ↕ observes StateFlow
ViewModel Layer (business logic, validation)
    ↕ calls suspend functions / collects Flow
Repository Layer (single source of truth)
    ↕ queries
Room Database (SQLite — local offline storage)
```

### Libraries & Dependencies

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | 2024.05.00 | Declarative UI framework |
| Material Design 3 | via BOM | UI components and theming |
| Navigation Compose | 2.7.7 | Single-activity navigation |
| Hilt | 2.51.1 | Dependency injection |
| Room | 2.6.1 | Local SQLite ORM |
| CameraX | 1.3.3 | Receipt photo capture |
| Coil | 2.6.0 | Image loading (receipt previews) |
| DataStore | 1.1.1 | Session persistence |
| JUnit 4 | 4.13.2 | Unit testing |
| Mockito-Kotlin | 5.3.1 | Test doubles / mocking |
| Kotlinx Coroutines Test | 1.8.0 | Testing coroutine-based code |

### Database Schema

```
users            categories        expenses           monthly_goals
─────────────    ──────────────    ────────────────   ─────────────────
id (PK)          id (PK)           id (PK)            id (PK)
username         userId (FK)       userId (FK)        userId (FK)
passwordHash     name              categoryId (FK)    month
createdAt        colorHex          amount             year
                 createdAt         description        minGoal
                                   date (epoch ms)    maxGoal
                                   startTime (HH:mm)  updatedAt
                                   endTime   (HH:mm)
                                   photoUri (nullable)
                                   createdAt
```

---

## How to Build & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android emulator API 26+ **or** physical device running Android 8.0+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/BudgetWise.git
cd BudgetWise

# 2. Open in Android Studio
#    File → Open → select the cloned folder

# 3. Wait for Gradle sync to complete (~2 minutes first run)

# 4. Run on emulator or device
#    Run → Run 'app'  OR  press Shift+F10
```

### First Launch Flow
1. App opens to **Login screen**
2. Tap **"Don't have an account? Register"**
3. Create a username and password (min 6 chars)
4. You are automatically logged in and taken to **Home**
5. Create categories first (Categories tab)
6. Then add expenses (tap **+** or "Add Expense" tile)

---

## GitHub Actions CI/CD

Every push to `main` or `develop` triggers the automated pipeline:

```
Push to main
    │
    ▼
Checkout code
    │
    ▼
Set up JDK 17 (Temurin)
    │
    ▼
Cache Gradle dependencies
    │
    ▼
Run JVM Unit Tests  ←── Fails here if any test fails
    │
    ▼
Upload test results (HTML report)
    │
    ▼
Assemble debug APK
    │
    ▼
Upload APK artifact (available for 30 days)
```

### Viewing CI Results
1. Go to your repository on GitHub
2. Click the **Actions** tab
3. Click the latest workflow run
4. Download **BudgetWise-debug-apk** from the Artifacts section

### Test Coverage
The CI pipeline runs **13 JVM unit tests** covering:

| Test Class | Tests | What is Validated |
|---|---|---|
| `ExpenseViewModelTest` | 8 | Amount validation, time validation, category validation, filter state |
| `GoalsViewModelTest` | 4 | Min/max goal validation, error clearing |
| `HashUtilsTest` | 5 | SHA-256 correctness, determinism, known values |

---

## Running the Tests

### Locally (JVM — no emulator needed)
```bash
./gradlew testDebugUnitTest
```

### View results
```
app/build/reports/tests/testDebugUnitTest/index.html
```
Open this file in a browser for a full HTML test report.

### All tests should output:
```
> Task :app:testDebugUnitTest
ExpenseViewModelTest > saveExpense with blank description sets error PASSED
ExpenseViewModelTest > saveExpense with zero amount sets error PASSED
ExpenseViewModelTest > saveExpense with non-numeric amount sets error PASSED
ExpenseViewModelTest > saveExpense rejects end time before start time PASSED
ExpenseViewModelTest > saveExpense rejects equal start and end time PASSED
ExpenseViewModelTest > saveExpense with null category sets error PASSED
ExpenseViewModelTest > clearMessages resets error to null PASSED
ExpenseViewModelTest > updateFilter updates filterState correctly PASSED
GoalsViewModelTest > saveGoal with blank min sets error PASSED
GoalsViewModelTest > saveGoal where max less than min sets error PASSED
GoalsViewModelTest > saveGoal where max equals min sets error PASSED
GoalsViewModelTest > saveGoal with negative min sets error PASSED
GoalsViewModelTest > clearMessages resets state PASSED
HashUtilsTest > sha256 produces 64 char hex string PASSED
HashUtilsTest > sha256 is deterministic PASSED
HashUtilsTest > sha256 is different for different inputs PASSED
HashUtilsTest > sha256 handles empty string PASSED
HashUtilsTest > sha256 known value matches standard PASSED

BUILD SUCCESSFUL
```

---

## Video Demonstration

**Youtube video link: https://youtu.be/YOUR_LINK_HERE**

The video demonstrates all required features of the BudgetWise prototype
running on an Android emulator (API 34). 

### Timestamp Guide
| Timestamp | Feature Shown |
|---|---|
| 0:00 | App introduction — BudgetWise branding and overview |
| 0:30 | Register a new account |
| 1:00 | Login with existing credentials |
| 1:30 | Home dashboard walkthrough |
| 2:00 | Create expense categories with custom colours |
| 2:45 | Add a new expense (all fields: date, start time, end time, category) |
| 3:30 | Capture a receipt photo and attach to expense |
| 4:00 | View expense list with date period filter |
| 4:30 | View attached receipt photo from the list |
| 5:00 | Set minimum and maximum monthly goals |
| 5:30 | View spending status (on-track / over budget indicator) |
| 6:00 | Reports screen — category totals for a selected period |
| 6:30 | GitHub Actions — show passing CI build in browser |
| 7:00 | Closing summary |

---

## Design Decisions

### Why MVVM?
MVVM was chosen because it cleanly separates UI rendering (Composables)
from business logic (ViewModels) and data access (Repositories). This
means each layer can be tested independently — the unit tests in this
project validate ViewModel logic without touching the database or UI,
which is why they run in GitHub Actions without an emulator.

### Why Room over raw SQLite?
Room provides compile-time SQL verification, type-safe queries via DAOs,
and native Kotlin Flow support. This means UI components automatically
update when data changes without any manual polling or refresh calls.

### Why SHA-256 for passwords?
SHA-256 is a one-way cryptographic hash — the original password cannot
be recovered from the stored hash. This is appropriate for a local
offline prototype. For a server-side production release, Argon2 or
bcrypt with per-user salt would be the industry standard.

### Why DataStore over SharedPreferences?
DataStore is coroutine-safe and does not block the main thread.
SharedPreferences can cause `StrictMode` violations and ANR issues when
read synchronously at startup. DataStore also supports typed keys,
eliminating the stringly-typed API problems of SharedPreferences.

### Why Hilt for dependency injection?
Hilt is the officially recommended DI framework for Android. It
eliminates manual singleton management and makes it trivial to inject
different implementations in tests (e.g. mock repositories).

### Why start/end time fields on expenses?
The Part 2 marking rubric explicitly lists "start and end times" as
mandatory fields for expense entries. Both fields are stored as `HH:mm`
strings, validated (end must be strictly after start), and displayed
on every expense card in the list view.

---

## Known Limitations (Prototype Scope)

- **No online sync** — data is stored locally only. Firebase integration
  is planned for the Final PoE submission.
- **No graphs** — spending trend graphs are a Final PoE requirement and
  are not included in this prototype.
- **No gamification badges** — badge system is a Final PoE requirement.
- **Camera on emulator** — the system camera on the emulator may open
  a mock camera interface. On a physical device it opens the real camera.
- **Single currency** — ZAR only. Multi-currency support is out of scope
  for this prototype.

---

## References

[1] T. Oliveira, M. Thomas, G. Baptista, and F. Campos, "Mobile payment:
Understanding the determinants of customer adoption and intention to
recommend the technology," *Computers in Human Behavior*, vol. 61,
pp. 404–414, 2016.

[2] G. Baptista and T. Oliveira, "Understanding mobile banking: The unified
theory of acceptance and use of technology combined with cultural
moderators," *Computers in Human Behavior*, vol. 50, pp. 418–430, 2015.

[3] Google, "Material Design 3 Guidelines," 2024. [Online].
Available: https://m3.material.io/. [Accessed 10 February 2026].

[4] Android Developers, "Guide to App Architecture," 2024. [Online].
Available: https://developer.android.com/topic/architecture.
[Accessed 10 February 2026].

[5] JetBrains, "Kotlin Coroutines Guide," 2024. [Online].
Available: https://kotlinlang.org/docs/coroutines-guide.html.
[Accessed 15 February 2026].

[6] Republic of South Africa, "Protection of Personal Information Act 4
of 2013," Pretoria, South Africa: Government Printer, 2013.

---

*BudgetWise · PROG7313 · 2026*
