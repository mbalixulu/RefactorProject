package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class SubmissionViewDTO {
  private Company company;
  private Req request;
  private List<SignatoryDTO> signatories;

  @Data
  public static class Company {
    private Long companyId;
    private String name;
  }

  @Data
  public static class Req {
    private Long requestId;
    private String requestIdForDisplay;
    private Long companyId;
    private Integer sla;
    private String type;
    private String status;
    private String subStatus;
    private String creator;
    private String updator;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String processId;
    private String assignedUser;
  }
}