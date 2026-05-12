package modules.user;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UserService} admin helpers and account lifecycle methods.
 * Uses {@link UserService#newInstanceForTesting()} and unique emails (same pattern as
 * {@link infrastructure.security.SecurityHardeningTest}) so behaviour is observable via persisted data.
 *
 * @version 1.1
 * @contributor Jiaze Wang
 * @update
 * - Updated strict admin tests for the dual seeded admin access policy
 * - Added regression coverage to prevent approved admin emails with non-admin roles from being promoted
 */
class UserServiceTest {

    @Test
    void isStrictAdmin_approvedActiveSeededAdmins_returnsTrue() {
        UserService service = UserService.newInstanceForTesting();

        Admin qmulAdmin = new Admin(" ADMIN@QMUL.AC.UK ", "AdminPass1");
        Admin buptAdmin = new Admin("Admin@BUPT.edu.cn", "AdminPass1");

        assertTrue(service.isStrictAdmin(qmulAdmin));
        assertTrue(service.isStrictAdmin(buptAdmin));
    }

    @Test
    void isStrictAdmin_unapprovedAdminOrWrongRoleOrInactive_returnsFalse() {
        UserService service = UserService.newInstanceForTesting();
        assertFalse(service.isStrictAdmin(null));

        Admin unapprovedAdmin = new Admin("admin.strict." + UUID.randomUUID() + "@qmul.ac.uk", "AdminPass1");
        assertFalse(service.isStrictAdmin(unapprovedAdmin));

        TA taUsingApprovedEmail = new TA("admin@qmul.ac.uk", "TaPass123");
        assertFalse(service.isStrictAdmin(taUsingApprovedEmail));

        MO moUsingApprovedEmail = new MO("admin@bupt.edu.cn", "MoPass123");
        assertFalse(service.isStrictAdmin(moUsingApprovedEmail));

        Admin pending = new Admin("admin@qmul.ac.uk", "AdminPass1");
        pending.setStatus(AccountStatus.PENDING);
        assertFalse(service.isStrictAdmin(pending));

        Admin disabled = new Admin("admin@bupt.edu.cn", "AdminPass1");
        disabled.setStatus(AccountStatus.DISABLED);
        assertFalse(service.isStrictAdmin(disabled));
    }

    @Test
    void ensureSeededAdminAccounts_doesNotPromoteApprovedEmailWithNonAdminRole() throws Exception {
        UserService service = UserService.newInstanceForTesting();
        Map<String, User> usersByEmail = getUsersByEmailMap(service);

        TA taUsingApprovedEmail = new TA("admin@qmul.ac.uk", "TaPass123");
        taUsingApprovedEmail.setUserId(999001L);
        taUsingApprovedEmail.setStatus(AccountStatus.ACTIVE);
        usersByEmail.put("admin@qmul.ac.uk", taUsingApprovedEmail);

        invokeEnsureSeededAdminAccounts(service);

        User storedUser = usersByEmail.get("admin@qmul.ac.uk");
        assertSame(taUsingApprovedEmail, storedUser);
        assertEquals(UserRole.TA, storedUser.getRole());
        assertFalse(service.isStrictAdmin(storedUser));
    }

    @SuppressWarnings("unchecked")
    private Map<String, User> getUsersByEmailMap(UserService service) throws Exception {
        Field field = UserService.class.getDeclaredField("usersByEmail");
        field.setAccessible(true);
        return (Map<String, User>) field.get(service);
    }

    private void invokeEnsureSeededAdminAccounts(UserService service) throws Exception {
        Method method = UserService.class.getDeclaredMethod("ensureSeededAdminAccounts");
        method.setAccessible(true);
        method.invoke(service);
    }

    @Test
    void approveMoAccount_setsMoActive() {
        UserService service = UserService.newInstanceForTesting();
        String email = "mo.approve." + UUID.randomUUID() + "@qmul.ac.uk";
        service.register(email, "MoPass123456", UserRole.MO);
        User mo = service.findByEmail(email);
        assertNotNull(mo);
        mo.setStatus(AccountStatus.PENDING);
        service.saveUser(mo);

        service.approveMoAccount(email);

        assertEquals(AccountStatus.ACTIVE, service.findByEmail(email).getStatus());
    }

    @Test
    void approveMoAccount_notMo_throws() {
        UserService service = UserService.newInstanceForTesting();
        String email = "ta.notmo." + UUID.randomUUID() + "@qmul.ac.uk";
        service.register(email, "TaPass123456", UserRole.TA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.approveMoAccount(email));
        assertTrue(ex.getMessage().contains("MO"));
    }

    @Test
    void approveMoAccount_unknownEmail_throws() {
        UserService service = UserService.newInstanceForTesting();
        assertThrows(IllegalArgumentException.class,
                () -> service.approveMoAccount("missing." + UUID.randomUUID() + "@qmul.ac.uk"));
    }

    @Test
    void updateAccountStatus_unknownEmail_throws() {
        UserService service = UserService.newInstanceForTesting();
        assertThrows(IllegalArgumentException.class,
                () -> service.updateAccountStatus("ghost." + UUID.randomUUID() + "@qmul.ac.uk",
                        AccountStatus.DISABLED));
    }

    @Test
    void disableAccount_setsDisabled() {
        UserService service = UserService.newInstanceForTesting();
        String email = "user.disable." + UUID.randomUUID() + "@qmul.ac.uk";
        service.register(email, "TaPass123456", UserRole.TA);

        service.disableAccount(email);

        assertEquals(AccountStatus.DISABLED, service.findByEmail(email).getStatus());
    }

    @Test
    void findById_and_getUserById_afterRegister_match() {
        UserService service = UserService.newInstanceForTesting();
        String email = "ta.find." + UUID.randomUUID() + "@qmul.ac.uk";
        User registered = service.register(email, "TaPass123456", UserRole.TA);
        Long id = registered.getUserId();
        assertNotNull(id);

        assertEquals(registered.getEmail(), service.findById(id).getEmail());
        assertEquals(registered.getEmail(), service.getUserById(id).getEmail());
    }

    @Test
    void findById_nullId_returnsNull() {
        assertNull(UserService.newInstanceForTesting().findById(null));
    }

    @Test
    void getUserById_null_returnsNull() {
        assertNull(UserService.newInstanceForTesting().getUserById(null));
    }

    @Test
    void emailExists_afterRegister_true() {
        UserService service = UserService.newInstanceForTesting();
        String email = "exists." + UUID.randomUUID() + "@qmul.ac.uk";
        assertFalse(service.emailExists(email));
        service.register(email, "TaPass123456", UserRole.TA);
        assertTrue(service.emailExists(email));
    }

    @Test
    void moImportResult_storesCountsAndErrors() {
        List<String> errors = List.of("e1");
        UserService.MOImportResult r = new UserService.MOImportResult(2, 1, errors);
        assertEquals(2, r.successCount);
        assertEquals(1, r.failCount);
        assertSame(errors, r.errors);
    }
}
