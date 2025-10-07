package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import lombok.Data;

@Data
public class ListOfValuesDTO {
  private Long lovId;
  private String type;
  private String subType;
  private String value;
  private Integer sortOrder;
  private Boolean isActive;
}