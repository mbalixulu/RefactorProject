package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import lombok.Data;

@Data
@XmlRootElement(name = "requestWrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestWrapper implements Serializable {

  @XmlElement(name = "request")
  private RequestDTO requestDTO;

}