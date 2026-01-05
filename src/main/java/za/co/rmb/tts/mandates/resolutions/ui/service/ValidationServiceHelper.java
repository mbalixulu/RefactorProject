package za.co.rmb.tts.mandates.resolutions.ui.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;

/**
 * Service class providing validation logic for various business rules
 * and data validation operations beyond basic ID number validation.
 */
@Service
public class ValidationServiceHelper {

  /**
   * Validates date range ensuring fromDate is before toDate.
   */
  public boolean validateDateRange(String fromDateStr, String toDateStr) {
    if (fromDateStr == null || fromDateStr.isBlank() || toDateStr == null || toDateStr.isBlank()) {
      return false;
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
      LocalDate fromDate = LocalDate.parse(fromDateStr, formatter);
      LocalDate toDate = LocalDate.parse(toDateStr, formatter);
      return fromDate.isBefore(toDate);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks if a user has admin approval permission based on role and status.
   */
  public boolean hasAdminApprovalPermission(String status, String userRole) {
    if ("USER".equalsIgnoreCase(userRole)) {
      return !"Admin Approval Pending".equalsIgnoreCase(status);
    }
    return true;
  }

  /**
   * Validates if a signatory object has all required fields empty (is blank).
   */
  public boolean isEmptySignatory(RequestDTO.Signatory s) {
    if (s == null) {
      return true;
    }
    String fn = s.getFullName() == null ? "" : s.getFullName().trim();
    String id = s.getIdNumber() == null ? "" : s.getIdNumber().trim();
    return fn.isEmpty() && id.isEmpty();
  }

  /**
   * Validates if account number and name are provided.
   */
  public boolean validateAccountData(String accountName, String accountNumber) {
    return accountName != null && !accountName.isBlank()
        && accountNumber != null && !accountNumber.isBlank();
  }

  /**
   * Validates if director data has required fields.
   */
  public boolean validateDirectorData(String name, String surname, String designation) {
    return name != null && !name.isBlank()
        && surname != null && !surname.isBlank()
        && designation != null && !designation.isBlank();
  }

  /**
   * Checks if company registration number is valid format.
   */
  public boolean isValidRegistrationNumber(String registrationNumber) {
    if (registrationNumber == null || registrationNumber.isBlank()) {
      return false;
    }
    // Basic validation - can be enhanced with specific format rules
    String cleaned = registrationNumber.trim().replaceAll("[^A-Za-z0-9]", "");
    return cleaned.length() >= 5 && cleaned.length() <= 20;
  }

  /**
   * Validates if at least one waiver tool is selected.
   */
  public boolean hasAtLeastOneWaiverTool(String... tools) {
    if (tools == null || tools.length == 0) {
      return false;
    }
    for (String tool : tools) {
      if (tool != null && !tool.isBlank()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Validates if checkbox values are properly set.
   */
  public boolean areCheckboxesValid(String check1, String check2) {
    return "true".equalsIgnoreCase(check1) && "true".equalsIgnoreCase(check2);
  }

  /**
   * Checks if request type selection is valid.
   */
  public boolean isValidRequestType(String requestType) {
    if (requestType == null || requestType.isBlank()) {
      return false;
    }
    return !"Please select".equalsIgnoreCase(requestType.trim());
  }

  /**
   * Validates signatory instruction field.
   */
  public boolean isValidInstruction(String instruction) {
    if (instruction == null || instruction.isBlank()) {
      return false;
    }
    return !"Please select".equalsIgnoreCase(instruction.trim());
  }
}
