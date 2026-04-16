package ta.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for TAProfile entity validation and completion logic.
 * No file I/O — only tests in-memory object behaviour.
 */
class TAProfileTest {

    private TAProfile profile;

    /** Builds a fully filled-out profile so individual tests can remove one field at a time. */
    @BeforeEach
    void setUp() {
        profile = new TAProfile(1L, "student@qmul.ac.uk");
        profile.setStudentId("231224413");
        profile.setSurname("Guo");
        profile.setForename("Zhixuan");
        profile.setChineseName("郭智轩");
        profile.setPhone("13800138000");
        profile.setGender(TAProfile.Gender.MALE);
        profile.setSchool("BUPT International School");
        profile.setSupervisor("Prof. Smith");
        profile.setStudentType(TAProfile.StudentType.MASTER);
        profile.setCurrentYear(TAProfile.Year.YEAR_1);
        profile.setCampus(TAProfile.Campus.XITUCHENG);
    }

    // ── isEmailValid ──────────────────────────────────────────────────────────

    @Test
    void validEmail_isEmailValid() {
        assertTrue(profile.isEmailValid());
    }

    @Test
    void nullEmail_isNotEmailValid() {
        profile.setEmail(null);
        assertFalse(profile.isEmailValid());
    }

    @Test
    void blankEmail_isNotEmailValid() {
        profile.setEmail("   ");
        assertFalse(profile.isEmailValid());
    }

    @Test
    void emailWithoutAtSign_isNotEmailValid() {
        profile.setEmail("nodomain");
        assertFalse(profile.isEmailValid());
    }

    // ── isPhoneValid ──────────────────────────────────────────────────────────

    @Test
    void validChinesePhone_isPhoneValid() {
        profile.setPhone("13800138000");
        assertTrue(profile.isPhoneValid());
    }

    @Test
    void validShortInternationalPhone_isPhoneValid() {
        profile.setPhone("12345678");
        assertTrue(profile.isPhoneValid());
    }

    @Test
    void tooShortPhone_isNotPhoneValid() {
        profile.setPhone("1234567");
        assertFalse(profile.isPhoneValid());
    }

    @Test
    void nullPhone_isNotPhoneValid() {
        profile.setPhone(null);
        assertFalse(profile.isPhoneValid());
    }

    @Test
    void lettersInPhone_isNotPhoneValid() {
        profile.setPhone("abc12345");
        assertFalse(profile.isPhoneValid());
    }

    // ── saveProfile / isProfileCompleted ──────────────────────────────────────

    @Test
    void fullyFilledProfile_isComplete_afterSaveProfile() {
        profile.saveProfile();
        assertTrue(profile.isProfileCompleted());
    }

    @Test
    void profileWithMissingSurname_isNotComplete() {
        profile.setSurname(null);
        profile.saveProfile();
        assertFalse(profile.isProfileCompleted());
    }

    @Test
    void profileWithMissingGender_isNotComplete() {
        profile.setGender(null);
        profile.saveProfile();
        assertFalse(profile.isProfileCompleted());
    }

    @Test
    void profileWithMissingSupervisor_isNotComplete() {
        profile.setSupervisor("");
        profile.saveProfile();
        assertFalse(profile.isProfileCompleted());
    }

    // ── markAsEdited resets profileCompleted ──────────────────────────────────

    @Test
    void editingField_resetProfileCompleted() {
        profile.saveProfile();
        assertTrue(profile.isProfileCompleted(), "Should be complete before edit");
        profile.setSurname("New");
        assertFalse(profile.isProfileCompleted(), "Any setter should reset completed flag");
    }

    // ── getCompletionPercentage ───────────────────────────────────────────────

    @Test
    void emptyProfile_lowCompletion() {
        // Default constructor pre-sets campus = XITUCHENG, so at least 1/13 fields counts.
        TAProfile empty = new TAProfile(2L, null);
        assertTrue(empty.getCompletionPercentage() < 20,
                "Nearly-empty profile should have < 20% completion");
    }

    @Test
    void fullyFilledProfile_100Completion() {
        profile.setPreviousExperience("Lab demonstrator for EBU6304");
        assertEquals(100, profile.getCompletionPercentage());
    }

    @Test
    void profileWithoutOptionals_highCompletion() {
        // Chinese name and previous experience are optional fields counted in the 13 total
        int pct = profile.getCompletionPercentage();
        assertTrue(pct >= 84, "Should be at least 84% (11/13 fields) with all required fields set");
    }

    // ── getMissingFields ──────────────────────────────────────────────────────

    @Test
    void noMissingFields_whenAllSet() {
        profile.setChineseName("郭智轩");
        assertTrue(profile.getMissingFields().isEmpty());
    }

    @Test
    void missingSurname_appearsInMissingFields() {
        profile.setSurname(null);
        assertTrue(profile.getMissingFields().contains("Surname"));
    }

    @Test
    void missingEmail_appearsInMissingFields() {
        profile.setEmail(null);
        assertTrue(profile.getMissingFields().contains("Email (QMplus account)"));
    }

    // ── getFullName ───────────────────────────────────────────────────────────

    @Test
    void fullName_whenBothNamesSet() {
        assertEquals("Guo Zhixuan", profile.getFullName());
    }

    @Test
    void fullName_fallsBackToChineseName() {
        TAProfile p = new TAProfile(3L, "x@qmul.ac.uk");
        p.setChineseName("郭智轩");
        assertEquals("郭智轩", p.getFullName());
    }
}
