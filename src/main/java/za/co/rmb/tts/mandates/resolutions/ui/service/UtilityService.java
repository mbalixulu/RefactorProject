package za.co.rmb.tts.mandates.resolutions.ui.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/**
 * Utility service providing common helper methods for string manipulation,
 * data transformation, and formatting operations.
 * 
 * <p>This service centralizes utility functions previously scattered across
 * the controller layer, improving code reusability and maintainability.</p>
 */
@Service
public class UtilityService {

  /**
   * Returns an empty string if the input is null, otherwise returns the trimmed string.
   *
   * @param s the input string
   * @return empty string if null, otherwise trimmed string
   */
  public String nz(String s) {
    return s == null ? "" : s.trim();
  }

  /**
   * Normalizes a registration number by removing non-alphanumeric characters
   * and converting to uppercase.
   *
   * @param s the registration number to normalize
   * @return normalized registration number
   */
  public String normReg(String s) {
    String t = nz(s);
    return t.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  /**
   * Escapes XML special characters in a string.
   *
   * @param s the string to escape
   * @return XML-escaped string
   */
  public String xmlEscape(String s) {
    if (s == null || s.isEmpty()) {
      return s;
    }
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  /**
   * Checks if a string contains only digits.
   *
   * @param s the string to check
   * @return true if the string contains only digits, false otherwise
   */
  public boolean isAllDigits(String s) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    for (char c : s.toCharArray()) {
      if (!Character.isDigit(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Truncates a string to a maximum length.
   *
   * @param s   the string to truncate
   * @param max the maximum length
   * @return truncated string
   */
  public String head(String s, int max) {
    if (s == null) {
      return "";
    }
    if (s.length() <= max) {
      return s;
    }
    return s.substring(0, max);
  }

  /**
   * Attempts to format a datetime string using ISO format.
   *
   * @param text   the datetime string to format
   * @param outFmt the output formatter
   * @return formatted datetime string or original text if parsing fails
   */
  public String tryFormatIso(String text, DateTimeFormatter outFmt) {
    if (text == null || text.isBlank()) {
      return text;
    }
    try {
      LocalDateTime ldt = LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      return ldt.format(outFmt);
    } catch (Exception e) {
      return text;
    }
  }

  /**
   * Formats epoch milliseconds to a datetime string.
   *
   * @param epoch  the epoch milliseconds
   * @param outFmt the output formatter
   * @return formatted datetime string
   */
  public String formatEpochMillis(long epoch, DateTimeFormatter outFmt) {
    LocalDateTime ldt = Instant.ofEpochMilli(epoch)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    return ldt.format(outFmt);
  }

  /**
   * Retrieves a parameter value from a map of parameter arrays.
   *
   * @param params the parameter map
   * @param key    the parameter key
   * @return the last non-blank value or null
   */
  public String getParam(Map<String, String[]> params, String key) {
    String[] arr = params.get(key);
    if (arr == null || arr.length == 0) {
      return null;
    }
    String last = null;
    for (String v : arr) {
      if (v != null && !v.isBlank()) {
        last = v;
      }
    }
    return last;
  }

  /**
   * Returns the first element of a string array.
   *
   * @param arr the array
   * @return first element or null
   */
  public String first(String[] arr) {
    return (arr == null || arr.length == 0) ? null : arr[0];
  }

  /**
   * Finds a "created" field in a JSON node (case-insensitive).
   *
   * @param node the JSON node
   * @return the created field value or null
   */
  public String findCreatedAnyCase(JsonNode node) {
    if (node == null || !node.isObject()) {
      return null;
    }
    var it = node.fields();
    while (it.hasNext()) {
      var entry = it.next();
      String key = entry.getKey();
      if (key.equalsIgnoreCase("created")) {
        JsonNode val = entry.getValue();
        return val.isTextual() ? val.asText() : val.toString();
      }
    }
    return null;
  }

  /**
   * Finds the first non-blank text value from specified field names in a JSON node.
   *
   * @param root       the root JSON node
   * @param fieldNames the field names to search
   * @return the first non-blank text value or null
   */
  public String firstNonBlankText(JsonNode root, String... fieldNames) {
    if (root == null || !root.isObject()) {
      return null;
    }
    for (String fn : fieldNames) {
      JsonNode n = root.get(fn);
      if (n != null && n.isTextual()) {
        String txt = n.asText().trim();
        if (!txt.isEmpty()) {
          return txt;
        }
      }
    }
    return null;
  }

  /**
   * Converts an object to pretty-printed JSON.
   *
   * @param o the object to convert
   * @return pretty-printed JSON string
   */
  public String toPrettyJson(Object o) {
    try {
      ObjectMapper om = new ObjectMapper();
      om.enable(SerializationFeature.INDENT_OUTPUT);
      return om.writeValueAsString(o);
    } catch (Exception e) {
      return "{}";
    }
  }

  /**
   * Dumps text content to a file for debugging purposes.
   *
   * @param baseName the base file name
   * @param ext      the file extension
   * @param content  the content to write
   * @return the path of the created file
   */
  public Path dumpText(String baseName, String ext, String content) {
    try {
      Path p = Paths.get("/tmp", baseName + ext);
      Files.write(p, content.getBytes(StandardCharsets.UTF_8));
      return p;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Deduplicates comma-separated values in a string.
   *
   * @return a function that removes duplicate values from comma-separated strings
   */
  public Function<String, String> getDedupeCommaFunction() {
    return s -> {
      String t = nz(s);
      if (t.isEmpty()) {
        return t;
      }
      String[] parts = t.split("\\s*,\\s*");
      if (parts.length <= 1) {
        return t;
      }
      java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
      for (String p : parts) {
        if (!p.isBlank()) {
          set.add(p);
        }
      }
      return String.join(", ", set);
    };
  }

  /**
   * Extracts instruction from a map or string object.
   *
   * @param a the object to extract from (Map or String)
   * @return the extracted instruction string
   */
  public String extractInstruction(Object a) {
    if (a == null) {
      return "";
    }
    if (a instanceof Map) {
      Map<?, ?> m = (Map<?, ?>) a;
      Object val = m.get("instruction");
      if (val == null) {
        val = m.get("instructions");
      }
      return val == null ? "" : String.valueOf(val).trim();
    }
    return a.toString().trim();
  }
}
