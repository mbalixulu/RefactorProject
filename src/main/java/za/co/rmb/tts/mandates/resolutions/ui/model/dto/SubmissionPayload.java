package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * UI-side DTO that mirrors the backend MandateResolutionSubmissionDTO
 * so RestTemplate serializes JSON exactly as backend expects.
 */
@Data
public class SubmissionPayload {
  private Company company;
  private Request request;
  private Waiver waiver;
  private List<Account> accounts;
  private List<Authority> authorities;

  @Data
  public static class Company {
    private String registrationNumber;
    private String name;
    private String address;
    private String creator;
    private String updator;
  }

  @Data
  public static class Request {
    private Long companyId;
    private Integer sla;
    private String processId;
    private String assignedUser;
    private String type;          // Mandates | Resolutions | Both
    private String status;        // Draft | In Progress | Completed | On Hold
    private String subStatus;     // e.g. Submitted
    private String creator;
    private String updator;
  }

  @Data
  public static class Waiver {
    private Long companyId;
    private String ucn;
    private String permittedTools;
    private LocalDateTime lastFetched;
    private String creator;
    private String updator;
  }

  @Data
  public static class Account {
    private Long companyId;
    private String accountName;
    private String accountNumber;
    private Boolean isActive = Boolean.TRUE;
    private String creator;
    private String updator;
    private List<Signatory> signatories;
  }

  @Data
  public static class Authority {
    private Long companyId;
    private String firstname;
    private String surname;
    private String designation;
    private Boolean isActive = Boolean.TRUE;
    private String creator;
    private String updator;
    private String instructions;
  }

  @Data
  public static class Signatory {
    private String fullName;
    private String idNumber;
    private String instructions;      // Add | Remove
    private LocalDateTime instructionsDate;
    private String capacity;
    private String groupCategory;
    private Boolean isActive = Boolean.TRUE;
    private String creator;
    private String updator;
  }
}