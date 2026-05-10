package infrastructure.util;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for shared Gson configuration (including {@link java.time.LocalDateTime} handling).
 */
class GsonUtilsTest {

    @Test
    void isValidJson_acceptsObjectAndArray() {
        assertTrue(GsonUtils.isValidJson("{}"));
        assertTrue(GsonUtils.isValidJson("[1,2]"));
        assertTrue(GsonUtils.isValidJson("\"hello\""));
    }

    @Test
    void isValidJson_rejectsNullBlankInvalid() {
        assertFalse(GsonUtils.isValidJson(null));
        assertFalse(GsonUtils.isValidJson("   "));
        assertFalse(GsonUtils.isValidJson("{"));
    }

    @Test
    void roundTrip_localDateTime_viaJsonProperty() {
        LocalDateTime t = LocalDateTime.of(2026, 5, 10, 14, 30, 0);
        String json = GsonUtils.toJson(Map.of("at", t));
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = GsonUtils.fromJson(json, Map.class);
        assertEquals("2026-05-10T14:30:00", parsed.get("at").toString());
    }

    @Test
    void fromJson_simpleBean() {
        String json = "{\"value\":42}";
        Sample bean = GsonUtils.fromJson(json, Sample.class);
        assertEquals(42, bean.value);
    }

    @Test
    void fromJson_withType_deserializesList() {
        String json = "[\"a\",\"b\"]";
        List<String> list = GsonUtils.fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        assertEquals(List.of("a", "b"), list);
    }

    static class Sample {
        int value;
    }
}
