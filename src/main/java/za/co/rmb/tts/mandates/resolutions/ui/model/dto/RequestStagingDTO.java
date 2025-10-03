package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestStagingDTO {
  private Long stagingId;

  //Company
  private String companyRegistrationNumber;
  private String companyName;
  private String companyAddress;

  //Request
  private Integer requestSla;
  private String requestType;       // Mandates|Resolutions|Both
  private String requestStatus;     // Draft|In Progress|Completed|On Hold
  private String requestSubStatus;

  //Waiver
  private String waiverUcn;
  private String waiverPermittedTools;
  private LocalDateTime waiverLastFetched;

  //Lists
  private List<AccountDraft> accounts;
  private List<AuthorityDraft> authorities;

  private String creator;
  private String updator;
  private java.time.LocalDateTime created;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AccountDraft {
    private String accountName;
    private String accountNumber;
    private Boolean isActive = Boolean.TRUE;
    private List<SignatoryDraft> signatories;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AuthorityDraft {
    private String firstname;
    private String surname;
    private String designation;
    private Boolean isActive = Boolean.TRUE;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class SignatoryDraft {
    private String fullName;
    private String idNumber;
    private String instructions;     // Add|Remove
    private LocalDateTime instructionsDate;
    private String capacity;
    private String groupCategory;
    private Boolean isActive = Boolean.TRUE;
  }
}