package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mirrors backend MandateResolutionSubmissionResultDTO exactly. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateResolutionSubmissionResultDTO {
  private Company company;
  private Request request;
  private Waiver waiver;
  private List<Account> accounts;
  private List<Authority> authorities;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Company {
    private Long companyId;
    private String registrationNumber;
    private String name;
    private String address;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Request {
    private Long requestId;
    private Long companyId;
    private Integer sla;
    private String processId;
    private String assignedUser;
    private String type;
    private String status;
    private String subStatus;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
    //ID exposed by backend entity getter
    private String requestIdForDisplay;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Waiver {
    private Long waiverId;
    private Long companyId;
    private String ucn;
    private String permittedTools;
    private LocalDateTime lastFetched;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Account {
    private Long accountId;
    private Long companyId;
    private String accountName;
    private String accountNumber;
    private Boolean isActive;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
    private java.util.List<Signatory> signatories;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Authority {
    private Long authorityId;
    private Long companyId;
    private String firstname;
    private String surname;
    private String designation;
    private Boolean isActive;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
    String instructions;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Signatory {
    private Long signatoryId;
    private Long accountId;
    private String fullName;
    private String idNumber;
    private String instructions;
    private LocalDateTime instructionsDate;
    private String capacity;
    private String groupCategory;
    private Boolean isActive;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
  }
}