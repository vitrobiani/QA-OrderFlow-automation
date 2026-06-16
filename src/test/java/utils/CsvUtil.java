package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used by Test174 (CSV export — hot) to find the freshly-downloaded order-history CSV
 * inside ./downloads and confirm it contains the expected order line.
 */
public class CsvUtil {

    /**
     * Wait up to `timeoutMs` for a CSV file to appear in `dir` whose name contains `nameContains`.
     * Returns the file or null on timeout. Polls every 500 ms.
     */
    public static File waitForCsv(String dir, String nameContains, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        File folder = new File(dir);
        while (System.currentTimeMillis() < deadline) {
            File[] files = folder.listFiles((d, n) ->
                    n.toLowerCase().endsWith(".csv") &&
                    n.toLowerCase().contains(nameContains.toLowerCase()));
            if (files != null && files.length > 0) {
                return newest(files);
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    private static File newest(File[] files) {
        File pick = files[0];
        for (File f : files) if (f.lastModified() > pick.lastModified()) pick = f;
        return pick;
    }

    /** Read all rows (split on comma; good enough for our exporter's plain CSV). */
    public static List<List<String>> readRows(File csv) {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(Arrays.asList(line.split(",")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV " + csv, e);
        }
        return rows;
    }

    /** True if any row contains the given cell value (trimmed). */
    public static boolean containsCell(List<List<String>> rows, String value) {
        for (List<String> row : rows) {
            for (String cell : row) {
                if (cell.trim().equalsIgnoreCase(value.trim())) return true;
            }
        }
        return false;
    }
}
