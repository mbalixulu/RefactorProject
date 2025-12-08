package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import za.co.rmb.tts.mandates.resolutions.ui.model.error.SearchResultsErrorModel;

@Data
@Getter
@Setter
@XmlRootElement(name = "requestDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestDetails {

  private Long companyId;
  private String registrationNumber;
  private String companyName;
  private String address;
  private String creatorCompany;
  private String updatorCompany;
  private LocalDateTime createdCompany;
  private LocalDateTime updatedCompany;

  private Long requestId;
  private Integer sla;
  private String processId;
  private String assignedUser;
  private String type;
  private String status;
  private String subStatus;
  private String creatorRequest;
  private String updatorRequest;
  private String createdReq;
  private String updatedReq;
  //ID exposed by backend entity getter
  private String requestIdForDisplay;
  private String ucn;
  private String permittedTools;

  private List<DirectorModel> listOfDirector;
  private List<AddAccountModel> listOfAddAccountModel;
  private List<CommentModel> listOfComment;
  private List<InstructionModel> listOfInstruction;

  private String checkReassignee;
  private String viewPageError;
  private String checkStatus;

  private String checkHoganVarificationPending;
  private String checkWindeedVarificationPending;
  private String checkHanisVarificationPending;
  private String checkAdminApprovePending;
  private String checkHoganUpdatePending;
  private String checkDocumentUpdatePending;
  private String checkHoldRecord;
  private String checkUnHoldRecord;

  private String checkDirectors;


}
