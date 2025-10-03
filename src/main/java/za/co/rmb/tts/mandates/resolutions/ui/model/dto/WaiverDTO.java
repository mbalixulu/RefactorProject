package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
@XmlRootElement(name = "waiver")
@XmlAccessorType(XmlAccessType.FIELD)
public class WaiverDTO implements Serializable {
  private Long waiverId;
  private Integer companyId;
  private String ucn;
  private String permittedTools; //Tool Name
  private LocalDateTime lastFetched;
  private LocalDateTime updated;
  private String updator;
  private LocalDateTime created;
  private String creator;
}
