package infrastructure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportUtilTest {

    @Test
    void exportRows_writesUtf8BomAndEscapedCells(@TempDir Path temp) throws Exception {
        Path out = temp.resolve("t.csv");
        CsvExportUtil.exportRows(out, new String[]{"Name", "Note"}, List.of(
                new String[]{"Alice", "Say \"hi\""},
                new String[]{"Bob", "Line1"}
        ));
        byte[] raw = Files.readAllBytes(out);
        assertEquals((byte) 0xEF, raw[0]);
        assertEquals((byte) 0xBB, raw[1]);
        assertEquals((byte) 0xBF, raw[2]);
        String text = Files.readString(out);
        assertTrue(text.contains("Alice"));
        assertTrue(text.contains("Bob"));
    }

    @Test
    void exportRows_emptyDataRows_stillWritesHeader(@TempDir Path temp) throws Exception {
        Path out = temp.resolve("empty.csv");
        CsvExportUtil.exportRows(out, new String[]{"Only"}, Collections.emptyList());
        assertTrue(Files.readString(out).contains("Only"));
    }
}
