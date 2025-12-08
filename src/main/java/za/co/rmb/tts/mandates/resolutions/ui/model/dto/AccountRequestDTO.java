package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class AccountRequestDTO {

  private Long companyId;         // required
  private String accountName;
  private String accountNumber;
  private Boolean isActive = Boolean.TRUE;
  private String creator;
  private String updator;
  private List<Signatory> signatories;  // optional; if set on PUT, replaces existing

  @Data public static class Signatory {
    private String fullName;
    private String idNumber;
    private String instructions;             // Add|Remove
    private LocalDateTime instructionsDate;
    private Boolean signatoryConfirmCheck;
    private String capacity;
    private String groupCategory;
    private Boolean isActive = Boolean.TRUE;
    private String creator;
    private String updator;
  }
}
