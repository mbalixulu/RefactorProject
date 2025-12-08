package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class AccountResponseDTO {

  private Long accountId;
  private Long companyId;
  private String accountName;
  private String accountNumber;
  private Boolean isActive;
  private String creator;
  private String updator;
  private LocalDateTime created;
  private LocalDateTime updated;
  private List<Signatory> signatories;

  @Data
  public static class Signatory {
    private Long signatoryId;
    private Long accountId;
    private String fullName;
    private String idNumber;
    private String instructions;
    private LocalDateTime instructionsDate;
    private Boolean signatoryConfirmCheck;
    private String capacity;
    private String groupCategory;
    private Boolean isActive;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
  }
}
