package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.SignatoryModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.WaveModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.CompanyDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestStagingDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO;

@Service
public class MandatesResolutionService {

  @Autowired
  private HttpSession httpSession;

  RestTemplate restTemplate = new RestTemplate();

  @Value("${mandates-resolutions-dao}")
  private String mandatesResolutionsDaoURL;

  public CompanyDTO getCompanyByRegistration(String registrationNumber) {
    String url =
        mandatesResolutionsDaoURL
        + "/api/company/registration?registrationNumber="
        + registrationNumber;
    ResponseEntity<CompanyDTO> response =
        restTemplate.getForEntity(url, CompanyDTO.class, registrationNumber);
    return response.getBody();
  }

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

  public String sendRequestStaging() {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    UserDTO dto = (UserDTO) httpSession.getAttribute("currentUser");
    String url = mandatesResolutionsDaoURL + "/api/request-staging";
    Map<String, Object> payload = new HashMap<>();
    payload.put("companyRegistrationNumber", requestWrapper.getRequest().getRegistrationNumber());
    payload.put("companyName", requestWrapper.getRequest().getCompanyName());
    payload.put("companyAddress", requestWrapper.getRequest().getCompanyAddress());
    if (requestWrapper.getRequestType() != null && requestWrapper.getRequestType() != "") {
      payload.put("requestType", requestWrapper.getRequestType());
      payload.put("draftWaiverConfirmCheck", requestWrapper.isCheckStyleTwo());
      payload.put("draftSigmaConfirmCheck", requestWrapper.isCheckStyleOne());
    }
    payload.put("requestStatus", "Draft");
    payload.put("requestSubStatus", requestWrapper.getStepForSave());
    payload.put("waiverUcn", "UCN-7788");
    String tools = String.join(",",
        requestWrapper.getToolOne(),
        requestWrapper.getToolTwo(),
        requestWrapper.getToolThree(),
        requestWrapper.getToolFour(),
        requestWrapper.getToolFive()
    );
    payload.put("waiverPermittedTools", tools);
    String currentDateTime = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    payload.put("waiverLastFetched", currentDateTime);
    payload.put("creator", dto.getUsername());
    List<Map<String, Object>> accounts = new ArrayList<>();
    if (requestWrapper.getListOfAddAccount() != null
        && !requestWrapper.getListOfAddAccount().isEmpty()) {
      for (AddAccountModel model : requestWrapper.getListOfAddAccount()) {
        Map<String, Object> account1 = new HashMap<>();
        account1.put("accountName", model.getAccountName());
        account1.put("accountNumber", model.getAccountNumber());
        account1.put("isActive", true);
        List<Map<String, Object>> signatories1 = new ArrayList<>();
        if (model.getListOfSignatory() != null) {
          for (SignatoryModel s : model.getListOfSignatory()) {
            Map<String, Object> signatory = new HashMap<>();
            signatory.put("fullName", s.getFullName());
            signatory.put("idNumber", s.getIdNumber());
            signatory.put("instructions", s.getInstruction());
            signatory.put("instructionsDate", "2025-08-25T09:30:00");
            signatory.put("capacity", s.getCapacity());
            signatory.put("groupCategory", s.getGroup());
            signatory.put("isActive", true);
            signatories1.add(signatory);
          }
        }
        account1.put("signatories", signatories1);
        accounts.add(account1);
      }
    }
    payload.put("accounts", accounts);
    List<Map<String, Object>> authorities = new ArrayList<>();
    if (requestWrapper.getDirectorModels() != null) {
      for (DirectorModel directorModel : requestWrapper.getDirectorModels()) {
        authorities.add(Map.of(
            "firstname", directorModel.getName(),
            "surname", directorModel.getSurname(),
            "designation", directorModel.getDesignation(),
            "isActive", true
        ));
      }
    }
    if (requestWrapper.getListOfDirectors() != null) {
      for (DirectorModel directorModel : requestWrapper.getListOfDirectors()) {
        authorities.add(Map.of(
            "firstname", directorModel.getName(),
            "surname", directorModel.getSurname(),
            "designation", directorModel.getDesignation(),
            "instructions", directorModel.getInstructions(),
            "isActive", true
        ));
      }
    }
    payload.put("authorities", authorities);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    return response.getBody();
  }

  public String sendRequestSignatureCard() {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
    String url = mandatesResolutionsDaoURL + "/api/request-staging";
    Map<String, Object> payload = new HashMap<>();
    payload.put("companyRegistrationNumber", requestWrapper.getRequest().getRegistrationNumber());
    payload.put("companyName", requestWrapper.getRequest().getCompanyName());
    payload.put("companyAddress", requestWrapper.getRequest().getCompanyAddress());
    if (requestWrapper.getRequestType() != null && requestWrapper.getRequestType() != "") {
      payload.put("requestType", requestWrapper.getRequestType());
      payload.put("draftWaiverConfirmCheck", Boolean.valueOf(requestWrapper.isCheckStyleTwo()));
      payload.put("draftSigmaConfirmCheck", Boolean.valueOf(requestWrapper.isCheckStyleOne()));
    }
    payload.put("requestStatus", "Draft");
    payload.put("requestSubStatus", "Step 3");
    payload.put("waiverUcn", "UCN-7788");
    String tools = String.join(",",
        requestWrapper.getToolOne(),
        requestWrapper.getToolTwo(),
        requestWrapper.getToolThree(),
        requestWrapper.getToolFour(),
        requestWrapper.getToolFive()
    );
    payload.put("waiverPermittedTools", tools);
    String currentDateTime = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    payload.put("waiverLastFetched", currentDateTime);
    payload.put("creator", user.getUsername());
    List<Map<String, Object>> accounts = new ArrayList<>();
    for (AddAccountModel addAccountModel : requestWrapper.getListOfAddAccount()) {
      Map<String, Object> accountE = new HashMap<>();
      List<Map<String, Object>> signatoriesE = new ArrayList<>();
      accountE.put("accountName", addAccountModel.getAccountName());
      accountE.put("accountNumber", addAccountModel.getAccountNumber());
      accountE.put("isActive", true);
      accountE.put("creator", user.getUsername());
      for (SignatoryModel appointedModel : addAccountModel.getListOfSignatory()) {
        Map<String, Object> signatory = new HashMap<>();
        signatory.put("fullName", appointedModel.getFullName());
        signatory.put("idNumber", appointedModel.getIdNumber());
        signatory.put("instructions", appointedModel.getInstruction());
        signatory.put("capacity", appointedModel.getCapacity());
        signatory.put("groupCategory", appointedModel.getGroup());
        signatory.put("signatoryConfirmCheck",
            Boolean.valueOf(appointedModel.getCheckDocConfirm()));
        signatory.put("isActive", appointedModel.getCheckDocConfirm());
        signatory.put("creator", user.getUsername());
        signatoriesE.add(signatory);
      }
      accountE.put("signatories", signatoriesE);
      accounts.add(accountE);
    }
    payload.put("accounts", accounts);
    List<Map<String, Object>> authorities = new ArrayList<>();
    if (requestWrapper.getDirectorModels() != null) {
      for (DirectorModel directorModel : requestWrapper.getDirectorModels()) {
        authorities.add(Map.of(
            "firstname", directorModel.getName(),
            "surname", directorModel.getSurname(),
            "designation", directorModel.getDesignation(),
            "isActive", true
        ));
      }
    }
    payload.put("authorities", authorities);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    return response.getBody();
  }

  public String createRequest() {
    RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
    String url = mandatesResolutionsDaoURL + "/api/submission";
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> company = new HashMap<>();
    company.put("registrationNumber", wrapper.getRequest().getRegistrationNumber());
    company.put("name", wrapper.getRequest().getCompanyName());
    company.put("address", wrapper.getRequest().getCompanyAddress());
    company.put("creator", user.getUsername());

    Map<String, Object> request = new HashMap<>();
    request.put("sla", 3);
    if (wrapper.getRequest().getStagingId() != null
        && wrapper.getRequest().getStagingId() != 0) {
      request.put("stagingId", wrapper.getRequest().getStagingId());
    }
    if (wrapper.getRequestType() != null && wrapper.getRequestType() != "") {
      request.put("type", wrapper.getRequestType());
      request.put("waiverConfirmCheck", Boolean.valueOf(wrapper.isCheckStyleTwo()));
      request.put("sigmaConfirmCheck", Boolean.valueOf(wrapper.isCheckStyleOne()));
    }
    request.put("status", "In Progress");
    request.put("subStatus", "Hogan Verification Pending");
    request.put("assignedUser", user.getUsername());
    request.put("creator", user.getUsername());

    Map<String, Object> waiver = new HashMap<>();
    waiver.put("ucn", "UCN-7788");
    String tools = String.join(",",
        wrapper.getToolOne(),
        wrapper.getToolTwo(),
        wrapper.getToolThree(),
        wrapper.getToolFour(),
        wrapper.getToolFive()
    );
    waiver.put("permittedTools", tools);
    waiver.put("lastFetched", "2025-08-25T12:00:00");
    waiver.put("creator", user.getUsername());
    List<Map<String, Object>> accounts = new ArrayList<>();
    for (AddAccountModel addAccountModel : wrapper.getListOfAddAccount()) {
      Map<String, Object> accountE = new HashMap<>();
      List<Map<String, Object>> signatoriesE = new ArrayList<>();
      accountE.put("accountName", addAccountModel.getAccountName());
      accountE.put("accountNumber", addAccountModel.getAccountNumber());
      accountE.put("isActive", true);
      accountE.put("creator", user.getUsername());
      for (SignatoryModel appointedModel : addAccountModel.getListOfSignatory()) {
        Map<String, Object> signatory = new HashMap<>();
        signatory.put("fullName", appointedModel.getFullName());
        signatory.put("idNumber", appointedModel.getIdNumber());
        signatory.put("instructions", appointedModel.getInstruction());
        signatory.put("capacity", appointedModel.getCapacity());
        signatory.put("groupCategory", appointedModel.getGroup());
        signatory.put("isActive", true);
        signatory.put("signatoryConfirmCheck",
            Boolean.valueOf(appointedModel.getCheckDocConfirm()));
        signatory.put("creator", user.getUsername());
        signatoriesE.add(signatory);
      }
      accountE.put("signatories", signatoriesE);
      accounts.add(accountE);
    }
    List<Map<String, Object>> authorities = new ArrayList<>();
    for (DirectorModel directorModel : wrapper.getDirectorModels()) {
      authorities.add(Map.of(
          "firstname", directorModel.getName(),
          "surname", directorModel.getSurname(),
          "designation", directorModel.getDesignation(),
          "isActive", true,
          "creator", user.getUsername()
      ));
    }

    if (wrapper.getListOfDirectors() != null) {
      for (DirectorModel directorModel : wrapper.getListOfDirectors()) {
        authorities.add(Map.of(
            "firstname", directorModel.getName(),
            "surname", directorModel.getSurname(),
            "designation", directorModel.getDesignation(),
            "instructions", directorModel.getInstructions(),
            "isActive", true,
            "creator", user.getUsername()
        ));
      }
    }

    requestBody.put("company", company);
    requestBody.put("request", request);
    requestBody.put("waiver", waiver);
    requestBody.put("accounts", accounts);
    requestBody.put("authorities", authorities);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    return response.getBody();
  }

  public String createRequestReso() {
    RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
    String url = mandatesResolutionsDaoURL + "/api/submission";
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> company = new HashMap<>();
    company.put("registrationNumber", wrapper.getRequest().getRegistrationNumber());
    company.put("name", wrapper.getRequest().getCompanyName());
    company.put("address", wrapper.getRequest().getCompanyAddress());
    company.put("creator", user.getUsername());

    Map<String, Object> request = new HashMap<>();
    request.put("sla", 3);
    if (wrapper.getRequest().getStagingId() != null
        && wrapper.getRequest().getStagingId() != 0) {
      request.put("stagingId", wrapper.getRequest().getStagingId());
    }
    if (wrapper.getRequestType() != null && wrapper.getRequestType() != "") {
      request.put("type", wrapper.getRequestType());
      request.put("waiverConfirmCheck", Boolean.valueOf(wrapper.isCheckStyleTwo()));
      request.put("sigmaConfirmCheck", Boolean.valueOf(wrapper.isCheckStyleOne()));
    }
    request.put("status", "In Progress");
    request.put("subStatus", "Hogan Verification Pending");
    request.put("assignedUser", user.getUsername());
    request.put("creator", user.getUsername());

    Map<String, Object> waiver = new HashMap<>();
    waiver.put("ucn", "UCN-7788");
    String tools = String.join(",",
        wrapper.getToolOne(),
        wrapper.getToolTwo(),
        wrapper.getToolThree(),
        wrapper.getToolFour(),
        wrapper.getToolFive()
    );
    waiver.put("permittedTools", tools);
    waiver.put("lastFetched", "2025-08-25T12:00:00");
    waiver.put("creator", user.getUsername());
    List<Map<String, Object>> authorities = new ArrayList<>();
    for (DirectorModel directorModel : wrapper.getDirectorModels()) {
      authorities.add(Map.of(
          "firstname", directorModel.getName(),
          "surname", directorModel.getSurname(),
          "designation", directorModel.getDesignation(),
          "isActive", true,
          "creator", user.getUsername()
      ));
    }

    if (wrapper.getListOfDirectors() != null) {
      for (DirectorModel directorModel : wrapper.getListOfDirectors()) {
        authorities.add(Map.of(
            "firstname", directorModel.getName(),
            "surname", directorModel.getSurname(),
            "designation", directorModel.getDesignation(),
            "instructions", directorModel.getInstructions(),
            "isActive", true,
            "creator", user.getUsername()
        ));
      }
    }
    requestBody.put("company", company);
    requestBody.put("request", request);
    requestBody.put("waiver", waiver);
    requestBody.put("authorities", authorities);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    return response.getBody();
  }

  public String createRequestMandates() {
    RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
    String url = mandatesResolutionsDaoURL + "/api/submission";
    Map<String, Object> requestBody = new HashMap<>();
    Map<String, Object> company = new HashMap<>();
    company.put("registrationNumber", wrapper.getRequest().getRegistrationNumber());
    company.put("name", wrapper.getRequest().getCompanyName());
    company.put("address", wrapper.getRequest().getCompanyAddress());
    company.put("creator", user.getUsername());
    Map<String, Object> request = new HashMap<>();
    request.put("sla", 3);
    if (wrapper.getRequest().getStagingId() != null
        && wrapper.getRequest().getStagingId() != 0) {
      request.put("stagingId", wrapper.getRequest().getStagingId());
    }
    if (wrapper.getRequestType() != null && wrapper.getRequestType() != "") {
      request.put("type", wrapper.getRequestType());
      request.put("waiverConfirmCheck", Boolean.valueOf(wrapper.isCheckStyleTwo()));
      request.put("sigmaConfirmCheck", Boolean.valueOf(wrapper.isCheckStyleOne()));
    }
    request.put("status", "In Progress");
    request.put("subStatus", "Hogan Verification Pending");
    request.put("assignedUser", user.getUsername());
    request.put("creator", user.getUsername());

    Map<String, Object> waiver = new HashMap<>();
    waiver.put("ucn", "UCN-7788");
    String tools = String.join(",",
        wrapper.getToolOne(),
        wrapper.getToolTwo(),
        wrapper.getToolThree(),
        wrapper.getToolFour(),
        wrapper.getToolFive()
    );
    waiver.put("permittedTools", tools);
    waiver.put("lastFetched", "2025-08-25T12:00:00");
    waiver.put("creator", user.getUsername());
    List<Map<String, Object>> accounts = new ArrayList<>();
    for (AddAccountModel addAccountModel : wrapper.getListOfAddAccount()) {
      Map<String, Object> accountE = new HashMap<>();
      List<Map<String, Object>> signatoriesE = new ArrayList<>();
      accountE.put("accountName", addAccountModel.getAccountName());
      accountE.put("accountNumber", addAccountModel.getAccountNumber());
      accountE.put("isActive", true);
      accountE.put("creator", user.getUsername());
      for (SignatoryModel appointedModel : addAccountModel.getListOfSignatory()) {
        Map<String, Object> signatory = new HashMap<>();
        signatory.put("fullName", appointedModel.getFullName());
        signatory.put("idNumber", appointedModel.getIdNumber());
        signatory.put("instructions", appointedModel.getInstruction());
        signatory.put("capacity", appointedModel.getCapacity());
        signatory.put("groupCategory", appointedModel.getGroup());
        signatory.put("isActive", true);
        signatory.put("signatoryConfirmCheck",
            Boolean.valueOf(appointedModel.getCheckDocConfirm()));
        signatory.put("creator", user.getUsername());
        signatoriesE.add(signatory);
      }
      accountE.put("signatories", signatoriesE);
      accounts.add(accountE);
    }
    List<Map<String, Object>> authorities = new ArrayList<>();
    for (DirectorModel directorModel : wrapper.getDirectorModels()) {
      authorities.add(Map.of(
          "firstname", directorModel.getName(),
          "surname", directorModel.getSurname(),
          "designation", directorModel.getDesignation(),
          "isActive", true,
          "creator", user.getUsername()
      ));
    }
    requestBody.put("company", company);
    requestBody.put("request", request);
    requestBody.put("waiver", waiver);
    requestBody.put("accounts", accounts);
    requestBody.put("authorities", authorities);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    return response.getBody();
  }

  public List<RequestStagingDTO> getAllDrafts() {
    String url = mandatesResolutionsDaoURL + "/api/request-staging/all";
    RequestStagingDTO[] raws =
        restTemplate.getForObject(url, RequestStagingDTO[].class);
    List<RequestStagingDTO> list = (raws == null) ? List.of() : Arrays.asList(raws);
    return list;
  }

  public RequestStagingDTO getDraftsById(Long id) {
    String url = mandatesResolutionsDaoURL + "/api/request-staging/" + id;
    RequestStagingDTO stage =
        restTemplate.getForObject(url, RequestStagingDTO.class, id);
    return stage;
  }

  public RequestWrapper processViewDraft(RequestStagingDTO stagingDTO) {
    RequestWrapper requestWrapper = new RequestWrapper();
    RequestDTO dto = new RequestDTO();
    dto.setRegistrationNumber(stagingDTO.getCompanyRegistrationNumber());
    dto.setCompanyName(stagingDTO.getCompanyName());
    dto.setCompanyAddress(stagingDTO.getCompanyAddress());
    dto.setSla(String.valueOf(stagingDTO.getRequestSla()));
    dto.setStagingId(stagingDTO.getStagingId());
    dto.setStatus(stagingDTO.getRequestStatus());
    dto.setSubStatus(stagingDTO.getRequestSubStatus());
    dto.setCurrentAssignedUser(stagingDTO.getAssignedUser());
    requestWrapper.setRequest(dto);
    httpSession.setAttribute("requestData", dto);
    requestWrapper.setCheckBackOption("false");
    String waiver = stagingDTO.getWaiverPermittedTools();
    List<String> listOfTools = Arrays.asList(waiver.split(","));
    if (listOfTools.size() > 0) {
      if ("null".equalsIgnoreCase(listOfTools.get(0))) {
        requestWrapper.setToolOne("");
      } else {
        requestWrapper.setToolOne(listOfTools.get(0));
      }
    } else {
      requestWrapper.setToolOne(null);
    }

    if (listOfTools.size() > 1) {
      if ("null".equalsIgnoreCase(listOfTools.get(1))) {
        requestWrapper.setToolTwo("");
      } else {
        requestWrapper.setToolTwo(listOfTools.get(1));
      }
    } else {
      requestWrapper.setToolTwo(null);
    }

    if (listOfTools.size() > 2) {
      if ("null".equalsIgnoreCase(listOfTools.get(2))) {
        requestWrapper.setToolThree("");
      } else {
        requestWrapper.setToolThree(listOfTools.get(2));
      }
    } else {
      requestWrapper.setToolThree(null);
    }

    if (listOfTools.size() > 3) {
      if ("null".equalsIgnoreCase(listOfTools.get(3))) {
        requestWrapper.setToolFour("");
      } else {
        requestWrapper.setToolFour(listOfTools.get(3));
      }
    } else {
      requestWrapper.setToolFour(null);
    }

    if (listOfTools.size() > 4) {
      if ("null".equalsIgnoreCase(listOfTools.get(4))) {
        requestWrapper.setToolFive("");
      } else {
        requestWrapper.setToolFive(listOfTools.get(4));
      }
    } else {
      requestWrapper.setToolFive(null);
    }
    List<RequestStagingDTO.AuthorityDraft> listOfAuthority = stagingDTO.getAuthorities();
    List<DirectorModel> listOfDireactor = new ArrayList<>();
    List<DirectorModel> listForLast = new ArrayList<>();
    for (RequestStagingDTO.AuthorityDraft authorityDraft : listOfAuthority) {
      DirectorModel model = new DirectorModel();
      if (authorityDraft.getInstructions() != null) {
        model.setName(authorityDraft.getFirstname());
        model.setSurname(authorityDraft.getSurname());
        model.setDesignation(authorityDraft.getDesignation());
        model.setInstructions(authorityDraft.getInstructions());
        int size = listForLast.size();
        model.setUserInList(++size);
        listForLast.add(model);
      } else {
        model.setName(authorityDraft.getFirstname());
        model.setSurname(authorityDraft.getSurname());
        model.setDesignation(authorityDraft.getDesignation());
        int size = listOfDireactor.size();
        model.setUserInList(++size);
        listOfDireactor.add(model);
      }
    }
    requestWrapper.setDirectorModels(listOfDireactor);
    requestWrapper.setListOfDirectors(listForLast);
    requestWrapper.setRequestType(stagingDTO.getRequestType());
    if ("Mandate and Resolution".equalsIgnoreCase(stagingDTO.getRequestType())) {
      requestWrapper.setCheckMandatesAndresolution("true");
      requestWrapper.setCheckStyleOne(stagingDTO.getDraftSigmaConfirmCheck());
      requestWrapper.setCheckStyleTwo(stagingDTO.getDraftWaiverConfirmCheck());
    } else {
      requestWrapper.setCheckMandatesAndresolution("false");
    }

    if ("Mandate".equalsIgnoreCase(stagingDTO.getRequestType())) {
      requestWrapper.setCheckMandates("true");
      requestWrapper.setCheckStyleOne(stagingDTO.getDraftSigmaConfirmCheck());
      requestWrapper.setCheckStyleTwo(stagingDTO.getDraftWaiverConfirmCheck());
    } else {
      requestWrapper.setCheckMandates("false");
    }

    if ("Resolution".equalsIgnoreCase(stagingDTO.getRequestType())) {
      requestWrapper.setCheckResolution("true");
      requestWrapper.setCheckStyleOne(stagingDTO.getDraftSigmaConfirmCheck());
      requestWrapper.setCheckStyleTwo(stagingDTO.getDraftWaiverConfirmCheck());
    } else {
      requestWrapper.setCheckResolution("false");
    }
    requestWrapper.setCheckDirectorButton("true");
    List<RequestStagingDTO.AccountDraft> listOfAccount = stagingDTO.getAccounts();
    if (listOfAccount != null && !listOfAccount.isEmpty()) {
      List<AddAccountModel> listOfAddAccount = new ArrayList<>();
      for (RequestStagingDTO.AccountDraft accountDraft : listOfAccount) {
        AddAccountModel addAccountModel = new AddAccountModel();
        addAccountModel.setAccountName(accountDraft.getAccountName());
        addAccountModel.setAccountNumber(accountDraft.getAccountNumber());
        int size = listOfAddAccount.size();
        addAccountModel.setUserInList(++size);
        List<RequestStagingDTO.SignatoryDraft> listOfSignatory = accountDraft.getSignatories();
        List<SignatoryModel> signatoryList = new ArrayList<>();
        for (RequestStagingDTO.SignatoryDraft signatoryDraft : listOfSignatory) {
          SignatoryModel model = new SignatoryModel();
          model.setFullName(signatoryDraft.getFullName());
          model.setIdNumber(signatoryDraft.getIdNumber());
          model.setInstruction(signatoryDraft.getInstructions());
          model.setCapacity(signatoryDraft.getCapacity());
          model.setGroup(signatoryDraft.getGroupCategory());
          model.setCheckRemoveOption("no");
          model.setCheckEdit("false");
          model.setUserInAccount(++size);
          int sig = signatoryList.size();
          model.setUserInList(++sig);
          model.setCheckDocConfirm("true");
          signatoryList.add(model);
        }
        addAccountModel.setListOfSignatory(signatoryList);
        listOfAddAccount.add(addAccountModel);
      }
      requestWrapper.setListOfAddAccount(listOfAddAccount);
    } else {
      requestWrapper.setListOfAddAccount(null);
    }

    httpSession.setAttribute("RequestWrapper", requestWrapper);
    return requestWrapper;
  }

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
}
