package utils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Tiny json-simple wrapper — exactly the style taught in the data-driven lecture.
 * Use these to load `testdata/*.json` files (categories, orders, search queries).
 */
public class JsonData {

    /** Read a flat JSON array of strings (e.g. testdata/categories.json). */
    @SuppressWarnings("unchecked")
    public static List<String> readStringArray(String path) {
        List<String> out = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(new FileReader(path));
            JSONArray arr = (JSONArray) parsed;
            for (Object o : arr) {
                out.add((String) o);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read string array from " + path, e);
        }
        return out;
    }

    /** Read a JSON array of objects (e.g. testdata/orders.json). */
    public static List<JSONObject> readObjectArray(String path) {
        List<JSONObject> out = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(new FileReader(path));
            JSONArray arr = (JSONArray) parsed;
            for (Object o : arr) {
                out.add((JSONObject) o);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object array from " + path, e);
        }
        return out;
    }

    /** Read a single JSON object (e.g. testdata/search_queries.json). */
    public static JSONObject readObject(String path) {
        try {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(new FileReader(path));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object from " + path, e);
        }
    }
}
