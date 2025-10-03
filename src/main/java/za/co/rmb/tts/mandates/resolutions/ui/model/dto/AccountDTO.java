package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountDTO {
  private String accountName;
  private String accountNumber;

  @XmlElementWrapper(name = "signatories")
  @XmlElement(name = "signatory")
  private List<SignatoryDTO> signatories;
}