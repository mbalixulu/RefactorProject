package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
      requestWrapper.setCheckStyleOne(user.get("check1"));
      requestWrapper.setCheckStyleTwo(user.get("check2"));
    }
    return requestWrapper;
  }

  public SignatoryModel setSignatory(Map<String, String> user) {
    SignatoryModel signatoryModel = new SignatoryModel();
    signatoryModel.setFullName(user.get("fullName"));
    signatoryModel.setIdNumber(user.get("idNumber"));
    signatoryModel.setInstruction(user.get("accountRef1"));
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

  public List<AddAccountModel> getAllAddSignator(List<AddAccountModel> listOfAccount) {
    List<AddAccountModel> addAccountModelList = new ArrayList<>();
    for (AddAccountModel model : listOfAccount) {
      AddAccountModel addAccountModel = new AddAccountModel();
      List<SignatoryModel> listOfSignatory = new ArrayList<>();
      for (SignatoryModel signatoryModel : model.getListOfSignatory()) {
        if ("Add".equalsIgnoreCase(signatoryModel.getInstruction())) {
          SignatoryModel signatoryData = new SignatoryModel();
          signatoryData.setFullName(signatoryModel.getFullName());
          signatoryData.setIdNumber(signatoryModel.getIdNumber());
          signatoryData.setInstruction(signatoryModel.getInstruction());
          int size = addAccountModelList.size();
          int sig = listOfSignatory.size();
          signatoryData.setUserInAccount(++size);
          signatoryData.setUserInList(++sig);
          listOfSignatory.add(signatoryData);
          addAccountModel.setAccountName(model.getAccountName());
          addAccountModel.setAccountNumber(model.getAccountNumber());
          addAccountModel.setUserInList(++size);
        } else {
          int size = addAccountModelList.size();
          addAccountModel.setAccountName(model.getAccountName());
          addAccountModel.setAccountNumber(model.getAccountNumber());
          addAccountModel.setUserInList(++size);
          addAccountModel.setCheckRemoveSignatory("true");
        }
      }
      addAccountModel.setListOfSignatory(listOfSignatory);
      addAccountModelList.add(addAccountModel);
    }
    return addAccountModelList;
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
      payload.put("draftWaiverConfirmCheck", Boolean.valueOf(requestWrapper.getCheckStyleTwo()));
      payload.put("draftSigmaConfirmCheck", Boolean.valueOf(requestWrapper.getCheckStyleOne()));
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
    RequestWrapper wrapperData =
        (RequestWrapper) httpSession.getAttribute("RequestWrapperData");
    UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
    String url = mandatesResolutionsDaoURL + "/api/request-staging";
    Map<String, Object> payload = new HashMap<>();
    payload.put("companyRegistrationNumber", requestWrapper.getRequest().getRegistrationNumber());
    payload.put("companyName", requestWrapper.getRequest().getCompanyName());
    payload.put("companyAddress", requestWrapper.getRequest().getCompanyAddress());
    if (requestWrapper.getRequestType() != null && requestWrapper.getRequestType() != "") {
      payload.put("requestType", requestWrapper.getRequestType());
      payload.put("draftWaiverConfirmCheck", Boolean.valueOf(requestWrapper.getCheckStyleTwo()));
      payload.put("draftSigmaConfirmCheck", Boolean.valueOf(requestWrapper.getCheckStyleOne()));
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
    List<AddAccountModel> listOFAddAccount = getListAddAccount(requestWrapper.getListOfAddAccount(),
        wrapperData.getListOfAddAccount());
    List<Map<String, Object>> accounts = new ArrayList<>();
    for (AddAccountModel addAccountModel : listOFAddAccount) {
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
    RequestWrapper wrapperData =
        (RequestWrapper) httpSession.getAttribute("RequestWrapperData");
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
    if (wrapper.getRequestType() != null && wrapper.getRequestType() != "") {
      request.put("type", wrapper.getRequestType());
      request.put("waiverConfirmCheck", Boolean.valueOf(wrapper.getCheckStyleTwo()));
      request.put("sigmaConfirmCheck", Boolean.valueOf(wrapper.getCheckStyleOne()));
    }
    request.put("status", "In Progress");
    request.put("subStatus", "Hogan Verification Pending");
    request.put("assignedUser", user.getUsername());
    request.put("creator", user.getUsername());

    Map<String, Object> waiver = new HashMap<>();
    waiver.put("ucn", "UCN-7788");
    waiver.put("permittedTools", "ToolA,ToolB");
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
    List<AddAccountModel> listOFAddAccount = getListAddAccount(wrapper.getListOfAddAccount(),
        wrapperData.getListOfAddAccount());
    List<Map<String, Object>> accounts = new ArrayList<>();
    for (AddAccountModel addAccountModel : listOFAddAccount) {
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
    if (wrapper.getRequestType() != null && wrapper.getRequestType() != "") {
      request.put("type", wrapper.getRequestType());
      request.put("waiverConfirmCheck", Boolean.valueOf(wrapper.getCheckStyleTwo()));
      request.put("sigmaConfirmCheck", Boolean.valueOf(wrapper.getCheckStyleOne()));
    }
    request.put("status", "In Progress");
    request.put("subStatus", "Hogan Verification Pending");
    request.put("assignedUser", user.getUsername());
    request.put("creator", user.getUsername());

    Map<String, Object> waiver = new HashMap<>();
    waiver.put("ucn", "UCN-7788");
    waiver.put("permittedTools", "ToolA,ToolB");
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
    RequestWrapper wrapperData =
        (RequestWrapper) httpSession.getAttribute("RequestWrapperData");
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
    if (wrapper.getRequestType() != null && wrapper.getRequestType() != "") {
      request.put("type", wrapper.getRequestType());
      request.put("waiverConfirmCheck", Boolean.valueOf(wrapper.getCheckStyleTwo()));
      request.put("sigmaConfirmCheck", Boolean.valueOf(wrapper.getCheckStyleOne()));
    }
    request.put("status", "In Progress");
    request.put("subStatus", "Hogan Verification Pending");
    request.put("assignedUser", user.getUsername());
    request.put("creator", user.getUsername());

    Map<String, Object> waiver = new HashMap<>();
    waiver.put("ucn", "UCN-7788");
    waiver.put("permittedTools", "ToolA,ToolB");
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
    List<AddAccountModel> listOFAddAccount = getListAddAccount(wrapper.getListOfAddAccount(),
        wrapperData.getListOfAddAccount());
    List<Map<String, Object>> accounts = new ArrayList<>();
    for (AddAccountModel addAccountModel : listOFAddAccount) {
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

  private List<AddAccountModel> getListAddAccount(List<AddAccountModel> listOne,
                                                  List<AddAccountModel> listTwo) {
    Map<String, AddAccountModel> merged = new HashMap<>();
    for (AddAccountModel acc : listOne) {
      merged.put(acc.getAccountName(), acc);
    }

    for (AddAccountModel acc2 : listTwo) {

      AddAccountModel existing = merged.get(acc2.getAccountName());

      if (existing != null) {
        List<SignatoryModel> mergedSigns = new ArrayList<>(existing.getListOfSignatory());

        for (SignatoryModel newSign : acc2.getListOfSignatory()) {
          Optional<SignatoryModel> found =
              mergedSigns.stream()
                  .filter(s -> s.getIdNumber().equals(newSign.getIdNumber()))
                  .findFirst();

          if (found.isPresent()) {
            SignatoryModel old = found.get();
            if (old.getFullName() == null) {
              old.setFullName(newSign.getFullName());
            }
            if (old.getInstruction() == null) {
              old.setInstruction(newSign.getInstruction());
            }
            if (old.getCapacity() == null) {
              old.setCapacity(newSign.getCapacity());
            }
            if (old.getGroup() == null) {
              old.setGroup(newSign.getGroup());
            }
            if (old.getCheckDocConfirm() == null) {
              old.setCheckDocConfirm(newSign.getCheckDocConfirm());
            }
          } else {
            mergedSigns.add(newSign);
          }
        }
        existing.setListOfSignatory(mergedSigns);
      } else {
        merged.put(acc2.getAccountName(), acc2);
      }
    }
    return new ArrayList<>(merged.values());
  }
}
