package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
@XmlRootElement(name = "company")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompanyDTO implements Serializable {
  private Long companyId;
  private String registrationNumber;
  private String name;
  private String address;
  private LocalDateTime updated;
  private String updator;
  private LocalDateTime created;
  private String creator;
}