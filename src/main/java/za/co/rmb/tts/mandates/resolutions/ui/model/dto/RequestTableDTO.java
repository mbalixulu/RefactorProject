package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestTableDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long requestId;
  private Long companyId;
  private Integer sla;
  private String companyName;
  private String status;
  private String subStatus;
  private String created;
  private String type;
  private String updated;
  private String processId;
  private String assignedUser;

  //ID from backend (entity @JsonProperty("requestIdForDisplay"))
  private String requestIdForDisplay;

  //For Draft table
  private String registrationNumber;

  @XmlElementWrapper(name = "directors")
  @XmlElement(name = "director")
  private List<DirectorDTO> directors;

  @XmlElementWrapper(name = "signatories")
  @XmlElement(name = "signatory")
  private List<SignatoryDTO> signatories;

  @XmlElementWrapper(name = "accounts")
  @XmlElement(name = "account")
  private List<AccountDTO> accounts;

  @XmlElementWrapper(name = "approvedComments")
  @XmlElement(name = "comment")
  private List<ViewCommentDTO> approvedComments;

  @XmlElementWrapper(name = "rejectedComments")
  @XmlElement(name = "comment")
  private List<ViewCommentDTO> rejectedComments;
}