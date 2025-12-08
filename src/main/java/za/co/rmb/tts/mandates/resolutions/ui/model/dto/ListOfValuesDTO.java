package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ListOfValuesDTO {
  private Long lovId;
  private String type;
  private String subType;
  private String requestStatus;
  private String value;
  private Integer sortOrder;
  private Boolean isActive;
}