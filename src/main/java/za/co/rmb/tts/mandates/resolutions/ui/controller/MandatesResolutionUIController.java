package za.co.rmb.tts.mandates.resolutions.ui.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.service.XSLTProcessorService;

@RestController
@RequestMapping("/ui")
public class MandatesResolutionUIController {

  private final XSLTProcessorService xsltProcessor;

  private static final String XML_PAGE_PATH = "/templates/xml/";
  private static final String XSL_PAGE_PATH = "/templates/xsl/";
  private static final String NODE_BACKEND_URL = "http://localhost:3000";

  @Value("${integration.mode}")
  private String integrationMode;

  @Autowired
  public MandatesResolutionUIController(XSLTProcessorService xsltProcessor) {
    this.xsltProcessor = xsltProcessor;
  }

  @PostMapping(produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayHomePage() {
    String page = xsltProcessor.returnPage(xmlPagePath("LoginPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/requestTable", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayRequestTable() {
    String page = xsltProcessor.generatePage(xslPagePath("LandingPage"), new RequestWrapper());
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/createRequest", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayCreateRequest() {
    String page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), new RequestWrapper());
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/searchResults", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displaySearchResults(@RequestBody RequestWrapper wrapper) {
    String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/nextStep", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> handleNextStep(
      @ModelAttribute RequestWrapper wrapper,
      @RequestParam(value = "mandateResolution", required = false) String requestType,
      HttpServletRequest request) {

    System.out.println("Received requestType: " + requestType);

    String confirmationCheck = null;

    if (requestType != null) {
      switch (requestType) {
        case "1":
          confirmationCheck = request.getParameter("confirmationCheckMandate");
          break;
        case "2":
          confirmationCheck = request.getParameter("confirmationCheckResolution");
          break;
        case "3":
          confirmationCheck = request.getParameter("confirmationCheckMandateResolution");
          break;
        default:
          break;
      }
    }

    System.out.println("Received confirmationCheck: " + confirmationCheck);

    String validationErrorXml = null;

    if (requestType == null || requestType.isBlank() || "-1".equals(requestType)) {
      validationErrorXml = "<page xmlns:comm=\"http://ws.online.fnb.co.za/common/\""
          + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
          + " id=\"\" heading=\" \" template=\"error\" version=\"1\">"
          + "<error xsi:type=\"validationError\">"
          + "<name>mandateResolution</name>"
          + "<code>0</code>"
          + "<message>Please select a valid request type.</message>"
          + "</error>"
          + "</page>";
    } else if (confirmationCheck == null || !"1".equals(confirmationCheck)) {
      validationErrorXml = "<page xmlns:comm=\"http://ws.online.fnb.co.za/common/\""
          + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
          + " id=\"\" heading=\" \" template=\"error\" version=\"1\">"
          + "<error xsi:type=\"validationError\">"
          + "<name>confirmationCheck</name>"
          + "<code>0</code>"
          + "<message>Please check the confirmation box to proceed.</message>"
          + "</error>"
          + "</page>";
    }

    if (validationErrorXml != null) {
      return ResponseEntity.ok(validationErrorXml);
    }

    //Route based on requestType
    switch (requestType) {
      case "1":
        //Call the MandatesAutoFill.xsl page
        return generateMandatesFillPage(
            1,   //Default accountCount
            null, //signatoryCounts
            null, //removeSignatoryAt
            null, // ddSignatoryAt
            null  //removeAccountAt
        );
      case "2":
        String resolutionPage = xsltProcessor.generatePage(xslPagePath("ResolutionAutoFill"),
            new RequestWrapper());
        return ResponseEntity.ok(resolutionPage);
      case "3":
        String mandateResolutionPage =
            xsltProcessor.generatePage(xslPagePath("MandateResolutionAutoFill"),
                new RequestWrapper());
        return ResponseEntity.ok(mandateResolutionPage);
      default:
        String fallbackXml = "<page xmlns:comm=\"http://ws.online.fnb.co.za/common/\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " id=\"\" heading=\" \" template=\"error\" version=\"1\">"
            + "<error xsi:type=\"validationError\">"
            + "<name>mandateResolution</name>"
            + "<code>0</code>"
            + "<message>Invalid request type.</message>"
            + "</error>"
            + "</page>";
        return ResponseEntity.ok(fallbackXml);
    }
  }

  @PostMapping(value = "/mandatesFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesFillPost(
      @RequestParam(defaultValue = "1") int accountCount,
      @RequestParam(required = false) String signatoryCounts,
      @RequestParam(required = false) String removeSignatoryAt,
      @RequestParam(required = false) Integer addSignatoryAt,
      @RequestParam(required = false) Integer removeAccountAt
  ) {
    return generateMandatesFillPage(accountCount, signatoryCounts,
        removeSignatoryAt, addSignatoryAt, removeAccountAt);
  }

  @GetMapping(value = "/mandatesFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesFillGet(
      @RequestParam(defaultValue = "1") int accountCount,
      @RequestParam(required = false) String signatoryCounts,
      @RequestParam(required = false) String removeSignatoryAt,
      @RequestParam(required = false) Integer addSignatoryAt,
      @RequestParam(required = false) Integer removeAccountAt
  ) {
    return generateMandatesFillPage(accountCount, signatoryCounts,
        removeSignatoryAt, addSignatoryAt, removeAccountAt);
  }

  private ResponseEntity<String> generateMandatesFillPage(
      int accountCount,
      String signatoryCounts,
      String removeSignatoryAt,
      Integer addSignatoryAt,
      Integer removeAccountAt
  ) {
    RequestWrapper wrapper = new RequestWrapper();
    RequestDTO dto = new RequestDTO();
    List<Integer> signatoryCountList = new ArrayList<>();

    //Parse signatoryCounts safely
    if (signatoryCounts != null && !signatoryCounts.isEmpty()) {
      String[] parts = signatoryCounts.split(",");
      for (String part : parts) {
        try {
          signatoryCountList.add(Integer.parseInt(part.trim()));
        } catch (NumberFormatException e) {
          signatoryCountList.add(1); //Fallback if invalid
        }
      }
    }

    //Ensure signatoryCountList matches accountCount
    while (signatoryCountList.size() < accountCount) {
      signatoryCountList.add(1);
    }

    //Handle Add Signatory
    if (addSignatoryAt != null && addSignatoryAt >= 1 && addSignatoryAt <= accountCount) {
      int index = addSignatoryAt - 1;
      signatoryCountList.set(index, signatoryCountList.get(index) + 1);
    }

    //Handle Remove Signatory
    if (removeSignatoryAt != null && !removeSignatoryAt.isEmpty()) {
      try {
        String[] parts = removeSignatoryAt.split("_");
        if (parts.length == 2) {
          int accountIndex = Integer.parseInt(parts[0]) - 1;
          int signatoryIndex = Integer.parseInt(parts[1]) - 1;
          if (accountIndex >= 0 && accountIndex < signatoryCountList.size()) {
            int currentCount = signatoryCountList.get(accountIndex);
            if (currentCount > 1 && signatoryIndex >= 0 && signatoryIndex < currentCount) {
              signatoryCountList.set(accountIndex, currentCount - 1);
            }
          }
        }
      } catch (NumberFormatException e) {
        System.out.println("Invalid removeSignatoryAt format: " + removeSignatoryAt);
      }
    }

    //Handle Remove Account
    if (removeAccountAt != null && removeAccountAt >= 1 && removeAccountAt <= accountCount) {
      int index = removeAccountAt - 1;
      if (index < signatoryCountList.size()) {
        signatoryCountList.remove(index);
        accountCount--;
      }
    }

    //Build DTO accounts with signatories
    List<RequestDTO.Account> accounts = new ArrayList<>();
    for (int i = 0; i < accountCount; i++) {
      RequestDTO.Account acc = new RequestDTO.Account();
      acc.setAccountName("");
      acc.setAccountNo("");

      int signatoryCount = signatoryCountList.get(i);
      List<RequestDTO.Signatory> signatories = new ArrayList<>();
      for (int j = 0; j < signatoryCount; j++) {
        RequestDTO.Signatory s = new RequestDTO.Signatory();
        s.setFullName("");
        s.setIdNumber("");
        s.setInstruction("");
        signatories.add(s);
      }
      acc.setSignatories(signatories);
      accounts.add(acc);
    }

    dto.setAccounts(accounts);
    wrapper.setRequestDTO(dto);

    String page = xsltProcessor.generatePage(xslPagePath("MandatesAutoFill"), wrapper);
    return ResponseEntity.ok(page);
  }

  @PostMapping(value = "/mandatesSignatureCard", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesSignatureCard() {
    String page = xsltProcessor.generatePage(xslPagePath("MandatesSignatureCard"),
        new RequestWrapper());
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/resolutionsFill", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayResolutionsFill(
      @RequestParam(defaultValue = "1") int directorCount,
      @RequestParam(required = false) Integer removeDirectorAt
  ) {
    RequestWrapper wrapper = new RequestWrapper();
    RequestDTO dto = new RequestDTO();

    List<RequestDTO.Director> directors = new ArrayList<>();

    //Builds the directors list
    for (int i = 0; i < directorCount; i++) {
      RequestDTO.Director director = new RequestDTO.Director();
      director.setName("");
      director.setSurname("");
      director.setDesignation("");
      directors.add(director);
    }

    //Remove button for directors
    if (removeDirectorAt != null) {
      int indexToRemove = removeDirectorAt - 1; //UI sends 1-based
      if (indexToRemove >= 0 && indexToRemove < directors.size()) {
        directors.remove(indexToRemove);
        directorCount--; //Reeflects the updated count
      }
    }

    dto.setDirectors(directors);
    wrapper.setRequestDTO(dto);

    String page = xsltProcessor.generatePage(xslPagePath("ResolutionAutoFill"), wrapper);
    return ResponseEntity.ok(page);
  }

  //Success Pages
  @PostMapping(value = "/mandatesSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayMandatesSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("MandatesSuccessPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/resolutionsSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayResolutionSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("ResolutionSuccessPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/viewRequestSuccess", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequestSuccess() {
    String page = xsltProcessor.returnPage(xmlPagePath("ViewRequestSuccessPage"));
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/viewRequestReject", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequestReject() {
    String page = xsltProcessor.generatePage(xslPagePath("ViewRequestRejectPage"),
        new RequestWrapper());
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PostMapping(value = "/viewRequest", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> displayViewRequest() {
    String page = xsltProcessor.generatePage(xslPagePath("ViewRequest"), new RequestWrapper());
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  //Search bar (CreateRequest.xsl) and values on SearchResults.xsl
  @PostMapping(value = "/searchCompanyDetails", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> fetchMergedDetails(
      @RequestParam("companyRegNumber") String registrationNumber,
      @RequestParam(value = "removeDirectorAt", required = false) Integer removeDirectorAt,
      @RequestParam(value = "directorCount", required = false) Integer directorCount) {
    RestTemplate restTemplate = new RestTemplate();
    String mergeUrl;

    //Determines which service url will be used
    boolean isMockMode = "MOCK".equalsIgnoreCase(integrationMode);
    if (isMockMode) {
      mergeUrl = NODE_BACKEND_URL + "/merge/simulation"; //Mock data (Mock services)
    } else {
      mergeUrl = NODE_BACKEND_URL + "/merge/search"; //Live data (Live integration)
    }

    try {
      //Prepares and send the request to the backend project
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
//      Creates the JSON request body containing the company
//      registration number typed in the search bar
      Map<String, String> requestBody = Map.of("registrationNumber", registrationNumber);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

      ResponseEntity<Map> response = restTemplate.postForEntity(mergeUrl, request, Map.class);
      Map<String, Object> data = response.getBody();

      System.out.println("Raw response from Node backend: " + data);

      if (data == null || !data.containsKey("hogan") || data.get("hogan") == null) {
        throw new RuntimeException("Missing Hogan data");
      }

      // Build RequestDTO and RequestWrapper
      RequestWrapper wrapper = new RequestWrapper();
      RequestDTO dto = new RequestDTO();
      dto.setRegistrationNumber(registrationNumber);

      //Extracts the hogan data and sets them into the DTO (RequestDTO)
      Map<String, Object> hoganData = (Map<String, Object>) data.get("hogan");
      dto.setCompanyName((String) hoganData.get("companyName"));
      dto.setCompanyAddress((String) hoganData.get("companyAddress"));

      //Extracts the directors list from hogan data
      List<Map<String, String>> directorsData = (List<Map<String, String>>)
          hoganData.get("directors");
      List<RequestDTO.Director> directors = new ArrayList<>();
      if (directorsData != null) {
        for (Map<String, String> directorMap : directorsData) {
          RequestDTO.Director d = new RequestDTO.Director();
          d.setName(directorMap.get("name"));
          d.setSurname(directorMap.get("surname"));
          d.setDesignation(directorMap.get("designation"));
          directors.add(d);
        }
      }

      // Handle "Add Director" dynamic row
      int currentCount = directors.size();
      if (directorCount != null && directorCount > currentCount) {
        int toAdd = directorCount - currentCount;
        for (int i = 0; i < toAdd; i++) {
          RequestDTO.Director emptyDirector = new RequestDTO.Director();
          emptyDirector.setName("");
          emptyDirector.setSurname("");
          emptyDirector.setDesignation("");
          directors.add(emptyDirector);
        }
      }

      // Handle "Remove Director" dynamic removal
      if (removeDirectorAt != null && removeDirectorAt > 0
          && removeDirectorAt <= directors.size()) {
        directors.remove(removeDirectorAt - 1); // position() is 1-based
      }
      dto.setDirectors(directors);

      List<String> tools = (List<String>) data.get("documentum");
      dto.setDocumentumTools(tools);

      //Sets editable based on MOCK mode on application.yaml (true= editable false= read only)
      dto.setEditable(isMockMode);

      wrapper.setRequestDTO(dto);

      String page = xsltProcessor.generatePage(xslPagePath("SearchResults"), wrapper);

      //Debug XML
      System.out.println("Generated XML to XSLT:\n" + page);

      return ResponseEntity.ok(page);

    } catch (Exception e) {
      e.printStackTrace();
      String errorXml = """
          <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" heading=" " id="" template="error" version="1" xsi:noNamespaceSchemaLocation="../xsd/v1/error_1.xsd">
              <error xsi:type="systemError">
                  <code>404</code>
                  <message>Company registration number not found.</message>
              </error>
          </page>
          """;
      return ResponseEntity.status(HttpStatus.OK).body(errorXml);
    }
  }

  @PostMapping(value = "/proceedWithoutHogan", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String>
      proceedWithoutHoganPage(@RequestParam("companyRegNumber") String registrationNumber) {
    RestTemplate restTemplate = new RestTemplate();
    String mergeUrl = NODE_BACKEND_URL + "/merge/search";

    try {
      // Prepare and send the request to the backend project
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      Map<String, String> requestBody = Map.of("registrationNumber", registrationNumber);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

      ResponseEntity<Map> response = restTemplate.postForEntity(mergeUrl, request, Map.class);
      Map<String, Object> data = response.getBody();

      System.out.println("Raw response from Node backend: " + data);

      if (data == null) {
        throw new RuntimeException("No data returned from Node backend");
      }

      // Process documentum tools and registration number only
      RequestWrapper wrapper = new RequestWrapper();
      RequestDTO dto = new RequestDTO();
      dto.setRegistrationNumber(registrationNumber);

      List<String> tools = (List<String>) data.get("documentum");
      dto.setDocumentumTools(tools);

      wrapper.setRequestDTO(dto);

      String page = xsltProcessor.generatePage(xslPagePath("ProceedWithoutHogan"), wrapper);
      System.out.println("Received companyRegNumber: " + registrationNumber);
      return ResponseEntity.ok(page);

    } catch (Exception e) {
      e.printStackTrace();
      String errorXml = """
          <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
          <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" heading=" " id="" template="error" version="1" xsi:noNamespaceSchemaLocation="../xsd/v1/error_1.xsd">
              <error xsi:type="systemError">
                  <code>404</code>
                  <message>Company registration number not found.</message>
              </error>
          </page>
          """;
      return ResponseEntity.status(HttpStatus.OK).body(errorXml);
    }
  }

  private String xmlPagePath(String pageName) {
    return XML_PAGE_PATH + pageName + ".xml";
  }

  private String xslPagePath(String pageName) {
    return XSL_PAGE_PATH + pageName + ".xsl";
  }
}