package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ApproveRejectErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.MandatesAutoFillErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.MandatesSignatureCardErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ResolutionsAutoFillErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SearchResultsErrorModel;

@Data
@Getter
@Setter
@XmlRootElement(name = "requestWrapper")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestWrapper {

  @XmlElement(name = "request")
  private RequestDTO request;

  private String requestType;
  private String checkResolution;
  private String checkMandates;
  private String checkMandatesAndresolution;
  private String checkCreate;
  private String checkDirectorPage;
  private boolean checkStyleOne;
  private boolean checkStyleTwo;
  private List<DirectorModel> directorModels;
  private List<DirectorModel> listOfDirectors;
  private String checkSecondDirectorList;
  private String checkDirectorEmpty;
  private String accountCheck;
  private List<AddAccountModel> listOfAddAccount;
  private String checkAccountSection;
  private String checkSignatureCard;
  private String stepForSave;
  private String toolOne;
  private String toolTwo;
  private String toolThree;
  private String toolFour;
  private String toolFive;
  private String checkBackOption;
  private String checkDirectorButton;


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