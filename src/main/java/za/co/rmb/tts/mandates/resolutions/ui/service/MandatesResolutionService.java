package za.co.rmb.tts.mandates.resolutions.ui.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import za.co.rmb.tts.mandates.resolutions.ui.model.AddAccountModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.DirectorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.ExportModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestDetails;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.SignatoryModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.AccountRequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.AccountResponseDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.AuthorityDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.CompanyDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestStagingDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestTableDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO;

/**
 * Lightweight service for managing mandate and resolution UI operations.
 * This service has been refactored to delegate core responsibilities to specialized services:
 * - {@link MandateCaptureService} for capturing and submitting mandates
 * - {@link SearchMandatesService} for searching and retrieving mandate data
 * - {@link AuditTrailService} for audit trail and status management
 *
 * This service now focuses on UI-related helper methods and session management.
 */
@Service
public class MandatesResolutionService {

  @Autowired
  private HttpSession httpSession;

  @Autowired
  private MandateCaptureService mandateCaptureService;

  @Autowired
  private SearchMandatesService searchMandatesService;

  @Autowired
  private AuditTrailService auditTrailService;

  RestTemplate restTemplate = new RestTemplate();

  @Value("${mandates-resolutions-dao}")
  private String mandatesResolutionsDaoURL;

  // ========== Deprecated Delegation Methods - Use new services instead ==========

  /**
   * @deprecated Use {@link SearchMandatesService#getCompanyByRegistration(String)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public CompanyDTO getCompanyByRegistration(String registrationNumber) {
    return searchMandatesService.getCompanyByRegistration(registrationNumber);
  }

  // ========== UI Helper Methods - Session and Model Management ==========

  /**
   * Sets director information from a map into a director model.
   * Used for UI form handling.
   */

  public DirectorModel setAllDirctors(Map<String, String> director, String instructionCheck) {
    DirectorModel directorModel;
    if ("false".equalsIgnoreCase(instructionCheck)) {
      directorModel = (DirectorModel) httpSession.getAttribute("Dirctors");
      directorModel.setName(director.get("name"));
      directorModel.setDesignation(director.get("designation"));
      directorModel.setSurname(director.get("surname"));
      directorModel.setInstructions(null);

    } else {
      directorModel = (DirectorModel) httpSession.getAttribute("DirctorsNew");
      directorModel.setName(director.get("name"));
      directorModel.setDesignation(director.get("designation"));
      directorModel.setSurname(director.get("surname"));
      directorModel.setInstructions(director.get("instructions"));
    }
    return directorModel;
  }

  public void removeSpecificAdmin(int userInList) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<DirectorModel> listOfDirector = requestWrapper.getDirectorModels();
    listOfDirector.removeIf(user -> user.getUserInList() == userInList);
    requestWrapper.setDirectorModels(listOfDirector);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
  }

  public void removeSpecificAdminReso(int userInList) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<DirectorModel> listOfDirector = requestWrapper.getListOfDirectors();
    listOfDirector.removeIf(user -> user.getUserInList() == userInList);
    requestWrapper.setListOfDirectors(listOfDirector);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
  }

  public void removeSpecificSignatory(int userInList) {
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    List<SignatoryModel> listOfDirector = addAccountModel.getListOfSignatory();
    listOfDirector.removeIf(user -> user.getUserInList() == userInList);
    addAccountModel.setListOfSignatory(listOfDirector);
    httpSession.setAttribute("Signatory", addAccountModel);
  }

  public void removeSpecificAdminResoEditWithId(Long directorId) {
    RequestDetails requestDetails =
        (RequestDetails) httpSession.getAttribute("RequestDetails");
    List<DirectorModel> listOfDirector = requestDetails.getListOfDirector();
    for (int i = 0; i < listOfDirector.size(); i++) {
      DirectorModel model = listOfDirector.get(i);
      if (model.getDirectorId() == directorId) {
        model.setCheckDelete("Yes");
        listOfDirector.set(i, model);
      }
    }
    requestDetails.setListOfDirector(listOfDirector);
    httpSession.setAttribute("RequestDetails", requestDetails);
  }

  public void removeSpecificAdminResoEditWithIdUndo(Long directorId) {
    RequestDetails requestDetails =
        (RequestDetails) httpSession.getAttribute("RequestDetails");
    List<DirectorModel> listOfDirector = requestDetails.getListOfDirector();
    for (int i = 0; i < listOfDirector.size(); i++) {
      DirectorModel model = listOfDirector.get(i);
      if (model.getDirectorId() == directorId) {
        model.setCheckDelete("No");
        listOfDirector.set(i, model);
      }
    }
    requestDetails.setListOfDirector(listOfDirector);
    httpSession.setAttribute("RequestDetails", requestDetails);
  }

  public DirectorModel getDirectorDetails(DirectorModel directorModel,
                                          List<DirectorModel> listOfDirectorList,
                                          String userInList) {
    Optional<DirectorModel> matchedDirctor = listOfDirectorList.stream()
        .filter(model -> userInList.equalsIgnoreCase(String.valueOf(model.getUserInList())))
        .findFirst();
    directorModel = matchedDirctor
        .map(director -> copyDirectorModel(director))
        .orElseGet(DirectorModel::new);
    return directorModel;
  }

  public DirectorModel copyDirectorModel(DirectorModel source) {
    DirectorModel target = new DirectorModel();
    target.setName(source.getName());
    target.setSurname(source.getSurname());
    target.setDesignation(source.getDesignation());
    target.setUserInList(source.getUserInList());
    return target;
  }

  public DirectorModel getDirectorDetailsReso(DirectorModel directorModel,
                                              List<DirectorModel> listOfDirectorList,
                                              String userInList) {
    Optional<DirectorModel> matchedDirctor = listOfDirectorList.stream()
        .filter(model -> userInList.equalsIgnoreCase(String.valueOf(model.getUserInList())))
        .findFirst();
    directorModel = matchedDirctor
        .map(director -> copyDirectorModelReso(director))
        .orElseGet(DirectorModel::new);
    return directorModel;
  }

  public DirectorModel copyDirectorModelReso(DirectorModel source) {
    DirectorModel target = new DirectorModel();
    target.setName(source.getName());
    target.setSurname(source.getSurname());
    target.setDesignation(source.getDesignation());
    target.setInstructions(source.getInstructions());
    target.setUserInList(source.getUserInList());
    return target;
  }

  public List<DirectorModel> getUpdatedDirector(
      List<DirectorModel> directorModelList,
      String userInList,
      Map<String, String> user) {

    directorModelList.stream()
        .filter(d -> userInList.equalsIgnoreCase(String.valueOf(d.getUserInList())))
        .findFirst()
        .ifPresent(d -> {
          d.setName(user.getOrDefault("name", d.getName()));
          d.setSurname(user.getOrDefault("surname", d.getSurname()));
          d.setDesignation(user.getOrDefault("designation", d.getDesignation()));
        });

    return directorModelList;
  }

  public List<DirectorModel> getUpdatedDirectorReso(
      List<DirectorModel> directorModelList,
      String userInList,
      Map<String, String> user) {

    directorModelList.stream()
        .filter(d -> userInList.equalsIgnoreCase(String.valueOf(d.getUserInList())))
        .findFirst()
        .ifPresent(d -> {
          d.setName(user.getOrDefault("name", d.getName()));
          d.setSurname(user.getOrDefault("surname", d.getSurname()));
          d.setDesignation(user.getOrDefault("designation", d.getDesignation()));
          d.setInstructions(user.getOrDefault("instructions", d.getInstructions()));
        });

    return directorModelList;
  }

  public SignatoryModel getSignatory(SignatoryModel signatory,
                                     List<SignatoryModel> listOfDirectorList,
                                     String userInList) {
    Optional<SignatoryModel> matchedDirctor = listOfDirectorList.stream()
        .filter(model -> userInList.equalsIgnoreCase(String.valueOf(model.getUserInList())))
        .findFirst();
    signatory = matchedDirctor
        .map(director -> copySignatoryModel(director))
        .orElseGet(SignatoryModel::new);
    return signatory;
  }

  public SignatoryModel copySignatoryModel(SignatoryModel source) {
    SignatoryModel target = new SignatoryModel();
    target.setFullName(source.getFullName());
    target.setIdNumber(source.getIdNumber());
    target.setInstruction(source.getInstruction());
    target.setUserInList(source.getUserInList());
    return target;
  }

  public List<SignatoryModel> getUpdatedSignatory(
      List<SignatoryModel> listSignatory,
      String userInList,
      Map<String, String> user) {

    listSignatory.stream()
        .filter(d -> userInList.equalsIgnoreCase(String.valueOf(d.getUserInList())))
        .findFirst()
        .ifPresent(d -> {
          d.setFullName(user.getOrDefault("fullName", d.getFullName()));
          d.setIdNumber(user.getOrDefault("idNumber", d.getIdNumber()));
          d.setInstruction(user.getOrDefault("accountRef1", d.getInstruction()));
          d.setUserInList(Integer.parseInt(userInList));
        });

    return listSignatory;
  }

  public RequestWrapper setSearchResult(Map<String, String> user) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    RequestDTO createModel = (RequestDTO) httpSession.getAttribute("requestData");
    createModel.setCompanyName(user.get("companyName"));
    createModel.setCompanyAddress(user.get("companyAddress"));
    requestWrapper.setRequest(createModel);
    if (!user.get("toolOne").isBlank()) {
      requestWrapper.setToolOne(user.get("toolOne"));
    }

    if (!user.get("toolTwo").isBlank()) {
      requestWrapper.setToolTwo(user.get("toolTwo"));
    }

    if (!user.get("toolThree").isBlank()) {
      requestWrapper.setToolThree(user.get("toolThree"));
    }

    if (!user.get("toolFour").isBlank()) {
      requestWrapper.setToolFour(user.get("toolFour"));
    }

    if (!user.get("toolFive").isBlank()) {
      requestWrapper.setToolFive(user.get("toolFive"));
    }
    requestWrapper.setRequestType(user.get("mandateResolution"));
    if (!user.get("mandateResolution").isBlank()) {
      requestWrapper.setCheckStyleOne(Boolean.parseBoolean(user.get("check1")));
      requestWrapper.setCheckStyleTwo(Boolean.parseBoolean(user.get("check2")));
    }
    return requestWrapper;
  }

  public RequestWrapper setSearchResultDraft(Map<String, String> user,
                                             RequestWrapper requestWrapper) {
    RequestDTO createModel = (RequestDTO) httpSession.getAttribute("requestData");
    createModel.setCompanyName(user.get("companyName"));
    createModel.setCompanyAddress(user.get("companyAddress"));
    requestWrapper.setRequest(createModel);
    if (!user.get("toolOne").isBlank()) {
      requestWrapper.setToolOne(user.get("toolOne"));
    }

    if (!user.get("toolTwo").isBlank()) {
      requestWrapper.setToolTwo(user.get("toolTwo"));
    }

    if (!user.get("toolThree").isBlank()) {
      requestWrapper.setToolThree(user.get("toolThree"));
    }

    if (!user.get("toolFour").isBlank()) {
      requestWrapper.setToolFour(user.get("toolFour"));
    }

    if (!user.get("toolFive").isBlank()) {
      requestWrapper.setToolFive(user.get("toolFive"));
    }
    requestWrapper.setRequestType(user.get("mandateResolution"));
    if (!user.get("mandateResolution").isBlank()) {
      requestWrapper.setCheckStyleOne(Boolean.parseBoolean(user.get("check1")));
      requestWrapper.setCheckStyleTwo(Boolean.parseBoolean(user.get("check2")));
    }
    return requestWrapper;
  }

  public SignatoryModel setSignatory(Map<String, String> user) {
    SignatoryModel signatoryModel = new SignatoryModel();
    signatoryModel.setFullName(user.get("fullName"));
    signatoryModel.setIdNumber(user.get("idNumber"));
    signatoryModel.setInstruction(user.get("accountRef1"));
    signatoryModel.setCheckRemoveOption("no");
    return signatoryModel;
  }

  public AddAccountModel getAccount(List<AddAccountModel> addAccountModelList, String userInList) {
    if (addAccountModelList == null || addAccountModelList.isEmpty() || userInList == null) {
      return new AddAccountModel();
    }
    for (AddAccountModel model : addAccountModelList) {
      if (userInList.equalsIgnoreCase(String.valueOf(model.getUserInList()))) {
        AddAccountModel result = new AddAccountModel();
        result.setAccountNumber(model.getAccountNumber());
        result.setAccountName(model.getAccountName());
        List<SignatoryModel> listOfSignatory = model.getListOfSignatory();
        for (int i = 0; i < listOfSignatory.size(); i++) {
          SignatoryModel signatoryModel = listOfSignatory.get(i);
          signatoryModel.setCheckEdit("true");
          listOfSignatory.set(i, signatoryModel);
        }
        result.setListOfSignatory(listOfSignatory);
        result.setListOfSignatory(model.getListOfSignatory());
        result.setUserInList(model.getUserInList());
        return result;
      }
    }
    return new AddAccountModel();
  }

  public List<AddAccountModel> updateAccount(List<AddAccountModel> addAccountModelList,
                                             String userInList,
                                             Map<String, String> user) {

    if (addAccountModelList == null || addAccountModelList.isEmpty()
        || userInList == null || userInList.isBlank()
        || user == null || user.isEmpty()) {
      return addAccountModelList; // nothing to update
    }

    for (AddAccountModel model : addAccountModelList) {
      if (userInList.equalsIgnoreCase(String.valueOf(model.getUserInList()))) {
        model.setAccountNumber(user.getOrDefault("accountNo", model.getAccountNumber()));
        model.setAccountName(user.getOrDefault("accountName", model.getAccountName()));
        model.setUserInList(Integer.parseInt(userInList));
        break;
      }
    }
    return addAccountModelList;
  }

  public AddAccountModel updateAccountSingle(List<AddAccountModel> addAccountModelList,
                                             String userInList,
                                             Map<String, String> user) {
    AddAccountModel addAccountModel = new AddAccountModel();
    if (addAccountModelList == null || addAccountModelList.isEmpty()
        || userInList == null || userInList.isBlank()
        || user == null || user.isEmpty()) {
      return new AddAccountModel();
    }

    for (AddAccountModel model : addAccountModelList) {
      if (userInList.equalsIgnoreCase(String.valueOf(model.getUserInList()))) {
        addAccountModel.setAccountNumber(model.getAccountNumber());
        addAccountModel.setAccountName(model.getAccountName());
        addAccountModel.setListOfSignatory(model.getListOfSignatory());
        addAccountModel.setUserInList(model.getUserInList());
        break;
      }
    }
    return addAccountModel;
  }

  public SignatoryModel getSignatoryData(List<AddAccountModel> listOfAccount, String userInList,
                                         String userInAccount) {
    SignatoryModel signatoryModels = new SignatoryModel();
    for (AddAccountModel model : listOfAccount) {
      for (SignatoryModel signatoryModel : model.getListOfSignatory()) {
        if (userInAccount.equalsIgnoreCase(String.valueOf(signatoryModel.getUserInAccount()))) {
          if (userInList.equalsIgnoreCase(String.valueOf(signatoryModel.getUserInList()))) {
            signatoryModels.setUserInList(signatoryModel.getUserInList());
            signatoryModels.setUserInAccount(signatoryModel.getUserInAccount());
            signatoryModels.setFullName(signatoryModel.getFullName());
            signatoryModels.setIdNumber(signatoryModel.getIdNumber());
            signatoryModels.setInstruction(signatoryModel.getInstruction());
            signatoryModels.setCapacity(signatoryModel.getCapacity());
            signatoryModels.setGroup(signatoryModel.getGroup());
            signatoryModels.setCheckDocConfirm(signatoryModel.getCheckDocConfirm());
            if ("true".equalsIgnoreCase(signatoryModel.getCheckDocConfirm())) {
              signatoryModels.setCheckDocConfirm(signatoryModel.getCheckDocConfirm());
            } else {
              signatoryModels.setCheckDocConfirm("false");
            }
          }
        }
      }
    }
    return signatoryModels;
  }

  public List<AddAccountModel> getAddAccountList(List<AddAccountModel> listOfAddAccount,
                                                 String userInList,
                                                 String userInAccount,
                                                 Map<String, String> user) {
    for (int i = 0; i < listOfAddAccount.size(); i++) {
      AddAccountModel account = listOfAddAccount.get(i);
      List<SignatoryModel> signatories = account.getListOfSignatory();
      for (int j = 0; j < signatories.size(); j++) {
        SignatoryModel signatory = signatories.get(j);
        if (userInAccount.equalsIgnoreCase(String.valueOf(signatory.getUserInAccount()))
            && userInList.equalsIgnoreCase(String.valueOf(signatory.getUserInList()))) {
          signatory.setCapacity(user.get("capacity"));
          signatory.setGroup(user.get("Group"));
          signatory.setCheckDocConfirm(user.get("confirm"));
          signatories.set(j, signatory);
          account.setListOfSignatory(signatories);
          listOfAddAccount.set(i, account);
          return listOfAddAccount;
        }
      }
    }
    return listOfAddAccount;
  }

  /**
   * @deprecated Use {@link MandateCaptureService#sendRequestStaging()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public String sendRequestStaging() {
    return mandateCaptureService.sendRequestStaging();
  }

  /**
   * @deprecated Use {@link MandateCaptureService#sendRequestSignatureCard()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public String sendRequestSignatureCard() {
    return mandateCaptureService.sendRequestSignatureCard();
  }

  /**
   * @deprecated Use {@link MandateCaptureService#createRequest()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public String createRequest() {
    return mandateCaptureService.createRequest();
  }

  /**
   * @deprecated Use {@link MandateCaptureService#createRequestReso()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public String createRequestReso() {
    return mandateCaptureService.createRequestReso();
  }

  /**
   * @deprecated Use {@link MandateCaptureService#createRequestMandates()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public String createRequestMandates() {
    return mandateCaptureService.createRequestMandates();
  }

  /**
   * @deprecated Use {@link SearchMandatesService#getAllDrafts()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public List<RequestStagingDTO> getAllDrafts() {
    return searchMandatesService.getAllDrafts();
  }

  /**
   * @deprecated Use {@link SearchMandatesService#getDraftsById(Long)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public RequestStagingDTO getDraftsById(Long id) {
    return searchMandatesService.getDraftsById(id);
  }

  /**
   * @deprecated Use {@link SearchMandatesService#processViewDraft(RequestStagingDTO)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public RequestWrapper processViewDraft(RequestStagingDTO stagingDTO) {
    return searchMandatesService.processViewDraft(stagingDTO);
  }

  /**
   * @deprecated Use {@link SearchMandatesService#exportCsv(ExportModel)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public byte[] exportCsv(ExportModel exportModel) throws IOException {
    return searchMandatesService.exportCsv(exportModel);
  }

  /**
   * @deprecated Use {@link SearchMandatesService#getRequestById(Long)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public RequestDetails getRequestById(Long requestId) throws JsonProcessingException {
    return searchMandatesService.getRequestById(requestId);
  }

  /**
   * @deprecated Use {@link AuditTrailService#statusUpdated(Long, String, String, String, String)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public void statusUpdated(Long requestId, String processOutcome, String subStatus,
                            String status, String currentUser) {
    auditTrailService.statusUpdated(requestId, processOutcome, subStatus, status, currentUser);
  }

  /**
   * @deprecated Use {@link AuditTrailService#statusCheck(String)} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public void statusCheck(String status) {
    auditTrailService.statusCheck(status);
  }

  /**
   * @deprecated Use {@link SearchMandatesService#getAllRecords()} instead.
   * This method will be removed in a future version.
   */
  @Deprecated
  public List<RequestTableDTO> getAllRecords() {
    return searchMandatesService.getAllRecords();
  }

  /**
   * Updates a draft request by staging ID - Step One.
   * Retained for complex session management logic.
   */
  public void updateDraftByStagingIdStepOne(Long stagingId, Map<String, String> user,
                                            RequestStagingDTO requestStagingDTO,
                                            RequestWrapper requestWrapper) {
    String url = mandatesResolutionsDaoURL + "/api/request-staging/" + stagingId;
    requestStagingDTO.setCompanyRegistrationNumber(requestStagingDTO
        .getCompanyRegistrationNumber());
    if ("Step 1".equalsIgnoreCase(requestWrapper.getStepForSave())) {
      requestStagingDTO.setCompanyName(user.get("companyName"));
      requestStagingDTO.setCompanyAddress(user.get("companyAddress"));
      if (user.get("mandateResolution").isBlank()) {
        requestStagingDTO.setRequestType(null);
      } else {
        requestStagingDTO.setRequestType(user.get("mandateResolution"));
        requestStagingDTO.setDraftSigmaConfirmCheck(Boolean.valueOf(user.get("check1")));
        requestStagingDTO.setDraftWaiverConfirmCheck(Boolean.valueOf(user.get("check2")));
      }
      //If Request Type is present setCheck Box
      String tools = String.join(",",
          user.get("toolOne"),
          user.get("toolTwo"),
          user.get("toolThree"),
          user.get("toolFour"),
          user.get("toolFive")
      );
      requestStagingDTO.setWaiverPermittedTools(tools);
    } else {
      requestStagingDTO.setCompanyName(requestWrapper.getRequest().getCompanyName());
      requestStagingDTO.setCompanyAddress(requestWrapper.getRequest().getCompanyAddress());
      if ("".equalsIgnoreCase(requestWrapper.getRequestType())
          || "null".equalsIgnoreCase(requestWrapper.getRequestType())) {
        requestStagingDTO.setRequestType(null);
      } else {
        requestStagingDTO.setRequestType(requestWrapper.getRequestType());
        requestStagingDTO.setDraftSigmaConfirmCheck(requestWrapper.isCheckStyleOne());
        requestStagingDTO.setDraftWaiverConfirmCheck(requestWrapper.isCheckStyleTwo());
      }
      String tools = String.join(",",
          requestWrapper.getToolOne(),
          requestWrapper.getToolTwo(),
          requestWrapper.getToolThree(),
          requestWrapper.getToolFour(),
          requestWrapper.getToolFive()
      );
      requestStagingDTO.setWaiverPermittedTools(tools);
    }
    List<RequestStagingDTO.AuthorityDraft> listOfAuthority = new ArrayList<>();
    if (requestWrapper.getDirectorModels() != null) {
      for (DirectorModel directorModel : requestWrapper.getDirectorModels()) {
        RequestStagingDTO.AuthorityDraft authorityDraft = new RequestStagingDTO.AuthorityDraft();
        authorityDraft.setFirstname(directorModel.getName());
        authorityDraft.setSurname(directorModel.getSurname());
        authorityDraft.setIsActive(true);
        authorityDraft.setDesignation(directorModel.getDesignation());
        listOfAuthority.add(authorityDraft);
      }
    }
    if (requestWrapper.getListOfDirectors() != null) {
      for (DirectorModel directorModel : requestWrapper.getListOfDirectors()) {
        RequestStagingDTO.AuthorityDraft authorityDraft = new RequestStagingDTO.AuthorityDraft();
        authorityDraft.setFirstname(directorModel.getName());
        authorityDraft.setSurname(directorModel.getSurname());
        authorityDraft.setDesignation(directorModel.getDesignation());
        authorityDraft.setInstructions(directorModel.getInstructions());
        authorityDraft.setIsActive(true);
        listOfAuthority.add(authorityDraft);
      }
    }
    requestStagingDTO.setAuthorities(listOfAuthority);
    List<RequestStagingDTO.AccountDraft> listOfAddAccount = new ArrayList<>();
    if (requestWrapper.getListOfAddAccount() != null) {
      for (AddAccountModel model : requestWrapper.getListOfAddAccount()) {
        RequestStagingDTO.AccountDraft accountDraft = new RequestStagingDTO.AccountDraft();
        accountDraft.setAccountName(model.getAccountName());
        accountDraft.setAccountNumber(model.getAccountNumber());
        accountDraft.setIsActive(true);
        List<RequestStagingDTO.SignatoryDraft> listOfSignatory = new ArrayList<>();
        for (SignatoryModel signatoryModel : model.getListOfSignatory()) {
          RequestStagingDTO.SignatoryDraft signatoryDraft = new RequestStagingDTO.SignatoryDraft();
          signatoryDraft.setFullName(signatoryModel.getFullName());
          signatoryDraft.setCapacity(signatoryModel.getCapacity());
          signatoryDraft.setIsActive(true);
          signatoryDraft.setIdNumber(signatoryModel.getIdNumber());
          signatoryDraft.setGroupCategory(signatoryModel.getGroup());
          signatoryDraft.setInstructions(signatoryModel.getInstruction());
          signatoryDraft.setSignatoryConfirmCheck(
              Boolean.valueOf(signatoryModel.getCheckDocConfirm()));
          listOfSignatory.add(signatoryDraft);
        }
        accountDraft.setSignatories(listOfSignatory);
        listOfAddAccount.add(accountDraft);
      }
      requestStagingDTO.setAccounts(listOfAddAccount);
      requestStagingDTO.setRequestSubStatus(requestWrapper.getStepForSave());
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<RequestStagingDTO> entity = new HttpEntity<>(requestStagingDTO, headers);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
  }

  public void updateViewRequest(RequestDetails requestDetails) {
    UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
    if (requestDetails.getListOfAddAccountModel() != null
        && !requestDetails.getListOfAddAccountModel().isEmpty()
        && requestDetails.getListOfAddAccountModel().size() > 1) {
      for (AddAccountModel acc : requestDetails.getListOfAddAccountModel()) {
        if ("Yes".equalsIgnoreCase(acc.getCheckDelete())) {
          restTemplate.delete(mandatesResolutionsDaoURL + "/api/account/" + acc.getAccountId());
        }
      }
    }

    if (requestDetails.getListOfDirector() != null
        && !requestDetails.getListOfDirector().isEmpty()) {
      for (DirectorModel director : requestDetails.getListOfDirector()) {
        if ("Yes".equalsIgnoreCase(director.getCheckDelete())) {
          restTemplate.delete(
              mandatesResolutionsDaoURL + "/api/authority/" + director.getDirectorId());
        }
      }
    }

    if ("Mandate".equalsIgnoreCase(requestDetails.getType())
        || "Mandate And Resolution".equalsIgnoreCase(requestDetails.getType())) {
      for (AddAccountModel model : requestDetails.getListOfAddAccountModel()) {
        if (model.getAccountId() != null
            && "Yes".equalsIgnoreCase(model.getCheckEditAccount())) {
          String url = mandatesResolutionsDaoURL + "/api/account/" + model.getAccountId();
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          AccountRequestDTO dto = new AccountRequestDTO();
          dto.setCompanyId(requestDetails.getCompanyId());
          dto.setAccountName(model.getAccountName());
          dto.setAccountNumber(model.getAccountNumber());
          dto.setIsActive(true);
          dto.setCreator(model.getCreator());
          dto.setUpdator(user.getUsername());
          List<AccountRequestDTO.Signatory> listOfSignatory = new ArrayList<>();
          for (SignatoryModel signatoryModel : model.getListOfSignatory()) {
            AccountRequestDTO.Signatory signatory = new AccountRequestDTO.Signatory();
            signatory.setFullName(signatoryModel.getFullName());
            signatory.setIdNumber(signatoryModel.getIdNumber());
            signatory.setInstructions(signatoryModel.getInstruction());
            signatory.setCapacity(signatoryModel.getCapacity());
            signatory.setGroupCategory(signatoryModel.getGroup());
            signatory.setCreator(signatoryModel.getCreator());
            signatory.setUpdator(user.getUsername());
            signatory.setSignatoryConfirmCheck(
                Boolean.valueOf(signatoryModel.getCheckDocConfirm()));
            listOfSignatory.add(signatory);
          }
          dto.setSignatories(listOfSignatory);
          HttpEntity<AccountRequestDTO> entity = new HttpEntity<>(dto, headers);
          ResponseEntity<AccountResponseDTO> response =
              restTemplate.exchange(url, HttpMethod.PUT, entity, AccountResponseDTO.class);
        }
      }
    }

    if ("Resolution".equalsIgnoreCase(requestDetails.getType())
        || "Mandate And Resolution".equalsIgnoreCase(requestDetails.getType())) {
      for (DirectorModel directorModel : requestDetails.getListOfDirector()) {
        if (directorModel.getDirectorId() != null
            && "Yes".equalsIgnoreCase(directorModel.getCheckUpdatedFlag())) {
          String url =
              mandatesResolutionsDaoURL + "/api/authority/" + directorModel.getDirectorId();
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          AuthorityDTO updated = new AuthorityDTO();
          updated.setCompanyId(requestDetails.getCompanyId());
          updated.setFirstname(directorModel.getName());
          updated.setSurname(directorModel.getSurname());
          updated.setDesignation(directorModel.getDesignation());
          updated.setInstructions(directorModel.getInstructions());
          updated.setIsActive(true);
          updated.setUpdator("admin.user");
          updated.setCreator(directorModel.getCreator());
          updated.setUpdator(user.getUsername());
          HttpEntity<AuthorityDTO> entity = new HttpEntity<>(updated, headers);
          ResponseEntity<AuthorityDTO> response =
              restTemplate.exchange(url, HttpMethod.PUT, entity, AuthorityDTO.class);
        } else if (directorModel.getDirectorId() == null) {
          String url = mandatesResolutionsDaoURL + "/api/authority";
          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          AuthorityDTO authority = new AuthorityDTO();
          authority.setCompanyId(requestDetails.getCompanyId());
          authority.setFirstname(directorModel.getName());
          authority.setSurname(directorModel.getSurname());
          authority.setDesignation(directorModel.getDesignation());
          authority.setInstructions(directorModel.getInstructions());
          authority.setIsActive(true);
          authority.setCreator(user.getUsername());
          authority.setUpdator(null);
          HttpEntity<AuthorityDTO> entity = new HttpEntity<>(authority, headers);
          ResponseEntity<AuthorityDTO> response =
              restTemplate.exchange(url, HttpMethod.POST, entity, AuthorityDTO.class);
        }
      }

    }
  }

}
