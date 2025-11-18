package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import lombok.Data;

import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestTableDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ApproveRejectErrorModel;

@Data
@XmlRootElement(name = "requests")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestTableWrapper {
  @XmlElement(name = "request")
  private List<RequestTableDTO> request;

  private RequestDTO requestDTO;

  // Inline error model for the checkbox on ViewRequest.xsl
  @XmlElement(name = "approveRejectErrorModel")
  private ApproveRejectErrorModel approveRejectErrorModel;

  // LOV payload so ViewRequest.xsl can render the Instructions list
  @XmlElement(name = "lovs")
  private LovsDTO lovs;

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class LovsDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "instructions")
    @XmlElement(name = "instruction")
    private java.util.List<String> instructions = new java.util.ArrayList<>();

    @XmlElementWrapper(name = "statuses")
    @XmlElement(name = "status")
    private java.util.List<String> statuses = new java.util.ArrayList<>();
  }
}