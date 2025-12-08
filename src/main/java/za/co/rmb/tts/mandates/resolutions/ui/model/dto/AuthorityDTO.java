package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
@XmlRootElement(name = "authority")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthorityDTO {
  private Long authorityId;
  private Long companyId;
  private String firstname;
  private String surname;
  private String designation;
  private Boolean isActive;
  private LocalDateTime updated;
  private String updator;
  private LocalDateTime created;
  private String creator;
  private String instructions;
}