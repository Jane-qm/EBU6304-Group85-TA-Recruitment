package infrastructure.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeAdapterTest {

    @Test
    void gson_roundTrip_localDateTime() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        LocalDateTime t = LocalDateTime.of(2026, 5, 10, 14, 30, 45);
        String json = gson.toJson(t);
        LocalDateTime parsed = gson.fromJson(json, LocalDateTime.class);
        assertEquals(t, parsed);
    }
}
