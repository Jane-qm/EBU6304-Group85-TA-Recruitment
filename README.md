# International School TA Recruitment System

**Module:** EBU6304 - Software Engineering Group Project  
**Group:** 85

## 📋 Project Overview

This is an Agile-developed application designed to streamline the Teaching Assistant recruitment process for BUPT International School.

## 🚀 Core Features (Iteration 1 \& 2)

* **TA Portal:** Profile creation, CV upload, job search, and application tracking.
* **Module Organiser (MO) Portal:** Job posting and applicant selection.
* **Admin Tools:** Workload monitoring for TAs.
* **AI-Powered (Planned):** Skill matching and workload balancing.

## 💻 Technical Specifications

* **Language:** Java (Stand-alone or Servlet/JSP).
* **Data Storage:** Plain text/CSV/JSON/XML (No Databases Allowed per project rules).
* **Architecture:** Simple, modular, and extensible design.

### JSON persistence (Iteration 2 — Product Backlog: 新增 JSON)

* **Tooling:** Google **Gson** via `common.util.GsonUtils` (shared `LocalDateTime` type adapter, pretty-print, validation helpers).
* **File access:** `common.dao.JsonPersistenceManager` creates missing `data/*.json` as `[]` on startup and ensures `data/cvs/` exists for uploaded CV files.
* **Managed list stores** (see constants on `JsonPersistenceManager`): `users.json`, `ta_profiles.json`, `mo_jobs.json`, `ta_applications.json`, `cv_infos.json`, `mo_offers.json`, `notifications.json`, `ta_cvs.json`.
* **Run from project root** so relative paths `data/` resolve correctly (e.g. `mvn exec:java` after `cd` into the repo).

## 📅 Key Assessment Dates

* **First Assessment:** 22nd March 2026 (Backlog \& Prototype).
* **Intermediate Assessment:** 12th April 2026 (Working Software V2).
* **Final Delivery:** 24th May 2026 (Final Product \& Video).

## 👥 The Agile Team

* ###### **Support** **TA:** Rongxuan Zhu\[GitHub Username:xuanxuanzhu77733-dotcom;QMID: 2025010108]
* **Member 1 :** Zhixuan Guo \[GitHub Username:Jane-qm ; QMID:231224413]
* **Member 2:** Can Chen\[GitHub Username:TUN-can ; QMID:231224309]
* **Member 3:** Yanwen Chen \[GitHub Username:cherrycoups0323 ; QMID:231224321]
* **Member 4:** Jiaza Wang \[GitHub Username:esme025 ; QMID:231225041]
* **Member 5:** Yiping Zheng \[GitHub Username:YiPZ66 ; QMID:231224631]
* **Member 6:** Jiayi Lou \[GitHub Username:lou20050802; QMID:231225063]

## 🛠 Setup \& Installation

```bash
mvn -q -DskipTests compile
mvn exec:java
```

Use JDK 17+. Execute from the repository root so JSON files are read/written under `./data`.

