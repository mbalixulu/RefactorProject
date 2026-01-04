package za.co.rmb.tts.mandates.resolutions.ui.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Service for managing workflow state transitions and status mappings.
 * 
 * <p>This service handles the complex workflow logic for request processing,
 * including status transitions, hold/unhold operations, and validation of
 * allowed substatus values.</p>
 */
@Service
public class WorkflowManagementService {

  // DAO-LEGAL "Pending" subStatus values
  private static final String SS_WINDEED_VER = "Windeed Verification Pending";
  private static final String SS_HOGAN_VER = "Hogan Verification Pending";
  private static final String SS_HANIS_VER = "Hanis Verification Pending";
  private static final String SS_ADMIN_APPROVAL = "Admin Approval Pending";
  private static final String SS_HOGAN_UPD = "Hogan Update Pending";
  private static final String SS_DOCU_UPD = "Documentum Update Pending";
  private static final String SS_DONE = "Request Updated Successfully";
  private static final String SS_REJECTED = "Rejected";

  // DAO-LEGAL "On Hold" labels
  private static final String HOLD_WINDEED_VER = "Verification On Hold for Windeed";
  private static final String HOLD_HOGAN_VER = "Verification On Hold for Hogan";
  private static final String HOLD_HANIS_VER = "Verification On Hold for Hanis";
  private static final String HOLD_ADMIN = "Admin Approval On Hold";
  private static final String HOLD_HOGAN_UPD = "Update on Hold for Hogan";
  private static final String HOLD_DOCU_UPD = "Update on Hold for Documentum";

  private static final Set<String> ALLOWED_SUBSTATUSES = new HashSet<>(
      Arrays.asList(
          "Hogan Verification Pending",
          "Windeed Verification Pending",
          "Hanis Verification Pending",
          "Admin Approval Pending",
          "Hogan Update Pending",
          "Documentum Update Pending"
      )
  );

  /**
   * Picks the very first subStatus right after creation.
   * 
   * <p>This can be varied by type ("Mandates"|"Resolutions"|"Both") if needed.</p>
   *
   * @param type the request type
   * @return the initial substatus for the given type
   */
  public String initialSubStatusForType(String type) {
    return "Hogan Verification Pending";
  }

  /**
   * Maps any legacy/typo variants to DAO-legal values BEFORE branching or PUT.
   * 
   * <p>This method normalizes substatus values to ensure compatibility with
   * backend validation rules.</p>
   *
   * @param s the substatus string to canonicalize
   * @return the canonical substatus string
   */
  public String canonical(String s) {
    if (s == null) {
      return "";
    }
    String t = s.trim();
    if (t.isEmpty()) {
      return t;
    }

    // Legacy/typo fixes
    if (t.equalsIgnoreCase("Hannis Verification Pending")) {
      return SS_HANIS_VER;
    }
    if (t.equalsIgnoreCase("Admin Verification Pending")) {
      return SS_ADMIN_APPROVAL;
    }
    if (t.equalsIgnoreCase("Submitted")) {
      return SS_HOGAN_VER;
    }
    if (t.equalsIgnoreCase("Completed") || t.equalsIgnoreCase("Request Completed")) {
      return SS_DONE;
    }

    // Normalize to exact DAO strings (Pending values)
    if (t.equalsIgnoreCase(SS_REJECTED)) {
      return SS_REJECTED;
    }
    if (t.equalsIgnoreCase(SS_HOGAN_VER)) {
      return SS_HOGAN_VER;
    }
    if (t.equalsIgnoreCase(SS_WINDEED_VER)) {
      return SS_WINDEED_VER;
    }
    if (t.equalsIgnoreCase(SS_HANIS_VER)) {
      return SS_HANIS_VER;
    }
    if (t.equalsIgnoreCase(SS_ADMIN_APPROVAL)) {
      return SS_ADMIN_APPROVAL;
    }
    if (t.equalsIgnoreCase(SS_HOGAN_UPD)) {
      return SS_HOGAN_UPD;
    }
    if (t.equalsIgnoreCase(SS_DOCU_UPD)) {
      return SS_DOCU_UPD;
    }
    if (t.equalsIgnoreCase(SS_DONE)) {
      return SS_DONE;
    }

    // Pass-through if caller accidentally supplies a DAO-legal HOLD label
    if (t.equalsIgnoreCase(HOLD_WINDEED_VER)
        || t.equalsIgnoreCase(HOLD_HOGAN_VER)
        || t.equalsIgnoreCase(HOLD_HANIS_VER)
        || t.equalsIgnoreCase(HOLD_ADMIN)
        || t.equalsIgnoreCase(HOLD_HOGAN_UPD)
        || t.equalsIgnoreCase(HOLD_DOCU_UPD)) {
      return t;
    }

    // Unknown: return as-is
    return t;
  }

  /**
   * Given the current PENDING subStatus, return the exact On-Hold label accepted by the DAO.
   * 
   * <p>If current is unknown, defaults to "Verification On Hold for Hogan".</p>
   *
   * @param currentSubStatus the current substatus
   * @return the corresponding hold label
   */
  public String toHoldLabel(String currentSubStatus) {
    String c = canonical(currentSubStatus);
    if (c == null || c.isBlank()) {
      return HOLD_HOGAN_VER;
    }
    switch (c) {
      case SS_WINDEED_VER:
        return HOLD_WINDEED_VER;
      case SS_HOGAN_VER:
        return HOLD_HOGAN_VER;
      case SS_HANIS_VER:
        return HOLD_HANIS_VER;
      case SS_ADMIN_APPROVAL:
        return HOLD_ADMIN;
      case SS_HOGAN_UPD:
        return HOLD_HOGAN_UPD;
      case SS_DOCU_UPD:
        return HOLD_DOCU_UPD;
      case SS_DONE:
      case SS_REJECTED:
      default:
        return HOLD_ADMIN;
    }
  }

  /**
   * Inverse of toHoldLabel: derive the original PENDING subStatus to restore when unholding.
   * 
   * <p>If label is unexpected, falls back to "Hogan Verification Pending".</p>
   *
   * @param holdLabel the hold label
   * @return the corresponding pending substatus
   */
  public String fromHoldLabel(String holdLabel) {
    if (holdLabel == null) {
      return SS_HOGAN_VER;
    }
    String s = holdLabel.trim();

    return switch (s) {
      case HOLD_WINDEED_VER -> SS_WINDEED_VER;
      case HOLD_HOGAN_VER -> SS_HOGAN_VER;
      case HOLD_HANIS_VER -> SS_HANIS_VER;
      case HOLD_ADMIN -> SS_ADMIN_APPROVAL;
      case HOLD_HOGAN_UPD -> SS_HOGAN_UPD;
      case HOLD_DOCU_UPD -> SS_DOCU_UPD;
      default -> SS_HOGAN_VER;
    };
  }

  /**
   * Approve path state machine (exact order specified).
   * 
   * <p>This method implements the workflow progression:
   * Hogan -> Windeed -> Hanis -> Admin -> Hogan Update -> Documentum Update -> Done</p>
   *
   * @param current the current substatus
   * @param approve true if approving, false if rejecting
   * @return the next substatus in the workflow
   */
  public String nextSubStatus(String current, boolean approve) {
    String c = canonical(current);
    if (approve) {
      if (SS_HOGAN_VER.equals(c)) {
        return SS_WINDEED_VER;
      }
      if (SS_WINDEED_VER.equals(c)) {
        return SS_HANIS_VER;
      }
      if (SS_HANIS_VER.equals(c)) {
        return SS_ADMIN_APPROVAL;
      }
      if (SS_ADMIN_APPROVAL.equals(c)) {
        return SS_HOGAN_UPD;
      }
      if (SS_HOGAN_UPD.equals(c)) {
        return SS_DOCU_UPD;
      }
      if (SS_DOCU_UPD.equals(c)) {
        return SS_DONE;
      }
      if (SS_DONE.equals(c)) {
        return SS_DONE;
      }
      return c;
    } else {
      return SS_REJECTED;
    }
  }

  /**
   * Normalizes an instruction string to backend-valid values.
   * 
   * <p>Maps various input strings to either "Add" or "Remove".</p>
   *
   * @param raw the raw instruction string
   * @return normalized instruction ("Add" or "Remove")
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
   * Cleans up substatus strings for display.
   * 
   * <p>Removes prefixes and formats the substatus for UI presentation.</p>
   *
   * @param sub the substatus to clean
   * @return cleaned substatus string
   */
  public String cleanSubStatus(String sub) {
    if (sub == null || sub.trim().isEmpty()) {
      return "";
    }
    String s = sub.trim();
    // Remove common prefixes if needed for display
    if (s.startsWith("Step ")) {
      return s;
    }
    return s;
  }

  /**
   * Checks if a substatus is in the allowed list.
   *
   * @param substatus the substatus to check
   * @return true if allowed, false otherwise
   */
  public boolean isAllowedSubstatus(String substatus) {
    return ALLOWED_SUBSTATUSES.contains(substatus);
  }

  /**
   * Infers request type from a page code.
   *
   * @param pageCode the page code
   * @return inferred type ("Mandates", "Resolutions", "Both", or default)
   */
  public String inferTypeFromPage(String pageCode) {
    if (pageCode == null) {
      return "SEARCH_RESULTS";
    }
    String p = pageCode.trim().toUpperCase();
    if (p.contains("MANDATE") && p.contains("RESOLUTION")) {
      return "Both";
    }
    if (p.contains("MANDATE")) {
      return "Mandates";
    }
    if (p.contains("RESOLUTION")) {
      return "Resolutions";
    }
    return "SEARCH_RESULTS";
  }
}
