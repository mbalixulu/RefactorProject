package za.co.rmb.tts.mandates.resolutions.ui.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Service class providing general utility methods for string manipulation,
 * normalization, and formatting operations used across the application.
 */
@Service
public class UtilityService {

  /**
   * Returns the input string if not null, otherwise returns empty string.
   */
  public String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  /**
   * Cleans up sub-status strings by removing specific prefixes and extra spaces.
   */
  public String cleanSubStatus(String sub) {
    if (sub == null || sub.isBlank()) {
      return "";
    }
    String s = sub.trim().replaceFirst("^(Step \\d+:?\\s*|Step \\d+)", "").trim();
    return s.isEmpty() ? sub.trim() : s;
  }

  /**
   * Deduplicates comma-separated values while preserving order.
   */
  public String dedupeComma(String s) {
    String t = nullToEmpty(s);
    if (t.isEmpty()) {
      return t;
    }
    String[] parts = t.split("\\s*,\\s*");
    if (parts.length <= 1) {
      return t;
    }
    Set<String> set = new LinkedHashSet<>();
    for (String p : parts) {
      if (!p.isBlank()) {
        set.add(p);
      }
    }
    return String.join(", ", set);
  }

  /**
   * Normalizes registration numbers by removing non-alphanumeric characters
   * and converting to uppercase.
   */
  public String normalizeRegistrationNumber(String s) {
    String t = nullToEmpty(s);
    return t.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  /**
   * Escapes XML special characters.
   */
  public String escapeXml(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  /**
   * Checks if all provided strings are blank or null.
   */
  public boolean allBlank(String... ss) {
    if (ss == null || ss.length == 0) {
      return true;
    }
    for (String s : ss) {
      if (s != null && !s.isBlank()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if a single string is blank.
   */
  public boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  /**
   * Converts a string to an optional integer, returning null if parsing fails.
   */
  public Integer parseIntegerOrNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Returns the first non-blank string from an array of values, or null.
   */
  public String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  /**
   * Checks if a string contains only digits.
   */
  public boolean isAllDigits(String s) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isDigit(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Truncates a string to a maximum length.
   */
  public String truncate(String s, int maxLength) {
    if (s == null || s.length() <= maxLength) {
      return s;
    }
    return s.substring(0, maxLength);
  }

  /**
   * Formats a LocalDateTime to ISO format string.
   */
  public String formatIsoDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "";
    }
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  /**
   * Normalizes instruction values to backend-valid ones ("Add" or "Remove").
   */
  public String normalizeInstruction(String raw) {
    if (raw == null || raw.trim().isEmpty()) {
      return "Add";
    }
    String t = raw.trim().toLowerCase();
    return switch (t) {
      case "add", "a", "+", "new", "create" -> "Add";
      case "remove", "r", "-", "delete", "del" -> "Remove";
      default -> "Add";
    };
  }

  /**
   * Converts request type codes to human-readable strings.
   */
  public String mapRequestType(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String s = raw.trim();
    if ("1".equals(s)) {
      return "Mandates";
    }
    if ("2".equals(s)) {
      return "Resolutions";
    }
    if ("3".equals(s)) {
      return "Both";
    }
    return null;
  }

  /**
   * Converts human-readable request types to codes.
   */
  public String mapTypeToCode(String type) {
    if (type == null) {
      return null;
    }
    String t = type.trim().toLowerCase();
    if (t.contains("mandate") && t.contains("resolution")) {
      return "3";
    }
    if (t.contains("resolution")) {
      return "2";
    }
    if (t.contains("mandate")) {
      return "1";
    }
    return null;
  }
}
