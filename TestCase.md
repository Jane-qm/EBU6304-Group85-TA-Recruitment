# Test Cases (Iteration 2 + Iteration 3)

Group: Group85  
Testers: Jiayi Lou, Zhixuan Guo

---

## Manual / UI Test Cases

### Iteration 2 (TC-001 – TC-014)

| Test ID | Test Function | Steps | Expected Result | Actual Result | Pass |
| ------- | ------------- | ----- | --------------- | ------------- | ---- |
| TC-001 | TA Registration | 1. Open system 2. Click Register 3. Select TA tab 4. Enter valid @qmul.ac.uk or @bupt.edu.cn email + password 5. Submit | Register successfully; data saved to users.json; Login page opens with **TA** tab pre-selected | Normal | Yes |
| TC-002 | TA Login | 1. Enter correct email & password on TA tab 2. Click Sign In | Login successful; TAMainFrame opens | Normal | Yes |
| TC-003 | TA Create Profile | 1. Login as TA 2. Go to My Profile 3. Fill all required fields 4. Save | Profile saved to ta_profiles.json | Normal | Yes |
| TC-004 | TA View Jobs | 1. Login as TA 2. Open Course Catalog tab | All published jobs displayed | Normal | Yes |
| TC-005 | TA Apply for Job | 1. Complete profile + upload CV 2. Select a published job 3. Click Apply | Application submitted; status = SUBMITTED; ta_applications.json updated | Normal | Yes |
| TC-006 | TA Profile Data Edit & Update | 1. Save a complete profile 2. Edit phone / available hours 3. Save again | Data updated in ta_profiles.json; refreshed view shows latest values | Normal | Yes |
| TC-007 | Invalid Login | Enter wrong password on correct role tab | Error message shown; login blocked | Normal | Yes |
| TC-008 | TA Personal Information Fill & Save | 1. Login as TA → My Profile 2. Fill all required fields (Surname, Forename, Student ID, Phone, Gender, School, Student Type, Year, Campus) 3. Fill optional fields (Chinese Name, Major, Hours/Week) 4. Save | All fields saved; ta_profiles.json updated; page reload retains data | Normal | Yes |
| TC-009 | TA Profile Required Field Validation | 1. Open My Profile 2. Leave ≥1 required field blank 3. Click Save | Error popup listing missing fields; save blocked | Normal | Yes |
| TC-010 | Registration Email Domain Validation | 1. Click Register 2. Enter email ending in @gmail.com or other non-university domain 3. Submit | Error: "Only university emails are allowed"; registration blocked | Normal | Yes |
| TC-011 | Registration with @bupt.edu.cn | 1. Click Register 2. Enter valid @bupt.edu.cn email 3. Submit | Registration succeeds; login page opens with correct role tab active | Normal | Yes |
| TC-012 | Auto Role Tab after Registration (TA) | 1. Register as TA 2. Observe login page | TA tab is highlighted / pre-selected on login page | Normal | Yes |
| TC-013 | Auto Role Tab after Registration (MO) | 1. Register as MO 2. Observe login page | MO tab is highlighted / pre-selected on login page | Normal | Yes |
| TC-014 | Auto Role Tab — Back to Login | 1. On Register page, switch to ADMIN tab 2. Click "Back to Login" | Login page opens with ADMIN tab pre-selected | Normal | Yes |

---

## JUnit Automated Tests (`mvn test`)

Run command:

```bash
mvn test
```

### Test Class: `common.domain.ApplicationStatusTest`

| Test Method | Input | Expected | Pass |
| ----------- | ----- | -------- | ---- |
| `pendingReview_isAwaitingReview` | `PENDING_REVIEW` | `true` | Yes |
| `submitted_isAwaitingReview` | `SUBMITTED` | `true` | Yes |
| `waitlisted_isNotAwaitingReview` | `WAITLISTED` | `false` | Yes |
| `hired_isNotAwaitingReview` | `HIRED` | `false` | Yes |
| `null_isNotAwaitingReview` | `null` | `false` | Yes |
| `pendingReview_isCancellable` | `PENDING_REVIEW` | `true` | Yes |
| `submitted_isCancellable` | `SUBMITTED` | `true` | Yes |
| `waitlisted_isCancellable` | `WAITLISTED` | `true` | Yes |
| `hired_isNotCancellable` | `HIRED` | `false` | Yes |
| `rejected_isNotCancellable` | `REJECTED` | `false` | Yes |
| `cancelled_isNotCancellable` | `CANCELLED` | `false` | Yes |
| `isCancelled_matchesCancelledOnly` | `CANCELLED` / `SUBMITTED` / `null` | `true` / `false` / `false` | Yes |
| `isRejected_matchesRejectedOnly` | `REJECTED` / `ACCEPTED` | `true` / `false` | Yes |
| `isHired_matchesHiredOnly` | `HIRED` / `ACCEPTED` | `true` / `false` | Yes |
| `isAccepted_matchesAcceptedOnly` | `ACCEPTED` / `HIRED` | `true` / `false` | Yes |
| `displayText_hired_returnsAccepted` | `HIRED` | `"accepted"` | Yes |
| `displayText_accepted_returnsAccepted` | `ACCEPTED` | `"accepted"` | Yes |
| `displayText_submitted_returnsPending` | `SUBMITTED` | `"pending"` | Yes |
| `displayText_waitlisted_returnsWaitlisted` | `WAITLISTED` | `"waitlisted"` | Yes |
| `displayText_rejected_returnsRejected` | `REJECTED` | `"rejected"` | Yes |
| `displayText_cancelled_returnsCancelled` | `CANCELLED` | `"cancelled"` | Yes |
| `displayText_null_returnsUnknown` | `null` | `"unknown"` | Yes |

### Test Class: `ta.entity.TAProfileTest`

| Test Method | What is tested | Pass |
| ----------- | -------------- | ---- |
| `validEmail_isEmailValid` | Valid email with `@` passes | Yes |
| `nullEmail_isNotEmailValid` | `null` email fails | Yes |
| `blankEmail_isNotEmailValid` | Blank/space email fails | Yes |
| `emailWithoutAtSign_isNotEmailValid` | Email without `@` fails | Yes |
| `validChinesePhone_isPhoneValid` | `138xxxxxxxx` passes | Yes |
| `validShortInternationalPhone_isPhoneValid` | 8-digit number passes | Yes |
| `tooShortPhone_isNotPhoneValid` | 7-digit number fails | Yes |
| `nullPhone_isNotPhoneValid` | `null` phone fails | Yes |
| `lettersInPhone_isNotPhoneValid` | Letters in phone fail | Yes |
| `fullyFilledProfile_isComplete_afterSaveProfile` | All fields set → `isProfileCompleted()=true` | Yes |
| `profileWithMissingSurname_isNotComplete` | Missing surname → incomplete | Yes |
| `profileWithMissingGender_isNotComplete` | Missing gender → incomplete | Yes |
| `profileWithMissingSupervisor_isNotComplete` | Empty supervisor → incomplete | Yes |
| `editingField_resetProfileCompleted` | Any setter resets `profileCompleted` flag | Yes |
| `emptyProfile_lowCompletion` | Near-empty profile < 20% | Yes |
| `fullyFilledProfile_100Completion` | All 13 fields set → 100% | Yes |
| `profileWithoutOptionals_highCompletion` | Required fields only → ≥84% | Yes |
| `noMissingFields_whenAllSet` | All fields set → empty missing list | Yes |
| `missingSurname_appearsInMissingFields` | Missing surname in list | Yes |
| `missingEmail_appearsInMissingFields` | Missing email in list | Yes |
| `fullName_whenBothNamesSet` | `"Guo Zhixuan"` | Yes |
| `fullName_fallsBackToChineseName` | Falls back to chineseName | Yes |

### Test Class: `auth.AuthServiceTest`

| Test Method | What is tested | Pass |
| ----------- | -------------- | ---- |
| `register_withQmulDomain_doesNotThrowDomainError` | `@qmul.ac.uk` passes domain check | Yes |
| `register_withBuptDomain_doesNotThrowDomainError` | `@bupt.edu.cn` passes domain check | Yes |
| `register_withGmailDomain_throwsDomainError` | `@gmail.com` rejected | Yes |
| `register_withArbitraryDomain_throwsDomainError` | `@evil.io` rejected | Yes |
| `register_withEmptyEmail_throwsIllegalArgument` | Empty email → exception | Yes |
| `register_withNullEmail_throwsIllegalArgument` | `null` email → exception | Yes |
| `register_withEmailMissingAtSign_throwsIllegalArgument` | `nodomain` → exception | Yes |
| `register_withShortPassword_throwsIllegalArgument` | `"abc"` (< 6 chars) → exception | Yes |
| `register_withNullPassword_throwsIllegalArgument` | `null` password → exception | Yes |
| `register_withBlankPassword_throwsIllegalArgument` | Blank password → exception | Yes |
| `login_withNullEmail_throwsIllegalArgument` | `null` email → exception | Yes |
| `login_withBlankEmail_throwsIllegalArgument` | Empty email → exception | Yes |
| `login_withUnregisteredEmail_returnsNull` | Unknown email → `null` | Yes |
| `login_withWrongPassword_returnsNull` | Wrong password → `null` | Yes |

**Total: 58 automated test cases — all passing.**

---

### Iteration 3 (TC-015 – TC-031)

| Test ID | Task | Test Function | Steps | Expected Result | Pass |
| ------- | ---- | ------------- | ----- | --------------- | ---- |
| TC-015 | MO-007.1 | TA rejection triggers MO notification | 1. MO sends offer to TA 2. TA logs in, opens "My Applications", clicks "Reject Offer" 3. MO logs in, checks notification bell | Notification bell shows unread badge; notification title contains course name + "Rejected"; body shows TA name, rejection time, waitlist hint | Yes |
| TC-016 | MO-007.1 | Notification content completeness | Read notification in MO portal | Content includes: course code + title, TA full name, timestamp (yyyy-MM-dd HH:mm format), hint to select from waitlist | Yes |
| TC-017 | MO-007.2 | From Waitlist button visible | 1. Log in as MO 2. Open Applicant Review tab | "From Waitlist" button appears next to "Send Official Offer" button | Yes |
| TC-018 | MO-007.2 | Empty waitlist message | 1. Select a job row with no WAITLISTED applications 2. Click "From Waitlist" | Dialog shows "No waitlisted candidates" message | Yes |
| TC-019 | MO-007.2 | One-click offer from waitlist | 1. Ensure a WAITLISTED application exists for a job 2. Select any row for that job 3. Click "From Waitlist" 4. Select candidate in dialog 5. Click "Send Offer to Selected" | Application status changes to OFFER_SENT; ta_applications.json updated; success dialog shown; TA receives offer notification | Yes |
| TC-020 | MO-008.1 | Hired TAs list appears in sidebar | Log in as MO; observe sidebar | "Hired TAs" navigation button visible in sidebar | Yes |
| TC-021 | MO-008.1 | Hired TAs table populated | 1. Ensure at least one TA has HIRED status for MO's job 2. Click "Hired TAs" in sidebar | Table shows TA Name, Major, Year, Phone, Email, Course, Hired At columns with correct data | Yes |
| TC-022 | MO-008.1 | View Full Profile button | 1. Select a row in Hired TAs table 2. Click "View Full Profile" | Modal dialog opens showing detailed TA profile and offer timestamp | Yes |
| TC-023 | MO-008.2 | Export CSV button visible | Open "Hired TAs" panel | "Export CSV" button visible in panel footer | Yes |
| TC-024 | MO-008.2 | Export CSV file content | 1. Click "Export CSV" 2. Choose save location 3. Open file in Excel | File opens correctly in Excel; columns: Course Name, TA Name, Student ID/Email, Phone, Hired Date; UTF-8 BOM present (Chinese chars readable) | Yes |
| TC-025 | MO-009.1 | Close Recruitment button visible | Open "Job Management" tab as MO | "Close Recruitment" button visible next to "Withdraw Selected" | Yes |
| TC-026 | MO-009.1 | Manual close changes status | 1. Select a PUBLISHED job 2. Click "Close Recruitment" 3. Confirm dialog | Job status changes to CLOSED in table; mo_jobs.json updated; TA course catalog no longer shows this job | Yes |
| TC-027 | MO-009.1 | Cannot close already-closed job | 1. Select a CLOSED job 2. Click "Close Recruitment" | Info dialog: "This job is already closed"; no state change | Yes |
| TC-028 | MO-009.2 | Auto-close on MO login | 1. Set a job's deadline to yesterday's date in mo_jobs.json 2. Log in as MO | Console shows auto-close log; job status updated to CLOSED; job no longer visible to TAs | Yes |
| TC-029 | ADM-001 | Default admin seeded at startup | 1. Remove admin@test.com from users.json 2. Restart application | Console logs "Default admin seeded"; admin@test.com entry recreated in users.json; login with admin123 succeeds | Yes |
| TC-030 | ADM-003 | TA blocked outside cycle | 1. Log in as Admin 2. Set cycle end to yesterday 3. Log in as TA 4. Attempt to apply for a job | Error dialog: "Applications are currently closed. Recruitment window: ... to ..." | Yes |
| TC-031 | ADM-004.2 | Audit log records admin actions | 1. Log in as Admin 2. Approve an MO 3. Disable an account 4. Reset a password 5. Check data/admin_audit.log | Each action produces one line in admin_audit.log with timestamp, admin email, action verb, and target email | Yes |
| TC-032 | SYS-001 | Wrong-role login blocked | 1. Select MO tab on login page 2. Enter TA credentials | Error: "Role mismatch. Account Role: TA"; login blocked; MO portal not opened | Yes |
| TC-033 | SYS-001 | RBAC guard in portal | Attempt to directly instantiate `TAMainFrame` with an MO user object in code | "Access denied. A TA account is required" dialog shown; LoginFrame opens; TAMainFrame closes | Yes |
| TC-034 | SYS-001 | permissions.json loaded | 1. Check console on startup 2. Verify data/permissions.json exists | Console logs "[PermissionService] Loaded RBAC matrix from data/permissions.json" | Yes |

---

