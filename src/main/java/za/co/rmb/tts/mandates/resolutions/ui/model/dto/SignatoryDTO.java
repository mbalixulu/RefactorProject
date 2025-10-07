package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class SignatoryDTO {
  private String fullName;
  private String idNumber;
  private String instructions;
  private String capacity;
  private String groupCategory;

  //For view convenience
  private String accountNumber;
  private String accountName;
}