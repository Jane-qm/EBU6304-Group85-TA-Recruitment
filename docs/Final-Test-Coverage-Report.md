# Final Test Coverage Report

## Scope

This document summarizes the final JUnit 5 white-box unit testing work completed for the International School TA Recruitment System.

The work remains within the required project constraints:

- Java 17
- Maven
- JUnit 5
- Mockito
- Gson + JSON local persistence
- Production code in `src/main/java`
- Test code in `src/test/java`
- No database
- No ORM

Covered core classes:

- `modules.application.ApplicationService`
- `modules.job.JobService`
- `modules.user.UserService`
- `modules.auth.AuthService`
- `modules.profile.TAProfileService`
- `modules.cv.CVService`
- `modules.config.SystemConfigService`
- `infrastructure.security.PermissionService`
- `infrastructure.security.PasswordService`

## Verification Result

Verification command:

```bash
mvn -q test
```

Final result:

- full Maven test suite passes
- newly added and reworked tests compile and execute successfully

Note on warning output:

- `Byte Buddy` / `Mockito` may print dynamic Java agent warnings on newer JDKs
- this is a JVM warning, not a test failure
- if `mvn -q test` finishes successfully, the verification result is valid

## Added or Reworked Test Classes

- `src/test/java/modules/application/ApplicationServiceTest.java`
- `src/test/java/modules/job/JobServiceTest.java`
- `src/test/java/modules/user/UserServiceTest.java`
- `src/test/java/modules/auth/AuthServiceTest.java`
- `src/test/java/modules/profile/TAProfileServiceTest.java`
- `src/test/java/modules/cv/CVServiceTest.java`
- `src/test/java/modules/config/SystemConfigServiceTest.java`
- `src/test/java/infrastructure/security/PermissionServiceTest.java`
- `src/test/java/infrastructure/security/PasswordServiceTest.java`

New test methods follow a unified naming style:

- `method_WhenScenario_Expected`

Each test method includes:

- a one-line scenario comment
- `Given`
- `When`
- `Then`

## Covered Business Rules

### `ApplicationService`

- recruitment window closed blocks submission
- incomplete TA profile blocks submission
- closed or expired jobs cannot be applied for
- duplicate application to the same job is rejected
- more than 3 active applications is rejected
- pending applications can be cancelled
- offer acceptance and rejection update status correctly
- expired offers can be auto-processed

### `JobService`

- the same module cannot have multiple open jobs
- publishing requires an open recruitment cycle
- deadline validation is enforced
- expired jobs can be auto-closed
- closing a job triggers linked application processing

### `UserService`

- registration success and duplicate-email failure
- successful login
- repeated wrong passwords trigger account locking
- locked accounts cannot log in
- password reset clears lock state
- admin account status operations are covered

### `AuthService`

- TA registration is restricted to `@qmul.ac.uk` and `@bupt.edu.cn`
- invalid registration domain is rejected
- login validation is covered
- password reset delegation is covered

### `TAProfileService`

- profile lookup, creation, save, update, refresh, and delete flows
- profile completion calculation is covered
- required-field gaps reduce completion below `100%`

### `CVService`

- CV upload, delete, download, default CV, sorting, and lookup flows
- duplicate CV name rejection is covered

### `SystemConfigService`

- config load and empty-config fallback
- recruitment cycle update
- in-cycle checks
- deadline validation
- publish-window validation

### `PermissionService`

- admin, MO, and TA role access rules
- null-role denial

### `PasswordService`

- hash generation
- null password rejection
- password verification success and failure
- malformed hash fallback
- legacy hash upgrade detection

## Minimal Production Changes Made For Testability

Only minimal constructor-based dependency injection support was added to make the service layer mockable with Mockito:

- `ApplicationService`
- `AuthService`
- `SystemConfigService`
- `CVService`
- `JobService`
- `TAProfileService`
- `UserService`

These changes do not alter the default application startup behavior because original default constructors remain intact.

## Requirement Check

Requirements already satisfied:

- core service tests added under `src/test/java`
- Mockito used for external dependencies where applicable
- naming unified for newly added tests
- key business-rule branches covered
- full suite verified with Maven
- final coverage documentation prepared in `docs`

Pragmatic exception:

- not every public method has a strict normal-path and exception-path pair
- some public methods intentionally return `null`, empty lists, or behave as thin delegators
- for those methods, forcing symmetric exception tests would add noise without meaningful defect coverage

This is an intentional testing choice, not an unfinished implementation gap.

## Final Judgment

The current test suite is suitable for coursework submission because it provides:

- white-box coverage of the core service layer
- direct regression protection for the main recruitment business rules
- clean Maven-based execution in the existing repository structure

The most important coursework requirement, from a quality standpoint, has been satisfied:

- core business rules are covered
- Mockito-based dependency isolation is used where meaningful
- the suite runs successfully with `mvn test`
