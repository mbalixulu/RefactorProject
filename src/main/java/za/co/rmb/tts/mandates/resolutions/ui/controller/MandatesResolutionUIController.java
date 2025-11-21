package za.co.rmb.tts.mandates.resolutions.ui.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import za.co.rmb.tts.mandates.resolutions.ui.model.AddAccountModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.DirectorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestTableWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.SignatoryModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.AccountDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.CompanyDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.ListOfValuesDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestStagingDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestTableDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.SignatoryDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.SubmissionPayload;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ApproveRejectErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.DirectorErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.MandatesAutoFillErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.MandatesSignatureCardErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.ResolutionsAutoFillErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SearchResultsErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SignatoryErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.service.MandatesResolutionService;
import za.co.rmb.tts.mandates.resolutions.ui.service.XSLTProcessorService;
import za.co.rmb.tts.mandates.resolutions.ui.util.ScreenValidation;

@RestController
@RequestMapping("/mandates-and-resolutions")
public class MandatesResolutionUIController {

  private final XSLTProcessorService xsltProcessor;
  private HttpSession httpSession;
  private final MandatesResolutionService mandatesResolutionService;
  private final ScreenValidation screenValidation;
  private final Map<String, RequestDTO> pdfExtractionDataCache = new HashMap<>();

  private static final String XML_PAGE_PATH = "/templates/xml/";
  private static final String XSL_PAGE_PATH = "/templates/xsl/";
  private static final Logger logger =
      LoggerFactory.getLogger(MandatesResolutionUIController.class);

  private static final org.slf4j.Logger EXPORT_LOG =
      org.slf4j.LoggerFactory.getLogger("MandatesResolutionsUIController.Export");

  private final RestTemplate restTemplate = new RestTemplate();

  private static final long MAX_FILE_BYTES = 10L * 1024L * 1024L; // 10MB

  @Value("${tts.dms.base-url}")
  private String ttsDmsBaseUrl; // e.g. http://localhost:8084/api/v1/documents

  @Value("${mandates-resolutions-dao}")
  private String mandatesResolutionsDaoURL; //e.g http://localhost:8083

  public MandatesResolutionUIController(XSLTProcessorService xsltProcessor,
                                        HttpSession httpSession,
                                        MandatesResolutionService mandatesResolutionService,
                                        ScreenValidation screenValidation) {
    this.xsltProcessor = xsltProcessor;
    this.httpSession = httpSession;
    this.mandatesResolutionService = mandatesResolutionService;
    this.screenValidation = screenValidation;
  }

  @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mandateResolutionsLanding(HttpServletRequest request,
                                                          HttpSession session) {
    try {
      session.invalidate();
      HttpSession newSession = request.getSession(true);
      final String employeeNumber =
          request.getParameter("bifrost.online.header.assisted.employeeNumber");
      if (employeeNumber == null || employeeNumber.isBlank()) {
        return ResponseEntity.ok(generateErrorPage("Missing Employee Number."));
      }
      final String backendUrl = mandatesResolutionsDaoURL + "/api/user/username/" + employeeNumber;
      ResponseEntity<UserDTO> resp =
          restTemplate.exchange(backendUrl, HttpMethod.GET, null, UserDTO.class);
      if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
        return ResponseEntity.ok(generateErrorPage("Invalid Employee Number."));
      }
      UserDTO user = resp.getBody();
      if (user.getEmployeeNumber() == null || user.getEmployeeNumber().isBlank()) {
        user.setEmployeeNumber(employeeNumber);
      }
      newSession.setAttribute("currentUser", user);
      String roleUp = (user.getUserRole() == null) ? "" : user.getUserRole().trim().toUpperCase();
      logger.info("Landing role resolved to: '{}'", roleUp);
      if (roleUp.contains("ADMIN")) {
        return displayAdminAll();
      } else {
        return goToDisplayRequestTable();
      }
    } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
      return ResponseEntity.ok(generateErrorPage("Invalid Employee Number."));
    } catch (Exception e) {
      return ResponseEntity.ok(generateErrorPage("Landing page is unavailable at this moment."));
    }
  }

  @PostMapping(value = "/logout", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> logout(HttpServletRequest request) {
    HttpSession s = request.getSession(false);
    if (s != null) {
      s.invalidate();
    }
    String page = xsltProcessor.generatePage(xslPagePath("LogoutPage"), new RequestWrapper());
    return ResponseEntity.ok(page);
  }

  @PostMapping(
      value = "/createRequest",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE }
  )
  public ResponseEntity<String> displayCreateRequest() {
    RequestWrapper requestWrapper = new RequestWrapper();
    requestWrapper.setCheckCreate("false");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    String page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/searchCompanyDetails", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> fetchMergedDetails(
      @ModelAttribute RequestDTO requestDto,
      @RequestParam Map<String, String> user,
      HttpServletRequest request) {
    boolean check = false;
    String page = "";
    RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    SearchResultsErrorModel searchResultsErrorModel = new SearchResultsErrorModel();
    String registrationNumber = null;
    if (user.get("companyRegNumber").isBlank() || user.get("companyRegNumber") == "") {
      searchResultsErrorModel.setRegiNumber("Company Registration Number can't be empty !");
      check = true;
    } else {
      registrationNumber = user.get("companyRegNumber");
    }
    Function<String, String> nz = s -> s == null ? "" : s.trim();
    Function<String, String> dedupeComma = s -> {
      String t = nz.apply(s);
      if (t.isEmpty()) {
        return t;
      }
      String[] parts = t.split("\\s*,\\s*");
      if (parts.length <= 1) {
        return t;
      }
      LinkedHashSet<String> set = new LinkedHashSet<>();
      for (String p : parts) {
        if (!p.isBlank()) {
          set.add(p);
        }
      }
      return String.join(", ", set);
    };
    Function<String, String> normReg = s -> {
      String t = nz.apply(s);
      return t.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    };
    String incomingReg = nz.apply(requestDto != null ? requestDto.getRegistrationNumber() : null);
    if (incomingReg.isBlank()) {
      incomingReg = nz.apply(registrationNumber);
    }
    boolean fromCreateSearch =
        "POST".equalsIgnoreCase(request.getMethod())
        && "create".equals(request.getParameter("origin"))
        && request.getParameter("companyRegNumber") != null;
    String daoName = null;
    String daoAddr = null;
    Map<String, Object> company = new HashMap<>();
    if (fromCreateSearch && !incomingReg.isBlank()) {
      String url = UriComponentsBuilder
          .fromHttpUrl(mandatesResolutionsDaoURL)
          .pathSegment("api", "company", "registration")
          .queryParam("registrationNumber", incomingReg)
          .toUriString();
      try {
        company = restTemplate.getForObject(url, Map.class);
        if (company == null) {
          return renderCreateRequestWithInline(incomingReg,
              "Company Registration Number not found.", "NOT_FOUND");
        }
        Object n = company.get("name");
        Object a = company.get("address");
        daoName = n == null ? "" : n.toString().trim();
        daoAddr = a == null ? "" : a.toString().trim();
      } catch (HttpStatusCodeException ex) {
        return renderCreateRequestWithInline(incomingReg,
            "Company Registration Number not found.", "NOT_FOUND");
      } catch (Exception ex) {
        return renderCreateRequestWithInline(incomingReg,
            "Company Registration Number not found.", "NOT_FOUND");
      }
    }
    RequestDTO dto = new RequestDTO();
    String currentReg = (dto != null) ? nz.apply(dto.getRegistrationNumber()) : "";
    boolean regChanged = (!incomingReg.isBlank()
                          && !normReg.apply(incomingReg).equals(normReg.apply(currentReg)));
    if (regChanged) {
      dto = new RequestDTO();
      dto.setRegistrationNumber(incomingReg);
    } else {
      if (dto == null) {
        dto = new RequestDTO();
      }
      if ((dto.getRegistrationNumber() == null || dto.getRegistrationNumber().isBlank())
          && !incomingReg.isBlank()) {
        dto.setRegistrationNumber(incomingReg);
      }
    }
    if (requestDto != null) {
      String nm = dedupeComma.apply(requestDto.getCompanyName());
      if (!nm.isBlank()) {
        dto.setCompanyName(nm);
      }
      String addr = dedupeComma.apply(requestDto.getCompanyAddress());
      if (!addr.isBlank()) {
        dto.setCompanyAddress(addr);
      }
      String regModel = nz.apply(requestDto.getRegistrationNumber());
      if (!regModel.isBlank()
          && normReg.apply(regModel).equals(normReg.apply(dto.getRegistrationNumber()))) {
        dto.setRegistrationNumber(regModel);
      }
    }
    if (fromCreateSearch) {
      if (daoName != null && !daoName.isBlank()) {
        dto.setCompanyName(daoName);
      }
      if (daoAddr != null && !daoAddr.isBlank()) {
        dto.setCompanyAddress(daoAddr);
      }
    }
    dto.setEditable(true);
    wrapper.setRequest(dto);
    httpSession.setAttribute("requestData", dto);
    httpSession.setAttribute("RequestWrapper", wrapper);
    if (check) {
      wrapper.setSearchResultsErrorModel(searchResultsErrorModel);
      page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), wrapper);
    } else {
      if (company != null) {
        page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
      } else {
        page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), wrapper);
      }
    }
    return ResponseEntity.ok(page);
  }

  private ResponseEntity<String> renderCreateRequestWithInline(String registrationNumber,
                                                               String message, String code) {
    RequestDTO dto = new RequestDTO();
    dto.setRegistrationNumber(registrationNumber == null ? "" : registrationNumber.trim());
    dto.setErrorMessage(message);
    dto.setErrorCode(code); //"REQUIRED" or "NOT_FOUND"
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), wrapper);
    return ResponseEntity.ok(page);
  }

  private ResponseEntity<String> renderCreateRequestWithInline(
      HttpSession session,
      String registrationNumber,
      String message
  ) {
    return renderCreateRequestWithInline(registrationNumber, message, null);
  }

  @PostMapping(value = "/backCreateReq", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backPopup() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    RequestDTO dto = (RequestDTO) httpSession.getAttribute("requestData");
    dto.setCompanyAddress(null);
    dto.setCompanyName(null);
    requestWrapper.setRequest(dto);
    requestWrapper.setCheckDirectorEmpty("false");
    requestWrapper.setSearchResultsErrorModel(null);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("CreateRequest"),
        (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/tablePopup", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> tablePopup(@RequestParam Map<String, String> wave) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    RequestDTO dto = (RequestDTO) httpSession.getAttribute("requestData");
    dto.setCompanyName(wave.get("companyName"));
    dto.setCompanyAddress(wave.get("companyAddress"));
    requestWrapper.setRequest(dto);
    if (!wave.get("toolOne").isBlank()) {
      requestWrapper.setToolOne(wave.get("toolOne"));
    }

    if (!wave.get("toolTwo").isBlank()) {
      requestWrapper.setToolTwo(wave.get("toolTwo"));
    }

    if (!wave.get("toolThree").isBlank()) {
      requestWrapper.setToolThree(wave.get("toolThree"));
    }

    if (!wave.get("toolFour").isBlank()) {
      requestWrapper.setToolFour(wave.get("toolFour"));
    }

    if (!wave.get("toolFive").isBlank()) {
      requestWrapper.setToolFive(wave.get("toolFive"));
    }
    requestWrapper.setRequestType(wave.get("mandateResolution"));
    if (!wave.get("mandateResolution").isBlank()) {
      requestWrapper.setCheckStyleOne(wave.get("check1"));
      requestWrapper.setCheckStyleTwo(wave.get("check2"));
    }
    requestWrapper.setCheckDirectorEmpty("false");
    requestWrapper.setSearchResultsErrorModel(null);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    DirectorModel directorModel = new DirectorModel();
    directorModel.setButtonCheck("true");
    directorModel.setPageCheck("false");
    httpSession.setAttribute("Dirctors", directorModel);
    page = xsltProcessor.generatePage(xslPagePath("Directors"), directorModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/tablePopupReso", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> tablePopupReso(@RequestParam Map<String, String> wave) {
    String page = "";
    DirectorModel directorModel = new DirectorModel();
    directorModel.setButtonCheck("true");
    directorModel.setPageCheck("true");
    httpSession.setAttribute("DirctorsNew", directorModel);
    page = xsltProcessor.generatePage(xslPagePath("Directors"), directorModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/backDirectorPopup", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backDirectorPopup() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    requestWrapper.setCheckDirectorEmpty("false");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("SearchResults"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/backDirectorPopupReso", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backDirectorPopupReso() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("Resolutions"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/submitAdminDetails", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitAdminDetails(@RequestParam Map<String, String> admin) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    boolean check = false;
    List<DirectorModel> directorModelList = requestWrapper.getDirectorModels();
    if (directorModelList == null) {
      directorModelList = new ArrayList<>();
    }
    DirectorErrorModel dirctorErrorModel = new DirectorErrorModel();
    DirectorModel directorModel = mandatesResolutionService.setAllDirctors(admin,
        "false");
    if (admin.get("name").isBlank()) {
      dirctorErrorModel.setName("Name can't be empty !");
      check = true;
    }

    if (admin.get("designation").isBlank()) {
      dirctorErrorModel.setDesignation("Designation can't be empty !");
      check = true;
    }

    if (admin.get("surname").isBlank()) {
      dirctorErrorModel.setSurname("Surname can't be empty !");
      check = true;
    }

    if (check) {
      directorModel.setDirectorErrorModel(dirctorErrorModel);
      page = xsltProcessor.generatePage(xslPagePath("Directors"), directorModel);
    } else {
      int size = directorModelList.size();
      directorModel.setUserInList(++size);
      directorModelList.add(directorModel);
      requestWrapper.setDirectorModels(directorModelList);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
      page = xsltProcessor.generatePage(xslPagePath("SearchResults"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/submitAdminDetailsReso", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitAdminDetailsReso(@RequestParam Map<String, String> admin) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    boolean check = false;
    List<DirectorModel> directorModelList = requestWrapper.getListOfDirectors();
    if (directorModelList == null) {
      directorModelList = new ArrayList<>();
    }
    DirectorErrorModel dirctorErrorModel = new DirectorErrorModel();
    DirectorModel directorModel = mandatesResolutionService.setAllDirctors(admin,
        "true");
    if (admin.get("name").isBlank()) {
      dirctorErrorModel.setName("Name can't be empty !");
      check = true;
    }

    if (admin.get("designation").isBlank()) {
      dirctorErrorModel.setDesignation("Designation can't be empty !");
      check = true;
    }

    if (admin.get("surname").isBlank()) {
      dirctorErrorModel.setSurname("Surname can't be empty !");
      check = true;
    }

    if (admin.get("instructions").isBlank()
        || "Please select".equalsIgnoreCase(admin.get("instructions"))) {
      dirctorErrorModel.setInstruction("Instruction can't be empty or Please select !");
      check = true;
    }

    if (check) {
      directorModel.setDirectorErrorModel(dirctorErrorModel);
      page = xsltProcessor.generatePage(xslPagePath("Directors"), directorModel);
    } else {
      int size = directorModelList.size();
      directorModel.setUserInList(++size);
      directorModelList.add(directorModel);
      requestWrapper.setListOfDirectors(directorModelList);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
      page = xsltProcessor.generatePage(xslPagePath("Resolutions"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/removeDirector/{userInList}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> removeDirector(@PathVariable String userInList,
                                               @RequestParam Map<String, String> user) {
    String page = "";
    mandatesResolutionService.removeSpecificAdmin(Integer.valueOf(userInList));
    RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/removeDirectorReso/{userInList}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> removeDirectorReso(@PathVariable String userInList,
                                                   @RequestParam Map<String, String> user) {
    String page = "";
    mandatesResolutionService.removeSpecificAdminReso(Integer.valueOf(userInList));
    RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    page = xsltProcessor.generatePage(xslPagePath("Resolutions"), wrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/editDirector/{userInList}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editDirector(@PathVariable String userInList) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<DirectorModel> listOfDirector = requestWrapper.getDirectorModels();
    DirectorModel directorModels = (DirectorModel) httpSession.getAttribute("Dirctors");
    directorModels = mandatesResolutionService.getDirectorDetails(directorModels,
        listOfDirector,
        userInList);
    directorModels.setButtonCheck("false");
    directorModels.setPageCheck("false");
    page = xsltProcessor.generatePage(xslPagePath("Directors"), directorModels);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/editDirectorReso/{userInList}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editDirectorReso(@PathVariable String userInList) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<DirectorModel> listOfDirector = requestWrapper.getListOfDirectors();
    DirectorModel directorModels = (DirectorModel) httpSession.getAttribute("DirctorsNew");
    directorModels = mandatesResolutionService.getDirectorDetailsReso(directorModels,
        listOfDirector,
        userInList);
    directorModels.setButtonCheck("false");
    directorModels.setPageCheck("true");
    page = xsltProcessor.generatePage(xslPagePath("Directors"), directorModels);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/updateDirectors/{userInList}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> updateDirectors(@RequestParam Map<String, String> admin,
                                                @PathVariable String userInList) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    boolean check = false;
    List<DirectorModel> directorModelList = requestWrapper.getDirectorModels();
    DirectorErrorModel dirctorErrorModel = new DirectorErrorModel();
    DirectorModel listofDirectors = (DirectorModel) httpSession.getAttribute("Dirctors");
    if (admin.get("name").isBlank()) {
      dirctorErrorModel.setName("Name can't be empty !");
      check = true;
    }

    if (admin.get("designation").isBlank()) {
      dirctorErrorModel.setDesignation("Designation can't be empty !");
      check = true;
    }

    if (admin.get("surname").isBlank()) {
      dirctorErrorModel.setSurname("Surname can't be empty !");
      check = true;
    }

    if (check) {
      listofDirectors.setDirectorErrorModel(dirctorErrorModel);
      listofDirectors.setButtonCheck("false");
      page = xsltProcessor.generatePage(xslPagePath("Directors"), listofDirectors);
    } else {
      directorModelList =
          mandatesResolutionService.getUpdatedDirector(directorModelList, userInList, admin);
      requestWrapper.setDirectorModels(directorModelList);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
      page = xsltProcessor.generatePage(xslPagePath("SearchResults"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/updateDirectorsReso/{userInList}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> updateDirectorsReso(@RequestParam Map<String, String> admin,
                                                    @PathVariable String userInList) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    boolean check = false;
    List<DirectorModel> directorModelList = requestWrapper.getListOfDirectors();
    DirectorErrorModel dirctorErrorModel = new DirectorErrorModel();
    DirectorModel listofDirectors = (DirectorModel) httpSession.getAttribute("DirctorsNew");
    if (admin.get("name").isBlank()) {
      dirctorErrorModel.setName("Name can't be empty !");
      check = true;
    }

    if (admin.get("designation").isBlank()) {
      dirctorErrorModel.setDesignation("Designation can't be empty !");
      check = true;
    }

    if (admin.get("surname").isBlank()) {
      dirctorErrorModel.setSurname("Surname can't be empty !");
      check = true;
    }

    if (admin.get("instructions").isBlank()
        || "Please select".equalsIgnoreCase(admin.get("instructions"))) {
      dirctorErrorModel.setInstruction("Instruction can't be empty or Please select !");
      check = true;
    }

    if (check) {
      listofDirectors.setDirectorErrorModel(dirctorErrorModel);
      listofDirectors.setButtonCheck("false");
      page = xsltProcessor.generatePage(xslPagePath("Directors"), listofDirectors);
    } else {
      directorModelList =
          mandatesResolutionService.getUpdatedDirectorReso(directorModelList, userInList, admin);
      requestWrapper.setDirectorModels(directorModelList);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
      page = xsltProcessor.generatePage(xslPagePath("Resolutions"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/requestType", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> requestType(@RequestParam Map<String, String> user) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");

    if ("Mandate".equalsIgnoreCase(user.get("mandateResolution"))) {
      requestWrapper.setCheckMandates("true");
      requestWrapper.setCheckResolution("false");
      requestWrapper.setCheckMandatesAndresolution("false");
      requestWrapper.setCheckStyleOne("false");
      requestWrapper.setCheckStyleTwo("false");
    }

    if ("Resolution".equalsIgnoreCase(user.get("mandateResolution"))) {
      requestWrapper.setCheckResolution("true");
      requestWrapper.setCheckMandates("false");
      requestWrapper.setCheckMandatesAndresolution("false");
      requestWrapper.setCheckStyleOne("false");
      requestWrapper.setCheckStyleTwo("false");
    }

    if ("Mandate And Resolution".equalsIgnoreCase(user.get("mandateResolution"))) {
      requestWrapper.setCheckMandatesAndresolution("true");
      requestWrapper.setCheckMandates("false");
      requestWrapper.setCheckResolution("false");
      requestWrapper.setCheckStyleOne("false");
      requestWrapper.setCheckStyleTwo("false");
    }
    RequestDTO dto = (RequestDTO) httpSession.getAttribute("requestData");
    dto.setCompanyName(user.get("companyName"));
    dto.setCompanyAddress(user.get("companyAddress"));
    requestWrapper.setRequest(dto);
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
    requestWrapper.setCheckDirectorEmpty("false");
    requestWrapper.setSearchResultsErrorModel(null);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("SearchResults"),
        (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/proceedToAccount",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE })
  public ResponseEntity<String> proceedToAccount(@RequestParam Map<String, String> user) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    SearchResultsErrorModel searchResultsErrorModel = new SearchResultsErrorModel();
    boolean check = false;
    String page = "";
    requestWrapper = mandatesResolutionService.setSearchResult(user);
    if (user.get("companyName").isBlank()) {
      searchResultsErrorModel.setCompanyName("Campany Name can't be empty !");
      check = true;
    }

    if (user.get("companyAddress").isBlank()) {
      searchResultsErrorModel.setCompanyAddress("Company Address can't be empty !");
      check = true;
    }

    if (user.get("toolOne").isBlank() && user.get("toolTwo").isBlank()
        && user.get("toolThree").isBlank() && user.get("toolFour").isBlank()
        && user.get("toolFive").isBlank()) {
      searchResultsErrorModel.setToolOne("At least One Waiver tool need for the Request !");
      check = true;
    }

    List<DirectorModel> directors = requestWrapper.getDirectorModels();
    if (directors == null || directors.isEmpty()) {
      requestWrapper.setCheckDirectorEmpty("true");
      check = true;
    } else {
      requestWrapper.setCheckDirectorEmpty("false");
    }

    if (user.get("mandateResolution").isBlank() || "Please select".equalsIgnoreCase(
        user.get("mandateResolution"))) {
      searchResultsErrorModel.setRequestType("Request Type can't be empty and Please select !");
      check = true;
    } else {
      if (user.get("check1").equalsIgnoreCase("false")) {
        searchResultsErrorModel.setCheckStyleOne("Select the Check box !");
        check = true;
      }
      if (user.get("check2").equalsIgnoreCase("false")) {
        searchResultsErrorModel.setCheckStyleTwo("Select the Check box !");
        check = true;
      }
    }

    if (requestWrapper.getListOfAddAccount() == null) {
      requestWrapper.setAccountCheck("false");
    } else {
      requestWrapper.setAccountCheck("true");
    }

    requestWrapper.setSearchResultsErrorModel(searchResultsErrorModel);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    if (check) {
      page = xsltProcessor.generatePage(xslPagePath("SearchResults"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    } else {
      page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/proceedToAccountReso",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE })
  public ResponseEntity<String> proceedToAccountReso(@RequestParam Map<String, String> user) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    SearchResultsErrorModel searchResultsErrorModel = new SearchResultsErrorModel();
    boolean check = false;
    String page = "";
    requestWrapper = mandatesResolutionService.setSearchResult(user);
    if (user.get("companyName").isBlank()) {
      searchResultsErrorModel.setCompanyName("Campany Name can't be empty !");
      check = true;
    }

    if (user.get("companyAddress").isBlank()) {
      searchResultsErrorModel.setCompanyAddress("Company Address can't be empty !");
      check = true;
    }
    List<DirectorModel> directors = requestWrapper.getDirectorModels();
    if (directors == null || directors.isEmpty()) {
      requestWrapper.setCheckDirectorEmpty("true");
      check = true;
    } else {
      requestWrapper.setCheckDirectorEmpty("false");
    }

    if (user.get("mandateResolution").isBlank() || "Please select".equalsIgnoreCase(
        user.get("mandateResolution"))) {
      searchResultsErrorModel.setRequestType("Request Type can't be empty and Please select !");
      check = true;
    } else {
      if (user.get("check1").equalsIgnoreCase("false")) {
        searchResultsErrorModel.setCheckStyleOne("Select the Check box !");
        check = true;
      }
      if (user.get("check2").equalsIgnoreCase("false")) {
        searchResultsErrorModel.setCheckStyleTwo("Select the Check box !");
        check = true;
      }
    }

    if (requestWrapper.getListOfAddAccount() == null) {
      requestWrapper.setAccountCheck("false");
    } else {
      requestWrapper.setAccountCheck("true");
    }

    requestWrapper.setSearchResultsErrorModel(searchResultsErrorModel);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    if (check) {
      page = xsltProcessor.generatePage(xslPagePath("SearchResults"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    } else {
      page = xsltProcessor.generatePage(xslPagePath("Resolutions"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/searchAccountSave",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE })
  public ResponseEntity<String> searchAccountSave(@RequestParam Map<String, String> user) {
    RequestWrapper requestWrapper =
        requestWrapper = mandatesResolutionService.setSearchResult(user);
    requestWrapper.setStepForSave("Step 1");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    mandatesResolutionService.sendRequestStaging();
    UserDTO dto = (UserDTO) httpSession.getAttribute("currentUser");
    if ("ADMIN".equalsIgnoreCase(dto.getUserRole())) {
      return displayAdminApproval();
    } else {
      return goToDisplayRequestTable();
    }
  }

  @PostMapping(value = "/backToAccountSearch", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backToAccountSearch() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    requestWrapper.setCheckDirectorEmpty("false");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("SearchResults"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/addAccount", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> addAccount() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    AddAccountModel addAccountModel = new AddAccountModel();
    addAccountModel.setButtonCheck("false");
    httpSession.setAttribute("Signatory", addAccountModel);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("AddAccount"),
        (AddAccountModel) httpSession.getAttribute("Signatory"));
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/cancelAddAccount", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> cancelAddAccount() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    if (requestWrapper.getListOfAddAccount() == null) {
      requestWrapper.setAccountCheck("false");
    } else {
      requestWrapper.setAccountCheck("true");
    }
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    addAccountModel.setCheckSignatoryList("false");
    httpSession.setAttribute("Signatory", addAccountModel);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/signatoryTablePopup", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> openSignatoryPopup(@RequestParam Map<String, String> user) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");

    if (requestWrapper.getListOfAddAccount() == null) {
      requestWrapper.setAccountCheck("false");
    } else {
      requestWrapper.setAccountCheck("true");
    }
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    addAccountModel.setAccountName(user.get("accountName"));
    addAccountModel.setAccountNumber(user.get("accountNo"));
    SignatoryModel signatoryModel = new SignatoryModel();
    signatoryModel.setButtonCheck("true");
    addAccountModel.setCheckSignatoryList("false");
    httpSession.setAttribute("Signatory", addAccountModel);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("AddSignatory"), signatoryModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/backToAddAccount", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backToAddAccount() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    if (requestWrapper.getListOfAddAccount() == null) {
      requestWrapper.setAccountCheck("false");
    } else {
      requestWrapper.setAccountCheck("true");
    }
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    page = xsltProcessor.generatePage(xslPagePath("AddAccount"), addAccountModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/submitSignatoryDetails", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitSignatory(@RequestParam Map<String, String> user) {
    String page = "";
    boolean check = false;
    SignatoryModel signatoryModel = new SignatoryModel();
    SignatoryErrorModel signatoryErrorModel = new SignatoryErrorModel();
    if (user.get("fullName").isBlank()) {
      signatoryErrorModel.setFullName("Full Name can't be empty !");
      check = true;
    }

    if (user.get("idNumber").isBlank()) {
      signatoryErrorModel.setIdNumber("Id number can't be empty !");
      check = true;
    }

    if (!screenValidation.validateSaIdNumber(user.get("idNumber"))) {
      signatoryErrorModel.setIdNumber("Provide your Correct SA Id Number !");
      check = true;
    }

    if (user.get("accountRef1").isBlank() || "Please select".equalsIgnoreCase(
        user.get("accountRef1"))) {
      signatoryErrorModel.setInstruction("Instruction can't be empty or Please select !");
      check = true;
    }
    if (check) {
      signatoryModel.setSignatoryErrorModel(signatoryErrorModel);
      signatoryModel.setButtonCheck("true");
      signatoryModel.setFullName(user.get("fullName"));
      signatoryModel.setIdNumber(user.get("idNumber"));
      signatoryModel.setInstruction(user.get("accountRef1"));
      page = xsltProcessor.generatePage(xslPagePath("AddSignatory"), signatoryModel);
    } else {
      AddAccountModel addAccountModel =
          (AddAccountModel) httpSession.getAttribute("Signatory");
      List<SignatoryModel> signatoryModels = addAccountModel.getListOfSignatory();
      if (signatoryModels == null) {
        signatoryModels = new ArrayList<>();
      }
      SignatoryModel signatoryModelData = mandatesResolutionService.setSignatory(user);
      int size = signatoryModels.size();
      signatoryModelData.setUserInList(++size);
      signatoryModels.add(signatoryModelData);
      addAccountModel.setListOfSignatory(signatoryModels);
      httpSession.setAttribute("Signatory", addAccountModel);
      page = xsltProcessor.generatePage(xslPagePath("AddAccount"),
          (AddAccountModel) httpSession.getAttribute("Signatory"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/SignatoryRemove/{userInList}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> signatoryRemove(@PathVariable String userInList) {
    String page = "";
    mandatesResolutionService.removeSpecificSignatory(Integer.valueOf(userInList));
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    page = xsltProcessor.generatePage(xslPagePath("AddAccount"), addAccountModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/SignatoryEdit/{userInList}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> signatoryEdit(@PathVariable String userInList) {
    String page = "";
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    SignatoryModel signatoryModel = new SignatoryModel();
    signatoryModel = mandatesResolutionService.getSignatory(signatoryModel,
        addAccountModel.getListOfSignatory(), userInList);
    signatoryModel.setButtonCheck("false");
    page = xsltProcessor.generatePage(xslPagePath("AddSignatory"), signatoryModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/EditSignatoryDetails/{userInList}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editSignatory(@RequestParam Map<String, String> user,
                                              @PathVariable String userInList) {
    String page = "";
    boolean check = false;
    SignatoryModel signatoryModel = new SignatoryModel();
    SignatoryErrorModel signatoryErrorModel = new SignatoryErrorModel();
    if (user.get("fullName").isBlank()) {
      signatoryErrorModel.setFullName("Full Name can't be empty !");
      check = true;
    }

    if (user.get("idNumber").isBlank()) {
      signatoryErrorModel.setIdNumber("Id number can't be empty !");
      check = true;
    }

    if (!screenValidation.validateSaIdNumber(user.get("idNumber"))) {
      signatoryErrorModel.setIdNumber("Provide your Correct SA Id Number !");
      check = true;
    }

    if (user.get("accountRef1").isBlank() || "Please select".equalsIgnoreCase(
        user.get("accountRef1"))) {
      signatoryErrorModel.setInstruction("Instruction can't be empty or Please select !");
      check = true;
    }
    if (check) {
      signatoryModel.setFullName(user.get("fullName"));
      signatoryModel.setIdNumber(user.get("idNumber"));
      signatoryModel.setInstruction(user.get("accountRef1"));
      signatoryModel.setButtonCheck("false");
      signatoryModel.setSignatoryErrorModel(signatoryErrorModel);
      page = xsltProcessor.generatePage(xslPagePath("AddSignatory"), signatoryModel);
    } else {
      AddAccountModel addAccountModel =
          (AddAccountModel) httpSession.getAttribute("Signatory");
      List<SignatoryModel> signatoryModels =
          mandatesResolutionService.getUpdatedSignatory(addAccountModel.getListOfSignatory(),
              userInList, user);
      addAccountModel.setListOfSignatory(signatoryModels);
      httpSession.setAttribute("Signatory", addAccountModel);
      page = xsltProcessor.generatePage(xslPagePath("AddAccount"),
          (AddAccountModel) httpSession.getAttribute("Signatory"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/addSignatoryWithAccount", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> addSignatoryWithAccount(@RequestParam Map<String, String> user) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<AddAccountModel> addAccountModelList = requestWrapper.getListOfAddAccount();
    if (addAccountModelList == null) {
      addAccountModelList = new ArrayList<>();
    }
    AddAccountModel addAccountModel =
        (AddAccountModel) httpSession.getAttribute("Signatory");
    boolean check = false;
    SignatoryErrorModel signatoryErrorModel = new SignatoryErrorModel();
    if (user.get("accountName").isBlank()) {
      signatoryErrorModel.setAccountName("Account Name can't be empty !");
      check = true;
    }

    if (user.get("accountNo").isBlank()) {
      signatoryErrorModel.setAccountNumber("Account Number can't be empty !");
      check = true;
    }

    if (addAccountModel.getListOfSignatory() == null) {
      addAccountModel.setCheckSignatoryList("true");
      check = true;
    } else {
      addAccountModel.setCheckSignatoryList("false");
    }
    addAccountModel.setAccountName(user.get("accountName"));
    addAccountModel.setAccountNumber(user.get("accountNo"));
    int size = addAccountModelList.size();
    addAccountModel.setUserInList(++size);
    List<SignatoryModel> listOfSignatory = addAccountModel.getListOfSignatory();
    for (int i = 0; i < listOfSignatory.size(); i++) {
      SignatoryModel signatoryModel = listOfSignatory.get(i);
      signatoryModel.setUserInAccount(addAccountModel.getUserInList());
      listOfSignatory.set(i, signatoryModel);
    }
    addAccountModelList.add(addAccountModel);
    requestWrapper.setListOfAddAccount(addAccountModelList);
    requestWrapper.setAccountCheck("true");
    httpSession.setAttribute("Signatory", addAccountModel);
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    if (check) {
      addAccountModel.setSignatoryErrorModel(signatoryErrorModel);
      addAccountModel.setButtonCheck("false");
      page = xsltProcessor.generatePage(xslPagePath("AddAccount"), addAccountModel);
    } else {
      RequestWrapper wrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
      page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/saveAccounts",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE })
  public ResponseEntity<String> saveAccounts(@RequestParam Map<String, String> user) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    requestWrapper.setStepForSave("Step 2");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    mandatesResolutionService.sendRequestStaging();
    UserDTO dto = (UserDTO) httpSession.getAttribute("currentUser");
    if ("ADMIN".equalsIgnoreCase(dto.getUserRole())) {
      return displayAdminApproval();
    } else {
      return goToDisplayRequestTable();
    }
  }

  @PostMapping(value = "/editSignatoryWithAccount/{userInList}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editSignatoryWithAccount(@RequestParam Map<String, String> user,
                                                         @PathVariable String userInList) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    AddAccountModel addAccountModel =
        mandatesResolutionService.getAccount(requestWrapper.getListOfAddAccount(), userInList);
    addAccountModel.setButtonCheck("true");
    httpSession.setAttribute("Signatory", addAccountModel);
    page = xsltProcessor.generatePage(xslPagePath("AddAccount"),
        (AddAccountModel) httpSession.getAttribute("Signatory"));
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/updateSignatoryWithAccount/{userInList}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> updateSignatoryWithAccount(@RequestParam Map<String, String> user,
                                                           @PathVariable String userInList) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<AddAccountModel> addAccountModelList =
        mandatesResolutionService.updateAccount(requestWrapper.getListOfAddAccount(), userInList,
            user);
    AddAccountModel addAccountModel =
        mandatesResolutionService.updateAccountSingle(requestWrapper.getListOfAddAccount(),
            userInList,
            user);
    boolean check = false;
    SignatoryErrorModel signatoryErrorModel = new SignatoryErrorModel();
    if (user.get("accountName").isBlank()) {
      signatoryErrorModel.setAccountName("Account Name can't be empty !");
      check = true;
    }

    if (user.get("accountNo").isBlank()) {
      signatoryErrorModel.setAccountNumber("Account Number can't be empty !");
      check = true;
    }

    if (addAccountModel.getListOfSignatory() == null || addAccountModel.getListOfSignatory()
        .isEmpty()) {
      addAccountModel.setCheckSignatoryList("true");
      check = true;
    } else {
      addAccountModel.setCheckSignatoryList("false");
    }

    if (check) {
      addAccountModel.setAccountName(user.get("accountName"));
      addAccountModel.setAccountNumber(user.get("accountNo"));
      addAccountModel.setSignatoryErrorModel(signatoryErrorModel);
      addAccountModel.setButtonCheck("true");
      page = xsltProcessor.generatePage(xslPagePath("AddAccount"), addAccountModel);
    } else {
      requestWrapper.setListOfAddAccount(addAccountModelList);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
      page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"),
          (RequestWrapper) httpSession.getAttribute("RequestWrapper"));
    }

    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/deleteSignatoryWithAccount/{userInList}",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> deleteSignatoryWithAccount(@PathVariable String userInList) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    List<AddAccountModel> listOfAddAccount = requestWrapper.getListOfAddAccount();
    int originalSize = listOfAddAccount.size();
    boolean removed = listOfAddAccount.removeIf(
        account -> userInList.equalsIgnoreCase(String.valueOf(account.getUserInList()))
    );
    if (removed) {
      requestWrapper.setListOfAddAccount(listOfAddAccount);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
    }
    String page = xsltProcessor.generatePage(
        xslPagePath("MandatesAutoFill"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/proceedToSignaturePage",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> proceedToSignaturePage() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    if (requestWrapper.getListOfAddAccount() == null || requestWrapper.getListOfAddAccount()
        .isEmpty()) {
      requestWrapper.setAccountCheck("false");
      page = xsltProcessor.generatePage(
          xslPagePath("MandatesAutoFill"), requestWrapper);
    } else {
      page = xsltProcessor.generatePage(
          xslPagePath("MandatesSignatureCard"), requestWrapper);
    }
    return ResponseEntity.ok(page);
  }

  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  @PostMapping(value = "/cancelToSignaturePage",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> cancelToSignaturePage() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    page = xsltProcessor.generatePage(
        xslPagePath("MandatesAutoFill"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/proceedSignatureCard",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> proceedSignatureCard(@RequestParam Map<String, String> user) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    requestWrapper.setCheckResolution("false");
    boolean check = false;
    for (AddAccountModel model : requestWrapper.getListOfAddAccount()) {
      for (SignatoryModel signatoryModel : model.getListOfSignatory()) {
        if (!"Remove".equalsIgnoreCase(signatoryModel.getInstruction())
            && user.get("capacity" + signatoryModel.getUserInList()
                        + signatoryModel.getUserInAccount()).isBlank()) {
          requestWrapper.setCheckSignatureCard("true");
          check = true;
        } else if (!"Remove".equalsIgnoreCase(signatoryModel.getInstruction())
                   && user.get("Group" + signatoryModel.getUserInList()
                               + signatoryModel.getUserInAccount()).isBlank()) {
          requestWrapper.setCheckSignatureCard("true");
          check = true;
        } else {
          requestWrapper.setCheckSignatureCard("false");
        }
      }
    }


    if (check) {
      page = xsltProcessor.generatePage(
          xslPagePath("MandatesSignatureCard"), requestWrapper);
    } else {
      page = xsltProcessor.generatePage(
          xslPagePath("Resolutions"), requestWrapper);
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/saveSignatureCard",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> saveSignatureCard() {
    String page = "";
    mandatesResolutionService.sendRequestSignatureCard();
    UserDTO dto = (UserDTO) httpSession.getAttribute("currentUser");
    if ("ADMIN".equalsIgnoreCase(dto.getUserRole())) {
      return displayAdminApproval();
    } else {
      return goToDisplayRequestTable();
    }
  }

  @PostMapping(value = "/backSignatureCard",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backSignatureCard() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    page = xsltProcessor.generatePage(
        xslPagePath("MandatesSignatureCard"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/editAddedSignatory/{userInList}/{userInAccount}",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editAddedSignatory(@PathVariable String userInList,
                                                   @PathVariable String userInAccount) {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    SignatoryModel signatoryModel =
        mandatesResolutionService.getSignatoryData(requestWrapper.getListOfAddAccount(),
            userInList, userInAccount);
    httpSession.setAttribute("signatory", signatoryModel);
    page = xsltProcessor.generatePage(
        xslPagePath("SignatoryGroup"), signatoryModel);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/updateAddedSignatory/{userInList}/{userInAccount}",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> updateAddedSignatory(@PathVariable String userInList,
                                                     @PathVariable String userInAccount,
                                                     @RequestParam Map<String, String> user) {

    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    SignatoryErrorModel signatoryErrorModel = new SignatoryErrorModel();
    boolean check = false;
    if (user.get("capacity").isBlank()) {
      signatoryErrorModel.setCapacity("Capacity can't be empty !");
      check = true;
    }

    if (user.get("Group").isBlank()) {
      signatoryErrorModel.setGroup("Group can't be empty !");
      check = true;
    }

    if (user.get("confirm").equalsIgnoreCase("false")) {
      signatoryErrorModel.setCheckDocConfirm("Please Confirm the check box !");
      check = true;
    }

    if (check) {
      SignatoryModel model = (SignatoryModel) httpSession.getAttribute("signatory");
      model.setGroup(user.get("Group"));
      model.setCapacity(user.get("capacity"));
      model.setSignatoryErrorModel(signatoryErrorModel);
      page = xsltProcessor.generatePage(xslPagePath("SignatoryGroup"), model);
    } else {
      List<AddAccountModel> listOfAddAccount =
          mandatesResolutionService.getAddAccountList(requestWrapper.getListOfAddAccount(),
              userInList, userInAccount, user);
      requestWrapper.setListOfAddAccount(listOfAddAccount);
      httpSession.setAttribute("RequestWrapper", requestWrapper);
      page = xsltProcessor.generatePage(
          xslPagePath("MandatesSignatureCard"), requestWrapper);
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/backSignature",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> backSignature() {
    String page = "";
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    page = xsltProcessor.generatePage(
        xslPagePath("MandatesSignatureCard"), requestWrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/saveAppointedDirectors",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> saveAppointedDirectors() {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    requestWrapper.setStepForSave("Step 4");
    httpSession.setAttribute("RequestWrapper", requestWrapper);
    mandatesResolutionService.sendRequestStaging();
    UserDTO dto = (UserDTO) httpSession.getAttribute("currentUser");
    if ("ADMIN".equalsIgnoreCase(dto.getUserRole())) {
      return displayAdminApproval();
    } else {
      return goToDisplayRequestTable();
    }
  }

  @PostMapping(value = "/submitFinalRecord",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitFinalRecord() {
    mandatesResolutionService.createRequest();
    String page = xsltProcessor.returnPage(
        xmlPagePath("MandatesSuccessPage"));
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/submitResoFinalRecord",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitResoFinalRecord() {
    mandatesResolutionService.createRequestReso();
    String page = xsltProcessor.returnPage(
        xmlPagePath("MandatesSuccessPage"));
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/submitMandateFinalRecord",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitMandateFinalRecord(@RequestParam Map<String, String> user) {
    RequestWrapper requestWrapper =
        (RequestWrapper) httpSession.getAttribute("RequestWrapper");
    requestWrapper.setCheckResolution("false");
    boolean check = false;
    String page = "";
    for (AddAccountModel model : requestWrapper.getListOfAddAccount()) {
      for (SignatoryModel signatoryModel : model.getListOfSignatory()) {
        if (!"Remove".equalsIgnoreCase(signatoryModel.getInstruction())
            && user.get("capacity" + signatoryModel.getUserInList()
                        + signatoryModel.getUserInAccount()).isBlank()) {
          requestWrapper.setCheckSignatureCard("true");
          check = true;
        } else if (!"Remove".equalsIgnoreCase(signatoryModel.getInstruction())
                   && user.get("Group" + signatoryModel.getUserInList()
                               + signatoryModel.getUserInAccount()).isBlank()) {
          requestWrapper.setCheckSignatureCard("true");
          check = true;
        } else {
          requestWrapper.setCheckSignatureCard("false");
        }
      }
    }

    if (check) {
      page = xsltProcessor.generatePage(
          xslPagePath("MandatesSignatureCard"), requestWrapper);
    } else {
      mandatesResolutionService.createRequestMandates();
      page = xsltProcessor.returnPage(
          xmlPagePath("MandatesSuccessPage"));
    }
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/finish",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> finish() {
    UserDTO dto = (UserDTO) httpSession.getAttribute("currentUser");
    if ("ADMIN".equalsIgnoreCase(dto.getUserRole())) {
      return displayAdminAll();
    } else {
      return goToDisplayRequestTable();
    }
  }

  @PostMapping(value = "/adminApproval", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminApproval() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
        all = new RequestTableDTO[0]; //No requests in DB
      }

      List<RequestTableDTO> pendingAdminApprovals = java.util.Arrays.stream(all)
          .filter(r -> {
            String ss = r.getSubStatus();
            return ss != null && "Admin Approval Pending".equalsIgnoreCase(ss.trim());
          })
          .peek(r -> {
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);
              r.setCompanyName(
                  companyResponse.getStatusCode().is2xxSuccessful()
                  && companyResponse.getBody() != null
                      ? companyResponse.getBody().getName()
                      : "Unknown"
              );
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          //Newest first
          .sorted(java.util.Comparator.comparing(
              (RequestTableDTO r) -> java.util.Optional.ofNullable(r.getCreated()).orElse(""),
              java.util.Comparator.naturalOrder()
          ).reversed())
          .toList();

      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(pendingAdminApprovals);

      String page = xsltProcessor.generatePage(xslPagePath("AdminApproval"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching Admin Approval Pending requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load Admin Approval Pending requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  //Admin Breach Page
  @PostMapping(value = "/adminBreach", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminBreach() {
    String page = xsltProcessor.generatePage(xslPagePath("AdminBreach"), new RequestWrapper());
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  //Admin All Page
  @RequestMapping(value = "/adminAll", method = { RequestMethod.GET, RequestMethod.POST }, produces
      = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminAll() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
        all = new RequestTableDTO[0];
      }

      //Parse created  supports local_date_time
      java.util.function.ToLongFunction<String> toEpochMillis = s -> {
        if (s == null) {
          return Long.MIN_VALUE;
        }
        String t = s.trim();
        if (t.isEmpty()) {
          return Long.MIN_VALUE;
        }
        try {
          java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(
              t, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception ignore) {
          //Intentionally ignore parse errors; use sentinel
        }
        try {
          return java.time.Instant.parse(t).toEpochMilli();
        } catch (Exception ignore) {
          //Intetionally empty
        }
        try {
          return Long.parseLong(t);
        } catch (Exception ignore) {
          //Intetionally empty
        }
        return Long.MIN_VALUE;
      };

      java.util.List<RequestTableDTO> allRequests =
          new java.util.ArrayList<>(java.util.Arrays.asList(all));

      // Enrich company + compute display id using your helper
      for (RequestTableDTO r : allRequests) {
        try {
          String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
          ResponseEntity<CompanyDTO> companyResponse =
              restTemplate.getForEntity(companyUrl, CompanyDTO.class);
          r.setCompanyName(
              companyResponse.getStatusCode().is2xxSuccessful() && companyResponse.getBody() != null
                  ? companyResponse.getBody().getName()
                  : "Unknown"
          );
        } catch (Exception ex) {
          logger.error("Error fetching company name for companyId {}: {}",
              r.getCompanyId(), ex.getMessage());
          r.setCompanyName("Unknown");
        }

        String typeLabel = r.getType() == null ? null : r.getType().trim();
        String formatted = DisplayIds.format(r.getRequestId(), typeLabel);
        r.setRequestIdForDisplay(formatted != null
            ? formatted
            : (r.getRequestId() == null ? "—" : "REQ - "
                                                +
                                                String.format("%04d", r.getRequestId())));
      }

      // Sort: created DESC, then numeric requestId DESC
      allRequests.sort(
          java.util.Comparator
              .comparingLong((RequestTableDTO r) -> toEpochMillis.applyAsLong(r.getCreated()))
              .reversed()
              .thenComparing(
                  java.util.Comparator.comparing(
                      RequestTableDTO::getRequestId,
                      java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                  ).reversed()
              )
      );

      if (logger.isDebugEnabled()) {
        for (int i = 0; i < Math.min(5, allRequests.size()); i++) {
          RequestTableDTO r = allRequests.get(i);
          logger.debug("adminAll[{}]: created={}, requestId={}, displayId={}",
              i, r.getCreated(), r.getRequestId(), r.getRequestIdForDisplay());
        }
      }

      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(allRequests);

      String page = xsltProcessor.generatePage(xslPagePath("AdminAll"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching all requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  @PostMapping(value = "/inProgressRequests", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayInProgressRequests(HttpSession session,
                                                          HttpServletRequest request) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      // Fetch all requests
      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (HttpClientErrorException.NotFound nf) {
        all = new RequestTableDTO[0];
      }

      final boolean admin = isAdmin(session);
      final String me = loggedInUsername(session, request);

      // Filter in-progress requests
      List<RequestTableDTO> inProgress = Arrays.stream(all)
          .filter(
              r -> r.getStatus() != null && "In Progress".equalsIgnoreCase(r.getStatus().trim()))
          .filter(r -> admin || (r.getAssignedUser() != null
                                 && !r.getAssignedUser().trim().isEmpty()
                                 && r.getAssignedUser().trim().equalsIgnoreCase(me)))
          .peek(r -> {
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);
              r.setCompanyName(companyResponse.getStatusCode().is2xxSuccessful()
                               && companyResponse.getBody() != null
                  ? companyResponse.getBody().getName()
                  : "Unknown");
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          .toList();

      RequestDTO requestDTO = new RequestDTO();
      UserDTO user = (UserDTO) session.getAttribute("currentUser");
      if ("ADMIN".equalsIgnoreCase(user.getUserRole())) {
        requestDTO.setSubStatus("Admin");
      } else {
        requestDTO.setSubStatus("User");
      }
      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(inProgress);
      wrapper.setRequestDTO(requestDTO);

      // Always use LandingPage XSLT
      String page = xsltProcessor.generatePage(xslPagePath("LandingPage"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching in-progress requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load in-progress requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  @PostMapping(value = "/completedRequests", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayCompletedRequests(HttpSession session,
                                                         HttpServletRequest request) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      // Fetch all requests
      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (HttpClientErrorException.NotFound nf) {
        all = new RequestTableDTO[0]; // No requests found
      }

      final boolean admin = isAdmin(session);
      final String me = loggedInUsername(session, request);

      List<RequestTableDTO> completedRequests = Arrays.stream(all)
          .filter(r -> "Completed".equalsIgnoreCase(r.getStatus()))
          // Admin sees all; normal user sees only assigned requests
          .filter(r -> admin || (r.getAssignedUser() != null && r.getAssignedUser()
              .trim()
              .equalsIgnoreCase(me)))
          .peek(r -> {
            // Ensure display ID
            if (r.getRequestIdForDisplay() == null || r.getRequestIdForDisplay().isBlank()) {
              r.setRequestIdForDisplay(
                  r.getRequestId() == null ? "" : String.valueOf(r.getRequestId()));
            }
            // Enrich company name
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);
              r.setCompanyName(companyResponse.getStatusCode().is2xxSuccessful()
                               && companyResponse.getBody() != null
                  ? companyResponse.getBody().getName()
                  : "Unknown");
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          .toList();

      RequestDTO requestDTO = new RequestDTO();
      UserDTO user = (UserDTO) session.getAttribute("currentUser");
      if ("ADMIN".equalsIgnoreCase(user.getUserRole())) {
        requestDTO.setSubStatus("Admin");
      } else {
        requestDTO.setSubStatus("User");
      }
      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(completedRequests);
      wrapper.setRequestDTO(requestDTO);

      // Use unified XSLT
      String page = xsltProcessor.generatePage(xslPagePath("CompletedRequests"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching completed requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load completed requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  @PostMapping(value = "/onHoldRequests", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayOnHoldRequests(HttpSession session,
                                                      HttpServletRequest request) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (HttpClientErrorException.NotFound nf) {
        all = new RequestTableDTO[0];
      }

      final boolean admin = isAdmin(session); // check user role
      final String me = loggedInUsername(session, request);

      List<RequestTableDTO> onHoldRequests = Arrays.stream(all)
          .filter(r -> "On Hold".equalsIgnoreCase(r.getStatus()))
          .filter(
              r -> admin || (r.getAssignedUser() != null && !r.getAssignedUser().trim().isEmpty()
                             && r.getAssignedUser().trim().equalsIgnoreCase(me)))
          .peek(r -> {
            if (r.getRequestIdForDisplay() == null || r.getRequestIdForDisplay().isBlank()) {
              r.setRequestIdForDisplay(
                  r.getRequestId() == null ? "" : String.valueOf(r.getRequestId()));
            }
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);
              r.setCompanyName(companyResponse.getStatusCode().is2xxSuccessful()
                               && companyResponse.getBody() != null ? companyResponse.getBody()
                  .getName() : "Unknown");
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          .toList();
      RequestDTO requestDTO = new RequestDTO();
      UserDTO user = (UserDTO) session.getAttribute("currentUser");
      if ("ADMIN".equalsIgnoreCase(user.getUserRole())) {
        requestDTO.setSubStatus("Admin");
      } else {
        requestDTO.setSubStatus("User");
      }
      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(onHoldRequests);
      wrapper.setRequestDTO(requestDTO);

      // use same XSL page for both roles
      String page = xsltProcessor.generatePage(xslPagePath("OnHoldRequests"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching on-hold requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load on-hold requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  @PostMapping(value = "/draftRequests", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayDraftRequests(HttpSession session,
                                                     HttpServletRequest request) {
    try {
      final String base = mandatesResolutionsDaoURL;
      final RestTemplate rt = new RestTemplate();

      // Fetch all draft requests
      RequestStagingDTO[] raws =
          rt.getForObject(base + "/api/request-staging/all", RequestStagingDTO[].class);
      List<RequestStagingDTO> list = (raws == null) ? List.of() : Arrays.asList(raws);

      final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
      final ObjectMapper OM = new ObjectMapper().findAndRegisterModules();
      RequestTableWrapper wrapper = new RequestTableWrapper();
      List<RequestTableDTO> rows = new ArrayList<>();

      // Determine user info
      UserDTO user = (UserDTO) session.getAttribute("currentUser");
      final boolean admin = user != null && "ADMIN".equalsIgnoreCase(user.getUserRole());
      final String me = (user != null) ? user.getEmployeeNumber() : "";

      RequestDTO requestDTO = new RequestDTO();
      for (RequestStagingDTO d : list) {
        // Filter: non-admins only see their own assigned requests
        if (!admin && (d.getCreator() == null || !d.getCreator().equalsIgnoreCase(me))) {
          continue;
        }

        RequestStagingDTO src = d;
        String createdStr = (d.getCreated() == null) ? "" : d.getCreated().format(FMT);
        // If blank, fetch raw JSON and search for created-like field
        if (createdStr.isBlank()) {
          try {
            ResponseEntity<String> resp =
                rt.getForEntity(base + "/api/request-staging/{id}", String.class, d.getStagingId());
            String body = resp.getBody();
            if (body != null && !body.isBlank()) {
              try {
                JsonNode root = OM.readTree(body);
                String raw = findCreatedAnyCase(root);
                if (raw != null && !raw.isBlank()) {
                  if (isAllDigits(raw)) {
                    createdStr = formatEpochMillis(Long.parseLong(raw), FMT);
                  } else {
                    createdStr = tryFormatIso(raw, FMT);
                  }
                }
              } catch (Exception ignore) {
                //
              }
            }
          } catch (Exception ignore) {
            //
          }
        }

        if (createdStr.isBlank() && src.getCreated() != null) {
          createdStr = src.getCreated().format(FMT);
        }

        UserDTO users = (UserDTO) session.getAttribute("currentUser");
        System.out.println("=======Print role========" + users.getUserRole());
        if ("ADMIN".equalsIgnoreCase(users.getUserRole())) {
          requestDTO.setSubStatus("Admin");
        } else {
          requestDTO.setSubStatus("User");
        }
        // Build table row
        RequestTableDTO r = new RequestTableDTO();
        r.setRequestId(src.getStagingId());
        r.setCompanyName(src.getCompanyName());
        r.setRegistrationNumber(src.getCompanyRegistrationNumber());
        r.setStatus(src.getRequestStatus());
        r.setSubStatus(cleanSubStatus(src.getRequestSubStatus()));
        r.setType(src.getRequestType());
        r.setCreated(createdStr);
        r.setUpdated(null);
        r.setAssignedUser(src.getAssignedUser());

        rows.add(r);
      }
      wrapper.setRequest(rows);
      wrapper.setRequestDTO(requestDTO);

      // Use single XSLT for all draft requests
      String page = xsltProcessor.generatePage(xslPagePath("DraftRequests"), wrapper);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);

    } catch (Exception e) {
      logger.error("Error loading draft requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load drafts.</error>
          </page>
          """;
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(fallbackError);
    }
  }

  //Admin Draft Page
  @PostMapping(value = "/adminDraft", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminDraft() {
    try {
      final String base = mandatesResolutionsDaoURL;
      final RestTemplate rt = new RestTemplate();

      RequestStagingDTO[] raws =
          rt.getForObject(base + "/api/request-staging/all", RequestStagingDTO[].class);
      List<RequestStagingDTO> list =
          (raws == null) ? java.util.List.of() : java.util.Arrays.asList(raws);

      final java.time.format.DateTimeFormatter FMT =
          java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

      final com.fasterxml.jackson.databind.ObjectMapper OM =
          new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

      RequestTableWrapper wrapper = new RequestTableWrapper();
      List<RequestTableDTO> rows = new java.util.ArrayList<>();

      for (RequestStagingDTO d : list) {
        RequestStagingDTO src = d;

        //1)Default binding first
        String createdStr = (d.getCreated() == null) ? "" : d.getCreated().format(FMT);

        // )If blank, fetch raw JSON and try rebind / find created-like field
        if (createdStr.isBlank()) {
          try {
            ResponseEntity<String> resp =
                rt.getForEntity(base + "/api/request-staging/{id}", String.class, d.getStagingId());
            String body = resp.getBody();
            if (body != null && !body.isBlank()) {
              try {
                RequestStagingDTO rebound = OM.readValue(body, RequestStagingDTO.class);
                if (rebound != null) {
                  src = rebound;
                }
              } catch (Exception ignore) {
                // intentionally empty
              }

              try {
                com.fasterxml.jackson.databind.JsonNode root = OM.readTree(body);
                String raw = findCreatedAnyCase(root);
                if (raw != null && !raw.isBlank()) {
                  if (isAllDigits(raw)) {
                    createdStr = formatEpochMillis(Long.parseLong(raw), FMT);
                  } else {
                    createdStr = tryFormatIso(raw, FMT);
                  }
                }
              } catch (Exception ignore) {
                // intentionally empty
              }
            }
          } catch (Exception ignore) {
            // intentionally empty
          }
        }

        if (createdStr.isBlank() && src.getCreated() != null) {
          createdStr = src.getCreated().format(FMT);
        }

        //Build row for the table
        RequestTableDTO r = new RequestTableDTO();
        r.setRequestId(src.getStagingId());
        r.setCompanyName(src.getCompanyName());
        r.setRegistrationNumber(src.getCompanyRegistrationNumber());
        r.setStatus(src.getRequestStatus());
        r.setSubStatus(cleanSubStatus(src.getRequestSubStatus()));
        r.setType(src.getRequestType());
        r.setCreated(createdStr);
        r.setUpdated(null);

        rows.add(r);
      }

      wrapper.setRequest(rows);
      String page = xsltProcessor.generatePage(xslPagePath("DraftRequests"), wrapper);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);

    } catch (Exception e) {
      logger.error("Error loading Admin Draft page: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load drafts.</error>
          </page>
          """;
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(fallbackError);
    }
  }

  //Admin Profile Page
  @PostMapping(value = "/adminProfile", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminProfile(HttpSession session,
                                                    HttpServletRequest request) {
    //Pull user from session
    za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO user =
        (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
            "currentUser");

    RequestDTO dto = new RequestDTO();
    if (user != null) {
      String displayName = currentDisplayId(session, request);
      dto.setLoggedInUsername(displayName);
      dto.setLoggedInEmail(user.getEmail());
    }

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);

    String page = xsltProcessor.generatePage(xslPagePath("AdminProfile"), wrapper);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
  }

  //Admin View Page
  @PostMapping(value = "/adminView/{requestId}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminView(
      @PathVariable Long requestId,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    try {
      //Remember last opened request for Cancel / fallbacks
      session.setAttribute("lastViewedRequestId", requestId);

      RestTemplate rt = new RestTemplate();

      //Resolve display name
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO user =
          (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
              "currentUser");

      String displayName = currentDisplayId(session, servletRequest);

      //1) Load submission
      String submissionUrl = mandatesResolutionsDaoURL + "/api/submission/" + requestId;
      ResponseEntity<za.co.rmb.tts.mandates.resolutions
          .ui.model.dto.MandateResolutionSubmissionResultDTO>
          subResp =
          rt.getForEntity(submissionUrl,
              za.co.rmb.tts.mandates.resolutions
                  .ui.model.dto.MandateResolutionSubmissionResultDTO.class);

      if (!subResp.getStatusCode().is2xxSuccessful() || subResp.getBody() == null) {
        throw new RuntimeException("Failed to fetch submission " + requestId);
      }
      var sub = subResp.getBody();

      //2) Fetch comments (DAO) — newestFirst=true
      String commentsUrl =
          mandatesResolutionsDaoURL + "/api/comment/request/" + requestId + "?newestFirst=true";
      ResponseEntity<java.util.List<java.util.Map<String, Object>>> commentsResp = rt.exchange(
          commentsUrl,
          HttpMethod.GET,
          null,
          new org.springframework.core.ParameterizedTypeReference
              <java.util.List<java.util.Map<String, Object>>>() {
          }
      );

      java.time.format.DateTimeFormatter viewFmt =
          java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

      java.util.List<za.co.rmb.tts.mandates.resolutions.ui.model.dto.ViewCommentDTO> approvedRows =
          new java.util.ArrayList<>();
      java.util.List<za.co.rmb.tts.mandates.resolutions.ui.model.dto.ViewCommentDTO> rejectedRows =
          new java.util.ArrayList<>();

      if (commentsResp.getStatusCode().is2xxSuccessful() && commentsResp.getBody() != null) {
        for (var c : commentsResp.getBody()) {
          String text =
              (c.get("commentText") == null) ? "" : String.valueOf(c.get("commentText")).trim();
          if (text.isEmpty()) {
            continue;
          }

          Object isInt = c.getOrDefault("isInternal", c.getOrDefault("isinternal", null));
          boolean reject = (isInt instanceof Boolean) ? ((Boolean) isInt)
              : "true".equalsIgnoreCase(String.valueOf(isInt));

          String creator =
              (c.get("creator") == null) ? "" : String.valueOf(c.get("creator")).trim();
          if (creator.isBlank() || "ui".equalsIgnoreCase(creator)) {
            creator = displayName;
          }

          String createdStr = "";
          Object created = c.get("created");
          if (created != null) {
            String raw = String.valueOf(created).trim();
            try {
              if (isAllDigits(raw)) {
                long val = Long.parseLong(raw);
                createdStr = formatEpochMillis(val, viewFmt);
              } else {
                createdStr = tryFormatIso(raw, viewFmt);
              }
            } catch (Exception ignore) {
              createdStr = raw;
            }
          }

          var row = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.ViewCommentDTO();
          row.setCreator(creator);
          row.setCreated(createdStr);
          row.setText(text);

          if (reject) {
            rejectedRows.add(row);
          } else {
            approvedRows.add(row);
          }
        }
      }

      //3) Helpers
      java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();
      java.util.function.Function<String, String> keyN =
          s -> s == null ? "" : s.trim().toUpperCase();
      java.util.function.BiFunction<String, String, String> accKey = (num, name) -> {
        String n = nz.apply(num);
        if (!n.isEmpty()) {
          return "NUM#" + n;
        }
        return "NAME#" + keyN.apply(name);
      };

      //4) Seed accounts map
      java.util.Map<String, AccountDTO> accountsByKey = new java.util.LinkedHashMap<>();
      if (sub.getAccounts() != null) {
        for (var a : sub.getAccounts()) {
          String k = accKey.apply(a.getAccountNumber(), a.getAccountName());
          AccountDTO bucket = accountsByKey.computeIfAbsent(k, kk -> {
            AccountDTO ad = new AccountDTO();
            ad.setAccountName(nz.apply(a.getAccountName()));
            ad.setAccountNumber(nz.apply(a.getAccountNumber()));
            ad.setSignatories(new java.util.ArrayList<>());
            return ad;
          });
          var apiSigs = a.getSignatories();
          if (apiSigs != null) {
            for (var s : apiSigs) {
              SignatoryDTO sd = new SignatoryDTO();
              sd.setFullName(nz.apply(s.getFullName()));
              sd.setIdNumber(nz.apply(s.getIdNumber()));
              sd.setInstructions(nz.apply(s.getInstructions()));
              sd.setCapacity(nz.apply(s.getCapacity()));
              sd.setGroupCategory(nz.apply(s.getGroupCategory()));
              sd.setAccountName(bucket.getAccountName());
              sd.setAccountNumber(bucket.getAccountNumber());
              bucket.getSignatories().add(sd);
            }
          }
        }
      }

      //5) Build wrapper for XSL
      RequestTableDTO view = new RequestTableDTO();
      if (sub.getRequest() != null) {
        view.setRequestId(sub.getRequest().getRequestId());
        view.setCompanyId(sub.getRequest().getCompanyId());
        view.setSla(sub.getRequest().getSla());
        view.setType(sub.getRequest().getType());
        view.setStatus(sub.getRequest().getStatus());
        view.setSubStatus(sub.getRequest().getSubStatus());
        view.setCreated(
            sub.getRequest().getCreated() != null ? sub.getRequest().getCreated().toString() :
                null);
        view.setUpdated(
            sub.getRequest().getUpdated() != null ? sub.getRequest().getUpdated().toString() :
                null);
        view.setProcessId(sub.getRequest().getProcessId());
        view.setAssignedUser(sub.getRequest().getAssignedUser());
        view.setRequestIdForDisplay(sub.getRequest().getRequestIdForDisplay());

        //Creator/Updator coming from the API payload (Display when viewing a request)
        String creator = nz.apply(sub.getRequest().getCreator());
        if (creator.isEmpty()) {
          creator = displayName; // fallback
        }
        view.setCreator(creator);

        String updator = nz.apply(sub.getRequest().getUpdator());
        if (updator.isEmpty()) {
          updator = displayName; // fallback
        }
        view.setUpdator(updator);

        //Directors from submission / fallbacks
        var dirs =
            new java.util.ArrayList<za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO>();
        boolean submissionHasInstr = false;

        var authsSub = sub.getAuthorities();
        if (authsSub != null && !authsSub.isEmpty()) {
          for (var a : authsSub) {
            boolean hasInstr = java.util.Arrays.stream(a.getClass().getMethods())
                .anyMatch(m -> m.getName().equals("getInstructions"));
            if (hasInstr) {
              submissionHasInstr = true;
            }

            String first = null;
            String last = null;
            String role = null;
            try {
              first = (String) a.getClass().getMethod("getFirstname").invoke(a);
            } catch (Exception ignore) {
              // intentionally empty
            }
            try {
              last = (String) a.getClass().getMethod("getSurname").invoke(a);
            } catch (Exception ignore) {
              // intentionally empty
            }
            try {
              role = (String) a.getClass().getMethod("getDesignation").invoke(a);
            } catch (Exception ignore) {
              // intentionally empty
            }

            String instEff = extractInstruction(a);

            var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
            dd.setName(nz.apply(first));
            dd.setSurname(nz.apply(last));
            dd.setDesignation(nz.apply(role));
            dd.setInstruction(instEff);
            if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                .isEmpty())) {
              dirs.add(dd);
            }
          }
        }

        if (!submissionHasInstr && sub.getRequest() != null
            && sub.getRequest().getCompanyId() != null) {
          try {
            Long companyId = sub.getRequest().getCompanyId();
            String url = mandatesResolutionsDaoURL + "/api/authority/company/" + companyId;
            var resp = rt.exchange(
                url, HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<
                    java.util.List<za.co.rmb.tts.mandates.resolutions
                        .ui.model.dto.AuthorityDTO>>() {
                }
            );
            var authsApi = (resp.getStatusCode().is2xxSuccessful()) ? resp.getBody() : null;
            if (authsApi != null && !authsApi.isEmpty()) {
              dirs.clear();
              for (var a : authsApi) {
                var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
                dd.setName(nz.apply(a.getFirstname()));
                dd.setSurname(nz.apply(a.getSurname()));
                dd.setDesignation(nz.apply(a.getDesignation()));
                String instr = nz.apply(a.getInstructions());
                if (instr.isEmpty()) {
                  Boolean active = a.getIsActive();
                  instr = (Boolean.FALSE.equals(active)) ? "Remove" : "Add";
                }
                dd.setInstruction(instr);
                if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                    .isEmpty())) {
                  dirs.add(dd);
                }
              }
            }
          } catch (Exception ignore) {
            // intentionally empty
          }
        }

        if (dirs.isEmpty() && sub.getRequest() != null) {
          try {
            var sourceDirs =
                (java.util.List<?>) sub.getRequest().getClass().getMethod("getDirectors")
                    .invoke(sub.getRequest());
            if (sourceDirs != null) {
              for (Object d : sourceDirs) {
                String name = null;
                String surname = null;
                String designation = null;
                String instruction = null;
                try {
                  name = (String) d.getClass().getMethod("getName").invoke(d);
                } catch (Exception ignore) {
                  // intentionally empty
                }
                try {
                  surname = (String) d.getClass().getMethod("getSurname").invoke(d);
                } catch (Exception ignore) {
                  // intentionally empty
                }
                try {
                  designation = (String) d.getClass().getMethod("getDesignation").invoke(d);
                } catch (Exception ignore) {
                  // intentionally empty
                }
                try {
                  instruction = (String) d.getClass().getMethod("getInstruction").invoke(d);
                } catch (Exception ignore) {
                  // intentionally empty
                }

                var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
                dd.setName(nz.apply(name));
                dd.setSurname(nz.apply(surname));
                dd.setDesignation(nz.apply(designation));
                dd.setInstruction(nz.apply(instruction));

                if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                    .isEmpty())) {
                  dirs.add(dd);
                }
              }
            }
          } catch (ReflectiveOperationException ignore) {
            // intentionally empty
          }
        }

        view.setDirectors(dirs);
      } else {
        view.setRequestId(requestId);
      }

      view.setCompanyName(
          (sub.getCompany() != null && sub.getCompany().getName() != null)
              ? sub.getCompany().getName() : "Unknown"
      );

      view.setApprovedComments(approvedRows);
      view.setRejectedComments(rejectedRows);

      view.setAccounts(new java.util.ArrayList<>(accountsByKey.values()));
      view.setSignatories(null);

      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(java.util.List.of(view));

      populateInstructions(wrapper, view.getSubStatus(), mandatesResolutionsDaoURL);

      Object approveErr = servletRequest.getAttribute("approveErr");
      String errParam = servletRequest.getParameter("err");
      if (approveErr != null || "chk".equalsIgnoreCase(String.valueOf(errParam))) {
        ApproveRejectErrorModel em = new ApproveRejectErrorModel();
        em.setConfirmationCheckMandate(
            (approveErr != null)
                ? String.valueOf(approveErr)
                : "Verification cannot proceed until the checkbox has been selected"
        );
        wrapper.setApproveRejectErrorModel(em);
      }
      UserDTO users = (UserDTO) session.getAttribute("currentUser");
      System.out.println("=====User Role==== " + users.getUserRole());
      RequestDTO requestDTO = new RequestDTO();
      if ("ADMIN".equalsIgnoreCase(users.getUserRole())) {
        requestDTO.setSubStatus("Admin");
      } else {
        requestDTO.setSubStatus("User");
      }
      wrapper.setRequestDTO(requestDTO);

      String page = xsltProcessor.generatePage(xslPagePath("ViewRequest"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      String fallbackError =
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          +
          "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
          +
          "    <error>Unable to load request for admin viewing.</error>\n"
          +
          "</page>\n";
      return ResponseEntity.ok(fallbackError);
    }
  }

  @PostMapping(value = "/adminReassign", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayAdminReassign(HttpSession session) {
    try {
      //Recover the last viewed id (set when AdminView loaded)
      Long requestId = (Long) session.getAttribute("lastViewedRequestId");

      //1) Pull users from DAO
      String url = mandatesResolutionsDaoURL + "/api/user/all";
      RestTemplate rt = new RestTemplate();
      ResponseEntity<UserDTO[]> resp = rt.getForEntity(url, UserDTO[].class);
      UserDTO[] body = (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null)
          ? resp.getBody() : new UserDTO[0];

      //2) Build model the XSL expects
      RequestDTO model = new RequestDTO();
      model.setRequestId(requestId);

      java.util.List<RequestDTO.UserOption> options = new java.util.ArrayList<>();
      for (UserDTO u : body) {
        String uname = u.getUsername() == null ? "" : u.getUsername().trim();
        String role = u.getUserRole() == null ? "" : u.getUserRole().trim();
        if (!uname.isEmpty()) {
          options.add(new RequestDTO.UserOption(uname, role));
        }
      }
      model.setUserOptions(options);

      //3)Wrap + render
      RequestWrapper wrapper = new RequestWrapper();
      wrapper.setRequest(model);

      String page = xsltProcessor.generatePage(xslPagePath("ReassignScreen"), wrapper);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);

    } catch (Exception e) {
      logger.error("Failed to load users for reassign popup", e);
      String errorXml = """
            <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <error>Unable to load the reassign popup.</error>
            </page>
          """;
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(errorXml);
    }
  }

  @PostMapping(
      value = "/adminReassignSubmit",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE }
  )
  public ResponseEntity<String> submitAdminReassign(
      @RequestParam(value = "selectUser", required = false) String newAssignee,
      @RequestParam(value = "requestId", required = false) Long requestId,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    try {
      //Debugging
      logger.info("adminReassignSubmit: Accept={}, Content-Type={}",
          servletRequest.getHeader("Accept"), servletRequest.getContentType());
      servletRequest.getParameterMap()
          .forEach((k, v) -> logger.info("adminReassignSubmit: param {} = {}", k,
              java.util.Arrays.toString(v)));
      logger.info("adminReassignSubmit: incoming requestId={}, selectUser={}", requestId,
          newAssignee);

      //Fallback ID from session
      if (requestId == null) {
        Object v = session.getAttribute("lastViewedRequestId");
        if (v instanceof Long) {
          requestId = (Long) v;
        } else if (v != null) {
          try {
            requestId = Long.valueOf(String.valueOf(v));
          } catch (NumberFormatException ignore) {
            // intentionally empty
          }
        }
        logger.info("adminReassignSubmit: requestId after session fallback={}", requestId);
      }

      //Validate required fields and return a page
      if (requestId == null) {
        logger.warn("adminReassignSubmit: requestId is NULL");
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body("""
                <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                  <error>
                    Missing requestId on submit. Please reopen the request and try again.
                  </error>
                </page>
                """);
      }
      if (newAssignee == null || newAssignee.isBlank()) {
        logger.warn("adminReassignSubmit: selectUser (newAssignee) is NULL/blank");
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body("""
                <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                  <error>Please select a user before submitting.</error>
                </page>
                """);
      }

      //Build JSON payload expected by DAO updateRequest (case-sensitive outcome)
      var payload = new java.util.LinkedHashMap<String, Object>();
      payload.put("assignedUser", newAssignee);
      payload.put("updator", currentDisplayId(session, servletRequest));
      payload.put("processOutcome", "ReAssign");

      //Headers
      org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

      String url = mandatesResolutionsDaoURL + "/api/request/" + requestId;
      logger.info("adminReassignSubmit: PUT {} with payload {}", url, payload);

      RestTemplate rt = new RestTemplate();
      var entity = new org.springframework.http.HttpEntity<>(payload, headers);
      var resp = rt.exchange(url, HttpMethod.PUT, entity, Object.class);
      logger.info("adminReassignSubmit: DAO response status={}", resp.getStatusCode());

      //Back to Admin View
      session.setAttribute("lastViewedRequestId", requestId);
      return displayAdminView(requestId, session, servletRequest);

    } catch (Exception e) {
      logger.error("Reassign submit failed", e);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_XML)
          .body("""
              <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <error>Unable to reassign the request.</error>
              </page>
              """);
    }
  }

  // ============= REQUEST TABLES =============

  //Pending Requests page after logging in
  @PostMapping(value = "/requestTable", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayRequestTable(HttpSession session,
                                                    HttpServletRequest request) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);

        //If DAO returns 2xx but body is null, treat as empty
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];

      } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
        //DAO uses 404 to mean "no requests" so treat as empty list
        all = new RequestTableDTO[0];
      }

      final boolean admin = isAdmin(session);
      final String me = loggedInUsername(session, request);

      List<RequestTableDTO> inProgress = java.util.Arrays.stream(all)
          .filter(r -> "In Progress".equalsIgnoreCase(r.getStatus()))
          //Show all if admin else only rows the user created
          .filter(r -> admin || (
              r.getCreator() != null
              && !r.getCreator().trim().isEmpty()
              && r.getCreator().trim().equalsIgnoreCase(me)
          ))
          .peek(r -> {
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);

              if (companyResponse.getStatusCode().is2xxSuccessful()
                  && companyResponse.getBody() != null) {
                r.setCompanyName(companyResponse.getBody().getName());
              } else {
                r.setCompanyName("Unknown");
              }
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          .toList();
      RequestDTO requestDTO = new RequestDTO();
      UserDTO user = (UserDTO) session.getAttribute("currentUser");
      if ("USER".equalsIgnoreCase(user.getUserRole())) {
        requestDTO.setSubStatus("User");
      }
      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(inProgress);
      wrapper.setRequestDTO(requestDTO);

      String page = xsltProcessor.generatePage(xslPagePath("LandingPage"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching in-progress requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load pending requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  //On Hold Tickets Page
  @PostMapping(value = "/requestTableOnHold", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayRequestTableOnHold(HttpSession session,
                                                          HttpServletRequest request) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
        //DAO uses 404 to mean "no requests"
        all = new RequestTableDTO[0];
      }

      final boolean admin = isAdmin(session);
      final String me = loggedInUsername(session, request);

      List<RequestTableDTO> onHoldRequests = java.util.Arrays.stream(all)
          .filter(r -> "On Hold".equalsIgnoreCase(r.getStatus()))
          //Show all if admin, else only rows the user created
          .filter(r -> admin || (
              r.getCreator() != null
              && !r.getCreator().trim().isEmpty()
              && r.getCreator().trim().equalsIgnoreCase(me)
          ))
          .peek(r -> {
            //Ensure display id present
            if (r.getRequestIdForDisplay() == null || r.getRequestIdForDisplay().isBlank()) {
              r.setRequestIdForDisplay(
                  r.getRequestId() == null ? "" : String.valueOf(r.getRequestId()));
            }
            //Company enrichment
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);
              if (companyResponse.getStatusCode().is2xxSuccessful()
                  && companyResponse.getBody() != null) {
                r.setCompanyName(companyResponse.getBody().getName());
              } else {
                r.setCompanyName("Unknown");
              }
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          .toList();

      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(onHoldRequests);

      logger.info("Fetched {} on-hold requests (post-filter)", onHoldRequests.size());

      String page = xsltProcessor.generatePage(xslPagePath("OnHold"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching on-hold requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load on-hold requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  //Completed Request Page
  @PostMapping(value = "/requestTableCompleted", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayRequestTableCompleted(HttpSession session,
                                                             HttpServletRequest request) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String backendUrl = mandatesResolutionsDaoURL + "/api/request/all";

      RequestTableDTO[] all;
      try {
        ResponseEntity<RequestTableDTO[]> response =
            restTemplate.getForEntity(backendUrl, RequestTableDTO[].class);
        all = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
            ? response.getBody()
            : new RequestTableDTO[0];
      } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
        //DAO uses 404 to mean "no requests"
        all = new RequestTableDTO[0];
      }

      final boolean admin = isAdmin(session);
      final String me = loggedInUsername(session, request);

      List<RequestTableDTO> completedRequests = java.util.Arrays.stream(all)
          .filter(r -> "Completed".equalsIgnoreCase(r.getStatus()))
          //Show all if admin, else only rows the user created
          .filter(r -> admin || (
              r.getCreator() != null
              && !r.getCreator().trim().isEmpty()
              && r.getCreator().trim().equalsIgnoreCase(me)
          ))
          .peek(r -> {
            //Ensure display id present
            if (r.getRequestIdForDisplay() == null || r.getRequestIdForDisplay().isBlank()) {
              r.setRequestIdForDisplay(
                  r.getRequestId() == null ? "" : String.valueOf(r.getRequestId()));
            }
            //Company enrichment
            try {
              String companyUrl = mandatesResolutionsDaoURL + "/api/company/" + r.getCompanyId();
              ResponseEntity<CompanyDTO> companyResponse =
                  restTemplate.getForEntity(companyUrl, CompanyDTO.class);
              if (companyResponse.getStatusCode().is2xxSuccessful()
                  && companyResponse.getBody() != null) {
                r.setCompanyName(companyResponse.getBody().getName());
              } else {
                r.setCompanyName("Unknown");
              }
            } catch (Exception ex) {
              logger.error("Error fetching company name for companyId {}: {}", r.getCompanyId(),
                  ex.getMessage());
              r.setCompanyName("Unknown");
            }
          })
          .toList();

      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(completedRequests);

      logger.info("Fetched {} completed requests (post-filter)", completedRequests.size());

      String page = xsltProcessor.generatePage(xslPagePath("Completed"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching completed requests: {}", e.getMessage(), e);
      String fallbackError = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page>
              <error>Unable to load completed requests.</error>
          </page>
          """;
      return ResponseEntity.ok(fallbackError);
    }
  }

  @RequestMapping(
      value = "/requestTableDraft",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> displayRequestTableDraft() {
    final String base = mandatesResolutionsDaoURL;
    final RestTemplate rt = new RestTemplate();

    RequestStagingDTO[] raws =
        rt.getForObject(base + "/api/request-staging/all", RequestStagingDTO[].class);
    List<RequestStagingDTO> list =
        (raws == null) ? java.util.List.of() : java.util.Arrays.asList(raws);

    final java.time.format.DateTimeFormatter FMT =
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    final com.fasterxml.jackson.databind.ObjectMapper OM =
        new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

    RequestTableWrapper wrapper = new RequestTableWrapper();
    List<RequestTableDTO> rows = new java.util.ArrayList<>();

    for (RequestStagingDTO d : list) {
      RequestStagingDTO src = d;

      //1) Try default binding first
      String createdStr = (d.getCreated() == null) ? "" : d.getCreated().format(FMT);

      //2) If still blank, fetch raw JSON and search for a created-like field
      if (createdStr.isBlank()) {
        try {
          ResponseEntity<String> resp =
              rt.getForEntity(base + "/api/request-staging/{id}", String.class, d.getStagingId());
          String body = resp.getBody();
          if (body != null && !body.isBlank()) {
            // DEBUG (uncomment once to verify what DAO returns)
            // logger.info("Draft {} raw JSON: {}", d.getStagingId(), body);

            //a) Try re-bind with our ObjectMapper (handles java-time)
            try {
              RequestStagingDTO rebound = OM.readValue(body, RequestStagingDTO.class);
              if (rebound != null) {
                src = rebound;
              }
            } catch (Exception ignore) {
              // intentionally empty
            }

            //b) Tree-scan for any case/shape of "created"
            try {
              com.fasterxml.jackson.databind.JsonNode root = OM.readTree(body);
              String raw = findCreatedAnyCase(root);

              if (raw != null && !raw.isBlank()) {
                if (isAllDigits(raw)) {
                  createdStr = formatEpochMillis(Long.parseLong(raw), FMT);
                } else {
                  createdStr = tryFormatIso(raw, FMT); //returns raw text if parse fails
                }
              }
            } catch (Exception ignore) {
              // intentionally empty
            }
          }
        } catch (Exception ignore) {
          // intentionally empty
        }
      }

      if (createdStr.isBlank() && src.getCreated() != null) {
        createdStr = src.getCreated().format(FMT);
      }

      //Build the table row
      RequestTableDTO r = new RequestTableDTO();
      r.setRequestId(src.getStagingId());
      r.setCompanyName(src.getCompanyName());
      r.setRegistrationNumber(src.getCompanyRegistrationNumber());
      r.setStatus(src.getRequestStatus());
      r.setSubStatus(cleanSubStatus(src.getRequestSubStatus()));
      r.setType(src.getRequestType());
      r.setCreated(createdStr);  //<created> for the XSL column
      r.setUpdated(null);

      rows.add(r);
    }

    wrapper.setRequest(rows);
    String page = xsltProcessor.generatePage(xslPagePath("DraftRequests"), wrapper);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
  }

  @PostMapping(value = "/requestTableProfile", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayRequestTableProfile(HttpSession session,
                                                           HttpServletRequest request) {
    //Pull user from session
    za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO user =
        (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO)
            session.getAttribute("currentUser");

    RequestDTO dto = new RequestDTO();

    String displayName = currentDisplayId(session, request);
    if (displayName == null || displayName.isBlank()) {
      displayName = "UI_USER";
    }
    dto.setLoggedInUsername(displayName);
    dto.setLoggedInEmail(user != null ? nz(user.getEmail()) : "");

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);

    String page = xsltProcessor.generatePage(xslPagePath("Profile"), wrapper);
    return ResponseEntity.ok(page);
  }

  // ============= Export as CSV =============
  @PostMapping(value = "/exportCSV", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayExportCSV() {
    try {
      var rt = new RestTemplate();
      var url = mandatesResolutionsDaoURL + "/api/lov?type=Dropdown&subType=RequestStatus";

      var resp = rt.exchange(url, HttpMethod.GET, null,
          new org.springframework.core.ParameterizedTypeReference<
              java.util.List<ListOfValuesDTO>>() {
          });

      // Exactly what DAO allows (regex is case-sensitive)
      final java.util.Set<String> allowed = java.util.Set.of(
          "Draft", "In Progress", "Completed", "On Hold", "Breached"
      );

      java.util.List<String> statuses = new java.util.ArrayList<>();
      if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
        for (var lov : resp.getBody()) {
          if (lov != null) {
            String v = lov.getValue() == null ? "" : lov.getValue().trim();
            if (!v.isEmpty() && allowed.contains(v)) {
              statuses.add(v);
            }
          }
        }
      }

      var wrapper = new RequestWrapper();
      var lovs = new RequestWrapper.LovsDTO();
      lovs.setStatuses(statuses);
      wrapper.setLovs(lovs);

      String page = xsltProcessor.generatePage(xslPagePath("ExportCSV"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      String fallback = """
          <?xml version="1.0" encoding="UTF-8"?>
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <error>Unable to load Export CSV page.</error>
          </page>
          """;
      return ResponseEntity.ok(fallback);
    }
  }

  @PostMapping(
      value = "/exportRequests",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  public ResponseEntity<byte[]> exportRequestsPost(
      HttpServletRequest request,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "fromDate", required = false) String fromDate,
      @RequestParam(name = "toDate", required = false) String toDate,
      @RequestParam(name = "type", required = false) String type
  ) {
    return exportRequestsDebugInternal(request, status, fromDate, toDate, "POST");
  }

  @GetMapping(
      value = "/exportRequests",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  public ResponseEntity<byte[]> exportRequestsGet(
      HttpServletRequest request,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "fromDate", required = false) String fromDate,
      @RequestParam(name = "toDate", required = false) String toDate,
      @RequestParam(name = "type", required = false) String type
  ) {
    return exportRequestsDebugInternal(request, status, fromDate, toDate, "GET");
  }

  private ResponseEntity<byte[]> exportRequestsDebugInternal(
      HttpServletRequest request,
      String status,
      String fromDate,
      String toDate,
      String method
  ) {
    String qs = request.getQueryString();
    String uriLine = method + " " + request.getRequestURI() + (qs == null ? "" : "?" + qs);

    logger.info("EXPORT DEBUG hit: {}", uriLine);
    EXPORT_LOG.info("EXPORT DEBUG hit: {}", uriLine);

    logger.info("EXPORT DEBUG values -> status='{}', "
                + "fromDate='{}', toDate='{}'", status, fromDate, toDate);
    EXPORT_LOG.info("EXPORT DEBUG values -> status='{}', "
                    + "fromDate='{}', toDate='{}'", status, fromDate, toDate);

    Map<String, String[]> pm = request.getParameterMap();
    if (pm == null || pm.isEmpty()) {
      logger.info("EXPORT DEBUG params -> (none)");
      EXPORT_LOG.info("EXPORT DEBUG params -> (none)");
    } else {
      List<String> lines = pm.entrySet().stream()
          .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
          .map(e -> e.getKey() + "=" + java.util.Arrays.toString(e.getValue()))
          .toList();
      String joined = String.join(" ; ", lines);
      logger.info("EXPORT DEBUG params -> {}", joined);
      EXPORT_LOG.info("EXPORT DEBUG params -> {}", joined);
    }

    //Return text file tiny
    String body = """
        EXPORT DEBUG
        ------------
        
        %s
        
        status   = %s
        fromDate = %s
        toDate   = %s
        
        all params:
        %s
        """.formatted(
        uriLine,
        String.valueOf(status),
        String.valueOf(fromDate),
        String.valueOf(toDate),
        (pm == null || pm.isEmpty())
            ? "(none)"
            : pm.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(e -> e.getKey() + "=" + java.util.Arrays.toString(e.getValue()))
                .collect(java.util.stream.Collectors.joining("\n"))
    );

    byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    HttpHeaders out = new HttpHeaders();
    out.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"export-debug.txt\"");
    out.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    out.setCacheControl("no-store");

    return new ResponseEntity<>(bytes, out, org.springframework.http.HttpStatus.OK);
  }

  @PostMapping(
      value = "/cancelCreateRequest",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE }
  )
  public ResponseEntity<String> cancelCreateRequest(HttpSession session) {
    UserDTO user = (UserDTO) session.getAttribute("currentUser");
    String role = (user != null && user.getUserRole() != null)
        ? user.getUserRole().trim().toUpperCase()
        : "";

    //Same routing as landing:
    if (role.contains("ADMIN")) {
      return displayAdminApproval(); //renders app-domain/mandates-and-resolutions/adminApproval
    } else {
      return goToDisplayRequestTable(); //renders app-domain/mandates-and-resolutions/requestTable
    }
  }

  @GetMapping(value = "/predictive/companyRegNumbers", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> predictiveCompanyRegNumbers(
      @RequestParam Map<String, String> params
  ) {
    //Accept a variety of param names used by different widgets
    String q = null;
    for (String key : new String[]{ "q", "term", "value", "text", "query", "input", "s",
        "companyRegNumber" }) {
      if (params.containsKey(key) && params.get(key) != null && !params.get(key).trim().isEmpty()) {
        q = params.get(key).trim();
        break;
      }
    }
    final String needle = (q == null) ? "" : q.toLowerCase();

    //Call DAO to get all companies once, then filter client-side
    String daoUrl = mandatesResolutionsDaoURL + "/api/company/all";
    org.springframework.web.client.RestTemplate rt =
        new org.springframework.web.client.RestTemplate();
    List<Map<String, Object>> companies;
    try {
      companies = rt.exchange(
          daoUrl,
          HttpMethod.GET,
          null,
          new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {
          }
      ).getBody();
    } catch (Exception e) {
      //Fail quietly: predictive should never block typing
      return ResponseEntity.ok("");
    }
    if (companies == null) {
      companies = java.util.Collections.emptyList();
    }

    //Extract, normalize and filter by prefix/contains
    List<String> regs = new java.util.ArrayList<>();
    for (Map<String, Object> row : companies) {
      Object rn = row.get("registrationNumber");
      if (rn != null) {
        String reg = rn.toString().trim();
        if (needle.isEmpty()
            || reg.toLowerCase().startsWith(needle)
            || reg.toLowerCase().contains(needle)) {
          regs.add(reg);
        }
      }
    }

    //Dedup + limit
    regs = regs.stream()
        .filter(s -> s != null && !s.isBlank())
        .distinct()
        .limit(15)
        .toList();

    String body = String.join("|", regs);
    return ResponseEntity.ok(body);
  }

  //Search Results page (accept both GET and POST; produce both XML variants)
  @RequestMapping(
      value = "/searchResults",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE }
  )
  public ResponseEntity<String> displaySearchResults(
      @RequestParam(value = "registrationNumber", required = false) String reg,
      @RequestParam(value = "pdfSessionId", required = false) String pdfSessionId,
      HttpSession session,
      HttpServletRequest request
  ) {
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    //1) Prefer the current submission (POSTed form field from CreateRequest)
    String posted = nz.apply(request.getParameter("companyRegNumber"));
    boolean hasPostedParam = request.getParameterMap().containsKey("companyRegNumber");

    //2) Next, consider the querystring value (GET /searchResults?registrationNumber=...)
    String queryReg = nz.apply(reg);

    // 3) Finally, consider what's already in the session DTO (user might be returning)
    String existingSid = (pdfSessionId != null && !pdfSessionId.isBlank())
        ? pdfSessionId
        : (String) session.getAttribute("pdfSessionId");
    RequestDTO existingDto = (existingSid != null) ? pdfExtractionDataCache.get(existingSid) : null;
    String regFromSession = (existingDto != null && existingDto.getRegistrationNumber() != null)
        ? existingDto.getRegistrationNumber().trim()
        : "";

    // 4) REQUIRED rule:
    //    - If the user actively posted the field and it's blank -> block (ignore session).
    //    - Else if no field was posted AND both query param and session are empty -> block.
    if ((hasPostedParam && posted.isEmpty())
        || (!hasPostedParam && queryReg.isEmpty() && regFromSession.isEmpty())) {
      return renderCreateRequestWithInlines(session, "", "This field is required.", "REQUIRED");
    }

    // 5) Choose effective registration number: POST > query > session
    String effectiveReg = !posted.isEmpty()
        ? posted
        : (!queryReg.isEmpty() ? queryReg : regFromSession);

    // 6) Acquire or create session id
    String sid = nz.apply(pdfSessionId).isEmpty()
        ? (String) session.getAttribute("pdfSessionId")
        : pdfSessionId;
    if (sid == null || sid.isBlank()) {
      sid = java.util.UUID.randomUUID().toString();
      session.setAttribute("pdfSessionId", sid);
    }

    // 7) Load or create DTO, set the reg, and ensure the page is renderable
    RequestDTO dto = pdfExtractionDataCache.get(sid);
    if (dto == null) {
      dto = new RequestDTO();
    }
    dto.setRegistrationNumber(effectiveReg);
    ensureAtLeastOneDirector(dto); // Guarantees one empty row so inputs render
    dto.setPdfSessionId(sid);
    dto.setEditable(true);

    // 8) Persist back to cache + session
    pdfExtractionDataCache.put(sid, dto);
    session.setAttribute("requestData", dto);

    //9) Render SearchResults page
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
    return ResponseEntity.ok(page);
  }

  private ResponseEntity<String> renderCreateRequestWithInlines(
      HttpSession session, String registrationNumber, String message, String code
  ) {
    RequestDTO dto = new RequestDTO();
    dto.setRegistrationNumber(registrationNumber == null ? "" : registrationNumber.trim());
    dto.setErrorMessage(message);
    dto.setErrorCode(code); //"REQUIRED" or "NOT_FOUND"
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), wrapper);
    return ResponseEntity.ok(page);
  }

  //Convenience overload so callers don't have to supply a code every time
  private ResponseEntity<String> renderCreateRequestWithInlines(
      HttpSession session,
      String registrationNumber,
      String message
  ) {
    return renderCreateRequestWithInlines(session, registrationNumber, message, null);
  }

  @GetMapping(
      value = "/searchCompanyDetails",
      produces = { MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE }
  )
  public ResponseEntity<String> searchCompanyDetailsGet(
      @RequestParam(value = "pdfSessionId", required = false) String pdfSessionIdParam,
      @RequestParam(value = "companyRegNumber", required = false) String registrationNumber,
      HttpSession session,
      HttpServletRequest request
  ) {
    // --- helpers
    java.util.function.Function<String, Integer> intOrNull = s -> {
      try {
        return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
      } catch (Exception ignore) {
        return null;
      }
    };

    Integer directorCount = intOrNull.apply(request.getParameter("directorCount"));
    Integer removeDirectorAt = intOrNull.apply(request.getParameter("removeDirectorAt"));
    Integer toolCount = intOrNull.apply(request.getParameter("toolCount"));
    Integer resolutionDocCount = intOrNull.apply(request.getParameter("resolutionDocCount"));

    // 1) Resolve pdfSessionId (prefer query, then session)
    String pdfSessionId = (pdfSessionIdParam != null && !pdfSessionIdParam.isBlank())
        ? pdfSessionIdParam.trim()
        : (String) session.getAttribute("pdfSessionId");
    if (pdfSessionId == null || pdfSessionId.isBlank()) {
      pdfSessionId = java.util.UUID.randomUUID().toString();
    }
    session.setAttribute("pdfSessionId", pdfSessionId);

    // 2) Load DTO (cache → session → new)
    RequestDTO dto = pdfExtractionDataCache.get(pdfSessionId);
    if (dto == null) {
      Object rd = session.getAttribute("requestData");
      dto = (rd instanceof RequestDTO) ? (RequestDTO) rd : new RequestDTO();
    }

    // 3) Ensure base state (don’t wipe existing values)
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);

    if (dto.getDirectors() == null) {
      dto.setDirectors(new java.util.ArrayList<>());
    }
    if (dto.getDocumentumTools() == null) {
      dto.setDocumentumTools(new java.util.ArrayList<>());
    }
    if (dto.getResolutionDocs() == null) {
      dto.setResolutionDocs(new java.util.ArrayList<>());
    }

    if (registrationNumber != null && !registrationNumber.isBlank()) {
      if (dto.getRegistrationNumber() == null || dto.getRegistrationNumber().isBlank()) {
        dto.setRegistrationNumber(registrationNumber.trim());
      }
    }

    // 4) Apply requested mutations without clearing existing values
    java.util.List<RequestDTO.Director> directors = dto.getDirectors();
    if (directors.isEmpty()) {
      directors.add(new RequestDTO.Director());
    }

    if (removeDirectorAt != null
        &&
        removeDirectorAt >= 1
        &&
        removeDirectorAt <= directors.size()) {
      directors.remove(removeDirectorAt - 1);
      if (directors.isEmpty()) {
        directors.add(new RequestDTO.Director());
      }
    } else if (directorCount != null && directorCount > directors.size()) {
      for (int i = directors.size(); i < directorCount; i++) {
        directors.add(new RequestDTO.Director());
      }
    }
    dto.setDirectors(directors);

    if (toolCount != null && toolCount > dto.getDocumentumTools().size()) {
      for (int i = dto.getDocumentumTools().size(); i < toolCount; i++) {
        dto.getDocumentumTools().add("");
      }
    }

    if (resolutionDocCount != null && resolutionDocCount > dto.getResolutionDocs().size()) {
      for (int i = dto.getResolutionDocs().size(); i < resolutionDocCount; i++) {
        dto.getResolutionDocs().add("");
      }
    }

    // 5) Persist + render
    pdfExtractionDataCache.put(pdfSessionId, dto);
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
    return ResponseEntity.ok(page);
  }

  // ========== SAVE DRAFT FROM SEARCH RESULTS ==========
  @PostMapping(value = "/saveDraftSearchResults", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> saveDraftSearchResults(@ModelAttribute RequestDTO form,
                                                       HttpServletRequest httpReq) {
    try {
      //Log raw incoming params so we can see how the UI posts (e.g. directors[0].name0)
      httpReq.getParameterMap().forEach((k, v) ->
          logger.debug("[saveDraft] {} = {}", k, Arrays.toString(v)));

      final String base = mandatesResolutionsDaoURL;
      RestTemplate rt = new RestTemplate();

      //If updating, fetch existing so we can merge (prevents wiping authorities on empty posts)
      RequestStagingDTO existing = null;
      if (form.getStagingId() != null) {
        try {
          existing = rt.getForObject(
              base + "/api/request-staging/{id}",
              RequestStagingDTO.class,
              form.getStagingId()
          );
        } catch (Exception ex) {
          logger.warn("Could not fetch existing draft {} for merge: {}",
              form.getStagingId(), ex.getMessage());
        }
      }

      //Build outbound staging DTO
      RequestStagingDTO dto = new RequestStagingDTO();
      dto.setStagingId(form.getStagingId()); // null=create; non-null=update

      //Company
      dto.setCompanyRegistrationNumber(nz(form.getRegistrationNumber()));
      dto.setCompanyName(dedupeComma(form.getCompanyName()));
      dto.setCompanyAddress(dedupeComma(form.getCompanyAddress()));

      //Request (ok if null)
      dto.setRequestType(
          mapRequestType(form.getMandateResolution())); // "1|2|3" -> Mandates|Resolutions|Both
      dto.setRequestStatus("Draft");
      dto.setRequestSubStatus("Saved");

      //Waiver tools -> CSV
      if (form.getDocumentumTools() != null) {
        LinkedHashSet<String> tools = new LinkedHashSet<>();
        for (String t : form.getDocumentumTools()) {
          if (t != null && !t.isBlank()) {
            tools.add(t.trim());
          }
        }
        dto.setWaiverPermittedTools(String.join(", ", tools));
      }

      //Directors to Authorities (prefer parsed from raw params)
      List<RequestDTO.Director> parsed = parseDirectorsFromParams(httpReq.getParameterMap());
      logger.debug("[saveDraft] parsed {} directors from params", parsed.size());

      List<RequestDTO.Director> incomingDirectors =
          (!parsed.isEmpty() ? parsed :
              (form.getDirectors() == null ? List.of() : form.getDirectors()));

      if (!incomingDirectors.isEmpty()) {
        List<RequestStagingDTO.AuthorityDraft> auths = new ArrayList<>();
        for (RequestDTO.Director d : incomingDirectors) {
          if (d == null || isAllBlank(d.getName(), d.getSurname(), d.getDesignation())) {
            continue;
          }
          RequestStagingDTO.AuthorityDraft a = new RequestStagingDTO.AuthorityDraft();
          a.setFirstname(nz(d.getName()));
          a.setSurname(nz(d.getSurname()));
          a.setDesignation(nz(d.getDesignation()));
          a.setIsActive(Boolean.TRUE);
          auths.add(a);
        }
        dto.setAuthorities(auths.isEmpty() ? null : auths);
      } else {
        //Nothing posted → keep existing authorities on update to avoid wiping
        dto.setAuthorities(existing != null ? existing.getAuthorities() : null);
      }

      //Accounts not edited on this page — carry forward if updating
      dto.setAccounts(existing != null ? existing.getAccounts() : null);

      //Debug: show payload going to DAO ----
      try {
        ObjectMapper om = new ObjectMapper();
        logger.info("Outgoing payload to DAO:\n{}",
            om.writerWithDefaultPrettyPrinter().writeValueAsString(dto));
      } catch (Exception ignore) {
        // intentionally empty
      }

      //Create or Update
      if (dto.getStagingId() == null) {
        RequestStagingDTO saved =
            rt.postForObject(base + "/api/request-staging", dto, RequestStagingDTO.class);
        if (saved != null) {
          dto.setStagingId(saved.getStagingId());
        }
      } else {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        rt.exchange(base + "/api/request-staging/{id}",
            HttpMethod.PUT, new HttpEntity<>(dto, headers), Void.class, dto.getStagingId());
      }

      try {
        if (dto.getStagingId() != null) {
          RequestStagingDTO after = rt.getForObject(
              base + "/api/request-staging/{id}",
              RequestStagingDTO.class,
              dto.getStagingId()
          );
          logger.info("After-save authorities.size={}",
              (after != null && after.getAuthorities() != null) ? after.getAuthorities().size() :
                  null);
        }
      } catch (Exception ignore) {
        // intentionally empty
      }

      //Rebuild Draft table
      RequestStagingDTO[] raws =
          rt.getForObject(base + "/api/request-staging/all", RequestStagingDTO[].class);
      List<RequestStagingDTO> list = (raws == null) ? List.of() : Arrays.asList(raws);

      RequestTableWrapper wrapper = new RequestTableWrapper();
      List<RequestTableDTO> rows = new ArrayList<>();
      for (RequestStagingDTO d : list) {
        RequestTableDTO r = new RequestTableDTO();
        r.setRequestId(d.getStagingId());
        r.setCompanyName(d.getCompanyName());
        r.setRegistrationNumber(d.getCompanyRegistrationNumber());
        r.setStatus(d.getRequestStatus());
        r.setSubStatus(d.getRequestSubStatus());
        r.setType(d.getRequestType());
        rows.add(r);
      }
      wrapper.setRequest(rows);

      String page = xsltProcessor.generatePage(xslPagePath("Draft"), wrapper);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);

    } catch (Exception e) {
      logger.error("saveDraftSearchResults failed", e);
      String error = "<page><error>Unable to save draft.</error></page>";
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(error);
    }
  }

  @RequestMapping(
      value = "/draft/save",
      method = { RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> saveDraft(
      @ModelAttribute RequestDTO form,
      @RequestParam(value = "pageCode", required = false) String pageCode,
      HttpServletRequest req
  ) {
    //1) Inspect posted params
    req.getParameterMap().forEach((k, v) ->
        logger.debug("[/draft/save] {} = {}", k, java.util.Arrays.toString(v)));

    final String base = mandatesResolutionsDaoURL;
    final RestTemplate rt = new RestTemplate();

    //2) Resolve pageCode
    logger.info("[/draft/save] posted pageCode = {}", first(req.getParameterMap().get("pageCode")));
    String resolved = resolvePage(req, pageCode);
    String currentPage = normalizePageCode(resolved);
    RequestStagingDTO existing = null;

    //3) Load existing draft if any
    Long stagingIdFromForm = form.getStagingId();
    if (stagingIdFromForm == null) {
      String rawId = first(req.getParameterMap().get("stagingId"));
      if (rawId != null && !rawId.isBlank()) {
        try {
          stagingIdFromForm = Long.valueOf(rawId.trim());
        } catch (NumberFormatException ignore) {
          // intentionally empty
        }
      }
    }
    if (stagingIdFromForm != null) {
      try {
        existing = rt.getForObject(
            base + "/api/request-staging/{id}",
            RequestStagingDTO.class,
            stagingIdFromForm
        );
      } catch (Exception ex) {
        logger.warn("Could not fetch existing draft {}: {}", stagingIdFromForm, ex.getMessage());
      }
    }
    if ((currentPage == null || currentPage.isBlank()) && existing != null) {
      currentPage = normalizePageCode(extractLastPageCode(existing.getRequestSubStatus()));
    }
    if (currentPage == null || currentPage.isBlank()) {
      currentPage = "SEARCH_RESULTS";
    }
    logger.info("[/draft/save] RESOLVED pageCode = {}", currentPage);

    //4) Start from existing (or new)
    RequestStagingDTO dto = (existing != null) ? existing : new RequestStagingDTO();
    dto.setStagingId(stagingIdFromForm);
    dto.setRequestStatus("Draft");
    dto.setRequestSubStatus("Saved@" + currentPage);

    //5) Parse & merge posted fields
    Map<String, String[]> params = req.getParameterMap();

    //5.1 Company (prefer last non-blank so visible inputs beat any hidden)
    String reg = nz(lastNonBlank(req, "registrationNumber"));
    String name = dedupeComma(nz(lastNonBlank(req, "companyName")));
    String addr = dedupeComma(nz(lastNonBlank(req, "companyAddress")));
    if (!reg.isBlank()) {
      dto.setCompanyRegistrationNumber(reg);
    }
    if (!name.isBlank()) {
      dto.setCompanyName(name);
    }
    if (!addr.isBlank()) {
      dto.setCompanyAddress(addr);
    }

    //5.2 Request type — prefer the LAST non-blank value (dropdown beats hidden)
    String mr = nz(lastNonBlank(req, "mandateResolution"));        // dropdown
    if (mr.isBlank()) {
      mr = nz(lastNonBlank(req, "mandateResolutionCode")); // hidden
    }
    if (mr.isBlank()) {
      mr = nz(lastNonBlank(req, "requestType"));
    }
    if (mr.isBlank()) {
      mr = nz(lastNonBlank(req, "type"));
    }
    if (!mr.isBlank()) {
      dto.setRequestType(mapRequestType(mr));
    }

    //5.3 Waiver tools
    java.util.List<String> tools = parseDocumentumToolsFromParams(params);
    if (tools.isEmpty()) {
      String raw = nz(first(params.get("waiverPermittedTools")));
      if (!raw.isBlank()) {
        for (String t : raw.split(",")) {
          String tt = t == null ? "" : t.trim();
          if (!tt.isBlank()) {
            tools.add(tt);
          }
        }
      }
    }
    if (!tools.isEmpty()) {
      java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
      for (String t : tools) {
        set.add(t.trim());
      }
      dto.setWaiverPermittedTools(String.join(", ", set));
    }

    //5.4 Directors -> Authorities
    java.util.List<RequestDTO.Director> directors = parseDirectorsFromParamsGeneric(params);
    if (directors.isEmpty()) {
      // fallback for keys like directors[0].name0/surname0/designation0
      directors = parseDirectorsFromParamsSuffix(params);
    }
    if (!directors.isEmpty()) {
      java.util.List<RequestStagingDTO.AuthorityDraft> auths = new java.util.ArrayList<>();
      for (RequestDTO.Director d : directors) {
        if (d == null || isAllBlank(d.getName(), d.getSurname(), d.getDesignation())) {
          continue;
        }
        RequestStagingDTO.AuthorityDraft a = new RequestStagingDTO.AuthorityDraft();
        a.setFirstname(d.getName() == null ? "" : d.getName().trim());
        a.setSurname(d.getSurname() == null ? "" : d.getSurname().trim());
        a.setDesignation(d.getDesignation() == null ? "" : d.getDesignation().trim());
        a.setIsActive(Boolean.TRUE);
        auths.add(a);
      }
      if (!auths.isEmpty()) {
        java.util.LinkedHashMap<String, RequestStagingDTO.AuthorityDraft> uniq =
            new java.util.LinkedHashMap<>();
        for (RequestStagingDTO.AuthorityDraft a : auths) {
          String key = (
              (a.getFirstname() == null ? "" : a.getFirstname().trim())
              + "|" + (a.getSurname() == null ? "" : a.getSurname().trim())
              + "|" + (a.getDesignation() == null ? "" : a.getDesignation().trim())
          ).toLowerCase();
          uniq.putIfAbsent(key, a);
        }
        dto.setAuthorities(new java.util.ArrayList<>(uniq.values()));
      }
    }

    //5.5 Accounts + Signatories
    java.util.List<RequestStagingDTO.AccountDraft> accounts = parseAccountsFromParams(params);
    if (!accounts.isEmpty()) {
      dto.setAccounts(accounts);
    }

    // ---- FINAL TYPE DECISION (do not override explicit/page-derived) ----
    String decidedType = dto.getRequestType();

    // 1)If user posted a value, it’s already in dto via mapRequestType(mr)
    // 2)If still blank, reuse existing
    if (isBlank(decidedType) && existing != null && !isBlank(existing.getRequestType())) {
      decidedType = existing.getRequestType();
    }
    //3) If still blank, infer from the current page
    if (isBlank(decidedType)) {
      String byPage = inferTypeFromPage(currentPage); // "Both" wins only for BOTH pages
      if (!isBlank(byPage)) {
        decidedType = byPage;
      }
    }
    //4) Only if STILL blank, infer from lists
    boolean hasAccts = dto.getAccounts() != null && !dto.getAccounts().isEmpty();
    boolean hasAuth = dto.getAuthorities() != null && !dto.getAuthorities().isEmpty();
    if (isBlank(decidedType)) {
      if (hasAccts && hasAuth) {
        decidedType = "Both";
      } else if (hasAccts) {
        decidedType = "Mandates";
      } else if (hasAuth) {
        decidedType = "Resolutions";
      }
    }
    dto.setRequestType(decidedType);

    //5) Now normalize lists based on the final type
    // IMPORTANT: do NOT normalize on Search Results page (prevents directors disappearing)
    final boolean isSearchResults = "SEARCH_RESULTS".equalsIgnoreCase(currentPage);
    if (!isSearchResults) {
      normalizeListsByType(dto);
    }

    //6) Debug outbound payload
    try {
      com.fasterxml.jackson.databind.ObjectMapper om =
          new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();
      logger.info("[/draft/save] OUTBOUND JSON:\n{}",
          om.writerWithDefaultPrettyPrinter().writeValueAsString(dto));
    } catch (Exception ignore) {
      // intentionally empty
    }

    //7) Persist
    if (dto.getStagingId() == null) {
      RequestStagingDTO saved =
          rt.postForObject(base + "/api/request-staging", dto, RequestStagingDTO.class);
      if (saved != null) {
        dto.setStagingId(saved.getStagingId());
      }
    } else {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      rt.exchange(base + "/api/request-staging/{id}",
          HttpMethod.PUT, new HttpEntity<>(dto, headers), Void.class, dto.getStagingId());
    }

    // 8) Optional verify
    try {
      ResponseEntity<String> raw =
          rt.getForEntity(base + "/api/request-staging/{id}", String.class, dto.getStagingId());
      logger.info("[/draft/save] AFTER PUT raw GET JSON:\n{}", raw.getBody());
    } catch (Exception ex) {
      logger.warn("[/draft/save] After-PUT verify failed: {}", ex.getMessage());
    }

    // 9) Return the Draft table
    RequestStagingDTO[] raws =
        rt.getForObject(base + "/api/request-staging/all", RequestStagingDTO[].class);
    java.util.List<RequestStagingDTO> list =
        (raws == null) ? java.util.List.of() : java.util.Arrays.asList(raws);

    java.time.format.DateTimeFormatter fmt =
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    RequestTableWrapper wrapper = new RequestTableWrapper();
    java.util.List<RequestTableDTO> rows = new java.util.ArrayList<>();
    for (RequestStagingDTO d : list) {
      // Enrich per-row if created or requestType missing (DAO list may omit them)
      RequestStagingDTO src = d;
      if (d.getCreated() == null || isBlank(d.getRequestType())) {
        try {
          src = rt.getForObject(base + "/api/request-staging/{id}",
              RequestStagingDTO.class, d.getStagingId());
        } catch (Exception ignore) {
          // intentionally empty
        }
        if (src == null) {
          src = d;
        }
      }

      RequestTableDTO r = new RequestTableDTO();
      r.setRequestId(src.getStagingId());
      r.setCompanyName(src.getCompanyName());
      r.setRegistrationNumber(src.getCompanyRegistrationNumber());
      r.setStatus(src.getRequestStatus());
      r.setSubStatus(cleanSubStatus(src.getRequestSubStatus()));
      r.setType(src.getRequestType());  // for <type>
      r.setCreated(src.getCreated() == null ? "" : src.getCreated().format(fmt)); // formatted
      r.setUpdated(null);
      rows.add(r);
    }
    wrapper.setRequest(rows);

    String pageXml = xsltProcessor.generatePage(xslPagePath("DraftRequests"), wrapper);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(pageXml);
  }

  @PostMapping(value = "/nextStep", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> handleNextStep(
      @ModelAttribute RequestDTO requestDto,
      @RequestParam(value = "mandateResolution", required = false) String requestType,
      @RequestParam(value = "back", required = false) String back,
      HttpServletRequest request,
      HttpSession session,
      @RequestParam(value = "file", required = false) MultipartFile file
      // may be null on non-multipart
  ) {
    logger.debug("---- RAW PARAMS (nextStep) ----");
    request.getParameterMap()
        .forEach((k, v) -> logger.debug("{} = {}", k, java.util.Arrays.toString(v)));
    logger.debug("--------------------------------");
    logger.info("Received requestType: {}", requestType);

    final String reqSel = normalizeSelCode(requestType);
    final long MAX_FILE_BYTES = 10L * 1024 * 1024; // 10MB

    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();
    java.util.function.Function<String, String> dedupeComma = s -> {
      String t = nz.apply(s);
      if (t.isEmpty()) {
        return t;
      }
      String[] parts = t.split("\\s*,\\s*");
      if (parts.length <= 1) {
        return t;
      }
      java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
      for (String p : parts) {
        if (!p.isBlank()) {
          set.add(p);
        }
      }
      return String.join(", ", set);
    };

    // ---------- Opportunistically capture files if the form itself posted multipart ----------
    try {
      boolean captured = false;

      if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest mreq) {
        for (var e : mreq.getFileMap().entrySet()) {
          MultipartFile mf = e.getValue();
          if (mf != null && !mf.isEmpty()) {
            if (mf.getSize() > MAX_FILE_BYTES) {
              return ResponseEntity.ok("""
                  <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        id="" heading=" " template="error" version="1">
                    <error xsi:type="validationError"><name>file</name><code>0</code>
                      <message>File size exceeds 10MB.</message>
                    </error>
                  </page>
                  """);
            }
            String name = (mf.getOriginalFilename() == null || mf.getOriginalFilename().isBlank())
                ? e.getKey() : mf.getOriginalFilename();
            String ct = (mf.getContentType() == null || mf.getContentType().isBlank())
                ? "application/octet-stream" : mf.getContentType();

            getOrInitSessionFiles(session).add(
                new SessionFile(name, ct, mf.getSize(), mf.getBytes()));
            logger.info("nextStep: captured multipart field '{}' -> name={}, type={}, size={}",
                e.getKey(), name, ct, mf.getSize());
            captured = true;

            // reflect filename into DTO for UI
            RequestDTO namesDto = (RequestDTO) session.getAttribute("requestData");
            if (namesDto == null) {
              namesDto = new RequestDTO();
            }
            if (namesDto.getResolutionDocs() == null) {
              namesDto.setResolutionDocs(new java.util.ArrayList<>());
            }
            namesDto.getResolutionDocs().add(name);
            namesDto.setEditable(true);
            session.setAttribute("requestData", namesDto);
          }
        }
      }

      if (!captured) {
        // Legacy single param binding (often null on non-multipart)
        if (file != null && !file.isEmpty()) {
          if (file.getSize() > MAX_FILE_BYTES) {
            return ResponseEntity.ok("""
                <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      id="" heading=" " template="error" version="1">
                  <error xsi:type="validationError"><name>file</name><code>0</code>
                    <message>File size exceeds 10MB.</message>
                  </error>
                </page>
                """);
          }
          String name = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
              ? "uploaded-file" : file.getOriginalFilename();
          String ct = (file.getContentType() == null || file.getContentType().isBlank())
              ? "application/octet-stream" : file.getContentType();

          getOrInitSessionFiles(session).add(
              new SessionFile(name, ct, file.getSize(), file.getBytes()));
          logger.info("nextStep: captured single file -> name={}, type={}, size={}", name, ct,
              file.getSize());

          RequestDTO namesDto = (RequestDTO) session.getAttribute("requestData");
          if (namesDto == null) {
            namesDto = new RequestDTO();
          }
          if (namesDto.getResolutionDocs() == null) {
            namesDto.setResolutionDocs(new java.util.ArrayList<>());
          }
          namesDto.getResolutionDocs().add(name);
          namesDto.setEditable(true);
          session.setAttribute("requestData", namesDto);

        } else {
          // Non-multipart fallback: you'll only see a fake path string
          String postedPath = nz.apply(request.getParameter("file"));
          if (!postedPath.isBlank()) {
            logger.info(
                "nextStep: non-multipart detected; only path seen ({}). File should be uploaded "
                +
                "via <comm:fileUpload fileUploadUrl='/app-domain"
                + "/mandates-and-resolutions/mandates/attachment/upload"
                + "'>.",
                postedPath);
          }
        }
      }

      logger.info("nextStep: session uploadedFiles count = {}",
          getOrInitSessionFiles(session).size());
    } catch (Exception ex) {
      logger.error("nextStep: failed handling optional file(s)", ex);
      return ResponseEntity.ok("""
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                id="" heading=" " template="error" version="1">
            <error xsi:type="systemError"><code>500</code>
              <message>Upload failed.</message>
            </error>
          </page>
          """);
    }
    // ---------- end file capture ----------

    // Manual directors parsing from indexed params
    java.util.List<RequestDTO.Director> manualDirectors = new java.util.ArrayList<>();
    int idx = 0;
    while (true) {
      String name = request.getParameter("directors[" + idx + "].name" + idx);
      String surname = request.getParameter("directors[" + idx + "].surname" + idx);
      String designation = request.getParameter("directors[" + idx + "].designation" + idx);
      if (name == null && surname == null && designation == null) {
        break;
      }
      RequestDTO.Director d = new RequestDTO.Director();
      d.setName(name);
      d.setSurname(surname);
      d.setDesignation(designation);
      manualDirectors.add(d);
      idx++;
    }
    if (!manualDirectors.isEmpty()) {
      if (requestDto == null) {
        requestDto = new RequestDTO();
      }
      requestDto.setDirectors(manualDirectors);
      logger.info("Manually built {} directors from request params", manualDirectors.size());
    }

    // Back path -> merge + return to SearchResults
    if ("1".equals(back)) {
      String pdfSessionId = request.getParameter("pdfSessionId");
      if (pdfSessionId == null) {
        pdfSessionId = (String) session.getAttribute("pdfSessionId");
      }

      RequestDTO dto = (RequestDTO) session.getAttribute("requestData");
      if (dto == null && pdfSessionId != null) {
        dto = pdfExtractionDataCache.get(pdfSessionId);
      }
      if (dto == null) {
        dto = new RequestDTO();
      }

      if (dto.getDirectors() == null) {
        dto.setDirectors(new java.util.ArrayList<>());
      }
      if (dto.getDocumentumTools() == null) {
        dto.setDocumentumTools(new java.util.ArrayList<>());
      }
      if (dto.getResolutionDocs() == null) {
        dto.setResolutionDocs(new java.util.ArrayList<>());
      }

      if (requestDto != null) {
        String incomingName = dedupeComma.apply(requestDto.getCompanyName());
        if (!incomingName.isBlank() && !incomingName.equals(nz.apply(dto.getCompanyName()))) {
          dto.setCompanyName(incomingName);
        }
        String incomingAddr = dedupeComma.apply(requestDto.getCompanyAddress());
        if (!incomingAddr.isBlank() && !incomingAddr.equals(nz.apply(dto.getCompanyAddress()))) {
          dto.setCompanyAddress(incomingAddr);
        }
        String incomingReg = nz.apply(requestDto.getRegistrationNumber());
        if (!incomingReg.isBlank() && !incomingReg.equals(nz.apply(dto.getRegistrationNumber()))) {
          dto.setRegistrationNumber(incomingReg);
        }

        if (requestDto.getDirectors() != null && !requestDto.getDirectors().isEmpty()) {
          java.util.List<RequestDTO.Director> existing =
              dto.getDirectors() != null ? dto.getDirectors() : new java.util.ArrayList<>();
          java.util.List<RequestDTO.Director> incoming = requestDto.getDirectors();
          while (existing.size() < incoming.size()) {
            existing.add(new RequestDTO.Director());
          }
          for (int i2 = 0; i2 < incoming.size(); i2++) {
            RequestDTO.Director in = incoming.get(i2);
            RequestDTO.Director ex = existing.get(i2);
            if (in.getName() != null && !in.getName().isBlank()) {
              ex.setName(in.getName().trim());
            }
            if (in.getSurname() != null && !in.getSurname().isBlank()) {
              ex.setSurname(in.getSurname().trim());
            }
            if (in.getDesignation() != null && !in.getDesignation().isBlank()) {
              ex.setDesignation(in.getDesignation().trim());
            }
          }
          dto.setDirectors(existing);
        }

        if (requestDto.getDocumentumTools() != null
            && requestDto.getDocumentumTools().stream().anyMatch(t -> t != null && !t.isBlank())) {
          dto.setDocumentumTools(requestDto.getDocumentumTools());
        }
        if (requestDto.getResolutionDocs() != null
            && requestDto.getResolutionDocs().stream().anyMatch(r -> r != null && !r.isBlank())) {
          dto.setResolutionDocs(requestDto.getResolutionDocs());
        }
      }

      if (reqSel != null) {
        dto.setMandateResolution(reqSel);
      } else if (dto.getMandateResolution() == null || dto.getMandateResolution().isBlank()) {
        String raw = request.getParameter("mandateResolution");
        String norm = normalizeSelCode(raw);
        if (norm != null) {
          dto.setMandateResolution(norm);
        }
      }

      dto.setPdfSessionId(pdfSessionId);
      dto.setEditable(true);
      session.setAttribute("requestData", dto);
      if (pdfSessionId != null) {
        pdfExtractionDataCache.put(pdfSessionId, dto);
      }

      RequestWrapper wrapper = new RequestWrapper();
      wrapper.setRequest(dto);
      String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
      return ResponseEntity.ok(page);
    }

    // ---------- Proceed path ----------
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto = (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : null;
    if (dto == null) {
      dto = new RequestDTO();
    }

    if (dto.getDirectors() == null) {
      dto.setDirectors(new java.util.ArrayList<>());
    }
    if (dto.getDocumentumTools() == null) {
      dto.setDocumentumTools(new java.util.ArrayList<>());
    }
    if (dto.getResolutionDocs() == null) {
      dto.setResolutionDocs(new java.util.ArrayList<>());
    }

    if (requestDto != null) {
      String nm = dedupeComma.apply(requestDto.getCompanyName());
      if (!nm.isBlank()) {
        dto.setCompanyName(nm);
      }

      String addr = dedupeComma.apply(requestDto.getCompanyAddress());
      if (!addr.isBlank()) {
        dto.setCompanyAddress(addr);
      }

      String reg = nz.apply(requestDto.getRegistrationNumber());
      if (!reg.isBlank()) {
        dto.setRegistrationNumber(reg);
      }

      if (requestDto.getDirectors() != null && !requestDto.getDirectors().isEmpty()) {
        java.util.List<RequestDTO.Director> incoming = requestDto.getDirectors();
        while (dto.getDirectors().size() < incoming.size()) {
          dto.getDirectors().add(new RequestDTO.Director());
        }
        for (int i = 0; i < incoming.size(); i++) {
          RequestDTO.Director in = incoming.get(i);
          RequestDTO.Director ex = dto.getDirectors().get(i);
          if (in.getName() != null && !in.getName().isBlank()) {
            ex.setName(in.getName().trim());
          }
          if (in.getSurname() != null && !in.getSurname().isBlank()) {
            ex.setSurname(in.getSurname().trim());
          }
          if (in.getDesignation() != null && !in.getDesignation().isBlank()) {
            ex.setDesignation(in.getDesignation().trim());
          }
        }
      }

      if (requestDto.getDocumentumTools() != null && !requestDto.getDocumentumTools().isEmpty()) {
        java.util.List<String> incTools = requestDto.getDocumentumTools();
        while (dto.getDocumentumTools().size() < incTools.size()) {
          dto.getDocumentumTools().add("");
        }
        for (int i = 0; i < incTools.size(); i++) {
          String v = nz.apply(incTools.get(i));
          if (!v.isBlank()) {
            dto.getDocumentumTools().set(i, v);
          }
        }
      }
    }

    if (reqSel != null) {
      dto.setMandateResolution(reqSel);
    }

    if (pdfSessionId == null || pdfSessionId.isBlank()) {
      pdfSessionId = java.util.UUID.randomUUID().toString();
      session.setAttribute("pdfSessionId", pdfSessionId);
    }
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    pdfExtractionDataCache.put(pdfSessionId, dto);
    session.setAttribute("requestData", dto);

    // ---- Validations ----
    SearchResultsErrorModel errors = new SearchResultsErrorModel();
    boolean hasErrors = false;

    if (nz.apply(dto.getCompanyName()).isBlank()) {
      errors.setCompanyName("Company name is required");
      hasErrors = true;
    }
    if (nz.apply(dto.getCompanyAddress()).isBlank()) {
      errors.setCompanyAddress("Company address is required");
      hasErrors = true;
    }

    java.util.List<String> tools = dto.getDocumentumTools();
    if (tools != null && !tools.isEmpty()) {
      for (String t : tools) {
        if (nz.apply(t).isBlank()) {
          errors.setCompanyWaiver("Please enter the waiver tool");
          hasErrors = true;
          break;
        }
      }
    }

    java.util.List<RequestDTO.Director> directors = dto.getDirectors();
    if (directors == null || directors.isEmpty()) {
      errors.setFullName("Director name is required");
      errors.setSurname("Director surname is required");
      errors.setDesignation("Director designation is required");
      hasErrors = true;
    } else {
      boolean anyMissing = false;
      for (RequestDTO.Director d : directors) {
        boolean rowMissing = nz.apply(d.getName()).isBlank()
                             || nz.apply(d.getSurname()).isBlank()
                             || nz.apply(d.getDesignation()).isBlank();
        if (rowMissing) {
          anyMissing = true;
          break;
        }
      }
      if (anyMissing) {
        if (nz.apply(errors.getFullName()).isBlank()) {
          errors.setFullName("Director name is required");
        }
        if (nz.apply(errors.getSurname()).isBlank()) {
          errors.setSurname("Director surname is required");
        }
        if (nz.apply(errors.getDesignation()).isBlank()) {
          errors.setDesignation("Director designation is required");
        }
        hasErrors = true;
      }
    }

    if (hasErrors) {
      RequestWrapper wrapper = new RequestWrapper();
      wrapper.setRequest(dto);
      wrapper.setSearchResultsErrorModel(errors);
      String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
      return ResponseEntity.ok(page);
    }

    // Read all potential checkbox params
    String mandateSign = firstNonBlank(request,
        "confirmationCheckMandate",               // comm:name
        "confirmationCheckMandate_signatures"     // inputItem id
    );
    String mandateSigma = firstNonBlank(request,
        "confirmationCheckMandateSigma",          // comm:name
        "confirmationCheckMandate_sigma"          // inputItem id
    );

    String resolutionSign = firstNonBlank(request,
        "confirmationCheckResolution",
        "confirmationCheckResolution_signatures"
    );
    String resolutionSigma = firstNonBlank(request,
        "confirmationCheckResolutionSigma",
        "confirmationCheckResolution_sigma"
    );

    String mandateResolutionSign = firstNonBlank(request,
        "confirmationCheckMandateResolution",
        "confirmationCheckMandateResolution_signatures"
    );
    String mandateResolutionSigma = firstNonBlank(request,
        "confirmationCheckMandateResolutionSigma",
        "confirmationCheckMandateResolution_sigma"
    );

    //RequestType validation
    if (requestType == null || requestType.isBlank() || "-1".equals(requestType)) {
      return ResponseEntity.ok("""
          <page xmlns:comm="http://ws.online.fnb.co.za/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                id="" heading=" " template="error" version="1">
              <error xsi:type="validationError">
                  <name>mandateResolution</name><code>0</code>
                  <message>Please select a valid request type.</message>
              </error>
          </page>
          """);
    }

    // Require BOTH boxes for the active section
    boolean ok = switch (requestType) {
      case "1" -> "1".equals(mandateSign) && "1".equals(mandateSigma);   // Mandate
      case "2" -> "1".equals(resolutionSign) && "1".equals(resolutionSigma);   // Resolution
      // Mandate + Resolution
      case "3" -> "1".equals(mandateResolutionSign) && "1".equals(mandateResolutionSigma);
      default -> false;
    };

    if (!ok) {
      return ResponseEntity.ok("""
          <page xmlns:comm="http://ws.online.fnb.co.za/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                id="" heading=" " template="error" version="1">
              <error xsi:type="validationError">
                  <name>confirmationCheck</name><code>0</code>
                  <message>Please make sure both confirmation 
                  checkboxes are checked at the bottom of the page.</message>
              </error>
          </page>
          """);
    }

    if (reqSel != null) {
      dto.setMandateResolution(reqSel);
      pdfExtractionDataCache.put(pdfSessionId, dto);
      session.setAttribute("requestData", dto);
    }

    // Route
    logger.info("nextStep: routing to page for requestType {}", requestType);
    return switch (requestType) {
      case "1" -> generateMandatesFillPage(1, null, null, null, null);
      case "2" -> generateResolutionsFillPage(null, null);
      case "3" -> mrAccDetailsGet(session);
      default -> ResponseEntity.ok("""
          <page xmlns:comm="http://ws.online.fnb.co.za/common/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                id="" heading=" " template="error" version="1">
              <error xsi:type="validationError">
                <name>mandateResolution</name><code>0</code>
                <message>Invalid request type.</message>
              </error>
          </page>
          """);
    };
  }

  //mandatesFill POST
  @PostMapping(value = "/mandatesFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesFillPost(
      HttpServletRequest request,
      @RequestParam(defaultValue = "1") int accountCount,
      @RequestParam(required = false) String signatoryCounts,
      @RequestParam(required = false) String removeSignatoryAt, // "2_1"
      @RequestParam(required = false) Integer addSignatoryAt,   // 2
      @RequestParam(required = false) Integer removeAccountAt,
      HttpSession session
  ) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }

    //1) Parse + merge by position (single value per name now)
    java.util.List<RequestDTO.Account> parsed = parseAccountsFromRequest(request);
    mergeAccounts(dto, parsed);

    //2) Work on the live list
    java.util.List<RequestDTO.Account> accounts = dto.getAccounts();
    if (accounts == null) {
      accounts = new java.util.ArrayList<>();
    }

    //3) Apply UI actions AFTER merge
    if (addSignatoryAt != null && addSignatoryAt >= 1 && addSignatoryAt <= accounts.size()) {
      accounts.get(addSignatoryAt - 1).getSignatories().add(createBlankSignatory());
    }

    if (removeSignatoryAt != null && !removeSignatoryAt.isEmpty()) {
      try {
        String[] parts = removeSignatoryAt.split("_");
        if (parts.length == 2) {
          int a = Integer.parseInt(parts[0]) - 1;
          int s = Integer.parseInt(parts[1]) - 1;
          if (a >= 0 && a < accounts.size()) {
            java.util.List<RequestDTO.Signatory> signs = accounts.get(a).getSignatories();
            if (signs != null && s >= 0 && s < signs.size()) {
              signs.remove(s);
            }
            if (signs == null || signs.isEmpty()) {
              accounts.get(a).setSignatories(new java.util.ArrayList<>(
                  java.util.List.of(createBlankSignatory())));
            }
          }
        }
      } catch (NumberFormatException ignore) {
        // intentionally empty
      }
    }

    boolean deletingAccount =
        (removeAccountAt != null && removeAccountAt >= 1 && removeAccountAt <= accounts.size());
    if (deletingAccount) {
      accounts.remove(removeAccountAt - 1);
    }

    //4)Append blanks to reach accountCount (exactly +1 when you clicked Add)
    if (!deletingAccount) {
      while (accounts.size() < accountCount) {
        accounts.add(createBlankAccount());
      }
    }
    if (accounts.isEmpty()) {
      accounts.add(createBlankAccount());
    }

    dto.setAccounts(accounts);
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);

    //5)Persist
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    //6)Render
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
    return ResponseEntity.ok(page);
  }

  @GetMapping(value = "/mandatesFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesFillGet(
      @RequestParam(required = false) Long stagingId,
      @RequestParam(required = false) String pdfSessionId,
      @RequestParam(defaultValue = "1") int accountCount,
      @RequestParam(required = false) String signatoryCounts,
      @RequestParam(required = false) String removeSignatoryAt,
      @RequestParam(required = false) Integer addSignatoryAt,
      @RequestParam(required = false) Integer removeAccountAt,
      HttpServletRequest req, //Nneeded for applyAccAndSigsEditsFromRequest
      HttpSession session
  ) {
    //1) Keep existing session resolution
    if (pdfSessionId == null) {
      Object sessionVal = session.getAttribute("pdfSessionId");
      if (sessionVal != null) {
        pdfSessionId = sessionVal.toString();
      }
    }

    //2) When editing an existing draft, preload from DAO
    if (stagingId != null) {
      RequestWrapper wrapper = buildAutoFillWrapperFromStaging(stagingId, pdfSessionId);
      RequestDTO dto = wrapper.getRequest();
      if (dto == null) {
        dto = new RequestDTO();
        wrapper.setRequest(dto);
      }

      //Apply current request’s in-page edits (accountCount, remove*, field edits from inputs)
      applyAccAndSigsEditsFromRequest(req, dto);

      //Handle "add signatory" button (explicitly grow by 1 blank row)
      if (addSignatoryAt != null && addSignatoryAt > 0) {
        ensureAccounts(dto, addSignatoryAt);
        RequestDTO.Account acc = dto.getAccounts().get(addSignatoryAt - 1);
        int cur = (acc.getSignatories() == null) ? 0 : acc.getSignatories().size();
        ensureSignatories(acc, cur + 1);
      }

      session.setAttribute("pdfSessionId", dto.getPdfSessionId());
      session.setAttribute("requestData", dto);

      String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
    }

    // 3)Existing behavior: serve from PDF extraction cache if present
    if (pdfSessionId != null && pdfExtractionDataCache.containsKey(pdfSessionId)) {
      RequestDTO dto = pdfExtractionDataCache.get(pdfSessionId);
      session.setAttribute("pdfSessionId", pdfSessionId);
      session.setAttribute("requestData", dto);

      RequestWrapper wrapper = new RequestWrapper();
      wrapper.setRequest(dto);
      String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
    }

    // 4)Existing fallback
    return generateMandatesFillPage(
        accountCount, signatoryCounts, removeSignatoryAt, addSignatoryAt, removeAccountAt
    );
  }

  //First load / fallback
  private ResponseEntity<String> generateMandatesFillPage(
      int accountCount,
      String signatoryCounts,
      String removeSignatoryAt,
      Integer addSignatoryAt,
      Integer removeAccountAt
  ) {
    HttpSession session =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
            .getSession();
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");

    RequestDTO dto = (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : null;
    if (dto == null) {
      dto = new RequestDTO();
      dto.setAccounts(new java.util.ArrayList<>());
    }

    java.util.List<RequestDTO.Account> accounts =
        (dto.getAccounts() != null) ? dto.getAccounts() : new java.util.ArrayList<>();
    if (accounts.isEmpty()) {
      int target = Math.max(1, accountCount);
      for (int i = 0; i < target; i++) {
        accounts.add(createBlankAccount());
      }
    }

    if (addSignatoryAt != null && addSignatoryAt >= 1 && addSignatoryAt <= accounts.size()) {
      accounts.get(addSignatoryAt - 1).getSignatories().add(createBlankSignatory());
    }

    if (removeSignatoryAt != null && !removeSignatoryAt.isEmpty()) {
      try {
        String[] parts = removeSignatoryAt.split("_");
        if (parts.length == 2) {
          int a = Integer.parseInt(parts[0]) - 1;
          int s = Integer.parseInt(parts[1]) - 1;
          if (a >= 0 && a < accounts.size()) {
            java.util.List<RequestDTO.Signatory> signs = accounts.get(a).getSignatories();
            if (signs != null && s >= 0 && s < signs.size()) {
              signs.remove(s);
            }
          }
        }
      } catch (NumberFormatException ignore) {
        // intentionally empty
      }
    }

    if (removeAccountAt != null && removeAccountAt >= 1 && removeAccountAt <= accounts.size()) {
      accounts.remove(removeAccountAt - 1);
    }

    dto.setAccounts(accounts);
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);

    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
    return ResponseEntity.ok(page);
  }

  /**
   * Generate the Resolutions AutoFill page with persisted state in cache + session.
   * Keeps at least 1 director; supports removing a director by 1-based index.
   */
  private ResponseEntity<String> generateResolutionsFillPage(
      Integer directorCount,
      Integer removeDirectorAt
  ) {
    //Load from session/cache
    HttpSession session =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest()
            .getSession();

    String pdfSessionId = (String) session.getAttribute("pdfSessionId");

    RequestDTO dto = null;
    if (pdfSessionId != null) {
      dto = pdfExtractionDataCache.get(pdfSessionId);
    }
    if (dto == null) {
      dto = new RequestDTO();
      dto.setDirectors(new ArrayList<>());
    }

    List<RequestDTO.Director> directors = dto.getDirectors();
    if (directors == null) {
      directors = new ArrayList<>();
    }

    //Ensure at least directorCount directors exist
    if (directorCount == null || directorCount < 1) {
      directorCount = 1;
    }
    while (directors.size() < directorCount) {
      directors.add(createBlankDirector());
    }

    //Remove Director (1-based index)
    if (removeDirectorAt != null && removeDirectorAt >= 1 && removeDirectorAt <= directors.size()) {
      directors.remove(removeDirectorAt - 1);
      //Keep at least one row so the page renders
      if (directors.isEmpty()) {
        directors.add(createBlankDirector());
      }
    }

    //Persist into DTO / session / cache
    dto.setDirectors(directors);
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);

    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    //Wrap & render
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("ResolutionAutoFill"), wrapper);
    return ResponseEntity.ok(page);
  }

  // /mandatesSignatureCard POST (VALIDATING VERSION)
  @PostMapping(value = "/mandatesSignatureCard", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesSignatureCardPost(
      HttpServletRequest request,
      HttpSession session
  ) {
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }

    // 1) Parse & merge by position
    java.util.List<RequestDTO.Account> parsed = parseAccountsFromRequest(request);
    mergeAccounts(dto, parsed);

    // 2) Ensure there is at least one account and each account has at least one signatory row
    if (dto.getAccounts().isEmpty()) {
      dto.getAccounts().add(createBlankAccount());
    }
    for (RequestDTO.Account acc : dto.getAccounts()) {
      if (acc.getSignatories() == null) {
        acc.setSignatories(new java.util.ArrayList<>());
      }
      if (acc.getSignatories().isEmpty()) {
        acc.getSignatories().add(createBlankSignatory());
      }
    }

    // 3) Validate required fields for MandatesAutoFill
    MandatesAutoFillErrorModel errors = new MandatesAutoFillErrorModel();
    boolean hasErrors = false;

    for (RequestDTO.Account acc : dto.getAccounts()) {
      if (nz.apply(acc.getAccountName()).isBlank()) {
        errors.setAccountName("Account name is required");
        hasErrors = true;
      }
      if (nz.apply(acc.getAccountNo()).isBlank()) {
        errors.setAccountNo("Account number is required");
        hasErrors = true;
      }

      java.util.List<RequestDTO.Signatory> signs = acc.getSignatories();
      if (signs == null || signs.isEmpty()) {
        // Safety (shouldn’t happen due to step 2, but keep messages consistent)
        errors.setSignatoryFullName("Full name is required");
        errors.setSignatoryIdNumber("ID number is required");
        errors.setSignatoryInstruction("Instruction is required");
        hasErrors = true;
        continue;
      }

      for (RequestDTO.Signatory s : signs) {
        if (nz.apply(s.getFullName()).isBlank()) {
          errors.setSignatoryFullName("Full name is required");
          hasErrors = true;
        }
        if (nz.apply(s.getIdNumber()).isBlank()) {
          errors.setSignatoryIdNumber("ID number is required");
          hasErrors = true;
        }
        if (nz.apply(s.getInstruction()).isBlank()) {
          errors.setSignatoryInstruction("Instruction is required");
          hasErrors = true;
        }
      }
    }

    // 4) Persist the merged DTO (so the user sees their latest entries on re-render)
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    if (hasErrors) {
      //Stay on Mandates Auto Fill and show inline error messages
      RequestWrapper wrapper = new RequestWrapper();
      wrapper.setRequest(dto);
      wrapper.setMandatesAutoFillErrorModel(errors);

      String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
      return ResponseEntity.ok(page);
    }

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("MandatesSignatureCard"), wrapper);
    return ResponseEntity.ok(page);
  }

  // /mandatesSignatureCard GET (unchanged)
  @GetMapping(value = "/mandatesSignatureCard", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesSignatureCardGet(HttpSession session) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto = (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : null;
    if (dto == null) {
      dto = new RequestDTO();
      dto.setAccounts(new java.util.ArrayList<>());
      if (dto.getAccounts().isEmpty()) {
        RequestDTO.Account a = new RequestDTO.Account();
        a.setAccountName("");
        a.setAccountNo("");
        a.setSignatories(new java.util.ArrayList<>(java.util.List.of(createBlankSignatory())));
        dto.getAccounts().add(a);
      }
    }

    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("MandatesSignatureCard"), wrapper);
    return ResponseEntity.ok(page);
  }


  //Success Pages
  @PostMapping(value = "/mandatesSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("MandatesSuccessPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  // ======================= MANDATES SUBMIT =======================
  @PostMapping(value = "/mandatesSubmit", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> handleMandatesSubmit(HttpServletRequest request,
                                                     HttpSession session) {
    System.out.println("Submitting Mandates (FINAL)...");
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    try {
      // Load UI state
      String pdfSessionId = (String) session.getAttribute("pdfSessionId");
      RequestDTO uiData =
          (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
      if (uiData.getAccounts() == null) {
        uiData.setAccounts(new java.util.ArrayList<>());
      }

      // Merge last edits from form
      java.util.List<RequestDTO.Account> parsed = parseAccountsFromRequest(request);
      mergeAccounts(uiData, parsed);

      // ---- Validate ----
      MandatesSignatureCardErrorModel errors = new MandatesSignatureCardErrorModel();
      boolean hasErrors = false;
      if (uiData.getAccounts().isEmpty()) {
        uiData.getAccounts().add(createBlankAccount());
      }
      for (RequestDTO.Account acc : uiData.getAccounts()) {
        if (nz.apply(acc.getAccountName()).isBlank()) {
          errors.setAccountName("Account name is required");
          hasErrors = true;
        }
        if (nz.apply(acc.getAccountNo()).isBlank()) {
          errors.setAccountNo("Account number is required");
          hasErrors = true;
        }
        var signs = acc.getSignatories();
        if (signs == null || signs.isEmpty()) {
          errors.setSignatoryFullName("Full name is required");
          errors.setSignatoryIdNumber("ID number is required");
          errors.setCapacity("Capacity is required");
          errors.setGroup("Group is required");
          hasErrors = true;
          continue;
        }
        for (RequestDTO.Signatory s : signs) {
          String inst = nz.apply(s.getInstruction()).toUpperCase();
          if ("REMOVE".equals(inst)) {
            continue;
          }
          if (nz.apply(s.getFullName()).isBlank()) {
            errors.setSignatoryFullName("Full name is required");
            hasErrors = true;
          }
          if (nz.apply(s.getIdNumber()).isBlank()) {
            errors.setSignatoryIdNumber("ID number is required");
            hasErrors = true;
          }
          if (nz.apply(s.getCapacity()).isBlank()) {
            errors.setCapacity("Capacity is required");
            hasErrors = true;
          }
          if (nz.apply(s.getGroup()).isBlank()) {
            errors.setGroup("Group is required");
            hasErrors = true;
          }
        }
      }

      // Persist UI state back to session/cache
      uiData.setPdfSessionId(pdfSessionId);
      uiData.setEditable(true);
      if (pdfSessionId != null) {
        pdfExtractionDataCache.put(pdfSessionId, uiData);
      }
      session.setAttribute("requestData", uiData);

      if (hasErrors) {
        RequestWrapper wrapper = new RequestWrapper();
        wrapper.setRequest(uiData);
        wrapper.setMandatesSignatureCardErrorModel(errors);
        String page = xsltProcessor.generatePage(xslPagePath("MandatesSignatureCard"), wrapper);
        return ResponseEntity.ok(page);
      }

      // -- stamp logged-in user --
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO current =
          (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
              "currentUser");

      String uname = currentDisplayId(session, request);
      if (uname.isBlank()) {
        uname = "UI_USER";
      }
      uiData.setLoggedInUsername(uname);
      uiData.setLoggedInEmail(current != null ? nz.apply(current.getEmail()) : "");

      // ---- Submit to backend (creates the Request and returns requestId) ----
      SubmissionPayload payload = buildSubmissionPayload(uiData, "Mandates");
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO result
          = postSnapshotToBackend(payload);

      Long requestId = null;
      if (result != null && result.getRequest() != null) {
        var r = result.getRequest();
        requestId = r.getRequestId();
        logger.info("Mandates submission OK: requestId={}, processId={}, assignedUser={}",
            r.getRequestId(), r.getProcessId(), r.getAssignedUser());
      }

      // ---- DEBUG: see what we actually have in session ----
      @SuppressWarnings("unchecked")
      java.util.List<SessionFile> files =
          (java.util.List<SessionFile>) session.getAttribute("uploadedFiles");
      int fileCount = (files == null) ? 0 : files.size();
      logger.info("mandatesSubmit: session uploadedFiles count = {}", fileCount);

      // ---- Push any session-held files to TTS-Document-Management ----
      if (requestId != null && fileCount > 0) {
        String dmsUrl = (this.ttsDmsBaseUrl != null && !this.ttsDmsBaseUrl.isBlank())
            ? this.ttsDmsBaseUrl
            : "http://localhost:8084/api/v1/documents";

        org.springframework.web.client.RestTemplate rt =
            new org.springframework.web.client.RestTemplate();

        for (SessionFile sf : files) {
          try {
            java.util.Map<String, Object> meta = new java.util.LinkedHashMap<>();
            meta.put("name", sf.name);
            meta.put("type", sf.contentType);
            meta.put("tags", "MR");
            meta.put("refType", "MR_REQUEST");
            meta.put("refId", requestId);
            meta.put("creator", uname);

            org.springframework.http.client.MultipartBodyBuilder mbb =
                new org.springframework.http.client.MultipartBodyBuilder();
            mbb.part("file", new org.springframework.core.io.ByteArrayResource(sf.bytes) {
                  @Override
                  public String getFilename() {
                    return sf.name;
                  }
                })
                .contentType(org.springframework.http.MediaType.parseMediaType(sf.contentType));
            mbb.part("meta", meta, org.springframework.http.MediaType.APPLICATION_JSON);

            org.springframework.util.MultiValueMap<String, org.springframework.http.HttpEntity<?>>
                body = mbb.build();
            org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

            logger.info("Uploading to TTS DMS: name={}, type={}, size={}, refId={}",
                sf.name, sf.contentType, sf.size, requestId);

            org.springframework.http.ResponseEntity<String> upResp =
                rt.postForEntity(dmsUrl, new org.springframework.http.HttpEntity<>(body, headers),
                    String.class);

            logger.info("TTS DMS response: {} {}", upResp.getStatusCodeValue(),
                upResp.getStatusCode());
            logger.debug("TTS DMS body: {}", upResp.getBody());

            if (!upResp.getStatusCode().is2xxSuccessful()) {
              logger.warn("TTS upload failed for {} -> {}", sf.name, upResp.getStatusCode());
            }
          } catch (Exception upEx) {
            logger.warn("TTS upload error for {}", sf.name, upEx);
          }
        }

        // Clear after upload attempts
        session.removeAttribute("uploadedFiles");
      } else if (requestId == null) {
        logger.warn("No requestId returned; skipping TTS document upload.");
      }

      // ---- Success page ----
      return displayMandatesSuccess();

    } catch (Exception e) {
      logger.error("Mandates submit failed", e);
      return ResponseEntity.ok("""
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          id="" heading=" " template="error" version="1">
            <error xsi:type="systemError"><code>500</code>
            <message>Failed to save submission.</message></error>
          </page>
          """);
    }
  }

  @PostMapping(
      value = "/mandates/attachment/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> mandatesAttachmentUpload(HttpServletRequest request,
                                                         HttpSession session) {
    final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    int saved = 0;

    try {
      if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest mreq) {
        for (var e : mreq.getFileMap().entrySet()) {
          MultipartFile mf = e.getValue();
          if (mf != null && !mf.isEmpty()) {
            if (mf.getSize() > MAX_FILE_BYTES) {
              return ResponseEntity.ok("""
                    <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                          id="" heading=" " template="error" version="1">
                      <error xsi:type="validationError">
                        <name>file</name><code>0</code>
                        <message>File size exceeds 10MB.</message>
                      </error>
                    </page>
                  """);
            }
            String name = (mf.getOriginalFilename() == null || mf.getOriginalFilename().isBlank())
                ? e.getKey() : mf.getOriginalFilename();
            String ct = (mf.getContentType() == null || mf.getContentType().isBlank())
                ? "application/octet-stream" : mf.getContentType();

            getOrInitSessionFiles(session).add(
                new SessionFile(name, ct, mf.getSize(), mf.getBytes()));
            logger.info("UPLOAD: stored '{}' -> name={}, type={}, size={}", e.getKey(), name, ct,
                mf.getSize());
            saved++;

            //Exposes filename in DTO
            RequestDTO dto = (RequestDTO) session.getAttribute("requestData");
            if (dto == null) {
              dto = new RequestDTO();
            }
            if (dto.getResolutionDocs() == null) {
              dto.setResolutionDocs(new java.util.ArrayList<>());
            }
            dto.getResolutionDocs().add(name);
            dto.setEditable(true);
            session.setAttribute("requestData", dto);
          }
        }
      } else {
        logger.warn("UPLOAD: request is not multipart");
      }

      logger.info("UPLOAD: session uploadedFiles count now {}",
          getOrInitSessionFiles(session).size());

      //Return minimal success page
      return ResponseEntity.ok("""
            <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  id="" heading=" " template="blank" version="1">
              <message>OK</message>
            </page>
          """);
    } catch (Exception ex) {
      logger.error("UPLOAD: failed", ex);
      return ResponseEntity.ok("""
            <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  id="" heading=" " template="error" version="1">
              <error xsi:type="systemError"><code>500</code>
                <message>Upload failed.</message>
              </error>
            </page>
          """);
    }
  }

  @PostMapping(value = "/resolutionsFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayResolutionsFillPost(
      HttpServletRequest request,
      @RequestParam(required = false) Integer directorCount,
      @RequestParam(required = false) Integer removeDirectorAt,
      HttpSession session
  ) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getDirectors() == null) {
      dto.setDirectors(new java.util.ArrayList<>());
    }

    //Persist company fields coming as hidden inputs (for Back to SearchResults)
    String nm = request.getParameter("companyName");
    String ad = request.getParameter("companyAddress");
    String rn = request.getParameter("registrationNumber");
    if (nm != null && !nm.isBlank()) {
      dto.setCompanyName(nm.trim());
    }
    if (ad != null && !ad.isBlank()) {
      dto.setCompanyAddress(ad.trim());
    }
    if (rn != null && !rn.isBlank()) {
      dto.setRegistrationNumber(rn.trim());
    }

    //1) Merge posted directors by position (from ResolutionAutoFill)
    java.util.List<RequestDTO.Director> parsed =
        parseDirectorsFromRequest(request); // uses directorName_/Surname_/Designation_
    mergeDirectorsByPosition(dto, parsed);

    java.util.List<RequestDTO.Director> directors = dto.getDirectors();
    if (directors == null) {
      directors = new java.util.ArrayList<>();
    }
    if (directors.isEmpty()) {
      directors.add(createBlankDirector());
    }

    //2) Removal FIRST (don’t re-pad during the same request)
    boolean removing =
        (removeDirectorAt != null && removeDirectorAt >= 1 && removeDirectorAt <= directors.size());
    if (removing) {
      directors.remove(removeDirectorAt - 1);
      if (directors.isEmpty()) {
        directors.add(createBlankDirector());
      }
    } else if (directorCount != null && directorCount > directors.size()) {
      // 3) Only pad when NOT removing
      for (int i = directors.size(); i < directorCount; i++) {
        directors.add(createBlankDirector());
      }
    }

    dto.setDirectors(directors);
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);

    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("ResolutionAutoFill"), wrapper);
    return ResponseEntity.ok(page);
  }

  @GetMapping(value = "/resolutionsFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayResolutionsFillGet(
      @RequestParam(required = false) Integer directorCount,
      @RequestParam(required = false) Integer removeDirectorAt
  ) {
    return generateResolutionsFillPage(directorCount, removeDirectorAt);
  }

  @PostMapping(value = "/resolutionsSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayResolutionSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("ResolutionSuccessPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  // ======================= RESOLUTIONS SUBMIT =======================
  @PostMapping(value = "/resolutionSubmit", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayResolutionSubmit(HttpServletRequest request,
                                                        HttpSession session) {
    System.out.println("Submitting Resolution (FINAL)...");
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    try {
      String pdfSessionId = (String) session.getAttribute("pdfSessionId");
      RequestDTO uiData =
          (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
      if (uiData.getDirectors() == null) {
        uiData.setDirectors(new java.util.ArrayList<>());
      }

      // Merge last edits (includes instruction)
      java.util.List<RequestDTO.Director> parsed = parseDirectorsFromRequest(request);
      mergeDirectorsByPosition(uiData, parsed);

      // ---- Validate ----
      ResolutionsAutoFillErrorModel errors = new ResolutionsAutoFillErrorModel();
      boolean hasErrors = false;
      if (uiData.getDirectors().isEmpty()) {
        uiData.getDirectors().add(createBlankDirector());
        errors.setDirectorName("Director name is required");
        errors.setDirectorSurname("Director surname is required");
        errors.setDirectorDesignation("Director designation is required");
        errors.setDirectorInstruction("Instruction is required");
        hasErrors = true;
      } else {
        for (RequestDTO.Director d : uiData.getDirectors()) {
          if (nz.apply(d.getName()).isBlank()) {
            errors.setDirectorName("Director name is required");
            hasErrors = true;
          }
          if (nz.apply(d.getSurname()).isBlank()) {
            errors.setDirectorSurname("Director surname is required");
            hasErrors = true;
          }
          if (nz.apply(d.getDesignation()).isBlank()) {
            errors.setDirectorDesignation("Director designation is required");
            hasErrors = true;
          }
          if (nz.apply(d.getInstruction()).isBlank()) {
            errors.setDirectorInstruction("Instruction is required");
            hasErrors = true;
          }
        }
      }

      uiData.setPdfSessionId(pdfSessionId);
      uiData.setEditable(true);
      if (pdfSessionId != null) {
        pdfExtractionDataCache.put(pdfSessionId, uiData);
      }
      session.setAttribute("requestData", uiData);

      if (hasErrors) {
        RequestWrapper wrapper = new RequestWrapper();
        wrapper.setRequest(uiData);
        wrapper.setResolutionsAutoFillErrorModel(errors);
        String page = xsltProcessor.generatePage(xslPagePath("ResolutionAutoFill"), wrapper);
        return ResponseEntity.ok(page);
      }

      // -- stamp logged-in user --
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO current =
          (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
              "currentUser");

      String uname = currentDisplayId(session, request);
      if (uname.isBlank()) {
        uname = "UI_USER";
      }

      uiData.setLoggedInUsername(uname);
      uiData.setLoggedInEmail(current != null ? nz.apply(current.getEmail()) : "");

      // ---- Submit to backend ----
      SubmissionPayload payload = buildSubmissionPayload(uiData, "Resolutions");
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO result =
          postSnapshotToBackend(payload);

      if (result != null && result.getRequest() != null) {
        var r = result.getRequest();
        logger.info("Resolutions submission OK: requestId={}, processId={}, assignedUser={}",
            r.getRequestId(), r.getProcessId(), r.getAssignedUser());
      }

      return displayResolutionSuccess();

    } catch (Exception e) {
      logger.error("Resolution submit failed", e);
      return ResponseEntity.ok("""
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          id="" heading=" " template="error" version="1">
            <error xsi:type="systemError"><code>500</code>
            <message>Failed to save resolution submission.</message></error>
          </page>
          """);
    }
  }

  // =======================  Mandates & Resolutions  =======================

  //ACCOUNTS
  @PostMapping(value = "/mandatesResolutionsAccDetails", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrAccDetailsPost(
      HttpServletRequest request,
      @RequestParam(defaultValue = "1") int accountCount,
      @RequestParam(required = false) String removeSignatoryAt, // "i_j"
      @RequestParam(required = false) Integer addSignatoryAt,   // i
      @RequestParam(required = false) Integer removeAccountAt,  // i
      HttpSession session
  ) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }

    //1) Parse + merge current form edits
    var parsed = parseAccountsFromRequest(request);
    mergeAccounts(dto, parsed);

    //2) Apply UI actions
    var accounts = dto.getAccounts();
    if (addSignatoryAt != null && addSignatoryAt >= 1 && addSignatoryAt <= accounts.size()) {
      accounts.get(addSignatoryAt - 1).getSignatories().add(createBlankSignatory());
    }
    if (removeSignatoryAt != null && !removeSignatoryAt.isBlank()) {
      try {
        String[] p = removeSignatoryAt.split("_");
        int ai = Integer.parseInt(p[0]) - 1;
        int si = Integer.parseInt(p[1]) - 1;
        if (ai >= 0 && ai < accounts.size()) {
          var sigs = accounts.get(ai).getSignatories();
          if (sigs != null && si >= 0 && si < sigs.size()) {
            sigs.remove(si);
          }
          if (sigs == null || sigs.isEmpty()) {
            accounts.get(ai).setSignatories(
                new java.util.ArrayList<>(java.util.List.of(createBlankSignatory())));
          }
        }
      } catch (Exception ignored) {
        // intentionally empty
      }
    }
    if (removeAccountAt != null && removeAccountAt >= 1 && removeAccountAt <= accounts.size()) {
      accounts.remove(removeAccountAt - 1);
    }

    //Pad to accountCount (only expand)
    while (accounts.size() < Math.max(1, accountCount)) {
      accounts.add(createBlankAccount());
    }

    dto.setAccounts(accounts);
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("MandatesResolutionsAccDetails"), wrapper);
    return ResponseEntity.ok(page);
  }

  @GetMapping(value = "/mandatesResolutionsAccDetails", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrAccDetailsGet(HttpSession session) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getAccounts() == null || dto.getAccounts().isEmpty()) {
      dto.setAccounts(new java.util.ArrayList<>(java.util.List.of(createBlankAccount())));
    }
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("MandatesResolutionsAccDetails"), wrapper);
    return ResponseEntity.ok(page);
  }

  //SIGNATURE CARD (VALIDATES ACCOUNT + APPOINTED SIGNATORIES like MandatesAutoFill)
  @PostMapping(value = "/mandatesResolutionsSignatureCard", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrSigCardPost(HttpServletRequest request, HttpSession session) {
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }

    // Merge current edits from Account Details
    var parsed = parseAccountsFromRequest(request);
    mergeAccounts(dto, parsed);

    // ---- Validate accounts + appointed signatories (REQUIRED on this page) ----
    boolean hasErrors = false;
    MandatesAutoFillErrorModel accErrors = new MandatesAutoFillErrorModel();

    if (dto.getAccounts().isEmpty()) {
      dto.getAccounts().add(createBlankAccount());
      accErrors.setAccountName("Account name is required");
      accErrors.setAccountNo("Account number is required");
      // signatory messages too (to light up the table)
      accErrors.setSignatoryFullName("Full name is required");
      accErrors.setSignatoryIdNumber("ID number is required");
      accErrors.setSignatoryInstruction("Instruction is required");
      hasErrors = true;
    } else {
      for (RequestDTO.Account a : dto.getAccounts()) {
        if (nz.apply(a.getAccountName()).isBlank()) {
          accErrors.setAccountName("Account name is required");
          hasErrors = true;
        }
        if (nz.apply(a.getAccountNo()).isBlank()) {
          accErrors.setAccountNo("Account number is required");
          hasErrors = true;
        }

        var sigs = a.getSignatories();
        if (sigs == null || sigs.isEmpty()) {
          accErrors.setSignatoryFullName("Full name is required");
          accErrors.setSignatoryIdNumber("ID number is required");
          accErrors.setSignatoryInstruction("Instruction is required");
          hasErrors = true;
        } else {
          for (RequestDTO.Signatory s : sigs) {
            if (nz.apply(s.getFullName()).isBlank()) {
              accErrors.setSignatoryFullName("Full name is required");
              hasErrors = true;
            }
            if (nz.apply(s.getIdNumber()).isBlank()) {
              accErrors.setSignatoryIdNumber("ID number is required");
              hasErrors = true;
            }
            if (nz.apply(s.getInstruction()).isBlank()) {
              accErrors.setSignatoryInstruction("Instruction is required");
              hasErrors = true;
            }
          }
        }
      }
    }

    // Persist + reflect state
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    if (hasErrors) {
      // Stay on Accounts page with inline errors
      RequestWrapper wrapper = new RequestWrapper();
      wrapper.setRequest(dto);
      wrapper.setMandatesAutoFillErrorModel(accErrors);

      String page =
          xsltProcessor.generatePage(xslPagePath("MandatesResolutionsAccDetails"), wrapper);
      return ResponseEntity.ok(page);
    }

    // No errors then proceed to Signature Card confirmation page
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page =
        xsltProcessor.generatePage(xslPagePath("MandatesResolutionsSignatureCard"), wrapper);
    return ResponseEntity.ok(page);
  }

  @GetMapping(value = "/mandatesResolutionsSignatureCard", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrSigCardGet(HttpSession session) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getAccounts() == null || dto.getAccounts().isEmpty()) {
      dto.setAccounts(new java.util.ArrayList<>(java.util.List.of(createBlankAccount())));
    }
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page =
        xsltProcessor.generatePage(xslPagePath("MandatesResolutionsSignatureCard"), wrapper);
    return ResponseEntity.ok(page);
  }

  // DIRECTORS
  @PostMapping(value = "/mandatesResolutionsDirectorsDetails", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrDirectorsPost(
      HttpServletRequest request,
      @RequestParam(required = false) Integer directorCount,
      @RequestParam(required = false) Integer removeDirectorAt,
      HttpSession session
  ) {
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();

    // Ensure lists exist
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }
    if (dto.getDirectors() == null) {
      dto.setDirectors(new java.util.ArrayList<>());
    }

    // Persist latest signature-card edits (accounts/signatories)
    java.util.List<RequestDTO.Account> parsedAcc = parseAccountsFromRequest(request);
    mergeAccounts(dto, parsedAcc);

    //Makes sure the structures exist so the next page can render safely.
    if (dto.getAccounts().isEmpty()) {
      dto.getAccounts().add(createBlankAccount());
    }
    for (RequestDTO.Account acc : dto.getAccounts()) {
      if (acc.getSignatories() == null) {
        acc.setSignatories(new java.util.ArrayList<>());
      }
      if (acc.getSignatories().isEmpty()) {
        acc.getSignatories().add(createBlankSignatory());
      }
    }

    // Existing directors logic
    var parsedDirs = parseDirectorsFromRequest(request);
    mergeDirectorsByPosition(dto, parsedDirs);

    var directors = dto.getDirectors();
    if (directors.isEmpty()) {
      directors.add(createBlankDirector());
    }

    if (removeDirectorAt != null && removeDirectorAt >= 1 && removeDirectorAt <= directors.size()) {
      directors.remove(removeDirectorAt - 1);
      if (directors.isEmpty()) {
        directors.add(createBlankDirector());
      }
    } else if (directorCount != null && directorCount > directors.size()) {
      for (int i = directors.size(); i < directorCount; i++) {
        directors.add(createBlankDirector());
      }
    }
    dto.setDirectors(directors);

    // Persist + render next page
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page =
        xsltProcessor.generatePage(xslPagePath("MandatesResolutionsDirectorsDetails"), wrapper);
    return ResponseEntity.ok(page);
  }

  @GetMapping(value = "/mandatesResolutionsDirectorsDetails", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrDirectorsGet(HttpSession session) {
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    RequestDTO dto =
        (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
    if (dto.getDirectors() == null || dto.getDirectors().isEmpty()) {
      dto.setDirectors(new java.util.ArrayList<>(java.util.List.of(createBlankDirector())));
    }
    dto.setPdfSessionId(pdfSessionId);
    dto.setEditable(true);
    if (pdfSessionId != null) {
      pdfExtractionDataCache.put(pdfSessionId, dto);
    }
    session.setAttribute("requestData", dto);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page =
        xsltProcessor.generatePage(xslPagePath("MandatesResolutionsDirectorsDetails"), wrapper);
    return ResponseEntity.ok(page);
  }

  //SUCCESS
  @PostMapping(value = "/mandatesResolutionsSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesResolutionsSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("MandatesResolutionsSuccess"));
    return ResponseEntity.ok(page);
  }

  // ======================= MANDATES & RESOLUTIONS SUBMIT =======================
  @PostMapping(value = "/mandatesResolutionsSubmit", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> mrSubmit(HttpServletRequest request, HttpSession session) {
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    try {
      String pdfSessionId = (String) session.getAttribute("pdfSessionId");
      RequestDTO uiData =
          (pdfSessionId != null) ? pdfExtractionDataCache.get(pdfSessionId) : new RequestDTO();
      if (uiData.getAccounts() == null) {
        uiData.setAccounts(new java.util.ArrayList<>());
      }
      if (uiData.getDirectors() == null) {
        uiData.setDirectors(new java.util.ArrayList<>());
      }

      // Last-moment merges
      mergeAccounts(uiData, parseAccountsFromRequest(request));
      mergeDirectorsByPosition(uiData, parseDirectorsFromRequest(request));

      // ---- Validate Directors (required on this page) ----
      boolean dirErrors = false;
      ResolutionsAutoFillErrorModel dirErr = new ResolutionsAutoFillErrorModel();
      if (uiData.getDirectors().isEmpty()) {
        uiData.getDirectors().add(createBlankDirector());
        dirErr.setDirectorName("Director name is required");
        dirErr.setDirectorSurname("Director surname is required");
        dirErr.setDirectorDesignation("Director designation is required");
        dirErrors = true;
      } else {
        for (RequestDTO.Director d : uiData.getDirectors()) {
          if (nz.apply(d.getName()).isBlank()) {
            dirErr.setDirectorName("Director name is required");
            dirErrors = true;
          }
          if (nz.apply(d.getSurname()).isBlank()) {
            dirErr.setDirectorSurname("Director surname is required");
            dirErrors = true;
          }
          if (nz.apply(d.getDesignation()).isBlank()) {
            dirErr.setDirectorDesignation("Director designation is required");
            dirErrors = true;
          }
        }
      }

      uiData.setPdfSessionId(pdfSessionId);
      uiData.setEditable(true);
      if (pdfSessionId != null) {
        pdfExtractionDataCache.put(pdfSessionId, uiData);
      }
      session.setAttribute("requestData", uiData);

      if (dirErrors) {
        RequestWrapper wrapErr = new RequestWrapper();
        wrapErr.setRequest(uiData);
        wrapErr.setResolutionsAutoFillErrorModel(dirErr);
        String page =
            xsltProcessor.generatePage(xslPagePath("MandatesResolutionsDirectorsDetails"), wrapErr);
        return ResponseEntity.ok(page);
      }

      // -- stamp logged-in user --
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO current =
          (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
              "currentUser");

      String uname = currentDisplayId(session, request);
      if (uname.isBlank()) {
        uname = "UI_USER";
      } //Extra fallback

      uiData.setLoggedInUsername(uname);
      uiData.setLoggedInEmail(current != null ? nz(current.getEmail()) : "");

      //Submit (backend handles workflow and returns processId/assignedUser)
      SubmissionPayload payload = buildSubmissionPayload(uiData, "Both");
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO result =
          postSnapshotToBackend(payload);

      System.out.println("Mandates & Resolutions submission OK. RequestId: "
                         + (result != null && result.getRequest() != null ? result.getRequest()
          .getRequestId() :
          "n/a"));

      return displayMandatesResolutionsSuccess();

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.ok("""
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          id="" heading=" " template="error" version="1">
            <error xsi:type="systemError"><code>500</code>
            <message>Failed to save Mandates & Resolutions submission.</message></error>
          </page>
          """);
    }
  }

// ===================== VIEW REQUEST + APPROVE/REJECT COMMENT FLOW =====================

  @PostMapping(value = "/viewRequestSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequestSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("ViewRequestSuccessPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/viewRequestSuccessRejectPage", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayviewRequestSuccessRejectPage() {
    String page = xsltProcessor.returnPage(xmlPagePath("ViewRequestSuccessRejectPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  //Open Reject panel (loads subStatus so XSL can label correctly)
  @PostMapping(value = "/viewRequestReject", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequestRejectPage(
      @RequestParam("requestId") Long requestId) {
    RequestWrapper wrapper = new RequestWrapper();
    RequestDTO dto = new RequestDTO();
    dto.setRequestId(requestId);

    String sub = null;
    try {
      RestTemplate rt = new RestTemplate();
      var resp = rt.getForEntity(mandatesResolutionsDaoURL + "/api/request/{id}", RequestDTO.class,
          requestId);
      if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
        sub = resp.getBody().getSubStatus();
        dto.setSubStatus(sub);
      }
    } catch (Exception ignore) {
      // intentionally empty
    }

    wrapper.setRequest(dto);

    //Load instructions for this subStatus
    populateInstructions(wrapper, sub, mandatesResolutionsDaoURL);

    String page = xsltProcessor.generatePage(xslPagePath("ViewRequestRejectPage"), wrapper);
    return ResponseEntity.ok(page);
  }

  // ===== Open Approve panel (loads subStatus so XSL can label correctly)
  @PostMapping(value = "/viewRequestApprovePage", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequestApprovePage(
      @RequestParam("requestId") Long requestId) {
    RequestWrapper wrapper = new RequestWrapper();
    RequestDTO dto = new RequestDTO();
    dto.setRequestId(requestId);

    String sub = null;
    try {
      RestTemplate rt = new RestTemplate();
      var resp = rt.getForEntity(mandatesResolutionsDaoURL + "/api/request/{id}", RequestDTO.class,
          requestId);
      if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
        sub = resp.getBody().getSubStatus();
        dto.setSubStatus(sub);
      }
    } catch (Exception ignore) {
      // intentionally empty
    }

    wrapper.setRequest(dto);

    //Load instructions for this subStatus
    populateInstructions(wrapper, sub, mandatesResolutionsDaoURL);

    String page = xsltProcessor.generatePage(xslPagePath("ViewRequestApprovePage"), wrapper);
    return ResponseEntity.ok(page);
  }

  /**
   * Submit a REJECT (comment required) to Completed + Rejected; backend can terminate Camunda
   * via outcome=Reject.
   */
  @PostMapping(value = "/comment/reject", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitRejectComment(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "subStatus", required = false) String subStatusFromForm,
      @RequestParam("commentbox") String commentText,
      @RequestParam(value = "confirmationCheckMandate", required = false) String confirm,
      HttpServletRequest servletRequest
  ) {
    // ---------- Happy path (no checkbox validation) ----------
    try {
      RestTemplate rt = new RestTemplate();

      // 1) Save REJECT comment (internal)
      String creator = (servletRequest.getUserPrincipal() != null)
          ? servletRequest.getUserPrincipal().getName()
          : "ui";

      var payload = new java.util.HashMap<String, Object>();
      payload.put("requestId", requestId);
      payload.put("commentText", (commentText == null ? "" : commentText.trim()));
      payload.put("isInternal", Boolean.TRUE);
      payload.put("creator", creator);
      rt.postForEntity(
          mandatesResolutionsDaoURL + "/api/comment",
          payload,
          Object.class
      );

      // 2) PUT: Completed + Rejected + processOutcome=Reject (triggers DAO -> reject-path)
      var update = new java.util.HashMap<String, Object>();
      update.put("status", "Completed");      // exact
      update.put("subStatus", SS_REJECTED);   // "Rejected"
      update.put("processOutcome", "Reject"); // IMPORTANT

      var h = new org.springframework.http.HttpHeaders();
      h.setContentType(MediaType.APPLICATION_JSON);
      var entity = new org.springframework.http.HttpEntity<>(update, h);
      rt.exchange(
          mandatesResolutionsDaoURL + "/api/request/{id}",
          HttpMethod.PUT,
          entity,
          Void.class,
          requestId
      );

      //Sanity log
      try {
        var check =
            rt.getForEntity(
                mandatesResolutionsDaoURL + "/api/request/{id}",
                RequestDTO.class,
                requestId
            );
        if (check.getStatusCode().is2xxSuccessful() && check.getBody() != null) {
          logger.info("After REJECT PUT -> status={}, subStatus={}, processId={}",
              check.getBody().getStatus(),
              check.getBody().getSubStatus(),
              check.getBody().getProcessId());
        }
      } catch (Exception ignore) {
        // intentionally empty
      }

      // Success page (XSL + wrapper with requestId)
      HttpSession session = servletRequest.getSession(false);
      RequestWrapper w = new RequestWrapper();
      RequestDTO d = new RequestDTO();
      d.setRequestId(requestId);
      w.setRequest(d);

      String page = isAdmin(session)
          ? xsltProcessor.generatePage(xslPagePath("ViewRequestSuccessRejectPageAdmin"), w)
          : xsltProcessor.generatePage(xslPagePath("ViewRequestSuccessRejectPage"), w);
      return ResponseEntity.ok(page);

    } catch (Exception ex) {
      logger.error("Failed to reject/update request: {}", ex.getMessage(), ex);
      return ResponseEntity.ok("""
            <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <error>Could not save rejection.</error>
            </page>
          """);
    }
  }

  /**
   * Submit an APPROVE (comment optional) -> advance subStatus + success page.
   */
  @PostMapping(value = "/comment/approve", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> submitApproveComment(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "subStatus", required = false) String subStatusFromForm,
      @RequestParam(value = "commentbox", required = false) String commentText,
      @RequestParam(value = "confirmationCheckMandate", required = false) String confirm,
      HttpServletRequest servletRequest
  ) {
    // ---------- Happy path (no checkbox validation) ----------
    try {
      RestTemplate rt = new RestTemplate();

      // 1) Optional APPROVE comment (external)
      if (commentText != null && !commentText.trim().isEmpty()) {
        var payload = new java.util.HashMap<String, Object>();
        payload.put("requestId", requestId);
        payload.put("commentText", commentText.trim());
        payload.put("isInternal", Boolean.FALSE);
        String creator = (servletRequest.getUserPrincipal() != null)
            ? servletRequest.getUserPrincipal().getName() : "ui";
        payload.put("creator", creator);
        rt.postForEntity(
            mandatesResolutionsDaoURL + "/api/comment",
            payload,
            Object.class
        );
      }

      // 2) Current subStatus (fresh)
      String currentSub = subStatusFromForm;
      try {
        var resp =
            rt.getForEntity(
                mandatesResolutionsDaoURL + "/api/request/{id}",
                RequestDTO.class,
                requestId
            );
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null
            && resp.getBody().getSubStatus() != null) {
          currentSub = resp.getBody().getSubStatus();
        }
      } catch (Exception ignore) {
        // intentionally empty
      }

      // Already final? Just show success (XSL + wrapper with requestId)
      if (canonical(currentSub).equalsIgnoreCase(SS_DONE)) {
        HttpSession session = servletRequest.getSession(false);
        RequestWrapper w = new RequestWrapper();
        RequestDTO d = new RequestDTO();
        d.setRequestId(requestId);
        w.setRequest(d);

        String page = isAdmin(session)
            ? xsltProcessor.generatePage(xslPagePath("ViewRequestSuccessPageAdmin"), w)
            : xsltProcessor.generatePage(xslPagePath("ViewRequestSuccessPage"), w);
        return ResponseEntity.ok(page);
      }

      String next = nextSubStatus(currentSub, true);
      String newStatus = next.equals(SS_DONE) ? "Completed" : "In Progress";

      // 3) Update request (+ outcome=Approve to advance Camunda)
      var update = new java.util.HashMap<String, Object>();
      update.put("status", newStatus);
      update.put("subStatus", next);
      update.put("processOutcome", "Approve");   // DAO field name

      var h = new org.springframework.http.HttpHeaders();
      h.setContentType(MediaType.APPLICATION_JSON);
      var entity = new org.springframework.http.HttpEntity<>(update, h);
      rt.exchange(
          mandatesResolutionsDaoURL + "/api/request/{id}",
          HttpMethod.PUT,
          entity,
          Void.class,
          requestId
      );

      //Success page (XSL + wrapper with requestId)
      HttpSession session = servletRequest.getSession(false);
      RequestWrapper w = new RequestWrapper();
      RequestDTO d = new RequestDTO();
      d.setRequestId(requestId);
      w.setRequest(d);

      String page = isAdmin(session)
          ? xsltProcessor.generatePage(xslPagePath("ViewRequestSuccessPageAdmin"), w)
          : xsltProcessor.generatePage(xslPagePath("ViewRequestSuccessPage"), w);
      return ResponseEntity.ok(page);

    } catch (Exception ex) {
      logger.error("Failed to approve/update request: {}", ex.getMessage(), ex);
      return ResponseEntity.ok("""
            <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <error>Could not complete approval.</error>
            </page>
          """);
    }
  }

  /**
   * Main View Request page (Also fetches newest comments for approve/reject).
   */
  @PostMapping(value = "/viewRequest/{requestId}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequest(
      @PathVariable Long requestId,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    try {
      RestTemplate rt = new RestTemplate();

      // Resolve display name
      za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO user =
          (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
              "currentUser");

      String displayName = currentDisplayId(session, servletRequest);

      // 1) Load submission (company, request, accounts + nested signatories)
      String submissionUrl = mandatesResolutionsDaoURL + "/api/submission/" + requestId;
      ResponseEntity<za.co.rmb.tts.mandates.resolutions.ui
          .model.dto.MandateResolutionSubmissionResultDTO>
          subResp =
          rt.getForEntity(
              submissionUrl,
              za.co.rmb.tts.mandates.resolutions.ui
                  .model.dto.MandateResolutionSubmissionResultDTO.class
          );

      if (!subResp.getStatusCode().is2xxSuccessful() || subResp.getBody() == null) {
        throw new RuntimeException("Failed to fetch submission " + requestId);
      }
      var sub = subResp.getBody();

      logger.info("viewRequest {} -> accounts={}, authorities={}, hasRequest={}",
          requestId,
          (sub.getAccounts() == null ? -1 : sub.getAccounts().size()),
          (sub.getAuthorities() == null ? -1 : sub.getAuthorities().size()),
          (sub.getRequest() != null));

      // 2) Fetch comments (DAO) — newestFirst=true
      String commentsUrl = mandatesResolutionsDaoURL
                           + "/api/comment/request/" + requestId + "?newestFirst=true";
      ResponseEntity<java.util.List<java.util.Map<String, Object>>> commentsResp = rt.exchange(
          commentsUrl,
          HttpMethod.GET,
          null,
          new org.springframework.core.ParameterizedTypeReference
              <java.util.List<java.util.Map<String, Object>>>() {
          }
      );

      //Format helper
      java.time.format.DateTimeFormatter viewFmt =
          java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

      java.util.List<za.co.rmb.tts.mandates.resolutions.ui.model.dto.ViewCommentDTO> approvedRows =
          new java.util.ArrayList<>();
      java.util.List<za.co.rmb.tts.mandates.resolutions.ui.model.dto.ViewCommentDTO> rejectedRows =
          new java.util.ArrayList<>();

      if (commentsResp.getStatusCode().is2xxSuccessful() && commentsResp.getBody() != null) {
        for (var c : commentsResp.getBody()) {
          String text =
              (c.get("commentText") == null) ? "" : String.valueOf(c.get("commentText")).trim();
          if (text.isEmpty()) {
            continue;
          }

          Object isInt = c.getOrDefault("isInternal", c.getOrDefault("isinternal", null));
          boolean reject = (isInt instanceof Boolean)
              ? ((Boolean) isInt)
              : "true".equalsIgnoreCase(String.valueOf(isInt));

          String creator =
              (c.get("creator") == null) ? "" : String.valueOf(c.get("creator")).trim();
          if (creator.isBlank() || "ui".equalsIgnoreCase(creator)) {
            creator = displayName;
          }

          String createdStr = "";
          Object created = c.get("created");
          if (created != null) {
            String raw = String.valueOf(created).trim();
            try {
              if (isAllDigits(raw)) {
                long val = Long.parseLong(raw);
                createdStr = formatEpochMillis(val, viewFmt);
              } else {
                createdStr = tryFormatIso(raw, viewFmt);
              }
            } catch (Exception ignore) {
              createdStr = raw;
            }
          }

          var row = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.ViewCommentDTO();
          row.setCreator(creator);
          row.setCreated(createdStr);
          row.setText(text);

          if (reject) {
            rejectedRows.add(row);
          } else {
            approvedRows.add(row);
          }
        }
      }

      //Newest approve/reject strings
      String newestReject = null;
      String newestApprove = null;
      if (commentsResp.getStatusCode().is2xxSuccessful() && commentsResp.getBody() != null) {
        for (var c : commentsResp.getBody()) {
          Object text = c.get("commentText");
          Object isInt = c.getOrDefault("isInternal", c.getOrDefault("isinternal", null));
          String t = (text == null) ? "" : text.toString().trim();
          boolean reject = (isInt instanceof Boolean) ? ((Boolean) isInt) :
              "true".equalsIgnoreCase(String.valueOf(isInt));

          if (reject && newestReject == null && !t.isEmpty()) {
            newestReject = t;
          }
          if (!reject && newestApprove == null && !t.isEmpty()) {
            newestApprove = t;
          }
          if (newestReject != null && newestApprove != null) {
            break;
          }
        }
      }

      logger.info("viewRequest {} -> comments: approved={}, rejected={}", requestId,
          approvedRows.size(), rejectedRows.size());

      // 3) Helpers
      java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();
      java.util.function.Function<String, String> keyN =
          s -> s == null ? "" : s.trim().toUpperCase();
      java.util.function.BiFunction<String, String, String> accKey = (num, name) -> {
        String n = nz.apply(num);
        if (!n.isEmpty()) {
          return "NUM#" + n;
        }
        return "NAME#" + keyN.apply(name);
      };

      // 4) Seed accounts map
      java.util.Map<String, AccountDTO> accountsByKey = new java.util.LinkedHashMap<>();
      if (sub.getAccounts() != null) {
        for (var a : sub.getAccounts()) {
          String k = accKey.apply(a.getAccountNumber(), a.getAccountName());
          AccountDTO bucket = accountsByKey.computeIfAbsent(k, kk -> {
            AccountDTO ad = new AccountDTO();
            ad.setAccountName(nz.apply(a.getAccountName()));
            ad.setAccountNumber(nz.apply(a.getAccountNumber()));
            ad.setSignatories(new java.util.ArrayList<>());
            return ad;
          });
          var apiSigs = a.getSignatories();
          if (apiSigs != null) {
            for (var s : apiSigs) {
              SignatoryDTO sd = new SignatoryDTO();
              sd.setFullName(nz.apply(s.getFullName()));
              sd.setIdNumber(nz.apply(s.getIdNumber()));
              sd.setInstructions(nz.apply(s.getInstructions()));
              sd.setCapacity(nz.apply(s.getCapacity()));
              sd.setGroupCategory(nz.apply(s.getGroupCategory()));
              sd.setAccountName(bucket.getAccountName());
              sd.setAccountNumber(bucket.getAccountNumber());
              bucket.getSignatories().add(sd);
            }
          }
        }
      }

      // 5) Build wrapper for XSL
      RequestTableDTO view = new RequestTableDTO();
      if (sub.getRequest() != null) {
        view.setRequestId(sub.getRequest().getRequestId());
        view.setCompanyId(sub.getRequest().getCompanyId());
        view.setSla(sub.getRequest().getSla());
        view.setType(sub.getRequest().getType());
        view.setStatus(sub.getRequest().getStatus());
        view.setSubStatus(sub.getRequest().getSubStatus());
        view.setCreated(
            sub.getRequest().getCreated() != null ? sub.getRequest().getCreated().toString() :
                null);
        view.setUpdated(
            sub.getRequest().getUpdated() != null ? sub.getRequest().getUpdated().toString() :
                null);
        view.setProcessId(sub.getRequest().getProcessId());
        view.setAssignedUser(sub.getRequest().getAssignedUser());
        view.setRequestIdForDisplay(sub.getRequest().getRequestIdForDisplay());

        //Creator/Updator coming from the API payload (Display when viewing a request)
        String creator = nz.apply(sub.getRequest().getCreator());
        if (creator.isEmpty()) {
          creator = displayName; // fallback
        }
        view.setCreator(creator);

        String updator = nz.apply(sub.getRequest().getUpdator());
        if (updator.isEmpty()) {
          updator = displayName; // fallback
        }
        view.setUpdator(updator);

        // ---- Directors (set instruction everywhere) ----
        try {
          var dirs =
              new java.util.ArrayList<za.co.rmb.tts.mandates.resolutions
                  .ui.model.dto.DirectorDTO>();

          // 1) Prefer authorities returned on the submission
          var authsSub = sub.getAuthorities();
          boolean submissionHasInstr = false;

          if (authsSub != null && !authsSub.isEmpty()) {
            for (var a : authsSub) {
              boolean hasInstr = java.util.Arrays.stream(a.getClass().getMethods())
                  .anyMatch(m -> m.getName().equals("getInstructions"));
              if (hasInstr) {
                submissionHasInstr = true;
              }

              logger.info("AUTH from submission: class={}, hasGetInstructions={}",
                  a.getClass().getName(), hasInstr);

              //Map firstname/surname/designation
              String first = null;
              String last = null;
              String role = null;
              try {
                first = (String) a.getClass().getMethod("getFirstname").invoke(a);
              } catch (Exception ignore) {
                // intentionally empty
              }
              try {
                last = (String) a.getClass().getMethod("getSurname").invoke(a);
              } catch (Exception ignore) {
                // intentionally empty
              }
              try {
                role = (String) a.getClass().getMethod("getDesignation").invoke(a);
              } catch (Exception ignore) {
                // intentionally empty
              }

              // if the method doesn't exist, extractInstruction() will default to Add.
              String instEff = extractInstruction(a);

              var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
              dd.setName(nz.apply(first));
              dd.setSurname(nz.apply(last));
              dd.setDesignation(nz.apply(role));
              dd.setInstruction(instEff);
              if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                  .isEmpty())) {
                dirs.add(dd);
              }
            }
          }

//  If submission had NO instructions at all, replace with the DAO authorities
          if (!submissionHasInstr && sub.getRequest() != null
              && sub.getRequest().getCompanyId() != null) {
            try {
              Long companyId = sub.getRequest().getCompanyId();
              String url = mandatesResolutionsDaoURL + "/api/authority/company/" + companyId;
              var resp = rt.exchange(
                  url, HttpMethod.GET, null,
                  new org.springframework.core.ParameterizedTypeReference<
                      java.util.List<za.co.rmb.tts.mandates.resolutions.ui
                          .model.dto.AuthorityDTO>>() {
                  });

              var authsApi = (resp.getStatusCode().is2xxSuccessful()) ? resp.getBody() : null;
              if (authsApi != null && !authsApi.isEmpty()) {
                dirs.clear();
                for (var a : authsApi) {
                  var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
                  dd.setName(nz.apply(a.getFirstname()));
                  dd.setSurname(nz.apply(a.getSurname()));
                  dd.setDesignation(nz.apply(a.getDesignation()));
                  //  AuthorityDTO has instructions — use it directly
                  String instr = nz.apply(a.getInstructions());
                  if (instr.isEmpty()) {
                    //Last resort inference from isActive
                    Boolean active = a.getIsActive();
                    instr = (Boolean.FALSE.equals(active)) ? "Remove" : "Add";
                  }
                  dd.setInstruction(instr);
                  if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                      .isEmpty())) {
                    dirs.add(dd);
                  }
                }
              }
            } catch (Exception e) {
              logger.warn("Authority lookup fallback failed: {}", e.toString());
            }
          }


          // 2) Fallback: fetch by companyId -> List<AuthorityDTO>
          if (dirs.isEmpty() && sub.getRequest() != null
              && sub.getRequest().getCompanyId() != null) {
            Long companyId = sub.getRequest().getCompanyId();
            try {
              String url = mandatesResolutionsDaoURL + "/api/authority/company/" + companyId;
              var resp = rt.exchange(
                  url,
                  HttpMethod.GET,
                  null,
                  new org.springframework.core.ParameterizedTypeReference<
                      java.util.List<za.co.rmb.tts.mandates.resolutions.ui.model.dto.AuthorityDTO>
                      >() {
                  }
              );
              var authsApi = (resp.getStatusCode().is2xxSuccessful()) ? resp.getBody() : null;
              if (authsApi != null) {
                for (var a : authsApi) {
                  if (a == null) {
                    continue;
                  }

                  Boolean active = null;
                  try {
                    active = (Boolean) a.getClass().getMethod("getIsActive").invoke(a);
                  } catch (Exception ignore) {
                    // intentionally empty
                  }

                  String instr = null;
                  try {
                    instr = (String) a.getClass().getMethod("getInstructions").invoke(a);
                  } catch (Exception ignore) {
                    // intentionally empty
                  }

                  String instEff = (instr != null && !instr.isBlank())
                      ? instr.trim()
                      : (Boolean.FALSE.equals(active) ? "Remove" : "Add");

                  var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
                  dd.setName(nz.apply(a.getFirstname()));
                  dd.setSurname(nz.apply(a.getSurname()));
                  dd.setDesignation(nz.apply(a.getDesignation()));
                  dd.setInstruction(instEff);

                  if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                      .isEmpty())) {
                    dirs.add(dd);
                  }
                }
              }
            } catch (Exception e) {
              logger.warn("Authority lookup by companyId {} failed: {}", companyId, e.toString());
            }
          }

          // 3) Still nothing? Fall back to request.getDirectors()
          if (dirs.isEmpty() && sub.getRequest() != null) {
            try {
              var sourceDirs =
                  (java.util.List<?>) sub.getRequest().getClass().getMethod("getDirectors")
                      .invoke(sub.getRequest());
              if (sourceDirs != null) {
                for (Object d : sourceDirs) {
                  String name = null;
                  String surname = null;
                  String designation = null;
                  String instruction = null;
                  try {
                    name = (String) d.getClass().getMethod("getName").invoke(d);
                  } catch (Exception ignore) {
                    // intentionally empty
                  }
                  try {
                    surname = (String) d.getClass().getMethod("getSurname").invoke(d);
                  } catch (Exception ignore) {
                    // intentionally empty
                  }
                  try {
                    designation = (String) d.getClass().getMethod("getDesignation").invoke(d);
                  } catch (Exception ignore) {
                    // intentionally empty
                  }
                  try {
                    instruction = (String) d.getClass().getMethod("getInstruction").invoke(d);
                  } catch (Exception ignore) {
                    // intentionally empty
                  }

                  var dd = new za.co.rmb.tts.mandates.resolutions.ui.model.dto.DirectorDTO();
                  dd.setName(nz.apply(name));
                  dd.setSurname(nz.apply(surname));
                  dd.setDesignation(nz.apply(designation));
                  dd.setInstruction(nz.apply(instruction));

                  if (!(dd.getName().isEmpty() && dd.getSurname().isEmpty() && dd.getDesignation()
                      .isEmpty())) {
                    dirs.add(dd);
                  }
                }
              }
            } catch (ReflectiveOperationException ignore) {
              // ok
            }
          }

          logger.info("viewRequest {} -> directors(mapped)={}", requestId, dirs.size());
          view.setDirectors(dirs);

        } catch (Exception e) {
          logger.warn("Mapping directors failed: {}", e.toString());
        }

      } else {
        view.setRequestId(requestId);
      }

      view.setCompanyName(
          (sub.getCompany() != null && sub.getCompany().getName() != null)
              ? sub.getCompany().getName() : "Unknown"
      );

      //Inject newest approve/reject comments
      view.setApprovedComments(approvedRows);
      view.setRejectedComments(rejectedRows);

      view.setAccounts(new java.util.ArrayList<>(accountsByKey.values()));
      view.setSignatories(null);

      RequestTableWrapper wrapper = new RequestTableWrapper();
      wrapper.setRequest(java.util.List.of(view));

      populateInstructions(wrapper, view.getSubStatus(), mandatesResolutionsDaoURL);

      Object approveErr = servletRequest.getAttribute("approveErr");
      String errParam = servletRequest.getParameter("err");
      if (approveErr != null || "chk".equalsIgnoreCase(String.valueOf(errParam))) {
        ApproveRejectErrorModel em = new ApproveRejectErrorModel();
        em.setConfirmationCheckMandate(
            (approveErr != null)
                ? String.valueOf(approveErr)
                : "Verification cannot proceed until the checkbox has been selected"
        );
        wrapper.setApproveRejectErrorModel(em);
      }
      UserDTO users = (UserDTO) session.getAttribute("currentUser");
      System.out.println("=====User Role==== " + users.getUserRole());
      RequestDTO requestDTO = new RequestDTO();
      if ("ADMIN".equalsIgnoreCase(users.getUserRole())) {
        requestDTO.setSubStatus("Admin");
      } else {
        requestDTO.setSubStatus("User");
      }


      // Render
      String page = xsltProcessor.generatePages(xslPagePath("ViewRequest"), wrapper);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Error fetching request for view: {}", e.getMessage(), e);
      return ResponseEntity.ok("""
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
              <error>Unable to load request for viewing.</error>
          </page>
          """
      );
    }
  }

  @PostMapping(value = "/approve-validate", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> approveValidate(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "confirmationCheckMandate", required = false) String confirm,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    boolean checked = "1".equals(confirm) || "true".equalsIgnoreCase(String.valueOf(confirm));
    if (!checked) {
      servletRequest.setAttribute(
          "approveErr",
          "Verification cannot proceed until the checkbox has been selected"
      );
      return displayViewRequest(requestId, session, servletRequest);
    }

    return displayViewRequestApprovePage(requestId);
  }

  @PostMapping(value = "/reject-validate", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> rejectValidate(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "confirmationCheckMandate", required = false) String confirm,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    boolean checked = "1".equals(confirm) || "true".equalsIgnoreCase(String.valueOf(confirm));
    if (!checked) {
      servletRequest.setAttribute(
          "approveErr",
          "Verification cannot proceed until the checkbox has been selected"
      );
      return displayViewRequest(requestId, session, servletRequest);
    }

    return displayViewRequestRejectPage(requestId);
  }

  @PostMapping(value = "/admin-approve-validate", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminApproveValidate(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "confirmationCheckMandate", required = false) String confirm,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    boolean checked = "1".equals(confirm) || "true".equalsIgnoreCase(String.valueOf(confirm));
    if (!checked) {
      servletRequest.setAttribute(
          "approveErr",
          "Verification cannot proceed until the checkbox has been selected"
      );
      return displayAdminView(requestId, session, servletRequest);
    }

    return displayViewRequestApprovePage(requestId);
  }

  @PostMapping(value = "/admin-reject-validate", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminRejectValidate(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "confirmationCheckMandate", required = false) String confirm,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    //Same gating rule as Approve: checkbox must be selected
    boolean checked = "1".equals(confirm) || "true".equalsIgnoreCase(String.valueOf(confirm));
    if (!checked) {
      servletRequest.setAttribute(
          "approveErr",
          "Verification cannot proceed until the checkbox has been selected"
      );
      //Re-render the main ViewRequest page (no popup)
      return displayAdminView(requestId, session, servletRequest);
    }

    //Open reject page
    return displayViewRequestRejectPage(requestId);
  }


  @org.springframework.web.bind.annotation.RequestMapping(
      value = "/viewRequestHold",
      method = { org.springframework.web.bind.annotation.RequestMethod.GET,
          org.springframework.web.bind.annotation.RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> holdRequest(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "origin", required = false) String origin,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    try {
      RestTemplate rt = new RestTemplate();

      //1) Read current subStatus (fresh)
      String currentSub = null;
      try {
        var resp = rt.getForEntity(
            mandatesResolutionsDaoURL + "/api/request/{id}",
            RequestDTO.class, requestId);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
          currentSub = resp.getBody().getSubStatus();
        }
      } catch (Exception ignore) {
        // intentionally empty
      }

      //2) Derive the correct DAO-legal Hold label from current pending subStatus
      String holdLabel = toHoldLabel(currentSub); //uses helper

      //3) PUT to DAO: status + subStatus + processOutcome=Hold (advances Camunda)
      var payload = new java.util.LinkedHashMap<String, Object>();
      payload.put("status", "On Hold");
      payload.put("subStatus", holdLabel);
      payload.put("processOutcome", "Hold");
      payload.put("updator", currentDisplayId(session, servletRequest));

      var headers = new org.springframework.http.HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

      String url = mandatesResolutionsDaoURL + "/api/request/{id}";
      logger.info("Hold PUT {} payload={}",
          url.replace("{id}", String.valueOf(requestId)), payload);

      try {
        var entity = new org.springframework.http.HttpEntity<>(payload, headers);
        var resp = rt.exchange(url, HttpMethod.PUT, entity, Object.class, requestId);
        logger.info("Hold DAO response status={}", resp.getStatusCode());
      } catch (org.springframework.web.client.HttpServerErrorException e) {
        if (e.getStatusCode().value() == 503) {
          logger.warn("Hold: workflow unavailable for request {}. Body={}",
              requestId, e.getResponseBodyAsString());
          return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_XML)
              .body("""
                    <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                      <error>The workflow service is temporarily unavailable, so
                      we couldn't place the request on hold. Please try again shortly.</error>
                    </page>
                  """);
        }
        throw e;
      }

      //4) Return to the same screen the user was on
      return "admin".equalsIgnoreCase(origin)
          ? displayAdminView(requestId, session, servletRequest)
          : displayViewRequest(requestId, session, servletRequest);

    } catch (Exception e) {
      logger.error("Hold failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("""
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <error>Unable to place request on hold.</error>
          </page>
          """);
    }
  }

  // === UNHOLD ===
  @org.springframework.web.bind.annotation.RequestMapping(
      value = "/viewRequestUnhold",
      method = {
          org.springframework.web.bind.annotation.RequestMethod.GET,
          org.springframework.web.bind.annotation.RequestMethod.POST
      },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> unholdRequest(
      @RequestParam("requestId") Long requestId,
      @RequestParam(value = "origin", required = false) String origin,
      HttpSession session,
      HttpServletRequest servletRequest
  ) {
    try {
      RestTemplate rt = new RestTemplate();

      //1) Read current subStatus (should be the HOLD label)
      String currentHoldLabel = null;
      try {
        var resp = rt.getForEntity(
            mandatesResolutionsDaoURL + "/api/request/{id}",
            RequestDTO.class, requestId);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
          currentHoldLabel = resp.getBody().getSubStatus();
        }
      } catch (Exception e) {
        logger.warn("UnHold: GET "
                    + "/api/request/{} failed (continuing): {}", requestId, e.getMessage());
      }

      //2)Compute target pending subStatus
      String restoredPending = fromHoldLabel(currentHoldLabel);

      var headers = new org.springframework.http.HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
      String url = mandatesResolutionsDaoURL + "/api/request/{id}";

      // WORKFLOW-FIRST
      var wfPayload = new java.util.LinkedHashMap<String, Object>();
      wfPayload.put("processOutcome", "UnHold");          //adapter triggers Camunda lane
      wfPayload.put("outcome", "UnHold");                 //BPMN gateway: ${outcome == 'UnHold'}
      wfPayload.put("updator", currentDisplayId(session, servletRequest));

      boolean workflowOk = false;
      try {
        //Small retry (handles transient hiccups)
        for (int attempt = 1; attempt <= 2 && !workflowOk; attempt++) {
          logger.info("UnHold WF PUT (attempt {}) {} payload={}",
              attempt, url.replace("{id}", String.valueOf(requestId)), wfPayload);
          var entity = new org.springframework.http.HttpEntity<>(wfPayload, headers);
          var resp = rt.exchange(url, HttpMethod.PUT, entity, Object.class, requestId);
          logger.info("UnHold WF response status={}", resp.getStatusCode());
          workflowOk = resp.getStatusCode().is2xxSuccessful();
          if (!workflowOk) {
            Thread.sleep(200L * attempt); // simple backoff
          }
        }
      } catch (org.springframework.web.client.HttpServerErrorException e) {
        if (e.getStatusCode().value() == 503) {
          logger.warn("UnHold WF: workflow unavailable for request {}. Body={}",
              requestId, e.getResponseBodyAsString());
        } else {
          throw e; //non-503 server error
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }

      if (workflowOk) {
        //Workflow changer
        var dbSync = new java.util.LinkedHashMap<String, Object>();
        dbSync.put("status", "In Progress");
        dbSync.put("subStatus", restoredPending);
        dbSync.put("outcome", "In Progress");
        dbSync.put("updator", currentDisplayId(session, servletRequest));

        logger.info("UnHold DB-SYNC PUT {} payload={}",
            url.replace("{id}", String.valueOf(requestId)), dbSync);

        var entity = new org.springframework.http.HttpEntity<>(dbSync, headers);
        var resp2 = rt.exchange(url, HttpMethod.PUT, entity, Object.class, requestId);
        logger.info("UnHold DB-SYNC response status={}", resp2.getStatusCode());
      } else {
        //FALLBACK: workflow down -> DB-only update so UI recovers state ----
        var dbOnly = new java.util.LinkedHashMap<String, Object>();
        dbOnly.put("status", "In Progress");
        dbOnly.put("subStatus", restoredPending);
        dbOnly.put("outcome", "In Progress");
        dbOnly.put("updator", currentDisplayId(session, servletRequest));

        try {
          var entity = new org.springframework.http.HttpEntity<>(dbOnly, headers);
          var resp2 = rt.exchange(url, HttpMethod.PUT, entity, Object.class, requestId);
          logger.info("UnHold fallback (DB-only) status={}", resp2.getStatusCode());

          //Add an internal audit note so ops can reconcile once workflow is healthy
          try {
            var comment = new java.util.LinkedHashMap<String, Object>();
            comment.put("requestId", requestId);
            comment.put("commentText", "UnHold applied without workflow sync "
                                       + "(Camunda unavailable). Please reconcile once workflow "
                                       + "is back.");
            comment.put("isInternal", Boolean.TRUE);
            comment.put("creator", currentDisplayId(session, servletRequest));
            rt.postForEntity(mandatesResolutionsDaoURL + "/api/comment", comment, Object.class);
          } catch (Exception ignore) { /* non-fatal */ }

        } catch (Exception e2) {
          return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_XML)
              .body("""
                    <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                      <error>We couldn't take the request off hold, and the workflow 
                      service is down.Please try again shortly.</error>
                    </page>
                  """);
        }
      }

      //Return to appropriate view
      return "admin".equalsIgnoreCase(origin)
          ? displayAdminView(requestId, session, servletRequest)
          : displayViewRequest(requestId, session, servletRequest);

    } catch (Exception e) {
      logger.error("UnHold failed", e);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_XML)
          .body("""
                <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                  <error>Unable to take request off hold.</error>
                </page>
              """);
    }
  }

  @GetMapping(value = "/draft/view", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> viewDraft(
      @RequestParam("id") Long id,
      HttpSession session
  ) {
    final String base = mandatesResolutionsDaoURL;
    RestTemplate rt = new RestTemplate();

    RequestStagingDTO s =
        rt.getForObject(base + "/api/request-staging/{id}", RequestStagingDTO.class, id);
    if (s == null) {
      return ResponseEntity.notFound().build();
    }

    // 1) Which page to open? Prefer the token saved in subStatus "Saved@PAGE"
    String page = normalizePageCode(extractLastPageCode(s.getRequestSubStatus()));
    if (page == null) {
      // Fallback heuristic (old behaviour)
      page = (s.getAccounts() != null && !s.getAccounts().isEmpty())
          ? "MANDATES_AUTOFILL"
          : "SEARCH_RESULTS";
    }

    // 2)Keeps existing pdfSessionsId
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    if (pdfSessionId == null || pdfSessionId.isBlank()) {
      pdfSessionId = java.util.UUID.randomUUID().toString();
    }

    // 3) Build wrapper based on page and render appropriate XSL
    RequestWrapper w;
    String xsl;

    switch (page) {
      case "SEARCH_RESULTS" ->
        {
          w = buildSearchResultsWrapperFromStaging(id, pdfSessionId);
          xsl = "SearchResults";
        }
      case "DIRECTORS_DETAILS" ->
        {
          w = buildSearchResultsWrapperFromStaging(id, pdfSessionId);
          xsl = "MandatesResolutionsDirectorsDetails";
        }
      // Treat RESOLUTION_AUTOFILL like the directors/search path
      case "RESOLUTION_AUTOFILL" ->
        {
          w = buildSearchResultsWrapperFromStaging(id, pdfSessionId); // includes directors + tools
          xsl = "ResolutionAutoFill";
        }
      case "MANDATES_AUTOFILL",
           "ACC_DETAILS",
           "MANDATES_SIGNATURE_CARD",
           "MANDATES_RESOLUTIONS_SIGNATURE_CARD" ->
        {
          w = buildAutoFillWrapperFromStaging(id, pdfSessionId); // accounts + signatories pages
          xsl = switch (page) {
          case "ACC_DETAILS" -> "MandatesResolutionsAccDetails";
          case "MANDATES_SIGNATURE_CARD" -> "MandatesSignatureCard";
          case "MANDATES_RESOLUTIONS_SIGNATURE_CARD" -> "MandatesResolutionsSignatureCard";
          default -> "MandatesAutoFill";
          };
        }
      default ->
        {
          w = buildSearchResultsWrapperFromStaging(id, pdfSessionId);
          xsl = "SearchResults";
        }
    }

    //  Seed dropdown selection (numeric "1|2|3") into the wrapper for XSLT
    String sel = normalizeSelCode(s.getRequestType());
    if (sel == null && s.getRequestSubStatus() != null) {
      int at = s.getRequestSubStatus().lastIndexOf('@');
      if (at > -1) {
        sel = normalizeSelCode(s.getRequestSubStatus().substring(at + 1));
      }
    }
    if (sel != null && w != null && w.getRequest() != null) {
      w.getRequest().setMandateResolution(sel);
    }

    // Put in session for downstream flows
    session.setAttribute("pdfSessionId", w.getRequest().getPdfSessionId());
    session.setAttribute("requestData", w.getRequest());

    String pageXml = xsltProcessor.generatePage(xslPagePath(xsl), w);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(pageXml);
  }

  private ResponseEntity<String> renderDraft(Long id, HttpSession session) {
    final String base = mandatesResolutionsDaoURL;
    RestTemplate rt = new RestTemplate();

    RequestStagingDTO s =
        rt.getForObject(base + "/api/request-staging/{id}", RequestStagingDTO.class, id);
    if (s == null) {
      String xml = "<page><error>Draft not found.</error></page>";
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml);
    }

    // Decide page: if draft already has accounts, go to MandatesAutoFill; otherwise SearchResults
    boolean goAutoFill = (s.getAccounts() != null && !s.getAccounts().isEmpty());

    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    if (pdfSessionId == null || pdfSessionId.isBlank()) {
      pdfSessionId = java.util.UUID.randomUUID().toString();
    }

    if (goAutoFill) {
      RequestWrapper w = buildAutoFillWrapperFromStaging(id, pdfSessionId);

      String sel = normalizeSelCode(s.getRequestType());
      if (sel == null && s.getRequestSubStatus() != null) {
        int at = s.getRequestSubStatus().lastIndexOf('@');
        if (at > -1) {
          sel = normalizeSelCode(s.getRequestSubStatus().substring(at + 1));
        }
      }
      if (sel != null && w.getRequest() != null) {
        w.getRequest().setMandateResolution(sel);
      }

      session.setAttribute("pdfSessionId", w.getRequest().getPdfSessionId());
      session.setAttribute("requestData", w.getRequest());
      String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), w);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);

    } else {
      RequestWrapper w = buildSearchResultsWrapperFromStaging(id, pdfSessionId);

      //Seed dropdown selection for the SearchResults page
      String sel = normalizeSelCode(s.getRequestType());
      if (sel == null && s.getRequestSubStatus() != null) {
        int at = s.getRequestSubStatus().lastIndexOf('@');
        if (at > -1) {
          sel = normalizeSelCode(s.getRequestSubStatus().substring(at + 1));
        }
      }
      if (sel != null && w.getRequest() != null) {
        w.getRequest().setMandateResolution(sel);
      }

      session.setAttribute("pdfSessionId", w.getRequest().getPdfSessionId());
      session.setAttribute("requestData", w.getRequest());
      String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), w);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
    }
  }

  @RequestMapping(
      value = "/draft/view",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> viewDraftQuery(
      @RequestParam("id") Long id,
      HttpSession session
  ) {
    return viewDraft(id, session);
  }

  // ================ ADMIN EDIT ENDPOINTS =================
  //Edit request page
  @RequestMapping(
      value = "/adminEditRequest/{requestId}",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> adminDisplayEditRequest(@PathVariable Long requestId,
                                                        HttpSession session,
                                                        HttpServletRequest servletRequest) {
    try {
      String displayName = currentDisplayId(session, servletRequest);

      RequestTableWrapper wrapper = buildEditWrapper(requestId);

      //Normalize creator/updator
      java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();
      RequestTableDTO view = (wrapper.getRequest() != null && !wrapper.getRequest().isEmpty())
          ? wrapper.getRequest().get(0) : null;
      if (view != null) {
        String creator = nz.apply(view.getCreator());
        if (creator.isEmpty() || "ui".equalsIgnoreCase(creator)) {
          view.setCreator(displayName);
        }
        String updator = nz.apply(view.getUpdator());
        if (updator.isEmpty() || "ui".equalsIgnoreCase(updator)) {
          view.setUpdator(displayName);
        }
      }

      String page = xsltProcessor.generatePage(xslPagePath("AdminEditRequest"), wrapper);
      logWrapperAndPage("EditRequest", requestId, wrapper, page);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
    } catch (Exception e) {
      logger.error("Error fetching request for edit: {}", e.getMessage(), e);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_XML)
          .body("<page><error>Unable to load request for editing.</error></page>");
    }
  }

  //Add one blank signatory row to account at 1-based index {addSignatoryAt} ===
  @PostMapping(value = "/adminEditRequestAddSignatory/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminEditRequestAddSignatory(@PathVariable Long requestId,
                                                             @RequestParam("addSignatoryAt")
                                                             int accountIndex1) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getAccounts() == null) {
        view.setAccounts(new java.util.ArrayList<>());
      }
      int ai = Math.max(1, accountIndex1) - 1;

      if (ai >= 0 && ai < view.getAccounts().size()) {
        AccountDTO acc = view.getAccounts().get(ai);
        if (acc.getSignatories() == null) {
          acc.setSignatories(new java.util.ArrayList<>());
        }

        SignatoryDTO blank = new SignatoryDTO();
        blank.setFullName("");
        blank.setIdNumber("");
        blank.setInstructions("");
        blank.setCapacity("");
        blank.setGroupCategory("");
        blank.setAccountName(acc.getAccountName());
        blank.setAccountNumber(acc.getAccountNumber());

        acc.getSignatories().add(blank);
      }

      String page = xsltProcessor.generatePage(xslPagePath("AdminEditRequest"), wrapper);

      //Logging
      logger.info("AddSignatory: requestId={}, accountIndex1={}", requestId, accountIndex1);
      logWrapperAndPage("EditRequest-AddSignatory", requestId, wrapper, page);

      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Add signatory failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to add signatory row.</error></page>");
    }
  }

  //Remove signatory given "aPos_sPos" (both 1-based)
  @PostMapping(value = "/adminEditRequestRemoveSignatory/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminEditRequestRemoveSignatory(@PathVariable Long requestId,
                                                                @RequestParam("removeSignatoryAt")
                                                                String at) {
    try {
      String[] parts = at.split("_");
      int a1 = Integer.parseInt(parts[0]);
      int s1 = Integer.parseInt(parts[1]);

      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);

      if (view.getAccounts() != null) {
        int ai = a1 - 1;
        if (ai >= 0 && ai < view.getAccounts().size()) {
          var acc = view.getAccounts().get(ai);
          if (acc.getSignatories() != null) {
            int si = s1 - 1;
            if (si >= 0 && si < acc.getSignatories().size()) {
              acc.getSignatories().remove(si);
            }
          }
        }
      }

      //Dumps
      dumpWrapperJson("EditRequest-RemoveSignatory", wrapper, requestId);
      String page = xsltProcessor.generatePage(xslPagePath("AdminEditRequest"), wrapper);
      dumpPageXml("EditRequest-RemoveSignatory", page, requestId);

      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Remove signatory failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to remove signatory row.</error></page>");
    }
  }

  @PostMapping(value = "/adminEditRequestAddDirector/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminEditRequestAddDirector(@PathVariable Long requestId) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getDirectors() == null) {
        view.setDirectors(new java.util.ArrayList<>());
      }

      DirectorDTO blank = new DirectorDTO();
      blank.setAuthorityId(null);
      blank.setName("");
      blank.setSurname("");
      blank.setDesignation("");
      view.getDirectors().add(blank);

      String page = xsltProcessor.generatePage(xslPagePath("AdminEditRequest"), wrapper);
      logWrapperAndPage("EditRequest-AddDirector", requestId, wrapper, page);
      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Add director failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to add director row.</error></page>");
    }
  }

  @PostMapping(value = "/adminEditRequestRemoveDirector/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminEditRequestRemoveDirector(@PathVariable Long requestId,
                                                               @RequestParam("removeDirectorAt")
                                                               int index1) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getDirectors() != null) {
        int i = Math.max(1, index1) - 1;
        if (i >= 0 && i < view.getDirectors().size()) {
          view.getDirectors().remove(i);
        }
      }
      String page = xsltProcessor.generatePage(xslPagePath("AdminEditRequest"), wrapper);
      logWrapperAndPage("EditRequest-RemoveDirector", requestId, wrapper, page);
      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Remove director failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to remove director row.</error></page>");
    }
  }


  //Add blank account section (with no signatories by default) ===
  @PostMapping(value = "/adminEditRequestAddAccount/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> adminEditRequestAddAccount(@PathVariable Long requestId) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getAccounts() == null) {
        view.setAccounts(new java.util.ArrayList<>());
      }

      AccountDTO newAcc = new AccountDTO();
      newAcc.setAccountName("");
      newAcc.setAccountNumber("");
      newAcc.setSignatories(new java.util.ArrayList<>());

      view.getAccounts().add(newAcc);

      String page = xsltProcessor.generatePage(xslPagePath("AdminEditRequest"), wrapper);

      //LOGGING
      logger.info("AddAccount: requestId={}", requestId);
      logWrapperAndPage("EditRequest-AddAccount", requestId, wrapper, page);

      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Add account failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to add account section.</error></page>");
    }
  }

  //Remove account section at 1-based index {removeAccountAt}
  @RequestMapping(
      value = "/adminEditRequestRemoveAccount/{requestId}",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> adminEditRequestRemoveAccount(
      @PathVariable Long requestId,
      @RequestParam("removeAccountAt") int accountIndex1
  ) {
    try {
      logger.info("RemoveAccount CALLED: requestId={}, removeAccountAt={}", requestId,
          accountIndex1);

      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);

      if (view.getAccounts() != null) {
        int ai = Math.max(1, accountIndex1) - 1;
        if (ai >= 0 && ai < view.getAccounts().size()) {
          view.getAccounts().remove(ai);
        } else {
          logger.warn("RemoveAccount: index out of range. size={}, requestedZeroBased={}",
              view.getAccounts().size(), ai);
        }
      } else {
        logger.warn("RemoveAccount: accounts list is null");
      }

      dumpWrapperJson("EditRequest-RemoveAccount", wrapper, requestId);
      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);
      dumpPageXml("EditRequest-RemoveAccount", page, requestId);

      logger.info("RemoveAccount DONE: requestId={}, accountsNow={}",
          requestId, view.getAccounts() == null ? 0 : view.getAccounts().size());

      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Remove account failed: {}", e.getMessage(), e);
      String errorPage = renderSimpleErrorPage(
          "Remove Account",
          "Unable to remove account section.",
          "app-domain/mandates-and-resolutions/AdminEditRequest/" + requestId
      );
      return ResponseEntity.ok(errorPage);
    }
  }

  //Save edits from the Edit Request page and then go back to the Landing page
  @RequestMapping(
      value = "/adminEditRequestSave/{requestId}",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> adminEditRequestSave(@PathVariable Long requestId,
                                                     HttpServletRequest request) {
    try {
      //Parse form
      Map<String, String[]> params = request.getParameterMap();

      //Collect 1-based account indices present in the form
      java.util.regex.Pattern accIdxPat =
          java.util.regex.Pattern.compile("^(accountName|accountNo|accountId)_(\\d+)$");
      java.util.SortedSet<Integer> accountIdxs = new java.util.TreeSet<>();
      for (String k : params.keySet()) {
        java.util.regex.Matcher m = accIdxPat.matcher(k);
        if (m.matches()) {
          accountIdxs.add(Integer.parseInt(m.group(2)));
        }
      }

      //Build in-memory representation of the form rows
      class FormAcc {
        String accountId;  //may be null for new rows
        String accountName;
        String accountNo;
        java.util.List<Map<String, Object>> signatories = new java.util.ArrayList<>();
      }

      java.util.List<FormAcc> formAccounts = new java.util.ArrayList<>();

      for (Integer ai : accountIdxs) {
        FormAcc fa = new FormAcc();
        fa.accountId = getParam(params, "accountId_" + ai);   //hidden field from XSL
        fa.accountName = getParam(params, "accountName_" + ai);
        fa.accountNo = getParam(params, "accountNo_" + ai);

        //Signatory rows under this account
        java.util.regex.Pattern sigIdxPat = java.util.regex.Pattern.compile(
            "^(fullName|idNumber|capacity|group|instruction)_" + ai + "_(\\d+)$");
        java.util.SortedSet<Integer> sigIdxs = new java.util.TreeSet<>();
        for (String k : params.keySet()) {
          java.util.regex.Matcher m = sigIdxPat.matcher(k);
          if (m.matches()) {
            sigIdxs.add(Integer.parseInt(m.group(2)));
          }
        }
        for (Integer sj : sigIdxs) {
          String fullName = getParam(params, "fullName_" + ai + "_" + sj);
          String idNumber = getParam(params, "idNumber_" + ai + "_" + sj);
          String capacity = getParam(params, "capacity_" + ai + "_" + sj);
          String group = getParam(params, "group_" + ai + "_" + sj);
          String instruction = getParam(params, "instruction_" + ai + "_" + sj);
          if (instruction == null || instruction.isBlank()) {
            instruction =
                getParam(params, "origInstruction_" + ai + "_" + sj); // fallback to saved value
          }

          if (isAllBlank(fullName, idNumber, capacity, group, instruction)) {
            continue;
          }

          Map<String, Object> s = new java.util.LinkedHashMap<>();
          s.put("fullName", nz(fullName));
          s.put("idNumber", nz(idNumber));
          s.put("capacity", nz(capacity));
          s.put("groupCategory", nz(group));
          s.put("instructions", nz(instruction)); // Add|Remove
          fa.signatories.add(s);
        }

        //Skip completely blank account rows
        if (isAllBlank(fa.accountName, fa.accountNo) && fa.signatories.isEmpty()) {
          continue;
        }

        formAccounts.add(fa);
      }

      //Load current state (companyId & existing accounts)
      final String base = mandatesResolutionsDaoURL;
      RestTemplate rt = new RestTemplate();

      MandateResolutionSubmissionResultDTO sub =
          rt.getForObject(base + "/api/submission/{id}", MandateResolutionSubmissionResultDTO.class,
              requestId);
      if (sub == null || sub.getCompany() == null) {
        logger.error("Cannot load submission for requestId {}", requestId);
        return ResponseEntity.ok(
            "<page><error>Unable to save: submission not found.</error></page>");
      }
      Long companyId = sub.getCompany().getCompanyId();
      java.util.Map<Long, MandateResolutionSubmissionResultDTO.Account> existingById =
          new java.util.HashMap<>();
      if (sub.getAccounts() != null) {
        for (MandateResolutionSubmissionResultDTO.Account a : sub.getAccounts()) {
          existingById.put(a.getAccountId(), a);
        }
      }

      //Persist: PUT existing, POST new, DELETE removed
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

      java.util.Set<Long> keptExistingIds = new java.util.HashSet<>();

      //Upserts
      for (FormAcc fa : formAccounts) {
        Map<String, Object> dto = new java.util.LinkedHashMap<>();
        dto.put("companyId", companyId);
        dto.put("accountName", nz(fa.accountName));
        dto.put("accountNumber", nz(fa.accountNo));
        dto.put("isActive", Boolean.TRUE);
        dto.put("signatories", fa.signatories);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(dto, headers);

        if (fa.accountId != null && !fa.accountId.isBlank()) {
          Long accountId = Long.valueOf(fa.accountId);
          rt.exchange(base + "/api/account/{accountId}", HttpMethod.PUT, entity, Object.class,
              accountId);
          keptExistingIds.add(accountId);
          logger.info("Updated account {}", accountId);
        } else {
          var created = rt.postForEntity(base + "/api/account", entity, Object.class);
          logger.info("Created account; status={}", created.getStatusCode());
        }
      }

      //Deletes (anything that existed but isn’t in the form anymore)
      for (Long existingId : existingById.keySet()) {
        if (!keptExistingIds.contains(existingId)) {
          rt.delete(base + "/api/account/{accountId}", existingId);
          logger.info("Deleted account {}", existingId);
        }
      }

      // ===== DIRECTORS (Authorities) =====

      // 1) Parse rows from form: dirFirstName_#, dirSurname_#, dirDesignation_#, dirId_#
      java.util.regex.Pattern dirIdxPat =
          java.util.regex.Pattern.compile(
              "^(dirFirstName|dirSurname|dirDesignation|dirId)_(\\d+)$");
      java.util.SortedSet<Integer> dirIdxs = new java.util.TreeSet<>();
      for (String k : params.keySet()) {
        java.util.regex.Matcher m = dirIdxPat.matcher(k);
        if (m.matches()) {
          dirIdxs.add(Integer.parseInt(m.group(2)));
        }
      }

      class FormDir {
        String id;          // authorityId (blank/null for new)
        String firstName;
        String surname;
        String designation;
      }

      java.util.List<FormDir> formDirs = new java.util.ArrayList<>();
      for (Integer i : dirIdxs) {
        FormDir d = new FormDir();
        d.id = getParam(params, "dirId_" + i);
        d.firstName = getParam(params, "dirFirstName_" + i);
        d.surname = getParam(params, "dirSurname_" + i);
        d.designation = getParam(params, "dirDesignation_" + i);

        // skip rows that are completely blank
        if (!isAllBlank(d.firstName, d.surname, d.designation)) {
          formDirs.add(d);
        }
      }

      // 2) Build existing authorities map (by id) to detect deletes
      java.util.Map<Long, MandateResolutionSubmissionResultDTO.Authority> existingAuthById =
          new java.util.HashMap<>();
      if (sub.getAuthorities() != null) {
        for (MandateResolutionSubmissionResultDTO.Authority a : sub.getAuthorities()) {
          if (a != null && a.getAuthorityId() != null) {
            existingAuthById.put(a.getAuthorityId(), a);
          }
        }
      }

      java.util.Set<Long> keptAuthorityIds = new java.util.HashSet<>();

      // 3) Upserts (PUT for existing id, POST for new)
      for (FormDir d : formDirs) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("companyId", companyId);
        payload.put("firstname", nz(d.firstName));
        payload.put("surname", nz(d.surname));
        payload.put("designation", nz(d.designation));
        payload.put("isActive", Boolean.TRUE);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        if (d.id != null && !d.id.isBlank()) {
          Long id = Long.valueOf(d.id);
          rt.exchange(base + "/api/authority/{id}", HttpMethod.PUT, entity, Object.class, id);
          keptAuthorityIds.add(id);
          logger.info("Updated authority {}", id);
        } else {
          var created = rt.postForEntity(base + "/api/authority", entity, Object.class);
          logger.info("Created authority; status={}", created.getStatusCode());
        }
      }

// 4) Deletes (anything that existed but isn’t in the form anymore)
      for (Long id : existingAuthById.keySet()) {
        if (!keptAuthorityIds.contains(id)) {
          rt.delete(base + "/api/authority/{id}", id);
          logger.info("Deleted authority {}", id);
        }
      }

// Takes you to the View Request page once the edit is successful,
// and ALWAYS return valid XML to the UI.
      HttpSession session = request.getSession(false);
      ResponseEntity<String> view = displayAdminView(requestId, session, request);
      String body = (view != null ? view.getBody() : null);

// If the body is null/blank or doesn't start with '<', wrap a valid <page> so XSLT can parse it.
      if (body == null || body.isBlank() || body.charAt(0) != '<') {
        body =
            "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            +
            "  <error>Unexpected response after save. Please try again.</error>"
            +
            "</page>";
      }

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_XML)
          .body(body);

    } catch (Exception e) {
      logger.error("Save edit failed for requestId {}: {}", requestId, e.getMessage(), e);
      String error = """
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <error>Unable to save changes for this request.</error>
          </page>
          """;
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(error);
    }
  }

  //============ USER EDIT REQUEST ENDPOINTS ============

  //Edit request page
  @RequestMapping(
      value = "/editRequest/{requestId}",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> displayEditRequest(@PathVariable Long requestId,
                                                   HttpSession session,
                                                   HttpServletRequest servletRequest) {
    try {
      String displayName = currentDisplayId(session, servletRequest);

      RequestTableWrapper wrapper = buildEditWrapper(requestId);

      //Normalize creator/updator
      java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();
      RequestTableDTO view = (wrapper.getRequest() != null && !wrapper.getRequest().isEmpty())
          ? wrapper.getRequest().get(0) : null;
      if (view != null) {
        String creator = nz.apply(view.getCreator());
        if (creator.isEmpty() || "ui".equalsIgnoreCase(creator)) {
          view.setCreator(displayName);
        }
        String updator = nz.apply(view.getUpdator());
        if (updator.isEmpty() || "ui".equalsIgnoreCase(updator)) {
          view.setUpdator(displayName);
        }
      }

      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);
      logWrapperAndPage("EditRequest", requestId, wrapper, page);
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(page);
    } catch (Exception e) {
      logger.error("Error fetching request for edit: {}", e.getMessage(), e);
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_XML)
          .body("<page><error>Unable to load request for editing.</error></page>");
    }
  }

  //Add one blank signatory row to account at 1-based index {addSignatoryAt} ===
  @PostMapping(value = "/editRequestAddSignatory/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editRequestAddSignatory(@PathVariable Long requestId,
                                                        @RequestParam("addSignatoryAt")
                                                        int accountIndex1) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getAccounts() == null) {
        view.setAccounts(new java.util.ArrayList<>());
      }
      int ai = Math.max(1, accountIndex1) - 1;

      if (ai >= 0 && ai < view.getAccounts().size()) {
        AccountDTO acc = view.getAccounts().get(ai);
        if (acc.getSignatories() == null) {
          acc.setSignatories(new java.util.ArrayList<>());
        }

        SignatoryDTO blank = new SignatoryDTO();
        blank.setFullName("");
        blank.setIdNumber("");
        blank.setInstructions("");
        blank.setCapacity("");
        blank.setGroupCategory("");
        blank.setAccountName(acc.getAccountName());
        blank.setAccountNumber(acc.getAccountNumber());

        acc.getSignatories().add(blank);
      }

      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);

      //Logging
      logger.info("AddSignatory: requestId={}, accountIndex1={}", requestId, accountIndex1);
      logWrapperAndPage("EditRequest-AddSignatory", requestId, wrapper, page);

      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Add signatory failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to add signatory row.</error></page>");
    }
  }

  //Remove signatory given "aPos_sPos" (both 1-based)
  @PostMapping(value = "/editRequestRemoveSignatory/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editRequestRemoveSignatory(@PathVariable Long requestId,
                                                           @RequestParam("removeSignatoryAt")
                                                           String at) {
    try {
      String[] parts = at.split("_");
      int a1 = Integer.parseInt(parts[0]);
      int s1 = Integer.parseInt(parts[1]);

      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);

      if (view.getAccounts() != null) {
        int ai = a1 - 1;
        if (ai >= 0 && ai < view.getAccounts().size()) {
          var acc = view.getAccounts().get(ai);
          if (acc.getSignatories() != null) {
            int si = s1 - 1;
            if (si >= 0 && si < acc.getSignatories().size()) {
              acc.getSignatories().remove(si);
            }
          }
        }
      }

      //Dumps
      dumpWrapperJson("EditRequest-RemoveSignatory", wrapper, requestId);
      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);
      dumpPageXml("EditRequest-RemoveSignatory", page, requestId);

      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Remove signatory failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to remove signatory row.</error></page>");
    }
  }

  @PostMapping(value = "/editRequestAddDirector/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editRequestAddDirector(@PathVariable Long requestId) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getDirectors() == null) {
        view.setDirectors(new java.util.ArrayList<>());
      }

      DirectorDTO blank = new DirectorDTO();
      blank.setAuthorityId(null);
      blank.setName("");
      blank.setSurname("");
      blank.setDesignation("");
      view.getDirectors().add(blank);

      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);
      logWrapperAndPage("EditRequest-AddDirector", requestId, wrapper, page);
      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Add director failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to add director row.</error></page>");
    }
  }

  @PostMapping(value = "/editRequestRemoveDirector/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editRequestRemoveDirector(@PathVariable Long requestId,
                                                          @RequestParam("removeDirectorAt")
                                                          int index1) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getDirectors() != null) {
        int i = Math.max(1, index1) - 1;
        if (i >= 0 && i < view.getDirectors().size()) {
          view.getDirectors().remove(i);
        }
      }
      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);
      logWrapperAndPage("EditRequest-RemoveDirector", requestId, wrapper, page);
      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Remove director failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to remove director row.</error></page>");
    }
  }


  //Add blank account section (with no signatories by default) ===
  @PostMapping(value = "/editRequestAddAccount/{requestId}", produces =
      MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> editRequestAddAccount(@PathVariable Long requestId) {
    try {
      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);
      if (view.getAccounts() == null) {
        view.setAccounts(new java.util.ArrayList<>());
      }

      AccountDTO newAcc = new AccountDTO();
      newAcc.setAccountName("");
      newAcc.setAccountNumber("");
      newAcc.setSignatories(new java.util.ArrayList<>());

      view.getAccounts().add(newAcc);

      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);

      //LOGGING
      logger.info("AddAccount: requestId={}", requestId);
      logWrapperAndPage("EditRequest-AddAccount", requestId, wrapper, page);

      return ResponseEntity.ok(page);
    } catch (Exception e) {
      logger.error("Add account failed: {}", e.getMessage(), e);
      return ResponseEntity.ok("<page><error>Unable to add account section.</error></page>");
    }
  }

  //Remove account section at 1-based index {removeAccountAt}
  @RequestMapping(
      value = "/editRequestRemoveAccount/{requestId}",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> editRequestRemoveAccount(
      @PathVariable Long requestId,
      @RequestParam("removeAccountAt") int accountIndex1
  ) {
    try {
      logger.info("RemoveAccount CALLED: requestId={}, removeAccountAt={}", requestId,
          accountIndex1);

      RequestTableWrapper wrapper = buildEditWrapper(requestId);
      RequestTableDTO view = wrapper.getRequest().get(0);

      if (view.getAccounts() != null) {
        int ai = Math.max(1, accountIndex1) - 1;
        if (ai >= 0 && ai < view.getAccounts().size()) {
          view.getAccounts().remove(ai);
        } else {
          logger.warn("RemoveAccount: index out of range. size={}, requestedZeroBased={}",
              view.getAccounts().size(), ai);
        }
      } else {
        logger.warn("RemoveAccount: accounts list is null");
      }

      dumpWrapperJson("EditRequest-RemoveAccount", wrapper, requestId);
      String page = xsltProcessor.generatePage(xslPagePath("EditRequest"), wrapper);
      dumpPageXml("EditRequest-RemoveAccount", page, requestId);

      logger.info("RemoveAccount DONE: requestId={}, accountsNow={}",
          requestId, view.getAccounts() == null ? 0 : view.getAccounts().size());

      return ResponseEntity.ok(page);

    } catch (Exception e) {
      logger.error("Remove account failed: {}", e.getMessage(), e);
      String errorPage = renderSimpleErrorPage(
          "Remove Account",
          "Unable to remove account section.",
          "app-domain/mandates-and-resolutions/editRequest/" + requestId
      );
      return ResponseEntity.ok(errorPage);
    }
  }

  //Save edits from the Edit Request page and then go back to the Landing page
  @RequestMapping(
      value = "/editRequestSave/{requestId}",
      method = { RequestMethod.GET, RequestMethod.POST },
      produces = MediaType.APPLICATION_XML_VALUE
  )
  public ResponseEntity<String> editRequestSave(@PathVariable Long requestId,
                                                HttpServletRequest request) {
    try {
      //Parse form
      Map<String, String[]> params = request.getParameterMap();

      //Collect 1-based account indices present in the form
      java.util.regex.Pattern accIdxPat =
          java.util.regex.Pattern.compile("^(accountName|accountNo|accountId)_(\\d+)$");
      java.util.SortedSet<Integer> accountIdxs = new java.util.TreeSet<>();
      for (String k : params.keySet()) {
        java.util.regex.Matcher m = accIdxPat.matcher(k);
        if (m.matches()) {
          accountIdxs.add(Integer.parseInt(m.group(2)));
        }
      }

      //Build in-memory representation of the form rows
      class FormAcc {
        String accountId;  //may be null for new rows
        String accountName;
        String accountNo;
        java.util.List<Map<String, Object>> signatories = new java.util.ArrayList<>();
      }

      java.util.List<FormAcc> formAccounts = new java.util.ArrayList<>();

      for (Integer ai : accountIdxs) {
        FormAcc fa = new FormAcc();
        fa.accountId = getParam(params, "accountId_" + ai);   //hidden field from XSL
        fa.accountName = getParam(params, "accountName_" + ai);
        fa.accountNo = getParam(params, "accountNo_" + ai);

        //Signatory rows under this account
        java.util.regex.Pattern sigIdxPat = java.util.regex.Pattern.compile(
            "^(fullName|idNumber|capacity|group|instruction)_" + ai + "_(\\d+)$");
        java.util.SortedSet<Integer> sigIdxs = new java.util.TreeSet<>();
        for (String k : params.keySet()) {
          java.util.regex.Matcher m = sigIdxPat.matcher(k);
          if (m.matches()) {
            sigIdxs.add(Integer.parseInt(m.group(2)));
          }
        }
        for (Integer sj : sigIdxs) {
          String fullName = getParam(params, "fullName_" + ai + "_" + sj);
          String idNumber = getParam(params, "idNumber_" + ai + "_" + sj);
          String capacity = getParam(params, "capacity_" + ai + "_" + sj);
          String group = getParam(params, "group_" + ai + "_" + sj);
          String instruction = getParam(params, "instruction_" + ai + "_" + sj);
          if (instruction == null || instruction.isBlank()) {
            instruction =
                getParam(params, "origInstruction_" + ai + "_" + sj); // fallback to saved value
          }

          if (isAllBlank(fullName, idNumber, capacity, group, instruction)) {
            continue;
          }

          Map<String, Object> s = new java.util.LinkedHashMap<>();
          s.put("fullName", nz(fullName));
          s.put("idNumber", nz(idNumber));
          s.put("capacity", nz(capacity));
          s.put("groupCategory", nz(group));
          s.put("instructions", nz(instruction)); // Add|Remove
          fa.signatories.add(s);
        }

        //Skip completely blank account rows
        if (isAllBlank(fa.accountName, fa.accountNo) && fa.signatories.isEmpty()) {
          continue;
        }

        formAccounts.add(fa);
      }

      //Load current state (companyId & existing accounts)
      final String base = mandatesResolutionsDaoURL;
      RestTemplate rt = new RestTemplate();

      MandateResolutionSubmissionResultDTO sub =
          rt.getForObject(base + "/api/submission/{id}", MandateResolutionSubmissionResultDTO.class,
              requestId);
      if (sub == null || sub.getCompany() == null) {
        logger.error("Cannot load submission for requestId {}", requestId);
        return ResponseEntity.ok(
            "<page><error>Unable to save: submission not found.</error></page>");
      }
      Long companyId = sub.getCompany().getCompanyId();
      java.util.Map<Long, MandateResolutionSubmissionResultDTO.Account> existingById =
          new java.util.HashMap<>();
      if (sub.getAccounts() != null) {
        for (MandateResolutionSubmissionResultDTO.Account a : sub.getAccounts()) {
          existingById.put(a.getAccountId(), a);
        }
      }

      //Persist: PUT existing, POST new, DELETE removed
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

      java.util.Set<Long> keptExistingIds = new java.util.HashSet<>();

      //Upserts
      for (FormAcc fa : formAccounts) {
        Map<String, Object> dto = new java.util.LinkedHashMap<>();
        dto.put("companyId", companyId);
        dto.put("accountName", nz(fa.accountName));
        dto.put("accountNumber", nz(fa.accountNo));
        dto.put("isActive", Boolean.TRUE);
        dto.put("signatories", fa.signatories);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(dto, headers);

        if (fa.accountId != null && !fa.accountId.isBlank()) {
          Long accountId = Long.valueOf(fa.accountId);
          rt.exchange(base + "/api/account/{accountId}", HttpMethod.PUT, entity, Object.class,
              accountId);
          keptExistingIds.add(accountId);
          logger.info("Updated account {}", accountId);
        } else {
          var created = rt.postForEntity(base + "/api/account", entity, Object.class);
          logger.info("Created account; status={}", created.getStatusCode());
        }
      }

      //Deletes (anything that existed but isn’t in the form anymore)
      for (Long existingId : existingById.keySet()) {
        if (!keptExistingIds.contains(existingId)) {
          rt.delete(base + "/api/account/{accountId}", existingId);
          logger.info("Deleted account {}", existingId);
        }
      }

      // ===== DIRECTORS (Authorities) =====

      // 1) Parse rows from form: dirFirstName_#, dirSurname_#, dirDesignation_#, dirId_#
      java.util.regex.Pattern dirIdxPat =
          java.util.regex.Pattern.compile(
              "^(dirFirstName|dirSurname|dirDesignation|dirId)_(\\d+)$");
      java.util.SortedSet<Integer> dirIdxs = new java.util.TreeSet<>();
      for (String k : params.keySet()) {
        java.util.regex.Matcher m = dirIdxPat.matcher(k);
        if (m.matches()) {
          dirIdxs.add(Integer.parseInt(m.group(2)));
        }
      }

      class FormDir {
        String id;          // authorityId (blank/null for new)
        String firstName;
        String surname;
        String designation;
      }

      java.util.List<FormDir> formDirs = new java.util.ArrayList<>();
      for (Integer i : dirIdxs) {
        FormDir d = new FormDir();
        d.id = getParam(params, "dirId_" + i);
        d.firstName = getParam(params, "dirFirstName_" + i);
        d.surname = getParam(params, "dirSurname_" + i);
        d.designation = getParam(params, "dirDesignation_" + i);

        // skip rows that are completely blank
        if (!isAllBlank(d.firstName, d.surname, d.designation)) {
          formDirs.add(d);
        }
      }

      // 2) Build existing authorities map (by id) to detect deletes
      java.util.Map<Long, MandateResolutionSubmissionResultDTO.Authority> existingAuthById =
          new java.util.HashMap<>();
      if (sub.getAuthorities() != null) {
        for (MandateResolutionSubmissionResultDTO.Authority a : sub.getAuthorities()) {
          if (a != null && a.getAuthorityId() != null) {
            existingAuthById.put(a.getAuthorityId(), a);
          }
        }
      }

      java.util.Set<Long> keptAuthorityIds = new java.util.HashSet<>();

      // 3) Upserts (PUT for existing id, POST for new)
      for (FormDir d : formDirs) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("companyId", companyId);
        payload.put("firstname", nz(d.firstName));
        payload.put("surname", nz(d.surname));
        payload.put("designation", nz(d.designation));
        payload.put("isActive", Boolean.TRUE);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        if (d.id != null && !d.id.isBlank()) {
          Long id = Long.valueOf(d.id);
          rt.exchange(base + "/api/authority/{id}", HttpMethod.PUT, entity, Object.class, id);
          keptAuthorityIds.add(id);
          logger.info("Updated authority {}", id);
        } else {
          var created = rt.postForEntity(base + "/api/authority", entity, Object.class);
          logger.info("Created authority; status={}", created.getStatusCode());
        }
      }

// 4) Deletes (anything that existed but isn’t in the form anymore)
      for (Long id : existingAuthById.keySet()) {
        if (!keptAuthorityIds.contains(id)) {
          rt.delete(base + "/api/authority/{id}", id);
          logger.info("Deleted authority {}", id);
        }
      }

// Takes you to the View Request page once the edit is successful,
// and ALWAYS return valid XML to the UI.
      HttpSession session = request.getSession(false);
      ResponseEntity<String> view = displayViewRequest(requestId, session, request);
      String body = (view != null ? view.getBody() : null);

// If the body is null/blank or doesn't start with '<', wrap a valid <page> so XSLT can parse it.
      if (body == null || body.isBlank() || body.charAt(0) != '<') {
        body =
            "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            +
            "  <error>Unexpected response after save. Please try again.</error>"
            +
            "</page>";
      }

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_XML)
          .body(body);

    } catch (Exception e) {
      logger.error("Save edit failed for requestId {}: {}", requestId, e.getMessage(), e);
      String error = """
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <error>Unable to save changes for this request.</error>
          </page>
          """;
      return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(error);
    }
  }


//  @PostMapping(
//      value = "/file-upload",
//      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
//      produces = MediaType.APPLICATION_XML_VALUE
//  )
//  public ResponseEntity<String> handleFileUpload(
//      @RequestParam("file") MultipartFile uploadedFile,
//      @RequestParam(value = "registrationNumber") String registrationNumber,
//      HttpSession session
//  ) {
//    System.out.println("=== [file-upload] HIT ===");
//    System.out.println("File: " + (uploadedFile != null ? uploadedFile.getOriginalFilename() :
//    "NULL"));
//    System.out.println("Registration Number: " + registrationNumber);
//
//    if (uploadedFile == null || uploadedFile.isEmpty()) {
//      return ResponseEntity.ok("<page><error>No file provided</error></page>");
//    }
//
//    try {
//      ExtractedPdfDataDTO savedData = documentUploadClient.uploadFile(uploadedFile,
//      registrationNumber);
//      if (savedData != null) {
//        session.setAttribute("pdfSessionId", savedData.getPdfSessionId());
//        System.out.println("pdfSessionId set in session: " + savedData.getPdfSessionId());
//
//        RequestDTO dto = new RequestDTO();
//        dto.setPdfSessionId(savedData.getPdfSessionId());
//        dto.setRegistrationNumber(registrationNumber);
//        dto.setEditable(true);
//
//        pdfExtractionDataCache.put(dto.getPdfSessionId(), dto);
//      }
//      // IMPORTANT: Return lightweight <page>, not the next screen
//      return ResponseEntity.ok("""
//        <?xml version="1.0" encoding="UTF-8"?>
//        <page xmlns:comm="http://ws.online.fnb.co.za/v1/common/"
//              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//              id="upload"
//              template="message"
//              version="1">
//            <info>File uploaded successfully</info>
//        </page>
//        """);
//    } catch (Exception e) {
//      e.printStackTrace();
//      return ResponseEntity.ok("<page><error>Failed to upload file</error></page>");
//    }
//  }

  /**
   * STEP 2: Proceed after successful file upload
   * BiFrost submits the form again (without file). We render the actual XSL page here.
   */
  @PostMapping(
      value = "/proceedPdfExtraction",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
  )
  public ResponseEntity<Void> proceedPdfExtraction(
      @RequestParam(value = "registrationNumber", required = false) String registrationNumber,
      HttpSession session
  ) {
    System.out.println(">>> [proceedPdfExtraction] HIT <<<");

    try {
      String pdfSessionId = (String) session.getAttribute("pdfSessionId");
      if (pdfSessionId == null || pdfSessionId.isBlank()) {
        System.out.println("No file uploaded before proceeding.");
        return ResponseEntity.ok().build();
      }

      RequestDTO dto = pdfExtractionDataCache.get(pdfSessionId);
      if (dto == null) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
          System.out.println("Registration number is required.");
          return ResponseEntity.ok().build();
        }
        dto = new RequestDTO();
        dto.setPdfSessionId(pdfSessionId);
        dto.setRegistrationNumber(registrationNumber);
        dto.setEditable(true);
        pdfExtractionDataCache.put(pdfSessionId, dto);
      }

      System.out.println("PDF extraction started in background for session: " + pdfSessionId);

      return ResponseEntity.ok().build(); //

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Failed to process proceed step: " + e.getMessage());
      return ResponseEntity.ok().build();
    }
  }

  /**
   * Error handler for BiFrost if upload fails.
   */
  @PostMapping("/upload-error")
  public ResponseEntity<String> uploadError() {
    System.out.println(">>> [upload-error] File upload failed <<<");
    return ResponseEntity.ok("<page><error>Upload error</error></page>");
  }

  private String generateErrorPage(String message) {
    return """
        <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
        <page xmlns:comm="http://ws.online.fnb.co.za/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id=""
              heading=" "
              template="error"
              version="1">
            <error xsi:type="validationError">
                <name>login</name>
                <code>0</code>
                <message>%s</message>
            </error>
        </page>
        """.formatted(message);
  }

  /**
   * Helper used by all edit actions to build the XSL wrapper from backend data.
   */
  private RequestTableWrapper buildEditWrapper(Long requestId) {
    RestTemplate rt = new RestTemplate();

    // 1) Load submission (request, company, accounts + nested signatories if any)
    String submissionUrl = mandatesResolutionsDaoURL + "/api/submission/" + requestId;
    ResponseEntity<za.co.rmb.tts.mandates.resolutions.ui
        .model.dto.MandateResolutionSubmissionResultDTO> subResp =
        rt.getForEntity(submissionUrl,
            za.co.rmb.tts.mandates.resolutions.ui
                .model.dto.MandateResolutionSubmissionResultDTO.class);

    if (!subResp.getStatusCode().is2xxSuccessful() || subResp.getBody() == null) {
      throw new RuntimeException("Failed to fetch submission " + requestId);
    }
    var sub = subResp.getBody();

    // Helpers
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();
    java.util.function.Function<String, String> keyN = s -> s == null ? "" : s.trim().toUpperCase();
    java.util.function.BiFunction<String, String, String> accKey = (num, name) -> {
      String n = nz.apply(num);
      if (!n.isEmpty()) {
        return "NUM#" + n;
      }
      return "NAME#" + keyN.apply(name);
    };

    // 2) Seed accounts (keeps order)
    java.util.Map<String, AccountDTO> accountsByKey = new java.util.LinkedHashMap<>();
    if (sub.getAccounts() != null) {
      for (var a : sub.getAccounts()) {
        String k = accKey.apply(a.getAccountNumber(), a.getAccountName());
        AccountDTO bucket = accountsByKey.get(k);
        if (bucket == null) {
          bucket = new AccountDTO();
          bucket.setAccountName(nz.apply(a.getAccountName()));
          bucket.setAccountNumber(nz.apply(a.getAccountNumber()));
          bucket.setSignatories(new java.util.ArrayList<>());
          accountsByKey.put(k, bucket);
        }

        // Attach nested signatories if present
        try {
          if (a.getSignatories() != null) {
            for (var s : a.getSignatories()) {
              SignatoryDTO d = new SignatoryDTO();
              d.setFullName(nz.apply(s.getFullName()));
              d.setIdNumber(nz.apply(s.getIdNumber()));
              d.setInstructions(nz.apply(s.getInstructions()));
              d.setCapacity(nz.apply(s.getCapacity()));
              d.setGroupCategory(nz.apply(s.getGroupCategory()));
              d.setAccountName(bucket.getAccountName());
              d.setAccountNumber(bucket.getAccountNumber());
              bucket.getSignatories().add(d);
            }
          }
        } catch (NoSuchMethodError | RuntimeException ignored) {
          // If nested signatories are not present on Account from backend, rely on overlay
        }
      }
    }

    // 3) Overlay by request if DAO provides it
    try {
      String sigUrl = mandatesResolutionsDaoURL + "/api/signatory/byRequest/" + requestId;
      ResponseEntity<SignatoryDTO[]> sigResp = rt.getForEntity(sigUrl, SignatoryDTO[].class);
      if (sigResp.getStatusCode().is2xxSuccessful() && sigResp.getBody() != null) {
        for (SignatoryDTO s : sigResp.getBody()) {
          String k = accKey.apply(s.getAccountNumber(), s.getAccountName());
          AccountDTO bucket = accountsByKey.get(k);
          if (bucket == null) {
            bucket = new AccountDTO();
            bucket.setAccountNumber(nz.apply(s.getAccountNumber()));
            bucket.setAccountName(nz.apply(s.getAccountName()));
            bucket.setSignatories(new java.util.ArrayList<>());
            accountsByKey.put(k, bucket);
          }
          if ((s.getAccountName() == null
               || s.getAccountName().isBlank()) && bucket.getAccountName() != null) {
            s.setAccountName(bucket.getAccountName());
          }
          if ((s.getAccountNumber() == null
               || s.getAccountNumber().isBlank()) && bucket.getAccountNumber() != null) {
            s.setAccountNumber(bucket.getAccountNumber());
          }
          bucket.getSignatories().add(s);
        }
      }
    } catch (Exception ignored) {
      // intentionally empty
    }

    // 4) Build wrapper
    RequestTableDTO view = new RequestTableDTO();
    if (sub.getRequest() != null) {
      var r = sub.getRequest();

      view.setRequestId(r.getRequestId());
      view.setCompanyId(r.getCompanyId());
      view.setSla(r.getSla());
      view.setType(r.getType());
      view.setStatus(r.getStatus());
      view.setSubStatus(r.getSubStatus());
      view.setCreated(r.getCreated() != null ? r.getCreated().toString() : null);
      view.setUpdated(r.getUpdated() != null ? r.getUpdated().toString() : null);
      view.setProcessId(r.getProcessId());
      view.setAssignedUser(r.getAssignedUser());
      view.setRequestIdForDisplay(r.getRequestIdForDisplay());

      // --- Creator/Updator same fallback logic as View ---
      String creator = nz.apply(r.getCreator());
      String updator = nz.apply(r.getUpdator());
      if (creator == null || creator.isBlank() || "ui".equalsIgnoreCase(creator)) {
        creator = "UI";
      }
      if (updator == null || updator.isBlank() || "ui".equalsIgnoreCase(updator)) {
        updator = "UI";
      }
      view.setCreator(creator);
      view.setUpdator(updator);

      // ---- Directors (Authorities) ----
      if (sub.getAuthorities() != null) {
        var dirs = new java.util.ArrayList<DirectorDTO>();
        for (var a : sub.getAuthorities()) {
          if (a == null || Boolean.FALSE.equals(a.getIsActive())) {
            continue;
          }
          DirectorDTO dd = new DirectorDTO();
          dd.setAuthorityId(a.getAuthorityId());
          dd.setName(a.getFirstname() == null ? "" : a.getFirstname().trim());
          dd.setSurname(a.getSurname() == null ? "" : a.getSurname().trim());
          dd.setDesignation(a.getDesignation() == null ? "" : a.getDesignation().trim());
          dirs.add(dd);
        }
        view.setDirectors(dirs);
      }
    }

    view.setCompanyName(
        (sub.getCompany() != null && sub.getCompany().getName() != null)
            ? sub.getCompany().getName()
            : "Unknown"
    );

    view.setAccounts(new java.util.ArrayList<>(accountsByKey.values()));

    RequestTableWrapper wrapper = new RequestTableWrapper();
    wrapper.setRequest(java.util.List.of(view));
    return wrapper;
  }

  private static String extractInstruction(Object a) {
    String v = null;
    String[] cand =
          { "getInstructions", "getInstruction", "getChangeType", "getChange", "getStatus",
            "getAction"
          };
    for (String m : cand) {
      try {
        Object o = a.getClass().getMethod(m).invoke(a);
        if (o != null && !o.toString().trim().isEmpty()) {
          v = o.toString().trim();
          break;
        }
      } catch (Exception ignore) {
        // intentionally empty
      }
    }
    // If still blank, try to infer from isActive
    try {
      Object act = a.getClass().getMethod("getIsActive").invoke(a);
      if (v == null || v.isBlank()) {
        if (Boolean.FALSE.equals(act)) {
          return "Remove";
        }
        if (Boolean.TRUE.equals(act)) {
          return "Add";
        }
      }
    } catch (Exception ignore) {
      // intentionally empty
    }
    return (v == null || v.isBlank()) ? "Add" : v;
  }


  private void ensureAccounts(RequestDTO dto, int count) {
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }
    var list = dto.getAccounts();
    while (list.size() < count) {
      RequestDTO.Account a = new RequestDTO.Account();
      a.setAccountName("");
      setAccountNumber(a, "");
      a.setSignatories(new java.util.ArrayList<>());
      // seed with 1 empty RequestDTO.Signatory
      a.getSignatories().add(createBlankSignatory());
      list.add(a);
    }
  }

  // Prefer exact "both" pages BEFORE generic "resolution"/"mandate"
  private static String inferTypeFromPage(String pageCode) {
    if (pageCode == null) {
      return null;
    }
    String p = pageCode.toUpperCase();
    if (p.contains("MANDATE_RESOLUTION") || p.contains("BOTH")) {
      return "Both";
    }
    if (p.contains("RESOLUTION")) {
      return "Resolutions";
    }
    if (p.contains("MANDATE") || p.contains("ACC_DETAILS")) {
      return "Mandates";
    }
    return null;
  }

  private static void normalizeListsByType(RequestStagingDTO dto) {
    String t = dto.getRequestType();
    if (t == null) {
      return;
    }
    switch ((t == null ? "" : t.toUpperCase(Locale.ROOT))) {
      case "MANDATES" -> dto.setAuthorities(null);  // keep only Mandates
      case "RESOLUTIONS" -> dto.setAccounts(null);     // keep only Resolutions
      case "BOTH" ->
        {
        /* leave both lists as-is */
        }
      default ->
        {
        /* no-op or log/throw for unknown type */
        }
    }
  }

  private static String tryFormatIso(String text, java.time.format.DateTimeFormatter outFmt) {
    if (text == null || text.isBlank()) {
      return "";
    }
    try {
      return java.time.OffsetDateTime.parse(text).toLocalDateTime().format(outFmt);
    } catch (Exception ignore) {
      // intentionally empty
    }
    try {
      return java.time.LocalDateTime.parse(text).format(outFmt);
    } catch (Exception ignore) {
      // intentionally empty
    }
    try {
      return java.time.Instant.parse(text).atZone(java.time.ZoneId.systemDefault())
          .toLocalDateTime().format(outFmt);
    } catch (Exception ignore) {
      // intentionally empty
    }
    return text; // show raw if unknown format
  }

  private static String formatEpochMillis(long epoch, java.time.format.DateTimeFormatter outFmt) {
    if (String.valueOf(epoch).length() <= 10) {
      epoch = epoch * 1000L; // seconds -> millis
    }
    return java.time.Instant.ofEpochMilli(epoch)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDateTime()
        .format(outFmt);
  }

  private static boolean isAllDigits(String s) {
    if (s == null || s.isBlank()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }

  /**
   * Recursively search any object/array for a "created"-like field (case-insensitive).
   */
  private static String findCreatedAnyCase(com.fasterxml.jackson.databind.JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }

    if (node.isObject()) {
      java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> it =
          node.fields();
      while (it.hasNext()) {
        var e = it.next();
        String name = e.getKey();
        com.fasterxml.jackson.databind.JsonNode val = e.getValue();
        if (name != null) {
          String ln = name.toLowerCase();
          if (ln.equals("created") || ln.equals("createddate") || ln.equals("datecreated")
              || ln.equals("created_at") || ln.equals("createdat")) {
            if (val.isTextual()) {
              return val.asText();
            }
            if (val.isNumber()) {
              return String.valueOf(val.longValue());
            }
          }
        }
        String rec = findCreatedAnyCase(val);
        if (rec != null) {
          return rec;
        }
      }
    } else if (node.isArray()) {
      for (com.fasterxml.jackson.databind.JsonNode child : node) {
        String rec = findCreatedAnyCase(child);
        if (rec != null) {
          return rec;
        }
      }
    }
    return null;
  }

  private static String firstNonBlankText(com.fasterxml.jackson.databind.JsonNode root,
                                          String... keys) {
    for (String k : keys) {
      com.fasterxml.jackson.databind.JsonNode n = root.get(k);
      if (n != null && !n.isNull() && n.isTextual()) {
        String v = n.asText();
        if (v != null && !v.isBlank()) {
          return v;
        }
      }
    }
    return null;
  }

  private static Long firstNonNullLong(com.fasterxml.jackson.databind.JsonNode root,
                                       String... keys) {
    for (String k : keys) {
      com.fasterxml.jackson.databind.JsonNode n = root.get(k);
      if (n != null && !n.isNull() && n.isNumber()) {
        return n.longValue();
      }
    }
    return null;
  }


  private void ensureSignatories(RequestDTO.Account acc, int count) {
    if (acc.getSignatories() == null) {
      acc.setSignatories(new java.util.ArrayList<>());
    }
    var list = acc.getSignatories();
    while (list.size() < count) {
      list.add(createBlankSignatory());
    }
  }

  private void applyAccAndSigsEditsFromRequest(HttpServletRequest req, RequestDTO dto) {
    if (dto.getAccounts() == null) {
      dto.setAccounts(new java.util.ArrayList<>());
    }

    Integer accountCount = optInt(req.getParameter("accountCount"));
    if (accountCount != null && accountCount > 0) {
      ensureAccounts(dto, accountCount);
    }

    for (int i = 1; ; i++) {
      String name = req.getParameter("accountName_" + i);
      String no = req.getParameter("accountNo_" + i);
      if (name == null && no == null) {
        break;
      }

      ensureAccounts(dto, i);
      RequestDTO.Account acc = dto.getAccounts().get(i - 1);
      if (name != null) {
        acc.setAccountName(nz(name));
      }
      if (no != null) {
        setAccountNumber(acc, nz(no));
      }

      for (int j = 1; ; j++) {
        String fn = req.getParameter("fullName_" + i + "_" + j);
        String id = req.getParameter("idNumber_" + i + "_" + j);
        String ins = req.getParameter("instruction_" + i + "_" + j);
        String cp = req.getParameter("capacity_" + i + "_" + j);
        String gp = req.getParameter("group_" + i + "_" + j);
        if (fn == null && id == null && ins == null && cp == null && gp == null) {
          break;
        }

        ensureSignatories(acc, j);
        RequestDTO.Signatory s = acc.getSignatories().get(j - 1);
        if (fn != null) {
          s.setFullName(nz(fn));
        }
        if (id != null) {
          s.setIdNumber(nz(id));
        }
        if (ins != null) {
          s.setInstruction(nz(ins));  // singular
        }
        if (cp != null) {
          s.setCapacity(nz(cp));
        }
        if (gp != null) {
          s.setGroup(nz(gp));
        }
      }
    }

    String removeSigAt = req.getParameter("removeSignatoryAt");
    if (removeSigAt != null && removeSigAt.contains("_")) {
      String[] parts = removeSigAt.split("_");
      try {
        int ai = Math.max(1, Integer.parseInt(parts[0])) - 1;
        int si = Math.max(1, Integer.parseInt(parts[1])) - 1;
        var accs = dto.getAccounts();
        if (accs != null && ai >= 0 && ai < accs.size()) {
          var acc = accs.get(ai);
          if (acc.getSignatories() != null && si >= 0 && si < acc.getSignatories().size()) {
            acc.getSignatories().remove(si);
          }
        }
      } catch (Exception ignored) {
        // intentionally empty
      }
    }

    Integer removeAccAt = optInt(req.getParameter("removeAccountAt"));
    if (removeAccAt != null && removeAccAt > 0 && dto.getAccounts() != null) {
      int ai = removeAccAt - 1;
      if (ai >= 0 && ai < dto.getAccounts().size()) {
        dto.getAccounts().remove(ai);
      }
    }
  }

  private static String first(String[] arr) {
    return (arr == null || arr.length == 0) ? null : arr[0];
  }

  private static void updateSignatoriesFromParams(RequestStagingDTO dto,
                                                  java.util.Map<String, String[]> p) {
  }

  private static void updateSignatureCardExtras(RequestStagingDTO dto,
                                                java.util.Map<String, String[]> p) {
  }

  private static void updateUploadedDocs(RequestStagingDTO dto, java.util.Map<String, String[]> p) {
  }


  // ===== Logging helpers =====
  private static String head(String s, int max) {
    if (s == null) {
      return "null";
    }
    return (s.length() <= max) ? s :
        s.substring(0, max) + "\n... [truncated " + (s.length() - max) + " chars]";
  }

  private void dumpWrapperJson(String tag, Object wrapper, long requestId) {
    try {
      java.nio.file.Path p =
          java.nio.file.Files.createTempFile(tag + "-WRAPPER-" + requestId + "-", ".json");
      String json = new com.fasterxml.jackson.databind.ObjectMapper()
          .writerWithDefaultPrettyPrinter().writeValueAsString(wrapper);
      java.nio.file.Files.writeString(p, json);
      logger.warn("{} wrapper dumped to: {}", tag, p.toAbsolutePath());
    } catch (Exception ex) {
      logger.warn("{} wrapper dump failed: {}", tag, ex.toString());
    }
  }

  private void dumpPageXml(String tag, String pageXml, long requestId) {
    try {
      java.nio.file.Path p =
          java.nio.file.Files.createTempFile(tag + "-PAGE-" + requestId + "-", ".xml");
      java.nio.file.Files.writeString(p, pageXml);
      logger.warn("{} page XML dumped to: {}", tag, p.toAbsolutePath());
      logger.debug("{} rendered XML ({} chars):\n{}", tag, pageXml.length(), pageXml);
    } catch (Exception ex) {
      logger.warn("{} page dump failed: {}", tag, ex.toString());
    }
  }


  private static Path dumpText(String baseName, String ext, String content) {
    try {
      String fn = baseName + "-" + System.currentTimeMillis() + "." + ext;
      Path p = Paths.get(System.getProperty("java.io.tmpdir"), fn);
      Files.writeString(p, content == null ? "" : content, StandardCharsets.UTF_8);
      return p;
    } catch (Exception e) {
      // best-effort only
      return Paths.get("unknown");
    }
  }

  private static String toPrettyJson(Object o) {
    try {
      ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      return om.writeValueAsString(o);
    } catch (Exception e) {
      return String.valueOf(o);
    }
  }

  private void logWrapperAndPage(String label, Long requestId, RequestTableWrapper wrapper,
                                 String page) {
    try {
      // basic metrics
      int accCount = 0;
      int sigCount = 0;
      if (wrapper != null && wrapper.getRequest() != null && !wrapper.getRequest().isEmpty()) {
        var v = wrapper.getRequest().get(0);
        if (v.getAccounts() != null) {
          accCount = v.getAccounts().size();
          for (var a : v.getAccounts()) {
            if (a.getSignatories() != null) {
              sigCount += a.getSignatories().size();
            }
          }
        }
        if (v.getSignatories() != null) {
          sigCount += v.getSignatories().size();
        }
      }

      logger.info("{} render: requestId={}, accounts={}, signatories={}",
          label, requestId, accCount, sigCount);

      // pre-XSL (wrapper) as JSON
      String json = toPrettyJson(wrapper);
      logger.debug("{} wrapper JSON ({} chars):\n{}", label, json.length(), head(json, 1800));
      Path wfile = dumpText(label + "-WRAPPER-" + requestId, "json", json);
      logger.warn("{} wrapper dumped to: {}", label, wfile.toAbsolutePath());

      // rendered page (XML)
      logger.debug("{} rendered XML ({} chars):\n{}", label, (page == null ? 0 : page.length()),
          head(page, 1800));
      Path pfile = dumpText(label + "-PAGE-" + requestId, "xml", page);
      logger.warn("{} page XML dumped to: {}", label, pfile.toAbsolutePath());

    } catch (Exception ex) {
      logger.warn("logWrapperAndPage failed: {}", ex.toString());
    }
  }

  // Minimal XML escape (for short messages)
  private static String xmlEscape(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  // Build a valid ns1 page the renderer understands
  private static String renderSimpleErrorPage(String heading, String message, String backUrl) {
    return """
        <page xmlns:ns1="http://ws.online.fnb.co.za/v1/common/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id="errorPage" title="Error" template="main" version="1">
          <symbol xsi:type="ns1:formLayout">
            <ns1:form ns1:action="%3$s" ns1:name="errorForm">
              <ns1:sections ns1:align="left" ns1:width="full">
                <ns1:symbol xsi:type="ns1:textHeading">
                  <ns1:value>%1$s</ns1:value>
                </ns1:symbol>
              </ns1:sections>
              <ns1:sections ns1:align="left" ns1:width="full">
                <ns1:symbol xsi:type="ns1:text">
                  <ns1:value>%2$s</ns1:value>
                </ns1:symbol>
              </ns1:sections>
            </ns1:form>
          </symbol>
          <symbol xsi:type="ns1:footer" ns1:buttonAlign="right">
            <ns1:baseButton ns1:id="back"
                            ns1:url="%3$s"
                            ns1:label="Back"
                            ns1:formSubmit="false"
                            ns1:target="main"/>
          </symbol>
        </page>
        """.formatted(xmlEscape(heading), xmlEscape(message), xmlEscape(backUrl));
  }


  /**
   * Consistent XML error template generator
   */
  private String generateErrorPageFileUpload(String message) {
    return """
        <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
        <page xmlns:comm="http://ws.online.fnb.co.za/common/"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              id=""
              heading=" "
              template="error"
              version="1">
            <error xsi:type="validationError">
                <name>proceedPdfExtraction</name>
                <code>0</code>
                <message>%s</message>
            </error>
        </page>
        """.formatted(message);
  }

  // ======================= WORKFLOW STARTER =======================
  private void startWorkflow(Long companyId, String submittedBy) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String workflowUrl = "http://localhost:8082/workflow/start";

      Map<String, Object> variables = new HashMap<>();
      variables.put("companyId", companyId);
      variables.put("submittedBy", submittedBy);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("key", "mandatesResolutionsService");
      requestBody.put("data", variables);

      ResponseEntity<String> response =
          restTemplate.postForEntity(workflowUrl, requestBody, String.class);

      System.out.println("Workflow started, processId: " + response.getBody());
    } catch (Exception e) {
      System.err.println("Workflow failed: " + e.getMessage());
    }
  }

  // ======================= SUBMISSION MAPPER =======================

  /**
   * Build a submission payload (as Map) compatible with DAO service.
   * - Accounts are sent as a list.
   * - Signatories are flattened and carry their parent accountNumber for linkage.
   */

  // Helper: normalize a registration number for comparison (strip non-alnum, upper-case)
  private static String normReg(String s) {
    return (s == null) ? "" : s.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
  }

  private static String getParam(Map<String, String[]> params, String key) {
    String[] v = params.get(key);
    return (v != null && v.length > 0) ? v[0] : null;
  }

  private static boolean isAllBlank(String... vals) {
    if (vals == null) {
      return true;
    }
    for (String v : vals) {
      if (v != null && !v.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private boolean trySave(RestTemplate rt, String url, HttpMethod method, HttpEntity<?> entity) {
    try {
      ResponseEntity<String> resp = rt.exchange(url, method, entity, String.class);
      boolean ok = resp.getStatusCode().is2xxSuccessful();
      if (!ok) {
        logger.warn("Save attempt {} {} -> {} body={}", method, url, resp.getStatusCode(),
            resp.getBody());
      } else {
        logger.info("Save OK via {} {}", method, url);
      }
      return ok;
    } catch (org.springframework.web.client.HttpStatusCodeException ex) {
      logger.warn("Save attempt {} {} failed: {} {} body={}", method, url,
          ex.getStatusCode().value(), ex.getStatusText(), ex.getResponseBodyAsString());
      return false;
    } catch (Exception e) {
      logger.warn("Save attempt {} {} error: {}", method, url, e.toString());
      return false;
    }
  }

  /**
   * Treat untouched signatory rows as empty.
   */
  private static boolean isEmptySignatory(RequestDTO.Signatory s) {
    if (s == null) {
      return true;
    }
    return (s.getFullName() == null || s.getFullName().isBlank())
           && (s.getIdNumber() == null || s.getIdNumber().isBlank())
           && (s.getInstruction() == null || s.getInstruction().isBlank())
           && (s.getCapacity() == null || s.getCapacity().isBlank())
           && (s.getGroup() == null || s.getGroup().isBlank());
  }


  // ===== Session helpers (ALL RequestDTO.* types, no SignatoryDTO here) =====

  private static String nz(String s) {
    return s == null ? "" : s.trim();
  }

  private Integer optInt(String s) {
    try {
      return (s == null) ? null : Integer.valueOf(s.trim());
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * show "Saved" in table, but keep "@PAGE" in DB so we can reopen there
   */
  private static String cleanSubStatus(String sub) {
    if (sub == null) {
      return null;
    }
    int at = sub.indexOf('@');
    return (at >= 0) ? sub.substring(0, at) : sub;
  }

  /**
   * read the page token from "Saved@PAGE"; validate against known set
   */
  private static String extractLastPageCode(String sub) {
    if (sub == null) {
      return null;
    }
    int at = sub.indexOf('@');
    if (at < 0 || at == sub.length() - 1) {
      return null;
    }
    String code = sub.substring(at + 1).trim();
    java.util.Set<String> known = java.util.Set.of(
        "SEARCH_RESULTS",
        "MANDATES_AUTOFILL",
        "RESOLUTION_AUTOFILL",
        "ACC_DETAILS",
        "DIRECTORS_DETAILS",
        "MANDATES_SIGNATURE_CARD",
        "MANDATES_RESOLUTIONS_SIGNATURE_CARD"
    );
    return known.contains(code) ? code : null;
  }

  private void ensureLists(RequestDTO dto) {
    if (dto.getDirectors() == null) {
      dto.setDirectors(new java.util.ArrayList<>());
    }
    if (dto.getDocumentumTools() == null) {
      dto.setDocumentumTools(new java.util.ArrayList<>());
    }
    if (dto.getResolutionDocs() == null) {
      dto.setResolutionDocs(new java.util.ArrayList<>());
    }
  }

  private void ensureAtLeastOneDirector(RequestDTO dto) {
    ensureLists(dto);
    if (dto.getDirectors().isEmpty()) {
      dto.getDirectors().add(new RequestDTO.Director());
    }
  }

  private static String dedupeComma(String s) {
    String t = nz(s);
    if (t.isEmpty()) {
      return t;
    }
    String[] parts = t.split("\\s*,\\s*");
    java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
    for (String p : parts) {
      if (!p.isBlank()) {
        set.add(p);
      }
    }
    return String.join(", ", set);
  }

  private static String mapRequestType(String raw) {
    if (raw == null) {
      return null;
    }
    String s = raw.trim();
    if (s.isEmpty()) {
      return null;
    }

    // Numeric codes from the XSL (authoritative)
    if (s.equals("1")) {
      return "Mandates";
    }
    if (s.equals("2")) {
      return "Resolutions";
    }
    if (s.equals("3")) {
      return "Both";
    }

    // Common letters / synonyms / casing
    String u = s.toUpperCase();
    if (u.equals("M")) {
      return "Mandates";
    }
    if (u.equals("R")) {
      return "Resolutions";
    }
    if (u.equals("B")) {
      return "Both";
    }

    // Text values
    if (u.startsWith("MANDATE") && !u.contains("RESOLUTION")) {
      return "Mandates";
    }
    if (u.startsWith("RESOLUTION")) {
      return "Resolutions";
    }
    if (u.contains("BOTH") || u.contains("AND")) {
      return "Both";
    }

    // Unknown → leave null so later inference (page / lists) can decide
    return null;
  }

  /**
   * Map canonical type text -> UI dropdown code.
   */
  private static String mapTypeToCode(String type) {
    if (type == null) {
      return "";
    }
    String u = type.trim().toUpperCase();
    if (u.startsWith("MAN")) {
      return "1";
    }
    if (u.startsWith("RES")) {
      return "2";
    }
    if (u.startsWith("BOTH")) {
      return "3";
    }
    // if it is already "1|2|3" return as-is
    if (u.equals("1") || u.equals("2") || u.equals("3")) {
      return type.trim();
    }
    return "";
  }


  private static String normalizePageCode(String code) {
    if (code == null) {
      return null;
    }
    String c = code.trim().toUpperCase();
    return switch (c) {
      case "RESOLUTIONS_FILL" -> "RESOLUTION_AUTOFILL";
      default -> c;
    };
  }

  private static boolean allBlank(String... ss) {
    if (ss == null) {
      return true;
    }
    for (String s : ss) {
      if (s != null && !s.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  /**
   * Accepts directors[i].name|surname|designation OR directors[i].name0/surname0/designation0
   */
  private static java.util.List<RequestDTO.Director> parseDirectorsFromParamsSuffix(
      Map<String, String[]> params) {
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(
        "^directors\\[(\\d+)]\\.(name|surname|designation)(\\d+)?$");
    java.util.Map<Integer, RequestDTO.Director> byIdx = new java.util.TreeMap<>();
    params.forEach((key, values) -> {
      if (values == null || values.length == 0) {
        return;
      }
      String v = values[0] == null ? "" : values[0].trim();

      java.util.regex.Matcher m = p.matcher(key);
      if (!m.matches()) {
        return;
      }

      int idx = Integer.parseInt(m.group(1));
      String field = m.group(2);

      RequestDTO.Director d = byIdx.computeIfAbsent(idx, i -> new RequestDTO.Director());
      switch (field) {
        case "name" -> d.setName(v);
        case "surname" -> d.setSurname(v);
        case "designation" -> d.setDesignation(v);
        default ->
          {
          /* no-op */
          }
      }
    });
    java.util.List<RequestDTO.Director> out = new java.util.ArrayList<>();
    for (RequestDTO.Director d : byIdx.values()) {
      boolean allBlank =
          (d.getName() == null || d.getName().isBlank())
          && (d.getSurname() == null || d.getSurname().isBlank())
          && (d.getDesignation() == null || d.getDesignation().isBlank());
      if (!allBlank) {
        out.add(d);
      }
    }
    return out;
  }


  /**
   * Parse directors from raw params across all page variants.
   */
  private static java.util.List<RequestDTO.Director> parseDirectorsFromParamsGeneric(
      Map<String, String[]> params) {
    // Helpers
    java.util.function.Function<String[], String> lastNonBlank = arr -> {
      if (arr == null || arr.length == 0) {
        return null;
      }
      for (int i = arr.length - 1; i >= 0; i--) {
        String v = arr[i];
        if (v != null && !v.trim().isEmpty()) {
          return v.trim();
        }
      }
      String tail = arr[arr.length - 1];
      return tail == null ? null : tail.trim();
    };
    java.util.function.Function<String, String> nz = s -> s == null ? "" : s.trim();

    // Patterns:
    // A) directors[0].name  (directors[0].name0)
    java.util.regex.Pattern bracketPattern = java.util.regex.Pattern.compile(
        "^directors\\[(\\d+)]\\.(name|surname|designation)(\\d+)?$"
    );
    // B) directorName_1 / directorSurname_1 / directorDesignation_1  (1-based index)
    java.util.regex.Pattern underscorePattern = java.util.regex.Pattern.compile(
        "^director(Name|Surname|Designation)_(\\d+)$"
    );

    java.util.Map<Integer, RequestDTO.Director> byIndex = new java.util.TreeMap<>(); // keep order

    for (var e : params.entrySet()) {
      String key = e.getKey();
      String val = lastNonBlank.apply(e.getValue());
      if (val == null) {
        continue;
      }

      java.util.regex.Matcher bracketMatcher = bracketPattern.matcher(key);
      if (bracketMatcher.matches()) {
        int idx = Integer.parseInt(bracketMatcher.group(1)); // 0-based from UI table
        String field = bracketMatcher.group(2);

        RequestDTO.Director d = byIndex.computeIfAbsent(idx, i -> new RequestDTO.Director());
        switch (field) {
          case "name" -> d.setName(nz.apply(val));
          case "surname" -> d.setSurname(nz.apply(val));
          case "designation" -> d.setDesignation(nz.apply(val));
          default ->
            {
            /* intentionally ignore unknown fields */
            }
        }
        continue;
      }

      java.util.regex.Matcher underscoreMatcher = underscorePattern.matcher(key);
      if (underscoreMatcher.matches()) {
        String which = underscoreMatcher.group(1);   // Name | Surname | Designation
        int idx1 = Integer.parseInt(underscoreMatcher.group(2));
        int idx = Math.max(0, idx1 - 1);             // convert to 0-based

        RequestDTO.Director d = byIndex.computeIfAbsent(idx, i -> new RequestDTO.Director());
        switch (which) {
          case "Name" -> d.setName(nz.apply(val));
          case "Surname" -> d.setSurname(nz.apply(val));
          case "Designation" -> d.setDesignation(nz.apply(val));
          default ->
            {
            /* intentionally ignore unknown fields */
            }
        }
      }
    }

    // Build ordered list, drop fully blank rows
    java.util.List<RequestDTO.Director> out = new java.util.ArrayList<>();
    for (var entry : byIndex.entrySet()) {
      RequestDTO.Director d = entry.getValue();
      if (d == null) {
        continue;
      }
      boolean blank = (d.getName() == null || d.getName().isBlank())
                      && (d.getSurname() == null || d.getSurname().isBlank())
                      && (d.getDesignation() == null || d.getDesignation().isBlank());
      if (!blank) {
        out.add(d);
      }
    }
    return out;
  }

  /**
   * Collect any shape of documentumTools params into a clean, distinct list.
   */
  private static java.util.List<String> parseDocumentumToolsFromParams(
      java.util.Map<String, String[]> params) {
    java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
    for (java.util.Map.Entry<String, String[]> e : params.entrySet()) {
      String key = e.getKey();
      if (key == null) {
        continue;
      }

      // Standard MVC array binding: documentumTools and documentumTools[0], [1], ...
      if (key.equals("documentumTools") || key.startsWith("documentumTools[")) {
        for (String v : e.getValue()) {
          if (v != null && !v.isBlank()) {
            out.add(v.trim());
          }
        }
      }

      //Support a repeated single key "documentumTool"
      if (key.equals("documentumTool")) {
        for (String v : e.getValue()) {
          if (v != null && !v.isBlank()) {
            out.add(v.trim());
          }
        }
      }
    }
    return new java.util.ArrayList<>(out);
  }

  /**
   * Split a CSV safely into a list (trim, dedupe, drop blanks).
   */
  private static java.util.List<String> csvToList(String csv) {
    java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
    if (csv != null) {
      for (String part : csv.split("\\s*,\\s*")) {
        if (part != null && !part.isBlank()) {
          set.add(part.trim());
        }
      }
    }
    return new java.util.ArrayList<>(set);
  }

  /**
   * Return the framework error page (template="error") with your message.
   */
  private ResponseEntity<String> validationError(String message) {
    String body =
        "<page xmlns:comm=\"http://ws.online.fnb.co.za/common/\" "
        +
        "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        +
        "      id=\"\" heading=\" \" template=\"error\" version=\"1\">"
        +
        "  <error xsi:type=\"validationError\">"
        +
        "    <name>confirmationCheck</name>"
        +
        "    <code>0</code>"
        +
        "    <message>" + escapeXml(message) + "</message>"
        +
        "  </error>"
        +
        "</page>";
    return ResponseEntity
        .ok()
        .contentType(MediaType.TEXT_XML)   // engine expects XML here
        .body(body);
  }

  /**
   * minimal XML text escaper
   */
  private static String escapeXml(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&apos;");
  }

  private ResponseEntity<String> renderCreateRequestWithInlineNotFound(HttpSession session,
                                                                       String reg) {
    // ensure session id
    String pdfSessionId = (String) session.getAttribute("pdfSessionId");
    if (pdfSessionId == null || pdfSessionId.isBlank()) {
      pdfSessionId = java.util.UUID.randomUUID().toString();
      session.setAttribute("pdfSessionId", pdfSessionId);
    }

    RequestDTO dto = new RequestDTO();
    dto.setPdfSessionId(pdfSessionId);
    dto.setRegistrationNumber(reg == null ? "" : reg.trim());
    dto.setErrorMessage("Company registration number not found.");
    dto.setEditable(true);

    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);

    String page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), wrapper);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_XML)
        .body(page);
  }


  // Parse accounts & nested signatories posted by any of the XSL pages
  private List<RequestStagingDTO.AccountDraft> parseAccountsFromParams(
      Map<String, String[]> params) {

    // pick the last non-blank value (mirrors how browsers may post duplicates)
    final java.util.function.Function<String[], String> pick = arr -> {
      if (arr == null || arr.length == 0) {
        return "";
      }
      String best = null;
      for (String v : arr) {
        if (v != null && !v.trim().isEmpty()) {
          best = v.trim();
        }
      }
      if (best != null) {
        return best;
      }
      String tail = arr[arr.length - 1];
      return tail == null ? "" : tail.trim();
    };

    // --- Patterns (precompiled) ---
    final Pattern accNamePat = Pattern.compile("^accountName_(\\d+)$");
    final Pattern accNoPat = Pattern.compile("^accountNo_(\\d+)$");
    final Pattern accNumberPat = Pattern.compile("^accountNumber_(\\d+)$"); // alt key support

    final Pattern fullNamePat = Pattern.compile("^fullName_(\\d+)_(\\d+)$");
    final Pattern idNumPat = Pattern.compile("^idNumber_(\\d+)_(\\d+)$");
    final Pattern instrPat = Pattern.compile("^instruction_(\\d+)_(\\d+)$");
    final Pattern capacityPat = Pattern.compile("^capacity_(\\d+)_(\\d+)$");
    final Pattern groupPat = Pattern.compile("^group_(\\d+)_(\\d+)$");

    // Keep natural order by account index, and signatory index
    Map<Integer, RequestStagingDTO.AccountDraft> accountsByIdx = new TreeMap<>();
    Map<Integer, Map<Integer, RequestStagingDTO.SignatoryDraft>> signsByAcc = new HashMap<>();

    for (Map.Entry<String, String[]> e : params.entrySet()) {
      String key = e.getKey();
      String val = pick.apply(e.getValue());
      Matcher m;

      // accountName_#
      m = accNamePat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        RequestStagingDTO.AccountDraft acc =
            accountsByIdx.computeIfAbsent(i, k -> new RequestStagingDTO.AccountDraft());
        acc.setAccountName(val);
        if (acc.getIsActive() == null) {
          acc.setIsActive(Boolean.TRUE);
        }
        continue;
      }

      // accountNo_# (UI) or accountNumber_# (alt)
      m = accNoPat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        RequestStagingDTO.AccountDraft acc =
            accountsByIdx.computeIfAbsent(i, k -> new RequestStagingDTO.AccountDraft());
        acc.setAccountNumber(val);
        if (acc.getIsActive() == null) {
          acc.setIsActive(Boolean.TRUE);
        }
        continue;
      }
      m = accNumberPat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        RequestStagingDTO.AccountDraft acc =
            accountsByIdx.computeIfAbsent(i, k -> new RequestStagingDTO.AccountDraft());
        acc.setAccountNumber(val);
        if (acc.getIsActive() == null) {
          acc.setIsActive(Boolean.TRUE);
        }
        continue;
      }

      // fullName_#_#
      m = fullNamePat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        int j = Integer.parseInt(m.group(2));
        RequestStagingDTO.SignatoryDraft sd = signsByAcc
            .computeIfAbsent(i, k -> new TreeMap<>())
            .computeIfAbsent(j, k -> new RequestStagingDTO.SignatoryDraft());
        sd.setFullName(val);
        if (sd.getIsActive() == null) {
          sd.setIsActive(Boolean.TRUE);
        }
        continue;
      }

      // idNumber_#_#
      m = idNumPat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        int j = Integer.parseInt(m.group(2));
        RequestStagingDTO.SignatoryDraft sd = signsByAcc
            .computeIfAbsent(i, k -> new TreeMap<>())
            .computeIfAbsent(j, k -> new RequestStagingDTO.SignatoryDraft());
        sd.setIdNumber(val);
        if (sd.getIsActive() == null) {
          sd.setIsActive(Boolean.TRUE);
        }
        continue;
      }

      // instruction_#_#
      m = instrPat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        int j = Integer.parseInt(m.group(2));
        RequestStagingDTO.SignatoryDraft sd = signsByAcc
            .computeIfAbsent(i, k -> new TreeMap<>())
            .computeIfAbsent(j, k -> new RequestStagingDTO.SignatoryDraft());
        sd.setInstructions(val); // staging field is "instructions"
        if (sd.getIsActive() == null) {
          sd.setIsActive(Boolean.TRUE);
        }
        continue;
      }

      // capacity_#_#
      m = capacityPat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        int j = Integer.parseInt(m.group(2));
        RequestStagingDTO.SignatoryDraft sd = signsByAcc
            .computeIfAbsent(i, k -> new TreeMap<>())
            .computeIfAbsent(j, k -> new RequestStagingDTO.SignatoryDraft());
        sd.setCapacity(val);
        if (sd.getIsActive() == null) {
          sd.setIsActive(Boolean.TRUE);
        }
        continue;
      }

      // group_#_#
      m = groupPat.matcher(key);
      if (m.matches()) {
        int i = Integer.parseInt(m.group(1));
        int j = Integer.parseInt(m.group(2));
        RequestStagingDTO.SignatoryDraft sd = signsByAcc
            .computeIfAbsent(i, k -> new TreeMap<>())
            .computeIfAbsent(j, k -> new RequestStagingDTO.SignatoryDraft());
        sd.setGroupCategory(val);
        if (sd.getIsActive() == null) {
          sd.setIsActive(Boolean.TRUE);
        }
        continue;
      }
    }

    // Attach non-empty signatories to each account (and prune empty rows)
    for (Map.Entry<Integer, Map<Integer, RequestStagingDTO.SignatoryDraft>> accEntry :
        signsByAcc.entrySet()) {
      int i = accEntry.getKey();
      RequestStagingDTO.AccountDraft acc =
          accountsByIdx.computeIfAbsent(i, k -> new RequestStagingDTO.AccountDraft());
      List<RequestStagingDTO.SignatoryDraft> clean = new ArrayList<>();
      for (RequestStagingDTO.SignatoryDraft sd : accEntry.getValue().values()) {
        boolean emptyRow =
            isBlank(sd.getFullName())
            && isBlank(sd.getIdNumber())
            && isBlank(sd.getInstructions()); // capacity/group are optional
        if (!emptyRow) {
          clean.add(sd);
        }
      }
      acc.setSignatories(clean.isEmpty() ? null : clean);
      if (acc.getIsActive() == null) {
        acc.setIsActive(Boolean.TRUE);
      }
    }

    // Build final ordered list, dropping accounts that are truly empty
    List<RequestStagingDTO.AccountDraft> out = new ArrayList<>();
    for (RequestStagingDTO.AccountDraft acc : accountsByIdx.values()) {
      boolean hasAnything =
          !isBlank(acc.getAccountName())
          || !isBlank(acc.getAccountNumber())
          || (acc.getSignatories() != null && !acc.getSignatories().isEmpty());
      if (hasAnything) {
        if (acc.getIsActive() == null) {
          acc.setIsActive(Boolean.TRUE);
        }
        out.add(acc);
      }
    }

    logger.debug("[parseAccountsFromParams] detected accounts: {}", accountsByIdx.keySet());
    logger.debug("[parseAccountsFromParams] returning {} account(s)", out.size());
    return out;
  }

  private static String normalizeSelCode(String raw) {
    if (raw == null) {
      return null;
    }
    String r = raw.trim();
    if (r.isEmpty()) {
      return null;
    }

    //Already numeric
    if ("1".equals(r) || "2".equals(r) || "3".equals(r)) {
      return r;
    }

    switch (r.toUpperCase()) {
      case "MANDATE":
      case "MANDATES":
      case "M":
        return "1";
      case "RESOLUTION":
      case "RESOLUTIONS":
      case "R":
        return "2";
      case "BOTH":
      case "MANDATE & RESOLUTION":
      case "MANDATE AND RESOLUTION":
      case "M+R":
      case "MR":
        return "3";
      default:
        return null;
    }
  }

  private static String toTypeFromCode(String code) {
    return switch (code) {
      case "1" -> "MANDATE";
      case "2" -> "RESOLUTION";
      case "3" -> "BOTH";
      default -> null;
    };
  }

  /**
   * Prefer username, then employeeNumber, then numeric userId; then servlet principal; then "UI"
   */
  private String currentDisplayId(HttpSession session, HttpServletRequest request) {
    za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO u =
        (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO) session.getAttribute(
            "currentUser");

    String id = "";
    if (u != null) {
      if (u.getUsername() != null && !u.getUsername().isBlank()) {
        id = u.getUsername().trim();
      } else if (u.getEmployeeNumber() != null && !u.getEmployeeNumber().isBlank()) {
        id = u.getEmployeeNumber().trim();
      } else if (u.getUserId() != null) {
        id = String.valueOf(u.getUserId()); // convert Long -> String before using
      }
    }

    if (id.isEmpty() && request != null && request.getUserPrincipal() != null) {
      id = nz(request.getUserPrincipal().getName());
    }
    return id.isEmpty() ? "UI" : id;
  }

  //Users only see the ticket they created
  private String loggedInUsername(HttpSession session, HttpServletRequest req) {
    var u = (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO)
        session.getAttribute("currentUser");
    String un = (u == null) ? "" : nz(u.getUsername());
    return !un.isEmpty() ? un : nz(currentDisplayId(session, req));
  }

  private boolean isAdmin(HttpSession session) {
    var u = (za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO)
        session.getAttribute("currentUser");
    String role =
        (u == null || u.getUserRole() == null) ? "" : u.getUserRole().trim().toUpperCase();
    return role.contains("ADMIN");
  }

  // ---- Wrappers so existing zero-arg calls keep working ----

  // /requestTable
  private ResponseEntity<String> goToDisplayRequestTable() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = (attrs == null) ? null : attrs.getRequest();
    HttpSession sess = (req == null) ? null : req.getSession(false);
    return displayInProgressRequests(sess, req); // delegate to the annotated method
  }

  // /requestTableOnHold
  private ResponseEntity<String> goToDisplayRequestTableOnHold() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = (attrs == null) ? null : attrs.getRequest();
    HttpSession sess = (req == null) ? null : req.getSession(false);
    return displayRequestTableOnHold(sess, req);
  }

  // /requestTableCompleted
  private ResponseEntity<String> goToDisplayRequestTableCompleted() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest req = (attrs == null) ? null : attrs.getRequest();
    HttpSession sess = (req == null) ? null : req.getSession(false);
    return displayRequestTableCompleted(sess, req);
  }

  private RequestWrapper buildAutoFillWrapperFromStaging(Long stagingId, String pdfSessionId) {
    final String base = mandatesResolutionsDaoURL;
    RestTemplate rt = new RestTemplate();

    RequestDTO ui = new RequestDTO();
    ui.setPdfSessionId(nz(pdfSessionId));
    ui.setStagingId(stagingId);

    if (stagingId != null) {
      RequestStagingDTO s = rt.getForObject(base + "/api/request-staging/{id}",
          RequestStagingDTO.class, stagingId);

      if (s != null) {
        //Company
        ui.setCompanyName(nz(s.getCompanyName()));
        ui.setCompanyAddress(nz(s.getCompanyAddress()));
        ui.setRegistrationNumber(nz(s.getCompanyRegistrationNumber()));

        //Dropdown "1|2|3" mapped from text Mandates|Resolutions|Both
        ui.setMandateResolution(unmapRequestTypeToDropdown(s.getRequestType()));

        //Waiver tools -> hydrate as list for XSL
        ui.setDocumentumTools(csvToList(nz(s.getWaiverPermittedTools())));

        //Authorities -> Directors (needed for Mandates AutoFill)
        if (s.getAuthorities() != null && !s.getAuthorities().isEmpty()) {
          java.util.List<RequestDTO.Director> directors = new java.util.ArrayList<>();
          for (var a : s.getAuthorities()) {
            if (a == null || Boolean.FALSE.equals(a.getIsActive())) {
              continue;
            }
            RequestDTO.Director d = new RequestDTO.Director();
            d.setName(nz(a.getFirstname()));
            d.setSurname(nz(a.getSurname()));
            d.setDesignation(nz(a.getDesignation()));
            directors.add(d);
          }
          if (!directors.isEmpty()) {
            ui.setDirectors(directors);
          }
        }

        //Accounts + signatories
        if (s.getAccounts() != null) {
          java.util.List<RequestDTO.Account> accs = new java.util.ArrayList<>();
          for (var a : s.getAccounts()) {
            if (a == null || Boolean.FALSE.equals(a.getIsActive())) {
              continue;
            }

            RequestDTO.Account ua = new RequestDTO.Account();
            ua.setAccountName(nz(a.getAccountName()));
            setAccountNumber(ua, nz(a.getAccountNumber())); //Sets accountNo in UI DTO

            java.util.List<RequestDTO.Signatory> sigs = new java.util.ArrayList<>();
            if (a.getSignatories() != null) {
              for (var sd : a.getSignatories()) {
                if (sd == null || Boolean.FALSE.equals(sd.getIsActive())) {
                  continue;
                }

                RequestDTO.Signatory us = new RequestDTO.Signatory();
                us.setFullName(nz(sd.getFullName()));
                us.setIdNumber(nz(sd.getIdNumber()));
                us.setInstruction(nz(sd.getInstructions())); //"ADD"/"REMOVE"
                us.setCapacity(nz(sd.getCapacity()));
                us.setGroup(nz(sd.getGroupCategory()));
                sigs.add(us);
              }
            }
            if (sigs.isEmpty()) {
              sigs.add(createBlankSignatory());
            }
            ua.setSignatories(sigs);
            accs.add(ua);
          }
          ui.setAccounts(accs);
        }
      }
    }

    RequestWrapper w = new RequestWrapper();
    w.setRequest(ui);
    //Attach LOVs (Instructions)
    RequestWrapper.LovsDTO lovs = new RequestWrapper.LovsDTO();
    lovs.setInstructions(fetchLovValues("Dropdown", "Instructions"));
    w.setLovs(lovs);
    return w;
  }

  // For Search Results page (company details + directors + dropdown value)
  private RequestWrapper buildSearchResultsWrapperFromStaging(Long stagingId, String pdfSessionId) {
    final String base = mandatesResolutionsDaoURL;
    RestTemplate rt = new RestTemplate();

    RequestStagingDTO s =
        rt.getForObject(base + "/api/request-staging/{id}", RequestStagingDTO.class, stagingId);
    if (s == null) {
      s = new RequestStagingDTO();
    }

    RequestDTO req = new RequestDTO();
    req.setPdfSessionId(pdfSessionId);
    req.setStagingId(s.getStagingId());
    req.setRegistrationNumber(s.getCompanyRegistrationNumber());
    req.setCompanyName(s.getCompanyName());
    req.setCompanyAddress(s.getCompanyAddress());

    //  Pre-select dropdown using the code "1|2|3"
    req.setMandateResolution(mapTypeToCode(s.getRequestType()));

    //  Waiver tools -> documentumTools (plural setter; elements render as <documentumTool>)
    java.util.List<String> tools = new java.util.ArrayList<>();
    if (s.getWaiverPermittedTools() != null && !s.getWaiverPermittedTools().isBlank()) {
      for (String t : s.getWaiverPermittedTools().split(",")) {
        String tt = (t == null) ? "" : t.trim();
        if (!tt.isBlank()) {
          tools.add(tt);
        }
      }
    }
    req.setDocumentumTools(tools);

    //  Authorities -> Directors using RequestDTO.Director (NOT DirectorDTO)
    java.util.List<RequestDTO.Director> dirs = new java.util.ArrayList<>();
    if (s.getAuthorities() != null) {
      for (RequestStagingDTO.AuthorityDraft a : s.getAuthorities()) {
        if (a == null || Boolean.FALSE.equals(a.getIsActive())) {
          continue;
        }
        RequestDTO.Director d = new RequestDTO.Director();
        d.setName(a.getFirstname() == null ? "" : a.getFirstname().trim());
        d.setSurname(a.getSurname() == null ? "" : a.getSurname().trim());
        d.setDesignation(a.getDesignation() == null ? "" : a.getDesignation().trim());
        boolean blank =
            (d.getName().isBlank() && d.getSurname().isBlank() && d.getDesignation().isBlank());
        if (!blank) {
          dirs.add(d);
        }
      }
    }
    req.setDirectors(dirs);

    // Editable is boolean
    req.setEditable(true);

    RequestWrapper w = new RequestWrapper();
    w.setRequest(req);
    return w;
  }

  // "Mandate"|"Resolution"|"Mandate and resolution" -> "1"|"2"|"3"
  private static String unmapRequestTypeToDropdown(String type) {
    if (type == null) {
      return null;
    }
    switch (type.trim().toLowerCase()) {
      case "mandate":
        return "1";
      case "resolution":
        return "2";
      case "mandate and resolution":
        return "3";
      default:
        return null;
    }
  }

  // Fallback: reconstruct directors from request params if binder missed them
  private static List<RequestDTO.Director> parseDirectorsFromParams(Map<String, String[]> params) {
    // matches: directors[0].name   OR directors[0].name0  (same for surname/designation)
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(
        "^directors\\[(\\d+)]\\.(name|surname|designation)(\\d+)?$"
    );

    java.util.Map<Integer, RequestDTO.Director> byIndex = new java.util.HashMap<>();

    params.forEach((k, v) -> {
      java.util.regex.Matcher m = p.matcher(k);
      if (!m.matches()) {
        return;
      }

      int idx = Integer.parseInt(m.group(1));
      String field = m.group(2);
      String value = (v != null && v.length > 0) ? v[0] : null;

      RequestDTO.Director d = byIndex.computeIfAbsent(idx, i -> new RequestDTO.Director());
      if ("name".equals(field)) {
        d.setName(value);
      } else if ("surname".equals(field)) {
        d.setSurname(value);
      } else if ("designation".equals(field)) {
        d.setDesignation(value);
      }
    });

    // build ordered list, drop fully blank rows
    return byIndex.entrySet().stream()
        .sorted(java.util.Map.Entry.comparingByKey())
        .map(java.util.Map.Entry::getValue)
        .filter(d -> d != null && !(isAllBlank(d.getName(), d.getSurname(), d.getDesignation())))
        .toList();
  }


  // Some schemas use accountNo, some accountNumber – support both
  private void setAccountNumber(RequestDTO.Account a, String v) {
    try {
      a.getClass().getMethod("setAccountNo", String.class).invoke(a, v);
      return;
    } catch (Exception ignored) {
      // intentionally empty
    }
    try {
      a.getClass().getMethod("setAccountNumber", String.class).invoke(a, v);
    } catch (Exception ignored) {
      // intentionally empty
    }
  }

  private String getAccountNumber(RequestDTO.Account a) {
    try {
      return (String) a.getClass().getMethod("getAccountNo").invoke(a);
    } catch (Exception ignored) {
      // intentionally empty
    }
    try {
      return (String) a.getClass().getMethod("getAccountNumber").invoke(a);
    } catch (Exception ignored) {
      // intentionally empty
    }
    return "";
  }

  // --- factories (RequestDTO.*) ---
  private RequestDTO.Signatory createBlankSignatory() {
    RequestDTO.Signatory s = new RequestDTO.Signatory();
    s.setFullName("");
    s.setIdNumber("");
    s.setInstruction("");
    s.setCapacity("");
    s.setGroup("");
    return s;
  }

  private RequestDTO.Account createBlankAccount() {
    RequestDTO.Account a = new RequestDTO.Account();
    a.setAccountName("");
    setAccountNumber(a, "");
    a.setSignatories(new java.util.ArrayList<>(java.util.List.of(createBlankSignatory())));
    return a;
  }

  private RequestDTO.Director createBlankDirector() {
    RequestDTO.Director d = new RequestDTO.Director();
    d.setName("");
    d.setSurname("");
    d.setDesignation("");
    return d;
  }

  // For multivalued form fields, pick the last non-blank
  private static String lastNonBlank(HttpServletRequest req, String name) {
    String[] vals = req.getParameterValues(name);
    if (vals == null || vals.length == 0) {
      return null;
    }
    String best = null;
    for (String v : vals) {
      if (v != null && !v.trim().isEmpty()) {
        best = v.trim();
      }
    }
    return (best != null) ? best : vals[vals.length - 1];
  }


  // --- parse accounts/signatories posted by the XSL ---
  private java.util.List<RequestDTO.Account> parseAccountsFromRequest(HttpServletRequest request) {
    var accNamePat = java.util.regex.Pattern.compile("^accountName_(\\d+)$");
    var accNoPat = java.util.regex.Pattern.compile("^accountNo_(\\d+)$");
    var fullNamePat = java.util.regex.Pattern.compile("^fullName_(\\d+)_(\\d+)$");
    var idNumPat = java.util.regex.Pattern.compile("^idNumber_(\\d+)_(\\d+)$");
    var instrPat = java.util.regex.Pattern.compile("^instruction_(\\d+)_(\\d+)$");
    var capacityPat = java.util.regex.Pattern.compile("^capacity_(\\d+)_(\\d+)$");
    var groupPat = java.util.regex.Pattern.compile("^group_(\\d+)_(\\d+)$");

    java.util.SortedSet<Integer> accountIdxs = new java.util.TreeSet<>();
    for (String p : request.getParameterMap().keySet()) {
      if (accNamePat.matcher(p).matches()) {
        accountIdxs.add(Integer.parseInt(accNamePat.matcher(p).replaceAll("$1")));
      }
      if (accNoPat.matcher(p).matches()) {
        accountIdxs.add(Integer.parseInt(accNoPat.matcher(p).replaceAll("$1")));
      }
      var m = fullNamePat.matcher(p);
      if (m.matches()) {
        accountIdxs.add(Integer.parseInt(m.group(1)));
      }
      m = idNumPat.matcher(p);
      if (m.matches()) {
        accountIdxs.add(Integer.parseInt(m.group(1)));
      }
      m = instrPat.matcher(p);
      if (m.matches()) {
        accountIdxs.add(Integer.parseInt(m.group(1)));
      }
      m = capacityPat.matcher(p);
      if (m.matches()) {
        accountIdxs.add(Integer.parseInt(m.group(1)));
      }
      m = groupPat.matcher(p);
      if (m.matches()) {
        accountIdxs.add(Integer.parseInt(m.group(1)));
      }
    }

    java.util.List<RequestDTO.Account> out = new java.util.ArrayList<>();
    for (Integer idx : accountIdxs) {
      RequestDTO.Account a = new RequestDTO.Account();
      String nm = lastNonBlank(request, "accountName_" + idx);
      String no = lastNonBlank(request, "accountNo_" + idx);
      if (nm != null) {
        a.setAccountName(nm.trim());
      }
      if (no != null) {
        setAccountNumber(a, no.trim());
      }
      a.setSignatories(new java.util.ArrayList<>());

      java.util.SortedSet<Integer> sortedIdx = new java.util.TreeSet<>();
      for (String paramName : request.getParameterMap().keySet()) {
        var fullNameMatcher = fullNamePat.matcher(paramName);
        var idNumMatcher = idNumPat.matcher(paramName);
        var instrMatcher = instrPat.matcher(paramName);
        var capacityMatcher = capacityPat.matcher(paramName);
        var groupMatcher = groupPat.matcher(paramName);

        if (fullNameMatcher.matches() && Integer.parseInt(fullNameMatcher.group(1)) == idx) {
          sortedIdx.add(Integer.parseInt(fullNameMatcher.group(2)));
        }
        if (idNumMatcher.matches() && Integer.parseInt(idNumMatcher.group(1)) == idx) {
          sortedIdx.add(Integer.parseInt(idNumMatcher.group(2)));
        }
        if (instrMatcher.matches() && Integer.parseInt(instrMatcher.group(1)) == idx) {
          sortedIdx.add(Integer.parseInt(instrMatcher.group(2)));
        }
        if (capacityMatcher.matches() && Integer.parseInt(capacityMatcher.group(1)) == idx) {
          sortedIdx.add(Integer.parseInt(capacityMatcher.group(2)));
        }
        if (groupMatcher.matches() && Integer.parseInt(groupMatcher.group(1)) == idx) {
          sortedIdx.add(Integer.parseInt(groupMatcher.group(2)));
        }
      }

      for (Integer s : sortedIdx) {
        RequestDTO.Signatory sig = new RequestDTO.Signatory();
        String fn = lastNonBlank(request, "fullName_" + idx + "_" + s);
        String id = lastNonBlank(request, "idNumber_" + idx + "_" + s);
        String in = lastNonBlank(request, "instruction_" + idx + "_" + s);
        String cp = lastNonBlank(request, "capacity_" + idx + "_" + s);
        String gp = lastNonBlank(request, "group_" + idx + "_" + s);
        if (fn != null) {
          sig.setFullName(fn.trim());
        }
        if (id != null) {
          sig.setIdNumber(id.trim());
        }
        if (in != null) {
          sig.setInstruction(in.trim()); // << singular
        }
        if (cp != null) {
          sig.setCapacity(cp.trim());
        }
        if (gp != null) {
          sig.setGroup(gp.trim());       // << group
        }
        a.getSignatories().add(sig);
      }

      if (a.getSignatories().isEmpty()) {
        a.getSignatories().add(createBlankSignatory());
      }
      out.add(a);
    }
    return out;
  }

  private static String resolvePage(HttpServletRequest req, String pageCodeArg) {
    String page = null;

    // Prefer the last non-blank value from the raw params if duplicates exist
    String[] arr = req.getParameterValues("pageCode");
    if (arr != null && arr.length > 0) {
      for (String v : arr) {
        if (v != null && !v.isBlank()) {
          page = v.trim();
        }
      }
    }

    // Fallback to method argument
    if ((page == null || page.isBlank()) && pageCodeArg != null && !pageCodeArg.isBlank()) {
      page = pageCodeArg.trim();
    }

    return (page == null || page.isBlank()) ? "SEARCH_RESULTS" : page;
  }


  // --- merge accounts by position (non-blank only) ---
  private void mergeAccounts(RequestDTO target, java.util.List<RequestDTO.Account> parsed) {
    if (parsed == null || parsed.isEmpty()) {
      return;
    }
    if (target.getAccounts() == null) {
      target.setAccounts(new java.util.ArrayList<>());
    }

    for (int i = 0; i < parsed.size(); i++) {
      RequestDTO.Account src = parsed.get(i);
      while (target.getAccounts().size() <= i) {
        target.getAccounts().add(createBlankAccount());
      }
      RequestDTO.Account dst = target.getAccounts().get(i);

      if (nz(src.getAccountName()).length() > 0) {
        dst.setAccountName(nz(src.getAccountName()));
      }
      String srcNo = getAccountNumber(src);
      if (nz(srcNo).length() > 0) {
        setAccountNumber(dst, srcNo);
      }

      if (src.getSignatories() != null && !src.getSignatories().isEmpty()) {
        if (dst.getSignatories() == null) {
          dst.setSignatories(new java.util.ArrayList<>());
        }
        while (dst.getSignatories().size() < src.getSignatories().size()) {
          dst.getSignatories().add(createBlankSignatory());
        }

        for (int s = 0; s < src.getSignatories().size(); s++) {
          RequestDTO.Signatory ss = src.getSignatories().get(s);
          RequestDTO.Signatory dd = dst.getSignatories().get(s);
          if (nz(ss.getFullName()).length() > 0) {
            dd.setFullName(nz(ss.getFullName()));
          }
          if (nz(ss.getIdNumber()).length() > 0) {
            dd.setIdNumber(nz(ss.getIdNumber()));
          }
          if (nz(ss.getInstruction()).length() > 0) {
            dd.setInstruction(nz(ss.getInstruction())); // << singular
          }
          if (nz(ss.getCapacity()).length() > 0) {
            dd.setCapacity(nz(ss.getCapacity()));
          }
          if (nz(ss.getGroup()).length() > 0) {
            dd.setGroup(nz(ss.getGroup()));             // << group
          }
        }
      }
    }
  }

  // --- directors parse/merge ---
  private java.util.List<RequestDTO.Director> parseDirectorsFromRequest(
      HttpServletRequest request) {
    java.util.regex.Pattern namePat = java.util.regex.Pattern.compile("^directorName_(\\d+)$");
    java.util.regex.Pattern surPat = java.util.regex.Pattern.compile("^directorSurname_(\\d+)$");
    java.util.regex.Pattern desPat =
        java.util.regex.Pattern.compile("^directorDesignation_(\\d+)$");
    java.util.regex.Pattern insPat =
        java.util.regex.Pattern.compile("^directorInstruction_(\\d+)$"); // NEW

    java.util.SortedSet<Integer> idxs = new java.util.TreeSet<>();
    for (String p : request.getParameterMap().keySet()) {
      java.util.regex.Matcher m1 = namePat.matcher(p);
      java.util.regex.Matcher m2 = surPat.matcher(p);
      java.util.regex.Matcher m3 = desPat.matcher(p);
      java.util.regex.Matcher m4 = insPat.matcher(p); // NEW
      if (m1.matches()) {
        idxs.add(Integer.parseInt(m1.group(1)));
      }
      if (m2.matches()) {
        idxs.add(Integer.parseInt(m2.group(1)));
      }
      if (m3.matches()) {
        idxs.add(Integer.parseInt(m3.group(1)));
      }
      if (m4.matches()) {
        idxs.add(Integer.parseInt(m4.group(1))); // NEW
      }
    }

    java.util.List<RequestDTO.Director> out = new java.util.ArrayList<>();
    for (Integer i : idxs) {
      RequestDTO.Director d = new RequestDTO.Director();
      String nm = request.getParameter("directorName_" + i);
      String sn = request.getParameter("directorSurname_" + i);
      String dg = request.getParameter("directorDesignation_" + i);
      String in = request.getParameter("directorInstruction_" + i); // NEW
      if (nm != null) {
        d.setName(nm.trim());
      }
      if (sn != null) {
        d.setSurname(sn.trim());
      }
      if (dg != null) {
        d.setDesignation(dg.trim());
      }
      if (in != null) {
        d.setInstruction(in.trim());
      }
      out.add(d);
    }
    return out;
  }


  private void mergeDirectorsByPosition(RequestDTO target,
                                        java.util.List<RequestDTO.Director> parsed) {
    if (parsed == null || parsed.isEmpty()) {
      return;
    }
    if (target.getDirectors() == null) {
      target.setDirectors(new java.util.ArrayList<>());
    }
    java.util.List<RequestDTO.Director> dst = target.getDirectors();
    for (int i = 0; i < parsed.size(); i++) {
      while (dst.size() <= i) {
        dst.add(createBlankDirector());
      }
      RequestDTO.Director s = parsed.get(i);
      RequestDTO.Director d = dst.get(i);
      if (nz(s.getName()).length() > 0) {
        d.setName(nz(s.getName()));
      }
      if (nz(s.getSurname()).length() > 0) {
        d.setSurname(nz(s.getSurname()));
      }
      if (nz(s.getDesignation()).length() > 0) {
        d.setDesignation(nz(s.getDesignation()));
      }
      if (nz(s.getInstruction()).length() > 0) {
        d.setInstruction(nz(s.getInstruction()));
      }
    }
  }

  private java.util.List<String> fetchLovValues(String type, String subType) {
    final String base = mandatesResolutionsDaoURL;
    RestTemplate rt = new RestTemplate();
    try {
      ListOfValuesDTO[] arr = rt.getForObject(
          base + "/api/lov?type={t}&subType={s}",
          ListOfValuesDTO[].class,
          type, subType
      );
      java.util.List<String> out = new java.util.ArrayList<>();
      if (arr != null) {
        for (var it : arr) {
          if (it != null && (it.getIsActive() == null || Boolean.TRUE.equals(it.getIsActive()))) {
            String v = it.getValue();
            if (v != null && !v.isBlank()) {
              out.add(v.trim());
            }
          }
        }
      }
      // Guaranteed Add/Remove even if DAO returns nothing
      if (out.isEmpty()) {
        out = java.util.List.of("Add", "Remove");
      }
      return out;
    } catch (Exception ex) {
      logger.warn("LOV fetch failed for {}/{}: {}", type, subType, ex.toString());
      return java.util.List.of("Add", "Remove");
    }
  }

  // ===== Allowed sub-statuses & initial mapping =====
  private static final java.util.Set<String> ALLOWED_SUBSTATUSES = new java.util.HashSet<>(
      java.util.Arrays.asList(
          "Hogan Verification Pending",
          "Windeed Verification Pending",
          "Hanis Verification Pending",
          "Admin Approval Pending",
          "Hogan Update Pending",
          "Documentum Update Pending"
      )
  );

  /**
   * Pick the very first subStatus right after creation.
   * If you later want to vary by type ("Mandates"|"Resolutions"|"Both"), branch on the 'type' here.
   */
  private static String initialSubStatusForType(String type) {
    return "Hogan Verification Pending";
  }

  // ======================= UI -> Backend Submission Mapper =======================
  private SubmissionPayload buildSubmissionPayload(RequestDTO ui, String requestTypeRaw) {
    SubmissionPayload out = new SubmissionPayload();

    // --- Company ---
    SubmissionPayload.Company c = new SubmissionPayload.Company();
    c.setRegistrationNumber(nz(ui.getRegistrationNumber()));
    c.setName(nz(ui.getCompanyName()));
    c.setAddress(nz(ui.getCompanyAddress()));
    c.setCreator(nz(ui.getLoggedInUsername()));
    c.setUpdator(nz(ui.getLoggedInUsername()));
    out.setCompany(c);

    // --- Request (normalize "1|2|3" -> "Mandates|Resolutions|Both") ---
    String typeNorm = mapRequestType(requestTypeRaw);
    if (typeNorm == null) {
      String t = nz(requestTypeRaw).toLowerCase();
      if (t.contains("both")) {
        typeNorm = BackendEnums.TYPE_BOTH;
      } else if (t.contains("resol")) {
        typeNorm = BackendEnums.TYPE_RESOLUTIONS;
      } else if (t.contains("mandate")) {
        typeNorm = BackendEnums.TYPE_MANDATES;
      } else {
        typeNorm = BackendEnums.TYPE_MANDATES; // default
      }
    }

    // Ensure a non-blank creator (also used for assignedUser)
    String creator = nz(ui.getLoggedInUsername());
    if (creator.isBlank()) {
      creator = nz(ui.getLoggedInEmail());
    }
    if (creator.isBlank()) {
      creator = "UI_USER";
    }

    SubmissionPayload.Request r = new SubmissionPayload.Request();
    r.setSla(3);

    // ***** IMPORTANT: exact strings expected by DAO validation *****
    r.setStatus("In Progress");
    r.setType(typeNorm);

    // SubStatus: use UI value if already allowed, else choose an initial valid one
    String uiSub = nz(ui.getSubStatus());
    String startSub =
        ALLOWED_SUBSTATUSES.contains(uiSub) ? uiSub : initialSubStatusForType(typeNorm);
    r.setSubStatus(startSub);

    // Outcome should be null on create; DAO allows null for processOutcome
    r.setCreator(creator);
    r.setAssignedUser(creator);
    out.setRequest(r);

    // --- Accounts + nested signatories ---
    java.util.List<SubmissionPayload.Account> payloadAccounts = new java.util.ArrayList<>();

    if (ui.getAccounts() != null && !ui.getAccounts().isEmpty()) {
      for (RequestDTO.Account a : ui.getAccounts()) {
        if (a == null) {
          continue;
        }

        SubmissionPayload.Account pa = new SubmissionPayload.Account();
        pa.setAccountName(nz(a.getAccountName()));
        pa.setAccountNumber(nz(getAccountNumber(a)));
        pa.setCreator(creator);
        pa.setUpdator(creator);

        java.util.List<SubmissionPayload.Signatory> ps = new java.util.ArrayList<>();
        if (a.getSignatories() != null) {
          for (RequestDTO.Signatory s : a.getSignatories()) {
            if (s == null) {
              continue;
            }
            if (isEmptySignatory(s)) {
              continue;
            }

            SubmissionPayload.Signatory ns = new SubmissionPayload.Signatory();
            ns.setFullName(nz(s.getFullName()));
            ns.setIdNumber(nz(s.getIdNumber()));
            ns.setInstructions(normalizeInstruction(nz(s.getInstruction()))); // "Add" | "Remove"
            ns.setCapacity(nz(s.getCapacity()));
            ns.setGroupCategory(nz(s.getGroup()));
            ns.setInstructionsDate(java.time.LocalDateTime.now());
            ns.setCreator(creator);
            ns.setUpdator(creator);
            ps.add(ns);
          }
        }
        pa.setSignatories(ps);

        if (!nz(pa.getAccountName()).isEmpty()
            || !nz(pa.getAccountNumber()).isEmpty()
            || (ps != null && !ps.isEmpty())) {
          payloadAccounts.add(pa);
        }
      }
    }

    // Fallback: legacy flat signatories -> single "Unknown" account
    if (payloadAccounts.isEmpty() && ui.getSignatories() != null && !ui.getSignatories()
        .isEmpty()) {
      SubmissionPayload.Account pa = new SubmissionPayload.Account();
      pa.setAccountName("Unknown");
      pa.setAccountNumber("");
      pa.setCreator(creator);
      pa.setUpdator(creator);

      java.util.List<SubmissionPayload.Signatory> ps = new java.util.ArrayList<>();
      for (RequestDTO.Signatory s : ui.getSignatories()) {
        if (s == null) {
          continue;
        }
        if (isEmptySignatory(s)) {
          continue;
        }

        SubmissionPayload.Signatory ns = new SubmissionPayload.Signatory();
        ns.setFullName(nz(s.getFullName()));
        ns.setIdNumber(nz(s.getIdNumber()));
        ns.setInstructions(normalizeInstruction(nz(s.getInstruction())));
        ns.setCapacity(nz(s.getCapacity()));
        ns.setGroupCategory(nz(s.getGroup()));
        ns.setInstructionsDate(java.time.LocalDateTime.now());
        ns.setCreator(creator);
        ns.setUpdator(creator);
        ps.add(ns);
      }
      pa.setSignatories(ps);
      payloadAccounts.add(pa);
    }

    out.setAccounts(payloadAccounts);

    // --- Authorities (Directors) ---
    boolean hasDirectorsInput =
        ui.getDirectors() != null && ui.getDirectors().stream().anyMatch(d ->
            d != null && !(nz(d.getName()).isEmpty()
                           && nz(d.getSurname()).isEmpty()
                           && nz(d.getDesignation()).isEmpty())
        );

    boolean includeAuthorities =
        BackendEnums.TYPE_RESOLUTIONS.equals(typeNorm)
        || BackendEnums.TYPE_BOTH.equals(typeNorm)
        || (BackendEnums.TYPE_MANDATES.equals(typeNorm) && hasDirectorsInput);

    java.util.List<SubmissionPayload.Authority> payloadAuthorities = new java.util.ArrayList<>();
    if (includeAuthorities && ui.getDirectors() != null) {
      for (RequestDTO.Director d : ui.getDirectors()) {
        if (d == null) {
          continue;
        }
        boolean allBlank = nz(d.getName()).isEmpty()
                           && nz(d.getSurname()).isEmpty()
                           && nz(d.getDesignation()).isEmpty();
        if (allBlank) {
          continue;
        }

        SubmissionPayload.Authority a = new SubmissionPayload.Authority();
        a.setFirstname(nz(d.getName()));
        a.setSurname(nz(d.getSurname()));
        a.setDesignation(nz(d.getDesignation()));
        a.setInstructions(normalizeInstruction(nz(d.getInstruction()))); // "Add" | "Remove"
        a.setCreator(creator);
        a.setUpdator(creator);
        payloadAuthorities.add(a);
      }
    }
    out.setAuthorities(payloadAuthorities);

    int accCount = out.getAccounts() == null ? 0 : out.getAccounts().size();
    int sigCount = out.getAccounts() == null ? 0
        : out.getAccounts().stream()
            .mapToInt(a -> a.getSignatories() == null ? 0 : a.getSignatories().size())
            .sum();

    logger.info(
        "buildSubmissionPayload -> type={}, status={}, subStatus={}, accounts={}, signatories"
        + "(total)={}, authorities(directors)={}",
        typeNorm, r.getStatus(), r.getSubStatus(), accCount, sigCount, payloadAuthorities.size());

    return out;
  }

  private static final class BackendEnums {
    static final String TYPE_MANDATES = "Mandates";
    static final String TYPE_RESOLUTIONS = "Resolutions";
    static final String TYPE_BOTH = "Both";

    static final String STATUS_DRAFT = "Draft";
    static final String STATUS_IN_PROGRESS = "In Progress";
    static final String STATUS_COMPLETED = "Completed";
    static final String STATUS_ON_HOLD = "On Hold";
  }

  //View Request UnHold/Hold helpers
  private static String nonNull(String s) {
    return s == null ? "" : s;
  }

  private void persistStatusOnly(RestTemplate rt, Long requestId, String newStatus) {
    //Load current request to read existing subStatus (won’t change it)
    String submissionUrl = mandatesResolutionsDaoURL + "/api/submission/" + requestId;
    var subResp = rt.getForEntity(
        submissionUrl,
        za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO.class
    );
    if (!subResp.getStatusCode().is2xxSuccessful() || subResp.getBody() == null
        || subResp.getBody().getRequest() == null) {
      throw new RuntimeException("Failed to fetch submission/request " + requestId);
    }

    var reqDto = subResp.getBody().getRequest();
    String existingSubStatus = reqDto.getSubStatus();

    //Utility: build a minimal payload map
    java.util.Map<String, Object> minimal = new java.util.HashMap<>();
    minimal.put("requestId", requestId);
    minimal.put("status", newStatus);
    //Only include subStatus if the backend already has a value we received
    if (existingSubStatus != null && !existingSubStatus.isBlank()) {
      minimal.put("subStatus", existingSubStatus);
    }

    HttpHeaders hdr = new HttpHeaders();
    hdr.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Object> patchBody = new HttpEntity<>(java.util.Map.of("status", newStatus), hdr);
    HttpEntity<Object> smallBody = new HttpEntity<>(minimal, hdr);

    String base = mandatesResolutionsDaoURL;
    java.util.List<String> errors = new java.util.ArrayList<>();

    //1) PATCH /api/request/{id}
    try {
      RestTemplate patchRt = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
      patchRt.exchange(base + "/api/request/" + requestId, HttpMethod.PATCH, patchBody, Void.class);
      return;
    } catch (org.springframework.web.client.HttpStatusCodeException e) {
      errors.add(
          "PATCH " + base + "/api/request/" + requestId + " -> " + e.getRawStatusCode() + " : "
          + e.getResponseBodyAsString());
    } catch (Exception e) {
      errors.add(
          "PATCH " + base + "/api/request/" + requestId + " -> " + e.getClass().getSimpleName()
          + " : " + (e.getMessage() == null ? "" : e.getMessage()));
    }

    //2) PUT /api/request/{id} with a *small* body (status + existing subStatus if present)
    try {
      rt.exchange(base + "/api/request/" + requestId, HttpMethod.PUT, smallBody, Void.class);
      return;
    } catch (org.springframework.web.client.HttpStatusCodeException e) {
      errors.add("PUT " + base + "/api/request/" + requestId + " -> " + e.getRawStatusCode() + " : "
                 + e.getResponseBodyAsString());
    } catch (Exception e) {
      errors.add("PUT " + base + "/api/request/" + requestId + " -> " + e.getClass().getSimpleName()
                 + " : " + (e.getMessage() == null ? "" : e.getMessage()));
    }

    //3) POST /api/request (upsert) with same small body
    try {
      rt.exchange(base + "/api/request", HttpMethod.POST, smallBody, Void.class);
      return;
    } catch (org.springframework.web.client.HttpStatusCodeException e) {
      errors.add("POST " + base + "/api/request -> " + e.getRawStatusCode() + " : "
                 + e.getResponseBodyAsString());
    } catch (Exception e) {
      errors.add("POST " + base + "/api/request -> " + e.getClass().getSimpleName() + " : " + (
          e.getMessage() == null ? "" : e.getMessage()));
    }

    throw new RuntimeException(
        "Could not persist status='" + newStatus + "'. Tried:\n" + String.join("\n", errors));
  }

  /**
   * Map any raw instruction to backend-valid ones ("Add" or "Remove"). Defaults to Add.
   */
  private static String normalizeInstruction(String raw) {
    if (raw == null || raw.trim().isEmpty()) {
      return "Add";
    }
    String t = raw.trim().toLowerCase();
    return switch (t) {
      case "add", "a", "+", "new", "create" -> "Add";
      case "remove", "r", "-", "delete", "del" -> "Remove";
      default -> "Add"; // be permissive
    };
  }

  public final class DisplayIds {
    private DisplayIds() {
    }

    public static String format(Long requestId, String type) {
      if (requestId == null) {
        return null;
      }
      String prefix = "REQ";
      if (type != null) {
        String t = type.trim().toUpperCase();
        if (t.startsWith("MANDATE")) {
          prefix = "MAN";
        } else if (t.startsWith("RESOLUTION")) {
          prefix = "RES";
        } else if (t.startsWith("BOTH")) {
          prefix = "MR";
        }
      }
      return prefix + " - " + String.format("%04d", requestId);
    }
  }

  // ===== DAO-LEGAL "Pending" subStatus values  =====
  private static final String SS_WINDEED_VER = "Windeed Verification Pending";
  private static final String SS_HOGAN_VER = "Hogan Verification Pending";
  private static final String SS_HANIS_VER = "Hanis Verification Pending";
  private static final String SS_ADMIN_APPROVAL = "Admin Approval Pending";
  private static final String SS_HOGAN_UPD = "Hogan Update Pending";
  private static final String SS_DOCU_UPD = "Documentum Update Pending";
  private static final String SS_DONE = "Request Updated Successfully";
  private static final String SS_REJECTED = "Rejected";

  // ===== DAO-LEGAL "On Hold" labels  =====
// NOTE: DAO regex uses lowercase 'on' in "Update on Hold for ..."
  private static final String HOLD_WINDEED_VER = "Verification On Hold for Windeed";
  private static final String HOLD_HOGAN_VER = "Verification On Hold for Hogan";
  private static final String HOLD_HANIS_VER = "Verification On Hold for Hanis";
  private static final String HOLD_ADMIN = "Admin Approval On Hold";
  private static final String HOLD_HOGAN_UPD = "Update on Hold for Hogan";
  private static final String HOLD_DOCU_UPD = "Update on Hold for Documentum";

  /**
   * Map any legacy/typo variants to DAO-legal values BEFORE we branch or PUT.
   * Kept intentionally focused on PENDING states. We add a safe pass-through
   * for already-legal HOLD labels.
   */
  private static String canonical(String s) {
    if (s == null) {
      return "";
    }
    String t = s.trim();
    if (t.isEmpty()) {
      return t;
    }

    // ---- Legacy/typo fixes
    if (t.equalsIgnoreCase("Hannis Verification Pending")) {
      return SS_HANIS_VER;
    }
    if (t.equalsIgnoreCase("Admin Verification Pending")) {
      return SS_ADMIN_APPROVAL; // Verification -> Approval
    }
    if (t.equalsIgnoreCase("Submitted")) {
      return SS_HOGAN_VER;
    }
    if (t.equalsIgnoreCase("Completed") || t.equalsIgnoreCase("Request Completed")) {
      return SS_DONE;
    }

    // ---- Normalize to exact DAO strings (Pending values) ----
    if (t.equalsIgnoreCase(SS_REJECTED)) {
      return SS_REJECTED;
    }
    if (t.equalsIgnoreCase(SS_HOGAN_VER)) {
      return SS_HOGAN_VER;
    }
    if (t.equalsIgnoreCase(SS_WINDEED_VER)) {
      return SS_WINDEED_VER;
    }
    if (t.equalsIgnoreCase(SS_HANIS_VER)) {
      return SS_HANIS_VER;
    }
    if (t.equalsIgnoreCase(SS_ADMIN_APPROVAL)) {
      return SS_ADMIN_APPROVAL;
    }
    if (t.equalsIgnoreCase(SS_HOGAN_UPD)) {
      return SS_HOGAN_UPD;
    }
    if (t.equalsIgnoreCase(SS_DOCU_UPD)) {
      return SS_DOCU_UPD;
    }
    if (t.equalsIgnoreCase(SS_DONE)) {
      return SS_DONE;
    }

    //Pass-through if caller accidentally supplies a DAO-legal HOLD label ----
    if (t.equalsIgnoreCase(HOLD_WINDEED_VER)
        ||
        t.equalsIgnoreCase(HOLD_HOGAN_VER)
        ||
        t.equalsIgnoreCase(HOLD_HANIS_VER)
        ||
        t.equalsIgnoreCase(HOLD_ADMIN)
        ||
        t.equalsIgnoreCase(HOLD_HOGAN_UPD)
        ||
        t.equalsIgnoreCase(HOLD_DOCU_UPD)) {
      return t;
    }

    //Unknown: return as-is
    return t;
  }

  /**
   * Given the current PENDING subStatus, return the exact On-Hold label accepted by the DAO.
   * If current is unknown, default to "Verification On Hold for Hogan".
   */
  private static String toHoldLabel(String currentSubStatus) {
    String c = canonical(currentSubStatus);
    if (c == null || c.isBlank()) {
      return HOLD_HOGAN_VER; //safe default hold bucket
    }
    switch (c) {
      case SS_WINDEED_VER:
        return HOLD_WINDEED_VER;
      case SS_HOGAN_VER:
        return HOLD_HOGAN_VER;
      case SS_HANIS_VER:
        return HOLD_HANIS_VER;
      case SS_ADMIN_APPROVAL:
        return HOLD_ADMIN;
      case SS_HOGAN_UPD:
        return HOLD_HOGAN_UPD;   //"Update on Hold for Hogan"
      case SS_DOCU_UPD:
        return HOLD_DOCU_UPD;    //Update on Hold for Documentum"

      case SS_DONE:
      case SS_REJECTED:
      default:
        //Park under Admin approval hold if the "pending" doesn't map cleanly
        return HOLD_ADMIN;
    }
  }

  /**
   * Inverse of toHoldLabel: derive the original PENDING subStatus to restore when unholding.
   * If label is unexpected, fall back to "Hogan Verification Pending".
   */
  private static String fromHoldLabel(String holdLabel) {
    if (holdLabel == null) {
      return SS_HOGAN_VER;
    }
    String s = holdLabel.trim();

    return switch (s) {
      case HOLD_WINDEED_VER -> SS_WINDEED_VER;
      case HOLD_HOGAN_VER -> SS_HOGAN_VER;
      case HOLD_HANIS_VER -> SS_HANIS_VER;
      case HOLD_ADMIN -> SS_ADMIN_APPROVAL;
      case HOLD_HOGAN_UPD -> SS_HOGAN_UPD;   // "Update on Hold for Hogan"
      case HOLD_DOCU_UPD -> SS_DOCU_UPD;    // "Update on Hold for Documentum"
      default -> SS_HOGAN_VER;   // sensible default
    };
  }

  /**
   * Approve path state machine (exact order you specified).
   */
  private static String nextSubStatus(String current, boolean approve) {
    String c = canonical(current);
    if (approve) {
      if (SS_HOGAN_VER.equals(c)) {
        return SS_WINDEED_VER;
      }
      if (SS_WINDEED_VER.equals(c)) {
        return SS_HANIS_VER;
      }
      if (SS_HANIS_VER.equals(c)) {
        return SS_ADMIN_APPROVAL;
      }
      if (SS_ADMIN_APPROVAL.equals(c)) {
        return SS_HOGAN_UPD;
      }
      if (SS_HOGAN_UPD.equals(c)) {
        return SS_DOCU_UPD;
      }
      if (SS_DOCU_UPD.equals(c)) {
        return SS_DONE;                 // final hop
      }
      if (SS_DONE.equals(c)) {
        return SS_DONE;                 // already final
      }
      return c; // fallback
    } else {
      return SS_REJECTED; // explicit reject state (DAO-legal)
    }
  }

  private RequestDTO fetchRequest(Long id) {
    try {
      return new RestTemplate()
          .getForObject(mandatesResolutionsDaoURL + "/api/request/{id}", RequestDTO.class, id);
    } catch (Exception ignore) {
      return null;
    }
  }

  private void putRequestUpdate(Long requestId, String subStatusFromForm, boolean approve) {
    // 1) Preserve status from DAO (fallback to exact string "In Progress")
    String currentStatus = "In Progress";
    String currentSub = subStatusFromForm;

    RequestDTO dao = fetchRequest(requestId);
    if (dao != null) {
      try {
        String s = (String) RequestDTO.class.getMethod("getStatus").invoke(dao);
        if (s != null && !s.isBlank()) {
          currentStatus = s;
        }
      } catch (Throwable ignore) {
        // intentionally empty
      }

      if (currentSub == null || currentSub.isBlank()) {
        try {
          String ss = (String) RequestDTO.class.getMethod("getSubStatus").invoke(dao);
          if (ss != null) {
            currentSub = ss;
          }
        } catch (Throwable ignore) {
          // intentionally empty
        }
      }
    }

    // 2) Compute next subStatus via your mapping
    String nextSub = nextSubStatus(currentSub, approve);

    // 3) PUT minimal body (names must match DAO entity JSON)
    var body = new java.util.HashMap<String, Object>();
    body.put("status", currentStatus);
    body.put("subStatus", nextSub);
    body.put("outcome", approve ? "Approve" : "Reject");

    var headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

    new org.springframework.web.client.RestTemplate().exchange(
        mandatesResolutionsDaoURL + "/api/request/{id}",
        org.springframework.http.HttpMethod.PUT,
        new org.springframework.http.HttpEntity<>(body, headers),
        Void.class,
        requestId
    );
  }

  // ======================= Instructions helper for reject/approve instructions page==============

  //Returns the list of instruction strings for a given requestStatus value.
  //Fetch DAO-driven "Instructions" by status for Approve/Reject models
  private java.util.List<String> fetchLovInstructions(String requestStatusPrimary,
                                                      String requestStatusFallback) {
    final RestTemplate rt = new RestTemplate();
    final String base = mandatesResolutionsDaoURL
                        + "/api/lov?type={type}&subType={subType}&requestStatus={status}";

    java.util.function.Function<String, java.util.List<String>> call = (String statusVal) -> {
      if (statusVal == null || statusVal.trim().isEmpty()) {
        return java.util.List.of();
      }
      try {
        var resp = rt.exchange(
            base,
            HttpMethod.GET,
            null,
            new org.springframework.core.ParameterizedTypeReference<
                java.util.List<java.util.Map<String, Object>>>() {
            },
            "Readout", "Instructions", statusVal.trim()
        );
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
          return resp.getBody().stream()
              .map(m -> String.valueOf(m.getOrDefault("value", "")).trim())
              .filter(s -> !s.isEmpty())
              .toList();
        }
      } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
        //No LOV rows for this status — fine, fall back.
      } catch (Exception ignore) {
        //Network/parse/etc — ignore and fall back.
      }
      return java.util.List.of();
    };

    //Try subStatus first, then main status
    var items = call.apply(requestStatusPrimary);
    if (!items.isEmpty()) {
      return items;
    }

    items = call.apply(requestStatusFallback);
    return items.isEmpty() ? java.util.List.of() : items;
  }

  //Cleans up JSON string from database
  private static java.util.List<String> parseInstructionsFromLovValue(String raw) {
    java.util.List<String> out = new java.util.ArrayList<>();
    if (raw == null) {
      return out;
    }

    String s = raw.trim();

    //Case A: JSON array/object (DAO example)
    //Example: [{"line1":"Log into Sigma.", "line2":"Check ..."}]
    if (s.startsWith("[") || s.startsWith("{")) {
      try {
        ObjectMapper om = new ObjectMapper();
        JsonNode n = om.readTree(s);

        //If it's an array, take the first element (Example uses 1 object inside an array)
        if (n.isArray() && n.size() > 0) {
          n = n.get(0);
        }
        if (n.isObject()) {
          //Collect fields in a stable order: line1, line2, ... then any other keys
          java.util.List<String> known = new java.util.ArrayList<>();
          java.util.List<String> others = new java.util.ArrayList<>();

          java.util.Iterator<String> it = n.fieldNames();
          while (it.hasNext()) {
            others.add(it.next());
          }

          //Prefer numeric "lineN" ordering
          others.sort((a, b) -> {
            boolean s1IsLine = a.toLowerCase().startsWith("line");
            boolean s2IsLine = b.toLowerCase().startsWith("line");
            if (s1IsLine && s2IsLine) {
              try {
                int ai = Integer.parseInt(a.replaceAll("\\D+", ""));
                int bi = Integer.parseInt(b.replaceAll("\\D+", ""));
                return Integer.compare(ai, bi);
              } catch (Exception ignore) { /* fall through */ }
            }
            if (s1IsLine != s2IsLine) {
              return s1IsLine ? -1 : 1;
            }
            return a.compareToIgnoreCase(b);
          });

          for (String k : others) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull()) {
              String line = v.asText().trim();
              if (!line.isEmpty()) {
                known.add(line);
              }
            }
          }
          out.addAll(known);
          return out;
        }

        //If it's an array of strings, just add them
        if (n.isArray()) {
          for (JsonNode item : n) {
            if (item.isTextual()) {
              String line = item.asText().trim();
              if (!line.isEmpty()) {
                out.add(line);
              }
            }
          }
          if (!out.isEmpty()) {
            return out;
          }
        }
      } catch (Exception ignore) {
        //Fall through to non-JSON fallback
      }
    }

    // Case B: Plain text fallback — split on common delimiters/new lines
    //(covers any legacy non-JSON values)
    for (String tok : s.split("\\r?\\n|;|•|-\\s")) {
      String line = tok.trim();
      if (!line.isEmpty()) {
        out.add(line);
      }
    }
    if (out.isEmpty()) {
      out.add(s); //Last resort: whole string as single item
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private void populateInstructions(RequestWrapper wrapper, String subStatus, String daoBaseUrl) {
    try {
      if (wrapper == null) {
        return;
      }
      RequestWrapper.LovsDTO lovs = (wrapper.getLovs() == null)
          ? new RequestWrapper.LovsDTO()
          : wrapper.getLovs();

      final String status = (subStatus == null) ? "" : subStatus.trim();
      if (status.isEmpty()) {
        wrapper.setLovs(lovs);
        return;
      }

      RestTemplate rt = new RestTemplate();
      String url = daoBaseUrl + "/api/lov?type=Readout&subType=Instructions&requestStatus="
                   + java.net.URLEncoder.encode(status, java.nio.charset.StandardCharsets.UTF_8);

      //DAO returns a list of LOV rows, we only need each row's "value"
      var resp = rt.exchange(
          url,
          org.springframework.http.HttpMethod.GET,
          null,
          new org.springframework.core.ParameterizedTypeReference
              <java.util.List<java.util.Map<String, Object>>>() {
          }
      );

      if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
        for (var row : resp.getBody()) {
          Object v = (row == null) ? null : row.get("value");
          if (v != null) {
            for (String line : parseInstructionsFromLovValue(String.valueOf(v))) {
              lovs.getInstructions().add(line);
            }
          }
        }
      }

      wrapper.setLovs(lovs);
    } catch (Exception e) {
      //Non-fatal: if LOV fails, we just render the fallback in XSLT
      logger.warn("Could not load instructions LOV for '{}': {}", subStatus, e.toString());
    }
  }


  @SuppressWarnings("unchecked")
  private void populateInstructions(RequestTableWrapper wrapper,
                                    String subStatus, String daoBaseUrl) {
    try {
      if (wrapper == null) {
        return;
      }

      RequestTableWrapper.LovsDTO lovs = (wrapper.getLovs() == null)
          ? new RequestTableWrapper.LovsDTO()
          : wrapper.getLovs();

      final String status = (subStatus == null) ? "" : subStatus.trim();
      if (status.isEmpty()) {
        wrapper.setLovs(lovs);
        return;
      }

      RestTemplate rt = new RestTemplate();
      String url = daoBaseUrl + "/api/lov?type=Readout&subType=Instructions&requestStatus="
                   + java.net.URLEncoder.encode(status, java.nio.charset.StandardCharsets.UTF_8);

      var resp = rt.exchange(
          url,
          org.springframework.http.HttpMethod.GET,
          null,
          new org.springframework.core.ParameterizedTypeReference
              <java.util.List<java.util.Map<String, Object>>>() {
          }
      );

      if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
        for (var row : resp.getBody()) {
          Object v = (row == null) ? null : row.get("value");
          if (v != null) {
            for (String line : parseInstructionsFromLovValue(String.valueOf(v))) {
              lovs.getInstructions().add(line);
            }
          }
        }
      }

      wrapper.setLovs(lovs);
    } catch (Exception e) {
      logger.warn("Could not load instructions LOV for '{}': {}", subStatus, e.toString());
    }
  }

  private za.co.rmb.tts.mandates.resolutions.ui.model.dto.MandateResolutionSubmissionResultDTO
        postSnapshotToBackend(SubmissionPayload payload) {

    String url = mandatesResolutionsDaoURL + "/api/submission";
    RestTemplate rt = new RestTemplate();

    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

    org.springframework.http.HttpEntity<SubmissionPayload> entity =
        new org.springframework.http.HttpEntity<>(payload, headers);

    ResponseEntity<za.co.rmb.tts.mandates.resolutions.ui
        .model.dto.MandateResolutionSubmissionResultDTO>
        resp =
        rt.postForEntity(
            url,
            entity,
            za.co.rmb.tts.mandates.resolutions.ui
                .model.dto.MandateResolutionSubmissionResultDTO.class
        );

    if (!resp.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("Backend /api/submission returned status " + resp.getStatusCode());
    }
    return resp.getBody();
  }

  @SuppressWarnings("unchecked")
  private java.util.List<SessionFile> getOrInitSessionFiles(HttpSession session) {
    Object obj = session.getAttribute("uploadedFiles");
    if (obj instanceof java.util.List<?> list && !list.isEmpty() && list.get(
        0) instanceof SessionFile) {
      return (java.util.List<SessionFile>) obj;
    }
    java.util.List<SessionFile> fresh = new java.util.ArrayList<>();
    session.setAttribute("uploadedFiles", fresh);
    return fresh;
  }

  public static final class SessionFile implements java.io.Serializable {
    private final String name;
    private final String contentType;
    private final long size;
    private final byte[] bytes;

    public SessionFile(String name, String contentType, long size, byte[] bytes) {
      this.name = name;
      this.contentType = contentType;
      this.size = size;
      this.bytes = bytes;
    }

    public String getName() {
      return name;
    }

    public String getContentType() {
      return contentType;
    }

    public long getSize() {
      return size;
    }

    public byte[] getBytes() {
      return bytes;
    }
  }

  private static String errorPage(String message) {
    return """
               <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     id="" heading=" " template="error" version="1">
                 <error xsi:type="validationError">
                   <code>0</code>
                   <message>""" + message + "</message>\n"
           +
           "</error>\n"
           +
           "</page>";
  }

  /**
   * Add a file to session + expose name to RequestDTO so XSL can show it
   */
  private void addUploadedFileToSession(MultipartFile file, HttpSession session) throws Exception {
    String name = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
        ? "uploaded-file" : file.getOriginalFilename();
    String ct = (file.getContentType() == null || file.getContentType().isBlank())
        ? "application/octet-stream" : file.getContentType();

    getOrInitSessionFiles(session).add(new SessionFile(name, ct, file.getSize(), file.getBytes()));
    System.out.println("Session uploads count = " + getOrInitSessionFiles(session).size());

    RequestDTO dto = (RequestDTO) session.getAttribute("requestData");
    if (dto == null) {
      dto = new RequestDTO();
    }
    if (dto.getResolutionDocs() == null) {
      dto.setResolutionDocs(new ArrayList<>());
    }
    dto.getResolutionDocs().add(name);
    dto.setEditable(true);
    session.setAttribute("requestData", dto);
  }

  /**
   * Push all session files to TTS-Document-Management and clear the session list.
   */
  private void persistSessionFilesToTts(Long requestId, String creator, HttpSession session) {
    if (requestId == null) {
      return;
    }
    List<SessionFile> files = getOrInitSessionFiles(session);
    if (files.isEmpty()) {
      return;
    }

    RestTemplate rt = new RestTemplate();

    for (SessionFile sf : files) {
      try {
        // Build "meta" JSON inline (matches TTS DocumentUploadRequest)
        var meta = new java.util.LinkedHashMap<String, Object>();
        meta.put("name", sf.name);
        meta.put("type", sf.contentType);
        meta.put("tags", "MR");
        meta.put("refType", "MR_REQUEST");
        meta.put("refId", requestId);
        meta.put("creator", (creator == null || creator.isBlank()) ? "UI_USER" : creator);

        MultipartBodyBuilder mbb = new MultipartBodyBuilder();
        mbb.part("file", new ByteArrayResource(sf.bytes) {
          @Override
          public String getFilename() {
            return sf.name;
          }
        }).contentType(MediaType.parseMediaType(sf.contentType));
        mbb.part("meta", meta, MediaType.APPLICATION_JSON);

        MultiValueMap<String, HttpEntity<?>> body = mbb.build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> resp = rt.postForEntity(ttsDmsBaseUrl,
            new HttpEntity<>(body, headers), String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
          System.err.println("TTS upload failed for " + sf.name + " -> " + resp.getStatusCode());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Clear after attempting uploads
    files.clear();
    session.setAttribute("uploadedFiles", files);
  }

  private static String firstNonBlank(HttpServletRequest req, String... names) {
    for (String n : names) {
      String v = req.getParameter(n);
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return null;
  }

  /**
   * Mirror of your backend "Result" for convenience (only if you want typed access to response).
   */
// ======================= Result DTO (mirror backend) =======================
  @lombok.Data
  public static class MandateResolutionSubmissionResultDTO {
    private Company company;
    private Request request;
    private Waiver waiver;
    private java.util.List<Account> accounts;
    private java.util.List<Authority> authorities;
    private java.util.List<Signatory> signatories;

    @lombok.Data
    public static class Company {
      Long companyId;
      String registrationNumber;
      String name;
      String address;
      String creator;
      String updator;
      java.time.LocalDateTime created;
      java.time.LocalDateTime updated;
    }

    @lombok.Data
    public static class Request {
      Long requestId;
      Long companyId;
      Integer sla;
      String type;
      String status;
      String subStatus;
      String creator;
      String updator;
      java.time.LocalDateTime created;
      java.time.LocalDateTime updated;
    }

    @lombok.Data
    public static class Waiver {
      Long waiverId;
      Long companyId;
      String ucn;
      String permittedTools;
      java.time.LocalDateTime lastFetched;
      String creator;
      String updator;
      java.time.LocalDateTime created;
      java.time.LocalDateTime updated;
    }

    @lombok.Data
    public static class Account {
      Long accountId;
      Long companyId;
      String accountName;
      String accountNumber;
      Boolean isActive;
      String creator;
      String updator;
      java.time.LocalDateTime created;
      java.time.LocalDateTime updated;
    }

    @lombok.Data
    public static class Authority {
      Long authorityId;
      Long companyId;
      String firstname;
      String surname;
      String designation;
      Boolean isActive;
      String creator;
      String updator;
      java.time.LocalDateTime created;
      java.time.LocalDateTime updated;
    }

    @lombok.Data
    public static class Signatory {
      Long signatoryId;
      Long companyId;
      String fullName;
      String idNumber;
      String instructions;
      String capacity;
      String groupCategory;
      String creator;
      String updator;
      java.time.LocalDateTime created;
      java.time.LocalDateTime updated;
    }
  }

  private String xmlPagePath(String pageName) {
    return XML_PAGE_PATH + pageName + ".xml";
  }

  private String xslPagePath(String pageName) {
    return XSL_PAGE_PATH + pageName + ".xsl";
  }
}