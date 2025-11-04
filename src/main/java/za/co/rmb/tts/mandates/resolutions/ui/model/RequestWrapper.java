package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import lombok.Data;

import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ApproveRejectErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.MandatesAutoFillErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.MandatesSignatureCardErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ResolutionsAutoFillErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SearchResultsErrorModel;

@Data
@XmlRootElement(name = "requestWrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestWrapper implements Serializable {
  private static final long serialVersionUID = 1L;

  @XmlElement(name = "request")
  private RequestDTO request;

  //Search Results
  @XmlElement(name = "searchResultsErrorModel")
  private SearchResultsErrorModel searchResultsErrorModel;

  //Mandates Auto Fill
  @XmlElement(name = "mandatesAutoFillErrorModel")
  private MandatesAutoFillErrorModel mandatesAutoFillErrorModel;

  //Mandates Signature Card
  @XmlElement(name = "mandatesSignatureCardErrorModel")
  private MandatesSignatureCardErrorModel mandatesSignatureCardErrorModel;

  //Resolutions Auto Fill
  @XmlElement(name = "resolutionsAutoFillErrorModel")
  private ResolutionsAutoFillErrorModel resolutionsAutoFillErrorModel;

  @XmlElement(name = "approveRejectErrorModel")
  private ApproveRejectErrorModel approveRejectErrorModel;

  //LOV (List Of Values) payload for dropdowns
  @XmlElement(name = "lovs")
  private LovsDTO lovs;

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class LovsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "instructions")
    @XmlElement(name = "instruction")
    private java.util.List<String> instructions = new java.util.ArrayList<>();

    @XmlElementWrapper(name = "statuses")
    @XmlElement(name = "status")
    private java.util.List<String> statuses = new java.util.ArrayList<>();
  }
}