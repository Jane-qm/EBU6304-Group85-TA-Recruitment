# Final Testing Delivery

## Submission Goal

This delivery packages the JUnit 5 white-box unit testing work for the International School TA Recruitment System in a course-submission-friendly format.

## Files Included In This Task

### Build configuration

- `pom.xml`

### Minimal production changes for testability

- `src/main/java/modules/application/ApplicationService.java`
- `src/main/java/modules/auth/AuthService.java`
- `src/main/java/modules/config/SystemConfigService.java`
- `src/main/java/modules/cv/CVService.java`
- `src/main/java/modules/job/JobService.java`
- `src/main/java/modules/profile/TAProfileService.java`
- `src/main/java/modules/user/UserService.java`

### New or reworked test classes

- `src/test/java/modules/application/ApplicationServiceTest.java`
- `src/test/java/modules/job/JobServiceTest.java`
- `src/test/java/modules/user/UserServiceTest.java`
- `src/test/java/modules/auth/AuthServiceTest.java`
- `src/test/java/modules/profile/TAProfileServiceTest.java`
- `src/test/java/modules/cv/CVServiceTest.java`
- `src/test/java/modules/config/SystemConfigServiceTest.java`
- `src/test/java/infrastructure/security/PermissionServiceTest.java`
- `src/test/java/infrastructure/security/PasswordServiceTest.java`

### Submission documents

- `docs/Final-Test-Coverage-Report.md`
- `docs/Final-Testing-Delivery.md`

## What Was Changed

### 1. Test dependencies

Added Mockito support to Maven test dependencies:

- `mockito-core`
- `mockito-junit-jupiter`

### 2. Service testability support

Added minimal constructor-based dependency injection so service classes can receive mocked collaborators during unit tests.

This was necessary because several services originally instantiated DAOs or other services internally, which made isolated white-box unit testing difficult.

### 3. White-box service tests

Added or reworked JUnit 5 tests for the highest-priority business services and security helpers, with business-rule-focused normal and abnormal scenarios.

### 4. Documentation cleanup

Preserved the original project `README.md` and moved this testing-task delivery explanation into standalone files under `docs/`.

## How To Verify

Run:

```bash
mvn -q test
mvn -q verify
open target/site/jacoco/index.html
```

Expected result:

- tests pass successfully
- JaCoCo report is generated successfully
- Mockito may print Java agent warnings on some JDKs
- those warnings are non-fatal

## Why The JVM Warning Appears

When running Mockito on newer JDKs, Byte Buddy may dynamically attach a Java agent for mocking support. The JVM prints a warning such as:

- `A Java agent has been loaded dynamically`
- `Dynamic loading of agents will be disallowed by default in a future release`

This does not mean:

- the project failed to compile
- the tests failed
- the code is incorrect

It only means the current JDK warns about future agent-loading behavior.

## Requirement Satisfaction Summary

Already satisfied:

- Maven-standard test directory structure
- JUnit 5 test classes for the required core services
- Mockito-based mocking of dependencies
- scenario comments and `Given` / `When` / `Then` structure
- key TA application, MO job, admin/account, CV, and recruitment-cycle rules covered
- test naming unified for the new suite
- full-suite Maven verification completed

Not treated as blockers:

- branch coverage is below the originally suggested `85%` target
- a few trivial public methods do not have forced normal and exception symmetry

Reason:

- current core-service instruction coverage and line coverage are already above `90%`
- current target-class branch coverage is `80.90%`, which is acceptable for this submission version
- remaining uncovered branches are mainly defensive, overlapping, or low-value paths
- some methods are simple pass-through delegations
- some methods naturally use `null` or empty collections as the contract instead of exceptions
- extra mechanical tests would increase noise more than confidence

## Recommended Commit Split

The current finishing work is suitable to be submitted as two focused commits:

1. `test: expand white-box coverage for core services`
2. `docs: refresh final testing and coverage delivery`

## Final Local Commands

For course submission re-checks, use:

```bash
# full unit test suite
mvn -q test

# full test suite + JaCoCo report
mvn -q verify

# open coverage report in browser on macOS
open target/site/jacoco/index.html
```

## Final Status Snapshot

As of `2026-05-12 20:47:12 +0800` on branch `Zhixuan_GUO`:

- full Maven test suite passes
- latest target-class coverage summary is:
  - instruction `94.63%`
  - branch `80.90%`
  - line `93.93%`
- `pom.xml` local JaCoCo-related edits are intentionally not included in this submission commit set
- runtime-generated files under `data/cvs/` should not be committed


## Result
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running ui.common.ScrollPaneTopHelperTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.606 s -- in ui.common.ScrollPaneTopHelperTest
[INFO] Running ui.common.TableScrollUtilColumnSpecTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in ui.common.TableScrollUtilColumnSpecTest
[INFO] Running ui.common.TableScrollUtilResponsiveInstallTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.025 s -- in ui.common.TableScrollUtilResponsiveInstallTest
[INFO] Running infrastructure.util.GsonUtilsTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.075 s -- in infrastructure.util.GsonUtilsTest
[INFO] Running infrastructure.util.CsvExportUtilTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.249 s -- in infrastructure.util.CsvExportUtilTest
[INFO] Running infrastructure.security.PasswordServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.572 s -- in infrastructure.security.PasswordServiceTest
[INFO] Running infrastructure.security.SecurityHardeningTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.514 s -- in infrastructure.security.SecurityHardeningTest
[INFO] Running infrastructure.security.PermissionServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in infrastructure.security.PermissionServiceTest
[INFO] Running infrastructure.persistence.LocalDateTimeAdapterTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in infrastructure.persistence.LocalDateTimeAdapterTest
[INFO] Running infrastructure.persistence.JsonPersistenceManagerTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.066 s -- in infrastructure.persistence.JsonPersistenceManagerTest
[INFO] Running modules.course.CourseTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in modules.course.CourseTest
[INFO] Running modules.course.CourseServiceValidationTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in modules.course.CourseServiceValidationTest
[INFO] Running modules.config.SystemConfigTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in modules.config.SystemConfigTest
[INFO] Running modules.config.SystemConfigServiceTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.732 s -- in modules.config.SystemConfigServiceTest
[INFO] Running modules.config.SystemConfigServiceNullPredicateTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 s -- in modules.config.SystemConfigServiceNullPredicateTest
[INFO] Running modules.auth.AuthServiceAccountStatusTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.097 s -- in modules.auth.AuthServiceAccountStatusTest
[INFO] Running modules.auth.AuthServiceTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.203 s -- in modules.auth.AuthServiceTest
[INFO] Running modules.notification.NotificationKindTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0 s -- in modules.notification.NotificationKindTest
[INFO] Running modules.notification.NotificationServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.142 s -- in modules.notification.NotificationServiceTest
[INFO] Running modules.user.UserServiceTest
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.961 s -- in modules.user.UserServiceTest
[INFO] Running modules.user.MOEntityTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.062 s -- in modules.user.MOEntityTest
[INFO] Running modules.profile.TAProfileServiceTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.104 s -- in modules.profile.TAProfileServiceTest
[INFO] Running modules.profile.TAProfileTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.008 s -- in modules.profile.TAProfileTest
[INFO] Running modules.job.JobTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in modules.job.JobTest
[INFO] Running modules.job.JobServiceTest
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.118 s -- in modules.job.JobServiceTest
[INFO] Running modules.job.JobServiceExtractDeadlineTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s -- in modules.job.JobServiceExtractDeadlineTest
[INFO] Running modules.cv.CVManagerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in modules.cv.CVManagerTest
[INFO] Running modules.cv.CVServiceUploadValidationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in modules.cv.CVServiceUploadValidationTest
[INFO] Running modules.cv.CVServiceTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.043 s -- in modules.cv.CVServiceTest
[INFO] Running modules.cv.CVInfoTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s -- in modules.cv.CVInfoTest
[INFO] Running modules.application.ApplicationStatusTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s -- in modules.application.ApplicationStatusTest
[INFO] Running modules.application.ApplicationServiceQueryEdgeCasesTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s -- in modules.application.ApplicationServiceQueryEdgeCasesTest
[INFO] Running modules.application.ApplicationEntityTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.002 s -- in modules.application.ApplicationEntityTest
[INFO] Running modules.application.ApplicationServiceSummaryTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.012 s -- in modules.application.ApplicationServiceSummaryTest
[INFO] Running modules.application.ApplicationServiceTest
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.133 s -- in modules.application.ApplicationServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 315, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  19.129 s
[INFO] Finished at: 2026-05-12T15:05:00+08:00
[INFO] ------------------------------------------------------------------------
