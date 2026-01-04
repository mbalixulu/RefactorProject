package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO;

/**
 * Service responsible for capturing and submitting mandate requests.
 * This service handles the creation and submission of mandate, resolution,
 * and combined mandate-resolution requests.
 */
@Service
public class MandateCaptureService {

  @Autowired
  private HttpSession httpSession;

  RestTemplate restTemplate = new RestTemplate();

  @Value("${mandates-resolutions-dao}")
  private String mandatesResolutionsDaoURL;

  /**
   * Sends a request to staging as a draft.
   *
   * @return Response body from the staging API
   */
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

  /**
   * Sends a request for signature card to staging.
   *
   * @return Response body from the staging API
   */
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

  /**
   * Creates a mandate request with accounts and authorities.
   *
   * @return Response body from the submission API
   */
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

  /**
   * Creates a resolution request with authorities only.
   *
   * @return Response body from the submission API
   */
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

  /**
   * Creates a mandate-only request.
   *
   * @return Response body from the submission API
   */
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
}
