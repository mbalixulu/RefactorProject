package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import lombok.Data;

import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestTableDTO;

@Data
@XmlRootElement(name = "requests")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestTableWrapper {
  @XmlElement(name = "request")
  private List<RequestTableDTO> request;
}