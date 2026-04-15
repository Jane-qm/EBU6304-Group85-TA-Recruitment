# Test Cases (Iteration 2 + Iteration 3 additions)

Group: Group85
Testers: Jiayi Lou, Zhixuan Guo

---

## Manual / UI Test Cases

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


