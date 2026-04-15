# International School TA Recruitment System

**Module:** EBU6304 - Software Engineering Group Project  
**Group:** 85

## Project Overview

An Agile-developed Java Swing desktop application that streamlines the Teaching Assistant recruitment process for BUPT International School. All data is persisted as local JSON files (no database required).

---

## Core Features (Iterations 1 & 2)

| Role | Key Features |
|---|---|
| **TA** | Profile creation, CV upload, job search, application submission & tracking, offer accept/reject |
| **MO** | Job posting (draft → publish → withdraw), applicant screening, official offer dispatch |
| **Admin** | MO account lifecycle management, system data export (CSV), application cycle configuration |
* **AI-Powered (Planned):** Skill matching and workload balancing.

## Code Architecture

### Source Tree

```
src/
├── Main.java                          # Entry point: init JSON stores, set L&F, open LoginFrame
├── auth/
│   ├── AuthService.java               # Registration (domain check), login, password reset
│   ├── LoginFrame.java                # Login UI with role-tab selector
│   ├── RegisterFrame.java             # Multi-step registration UI
│   ├── AdminHomeFrame.java            # Admin portal (account mgmt, data viewer, cycle config)
│   └── MOHomeFrame.java              # Legacy MO home (superseded by MODashboardFrame)
├── common/
│   ├── dao/
│   │   ├── JsonPersistenceManager.java  # Central JSON read/write; creates data/ on startup
│   │   ├── UserFileDAO.java             # User CRUD with PersistedUser serialization
│   │   ├── MOJobDAO.java               # MO job list persistence
│   │   ├── MOOfferDAO.java             # Offer list persistence
│   │   ├── CVInfoDAO.java              # CV metadata persistence
│   │   ├── NotificationDAO.java        # Notification list persistence
│   │   └── LocalDateTimeAdapter.java   # Gson type adapter for LocalDateTime
│   ├── domain/
│   │   ├── ApplicationStatus.java      # Application status constants + helpers
│   │   └── NotificationKind.java       # Notification type constants
│   ├── entity/
│   │   ├── User.java                   # Base user entity (userId, email, role, status)
│   │   ├── TA.java                     # TA subclass
│   │   ├── MO.java                     # MO subclass
│   │   ├── Admin.java                  # Admin subclass
│   │   ├── UserRole.java               # Enum: TA, MO, ADMIN
│   │   ├── AccountStatus.java          # Enum: ACTIVE, PENDING, DISABLED
│   │   ├── MOJob.java                  # Job posting entity
│   │   ├── MOOffer.java               # Offer entity
│   │   ├── NotificationMessage.java    # In-app notification entity
│   │   └── SystemConfig.java           # Application cycle config entity
│   ├── service/
│   │   ├── UserService.java            # User registration, login, account management
│   │   ├── MOJobService.java           # Job CRUD with cycle & deadline validation
│   │   ├── MOOfferService.java         # Offer dispatch and status updates
│   │   ├── NotificationService.java    # Notification creation and routing
│   │   ├── PermissionService.java      # Role-based access guard
│   │   ├── PasswordService.java        # Password hashing and verification
│   │   └── SystemConfigService.java    # Read/write application cycle configuration
│   ├── ui/
│   │   ├── BaseFrame.java              # Shared Swing frame base class
│   │   ├── NotificationPopup.java      # System notification popup component
│   │   └── NotificationButtonFactory.java  # Factory for notification bell button
│   └── util/
│       ├── GsonUtils.java              # Shared Gson instance with LocalDateTime adapter
│       └── CsvExportUtil.java          # Generic list-to-CSV export utility
├── ta/
│   ├── controller/
│   │   ├── TAController.java           # Base TA controller wiring
│   │   ├── TAAuthController.java       # Auth-phase TA logic
│   │   ├── TAProfileController.java    # Profile save/validate coordination
│   │   ├── TAApplicationController.java # Submit, cancel, view application
│   │   └── TAOfferController.java      # Accept/reject received offer
│   ├── dao/
│   │   ├── TAProfileDAO.java           # TA profile JSON CRUD
│   │   ├── TAApplicationDAO.java       # Application JSON CRUD
│   │   └── CVDao.java                  # CV file + metadata CRUD
│   ├── entity/
│   │   ├── TAProfile.java              # TA profile entity + validation + completion %
│   │   ├── TAApplication.java          # Application entity (taId, jobId, status, cvId)
│   │   ├── CVInfo.java                 # CV metadata (name, path, uploadedAt)
│   │   └── CVManager.java              # In-memory CV list helper
│   ├── service/
│   │   ├── TAProfileService.java       # Profile init, save, completion check
│   │   ├── TAApplicationService.java   # Business rules: apply, cancel, MO-review transitions
│   │   └── CVService.java              # CV upload, list, delete
│   └── ui/
│       ├── TAMainFrame.java            # Tabbed main frame for TA portal
│       ├── TADashboardPanel.java       # Summary statistics panel
│       ├── TAProfilePanel.java         # Profile edit + CV management UI
│       ├── TACourseCatalogPanel.java   # Published job list + apply dialog
│       ├── TAApplicationsPanel.java    # Application list + offer action UI
│       ├── TAWorkloadPanel.java        # Workload info display
│       └── components/
│           ├── ActionButtonRenderer.java   # Table cell renderer for action buttons
│           └── StatusCellRenderer.java     # Table cell renderer for status badges
└── mo/
    └── ui/
        ├── MODashboardFrame.java         # MO portal tabbed frame
        ├── MOJobManagementPanel.java     # Job create/edit/publish/withdraw UI
        └── MOApplicantReviewPanel.java   # Applicant review + offer dispatch UI

data/                                  # Runtime JSON stores (created automatically)
├── users.json
├── ta_profiles.json
├── mo_jobs.json
├── ta_applications.json
├── ta_cvs.json
├── mo_offers.json
├── notifications.json
├── system_config.json
└── cvs/                               # Uploaded CV files

src/test/java/                         # JUnit 5 unit tests
├── common/domain/ApplicationStatusTest.java
├── ta/entity/TAProfileTest.java
└── auth/AuthServiceTest.java
```

### Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                         │
│  LoginFrame / RegisterFrame / TAMainFrame /         │
│  MODashboardFrame / AdminHomeFrame                  │
└────────────────────┬────────────────────────────────┘
                     │ calls
┌────────────────────▼────────────────────────────────┐
│                Controller Layer                     │
│  TAController / TAApplicationController /           │
│  TAOfferController / TAProfileController            │
└────────────────────┬────────────────────────────────┘
                     │ calls
┌────────────────────▼────────────────────────────────┐
│                 Service Layer                       │
│  AuthService / UserService / TAProfileService /     │
│  TAApplicationService / MOJobService /              │
│  MOOfferService / NotificationService / ...         │
└────────────────────┬────────────────────────────────┘
                     │ calls
┌────────────────────▼────────────────────────────────┐
│                  DAO Layer                          │
│  UserFileDAO / TAProfileDAO / TAApplicationDAO /    │
│  MOJobDAO / MOOfferDAO / CVDao / NotificationDAO    │
└────────────────────┬────────────────────────────────┘
                     │ reads/writes
┌────────────────────▼────────────────────────────────┐
│            JSON Persistence (data/*.json)           │
│  users · ta_profiles · mo_jobs · ta_applications   │
│  ta_cvs · mo_offers · notifications · system_config│
└─────────────────────────────────────────────────────┘
```

---

## Technical Specifications

- **Language:** Java 17, Swing UI
- **Build:** Maven (`pom.xml`)
- **Data Storage:** Local JSON files via Google Gson (no database)
- **Testing:** JUnit 5 (`mvn test`)
- **Allowed email domains:** `@qmul.ac.uk` or `@bupt.edu.cn`

---

## Setup & Installation

```bash
# Compile (skip tests for faster build)
mvn -q -DskipTests compile

# Run the application
mvn exec:java

# Run tests
mvn test
```

Run from the repository root so that relative paths under `./data/` resolve correctly.

---

## Team & Responsibilities

| # | Member | GitHub | QMID | Primary Contributions |
|---|---|---|---|---|
| TA | Rongxuan Zhu | [@xuanxuanzhu77733-dotcom](https://github.com/xuanxuanzhu77733-dotcom) | 2025010108 | Support TA, docs |
| 1 | Zhixuan Guo | [@Jane-qm](https://github.com/Jane-qm) | 231224413 | Gson persistence foundation, auth flow wiring, TA hiring workflow, bug fixes, CI |
| 2 | Can Chen | [@TUN-can](https://github.com/TUN-can) | 231224309 | TA section UI/UX, TAProfile entity, data persistence improvements, framework setup |
| 3 | Yanwen Chen | [@cherrycoups0323](https://github.com/cherrycoups0323) | 231224321 | Registration & profile completion UI, account status & password reset, MO offer logic |
| 4 | Jiaze Wang | [@esme025](https://github.com/esme025) | 231225041 | Admin portal (account lifecycle, CSV export, cycle config), AuthService, profile loading fix |
| 5 | Yiping Zheng | [@YiPZ66](https://github.com/YiPZ66) | 231224631 | Login UI, PermissionService, system notification popups, MO notification |
| 6 | Jiayi Lou | [@lou20050802](https://github.com/lou20050802) | 231225063 | UserFileDAO, TestCase documentation, JUnit environment setup |

---

## Key Assessment Dates

| Milestone | Date |
|---|---|
| First Assessment (Backlog & Prototype) | 22 March 2026 |
| Intermediate Assessment (Working Software V2) | 12 April 2026 |
| Final Delivery (Final Product & Video) | 24 May 2026 |

---

## Version History

### v2.1 — Iteration 2 Release (12 April 2026)

**What's Changed**

- TA profile (complete entity, validation, completion %) — @TUN-can (#25, #30–#35, #45)
- Gson persistence foundation and basic TA hiring flow — @Jane-qm (#27, #29)
- Feature-level permission checks and system notification popups — @YiPZ66 (#28, #39, #47)
- Job browse/detail, application workflow & offer notifications — @Jane-qm (#29)
- Admin module: portal, account lifecycle, CSV export, cycle config — @esme025 (#36–#38)
- MO applicant review with official offer logic — @cherrycoups0323 (#43, #46)
- JUnit 5 test environment setup — @Jane-qm (#41)
- TestCase documentation — @lou20050802 (#40)
- Bug fixes (profile loading, data alignment) — @Jane-qm (#48), @esme025 (#37–#38)

**Contributors:** @Jane-qm · @cherrycoups0323 · @esme025 · @lou20050802 · @YiPZ66 · @TUN-can

---

### v2.0 — Iteration 2 Release (mid-term, 12 April 2026)

Branch `iteration2-release` synced with main; Zhixuan GUO branch merged.

---

### v1.0 — Initial Milestone (8 April 2026)

**What's Changed**

- Project framework & entity layer — @TUN-can (#8)
- Background image, registration and login UI — @YiPZ66 (#12)
- Core infrastructure (Main.java, all entities, joint-debug baseline) — @Jane-qm (#14)
- Registration and profile completion UI — @cherrycoups0323 (#18)
- Gson persistence integration and auth flow — @Jane-qm (#21)
- AuthService enhancements — @esme025 (#16)
- UserFileDAO — @lou20050802 (#13)
- PermissionService and updated login UI — @YiPZ66 (#22)
- Account status check and password reset — @cherrycoups0323 (#26)
- README team table — all members (#1–#7)

**Contributors:** @Jane-qm · @cherrycoups0323 · @esme025 · @lou20050802 · @YiPZ66 · @TUN-can · @xuanxuanzhu77733-dotcom
