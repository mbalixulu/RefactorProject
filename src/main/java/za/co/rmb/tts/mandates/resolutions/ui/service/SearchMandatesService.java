package za.co.rmb.tts.mandates.resolutions.ui.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import za.co.rmb.tts.mandates.resolutions.ui.model.CommentModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.DirectorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.ExportModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.InstructionModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestDetails;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.SignatoryModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.AccountResponseDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.CommentDto;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.CompanyDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.ListOfValuesDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestStagingDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestTableDTO;

/**
 * Service responsible for searching and retrieving mandate and resolution data.
 * This service handles company lookups, draft retrieval, request searches,
 * and data export operations.
 */
@Service
public class SearchMandatesService {

  @Autowired
  private HttpSession httpSession;

  RestTemplate restTemplate = new RestTemplate();

  @Value("${mandates-resolutions-dao}")
  private String mandatesResolutionsDaoURL;

  /**
   * Retrieves company information by registration number.
   *
   * @param registrationNumber Company registration number
   * @return Company details
   */
  public CompanyDTO getCompanyByRegistration(String registrationNumber) {
    String url =
        mandatesResolutionsDaoURL
        + "/api/company/registration?registrationNumber="
        + registrationNumber;
    ResponseEntity<CompanyDTO> response =
        restTemplate.getForEntity(url, CompanyDTO.class, registrationNumber);
    return response.getBody();
  }

  /**
   * Retrieves all draft requests from staging.
   *
   * @return List of draft requests
   */
  public List<RequestStagingDTO> getAllDrafts() {
    String url = mandatesResolutionsDaoURL + "/api/request-staging/all";
    RequestStagingDTO[] raws =
        restTemplate.getForObject(url, RequestStagingDTO[].class);
    List<RequestStagingDTO> list = (raws == null) ? List.of() : Arrays.asList(raws);
    return list;
  }

  /**
   * Retrieves a specific draft request by ID.
   *
   * @param id Staging ID
   * @return Draft request details
   */
  public RequestStagingDTO getDraftsById(Long id) {
    String url = mandatesResolutionsDaoURL + "/api/request-staging/" + id;
    RequestStagingDTO stage =
        restTemplate.getForObject(url, RequestStagingDTO.class, id);
    return stage;
  }

  /**
   * Processes a draft request for viewing and editing.
   *
   * @param stagingDTO The staging DTO to process
   * @return Processed request wrapper
   */
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
          model.setCheckDocConfirm(String.valueOf(signatoryDraft.getSignatoryConfirmCheck()));
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

  /**
   * Exports requests to CSV based on filter criteria.
   *
   * @param exportModel Export filter criteria
   * @return CSV file as byte array
   * @throws IOException If export fails
   */
  public byte[] exportCsv(ExportModel exportModel) throws IOException {
    String url = mandatesResolutionsDaoURL + "/api/request/export";
    Map<String, Object> payload = new HashMap<>();
    payload.put("status", exportModel.getStatus());
    payload.put("fromDate", exportModel.getFromDate());
    payload.put("toDate", exportModel.getToDate());
    payload.put("type", null);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(
        MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )));
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<byte[]> response = restTemplate.exchange(
        url,
        HttpMethod.POST,
        entity,
        byte[].class
    );
    return response.getBody();
  }

  /**
   * Retrieves detailed request information by ID.
   *
   * @param requestId Request ID
   * @return Request details including comments, accounts, and authorities
   * @throws JsonProcessingException If JSON parsing fails
   */
  public RequestDetails getRequestById(Long requestId) throws JsonProcessingException {
    String urlRequest = mandatesResolutionsDaoURL + "/api/submission/" + requestId;
    ResponseEntity<MandateResolutionSubmissionResultDTO> subResp =
        restTemplate.getForEntity(urlRequest, MandateResolutionSubmissionResultDTO.class);
    MandateResolutionSubmissionResultDTO dto = subResp.getBody();
    RequestDetails requestDetails = new RequestDetails();
    requestDetails.setRequestId(dto.getRequest().getRequestId());
    requestDetails.setCompanyId(dto.getCompany().getCompanyId());
    requestDetails.setCompanyName(dto.getCompany().getName());
    requestDetails.setProcessId(dto.getRequest().getProcessId());
    requestDetails.setSla(dto.getRequest().getSla());
    requestDetails.setType(dto.getRequest().getType());
    requestDetails.setUpdatedReq(String.valueOf(dto.getRequest().getUpdated()));
    requestDetails.setStatus(dto.getRequest().getStatus());
    requestDetails.setSubStatus(dto.getRequest().getSubStatus());
    requestDetails.setCreatorRequest(dto.getRequest().getCreator());
    requestDetails.setCreatedReq(String.valueOf(dto.getRequest().getCreated()));
    requestDetails.setUpdatedReq(String.valueOf(dto.getRequest().getUpdated()));
    requestDetails.setUpdatorRequest(dto.getRequest().getUpdator());
    requestDetails.setAssignedUser(dto.getRequest().getAssignedUser());
    String urlComment = mandatesResolutionsDaoURL
                        + "/api/comment/request/" + requestId + "?newestFirst=true";

    ResponseEntity<CommentDto[]> response = restTemplate.exchange(
        urlComment,
        HttpMethod.GET,
        null,
        CommentDto[].class
    );
    List<CommentDto> listOfComment = List.of(response.getBody());
    List<CommentModel> listOfCommentModel = new ArrayList<>();
    for (CommentDto commentDto : listOfComment) {
      CommentModel commentModel = new CommentModel();
      commentModel.setName(commentDto.getCreator());
      commentModel.setCreatedDate(String.valueOf(commentDto.getCreated()));
      commentModel.setCommentedText(commentDto.getCommentText());
      listOfCommentModel.add(commentModel);
    }
    requestDetails.setListOfComment(listOfCommentModel);
    List<MandateResolutionSubmissionResultDTO.Authority> listOfAuthority = dto.getAuthorities();
    List<DirectorModel> listOfDirectorModel = new ArrayList<>();
    for (MandateResolutionSubmissionResultDTO.Authority authority : listOfAuthority) {
      if (authority.getInstructions() != null
          && authority.getInstructions() != "") {
        DirectorModel directorModel = new DirectorModel();
        directorModel.setName(authority.getFirstname());
        directorModel.setSurname(authority.getSurname());
        directorModel.setDesignation(authority.getDesignation());
        directorModel.setInstructions(authority.getInstructions());
        directorModel.setDirectorId(authority.getAuthorityId());
        int size = listOfDirectorModel.size();
        directorModel.setUserInList(++size);
        directorModel.setCheckDelete("No");
        directorModel.setCreator(authority.getCreator());
        directorModel.setUpdator(authority.getUpdator());
        listOfDirectorModel.add(directorModel);
      }
    }
    requestDetails.setListOfDirector(listOfDirectorModel);
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    if ("Mandate".equalsIgnoreCase(dto.getRequest().getType())
        || "Mandate And Resolution".equalsIgnoreCase(dto.getRequest().getType())) {
      String urlAccount = mandatesResolutionsDaoURL + "/api/account/company/"
                          + dto.getCompany().getCompanyId();


      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      ResponseEntity<AccountResponseDTO[]> responseAccount = restTemplate.exchange(
          urlAccount,
          HttpMethod.GET,
          entity,
          AccountResponseDTO[].class
      );
      List<AccountResponseDTO> listOfAccount = List.of(responseAccount.getBody());
      List<AddAccountModel> listOfAddAccount = new ArrayList<>();
      for (AccountResponseDTO account : listOfAccount) {
        AddAccountModel addAccountModel = new AddAccountModel();
        addAccountModel.setAccountName(account.getAccountName());
        addAccountModel.setAccountNumber(account.getAccountNumber());
        addAccountModel.setCheckDelete("No");
        int size = listOfAddAccount.size();
        addAccountModel.setUserInList(++size);
        addAccountModel.setAccountId(account.getAccountId());
        addAccountModel.setCreator(account.getCreator());
        addAccountModel.setUpdator(account.getUpdator());
        List<SignatoryModel> listOfSignatory = new ArrayList<>();
        for (AccountResponseDTO.Signatory signatory : account.getSignatories()) {
          SignatoryModel signatoryModel = new SignatoryModel();
          signatoryModel.setFullName(signatory.getFullName());
          signatoryModel.setGroup(signatory.getGroupCategory());
          signatoryModel.setCapacity(signatory.getCapacity());
          signatoryModel.setInstruction(signatory.getInstructions());
          signatoryModel.setIdNumber(signatory.getIdNumber());
          signatoryModel.setCheckRemoveOption("no");
          signatoryModel.setCheckEdit("false");
          int sig = listOfSignatory.size();
          signatoryModel.setUserInList(++sig);
          signatoryModel.setUserInAccount(++size);
          signatoryModel.setCreator(signatory.getCreator());
          signatoryModel.setUpdator(signatory.getUpdator());
          signatoryModel.setSignatoryId(signatory.getSignatoryId());
          listOfSignatory.add(signatoryModel);
        }
        addAccountModel.setListOfSignatory(listOfSignatory);
        listOfAddAccount.add(addAccountModel);
      }
      requestDetails.setListOfAddAccountModel(listOfAddAccount);
    }

    if (!"On Hold".equalsIgnoreCase(requestDetails.getStatus())
        && !"Completed".equalsIgnoreCase(requestDetails.getStatus())
        && !"Auto Closed".equalsIgnoreCase(requestDetails.getStatus())) {
      String url = mandatesResolutionsDaoURL + "/api/lov"
                   + "?type=Readout&subType=Instructions&requestStatus="
                   + dto.getRequest().getSubStatus();

      ResponseEntity<ListOfValuesDTO[]> responseLov =
          restTemplate.exchange(url, HttpMethod.GET, entity, ListOfValuesDTO[].class);

      ListOfValuesDTO[] lovArray = responseLov.getBody();

      String valueJson = lovArray[0].getValue();

      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, String>> instructionMapList =
          mapper.readValue(valueJson, new TypeReference<List<Map<String, String>>>() {
          });
      List<String> instructions = instructionMapList.stream()
          .flatMap(map -> map.values().stream())
          .collect(Collectors.toList());
      List<InstructionModel> listOfInstruction = new ArrayList<>();
      for (String str : instructions) {
        InstructionModel instructionModel = new InstructionModel();
        instructionModel.setInstruction(str);
        listOfInstruction.add(instructionModel);
      }
      requestDetails.setListOfInstruction(listOfInstruction);
    }
    return requestDetails;
  }

  /**
   * Retrieves all request records.
   *
   * @return List of all request records
   */
  public List<RequestTableDTO> getAllRecords() {
    String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";
    ResponseEntity<RequestTableDTO[]> response =
        restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
    List<RequestTableDTO> listOfRecord = Arrays.asList(response.getBody());
    return listOfRecord;
  }
}
