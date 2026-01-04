package za.co.rmb.tts.mandates.resolutions.ui.service.mandate;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import za.co.rmb.tts.mandates.resolutions.ui.model.AddAccountModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.DirectorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.SignatoryModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.DirectorErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SignatoryErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.util.ScreenValidation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for handling mandate and resolution capture workflows.
 * 
 * This service supports the following screens/features:
 * - Create Request (company search and validation)
 * - Director management (add, edit, remove directors)
 * - Account and signatory management
 * - PDF file upload and handling
 * 
 * Controller methods that delegate to this service:
 * - displayCreateRequest()
 * - fetchMergedDetails()
 * - tablePopup()
 * - submitAdminDetails()
 * - editDirector()
 * - updateDirectors()
 * - addAccount()
 * - submitSignatory()
 * - signatoryEdit()
 * - editSignatory()
 * - addSignatoryWithAccount()
 * 
 * Extracted from MandatesResolutionUIController for separation of concerns.
 */
@Service
public class MandateCaptureService {

    private final HttpSession httpSession;
    private final ScreenValidation screenValidation;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mandates-resolutions-dao}")
    private String mandatesResolutionsDaoURL;

    public MandateCaptureService(HttpSession httpSession, ScreenValidation screenValidation) {
        this.httpSession = httpSession;
        this.screenValidation = screenValidation;
    }

    /**
     * Search for company details by registration number.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public Map<String, Object> searchCompanyByRegistration(String registrationNumber) {
        String url = UriComponentsBuilder
            .fromHttpUrl(mandatesResolutionsDaoURL)
            .pathSegment("api", "company", "registration")
            .queryParam("registrationNumber", registrationNumber)
            .toUriString();
        
        try {
            Map<String, Object> company = restTemplate.getForObject(url, Map.class);
            return company;
        } catch (HttpStatusCodeException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Utility function to deduplicate comma-separated values.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public Function<String, String> getDedupeCommaFunction() {
        Function<String, String> nz = s -> s == null ? "" : s.trim();
        return s -> {
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
    }

    /**
     * Utility function to normalize registration numbers.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public Function<String, String> getNormRegFunction() {
        Function<String, String> nz = s -> s == null ? "" : s.trim();
        return s -> {
            String t = nz.apply(s);
            return t.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        };
    }

    /**
     * Validate director input and return error model if validation fails.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public DirectorErrorModel validateDirectorInput(Map<String, String> admin, boolean requireInstruction) {
        DirectorErrorModel errorModel = new DirectorErrorModel();
        boolean hasError = false;

        if (admin.get("name").isBlank()) {
            errorModel.setName("Name can't be empty !");
            hasError = true;
        }

        if (admin.get("designation").isBlank()) {
            errorModel.setDesignation("Designation can't be empty !");
            hasError = true;
        }

        if (admin.get("surname").isBlank()) {
            errorModel.setSurname("Surname can't be empty !");
            hasError = true;
        }

        if (requireInstruction) {
            if (admin.get("instructions").isBlank() || "Please select".equalsIgnoreCase(admin.get("instructions"))) {
                errorModel.setInstruction("Instruction can't be empty or Please select !");
                hasError = true;
            }
        }

        return hasError ? errorModel : null;
    }

    /**
     * Validate signatory input and return error model if validation fails.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public SignatoryErrorModel validateSignatoryInput(Map<String, String> user) {
        SignatoryErrorModel errorModel = new SignatoryErrorModel();
        boolean hasError = false;

        if (user.get("fullName").isBlank()) {
            errorModel.setFullName("Full Name can't be empty !");
            hasError = true;
        }

        if (user.get("idNumber").isBlank()) {
            errorModel.setIdNumber("Id number can't be empty !");
            hasError = true;
        }

        if (!screenValidation.validateSaIdNumber(user.get("idNumber"))) {
            errorModel.setIdNumber("Provide Valid SA Id Number !");
            hasError = true;
        }

        if (user.get("accountRef1").isBlank() || "Please select".equalsIgnoreCase(user.get("accountRef1"))) {
            errorModel.setInstruction("Instruction can't be empty or Please select !");
            hasError = true;
        }

        return hasError ? errorModel : null;
    }

    /**
     * Validate account input and return error model if validation fails.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public SignatoryErrorModel validateAccountInput(Map<String, String> user, AddAccountModel addAccountModel) {
        SignatoryErrorModel errorModel = new SignatoryErrorModel();
        boolean hasError = false;

        if (user.get("accountName").isBlank()) {
            errorModel.setAccountName("Account Name can't be empty !");
            hasError = true;
        }

        if (user.get("accountNo").isBlank()) {
            errorModel.setAccountNumber("Account Number can't be empty !");
            hasError = true;
        }

        List<SignatoryModel> listOfSignatory = addAccountModel.getListOfSignatory();
        if (listOfSignatory == null || listOfSignatory.isEmpty()) {
            addAccountModel.setCheckSignatoryList("true");
            hasError = true;
        } else {
            addAccountModel.setCheckSignatoryList("false");
        }

        return hasError ? errorModel : null;
    }

    /**
     * Add a director to the session's director list.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void addDirectorToSession(DirectorModel directorModel, boolean isResolution) {
        RequestWrapper requestWrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
        
        List<DirectorModel> directorModelList;
        if (isResolution) {
            directorModelList = requestWrapper.getListOfDirectors();
        } else {
            directorModelList = requestWrapper.getDirectorModels();
        }
        
        if (directorModelList == null) {
            directorModelList = new ArrayList<>();
        }

        int size = directorModelList.size();
        directorModel.setUserInList(++size);
        directorModelList.add(directorModel);

        if (isResolution) {
            requestWrapper.setListOfDirectors(directorModelList);
        } else {
            requestWrapper.setDirectorModels(directorModelList);
        }

        httpSession.setAttribute("RequestWrapper", requestWrapper);
    }

    /**
     * Add a signatory to the account model.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void addSignatoryToAccount(SignatoryModel signatoryModel) {
        AddAccountModel addAccountModel = (AddAccountModel) httpSession.getAttribute("Signatory");
        List<SignatoryModel> signatoryModels = addAccountModel.getListOfSignatory();
        
        if (signatoryModels == null) {
            signatoryModels = new ArrayList<>();
        }

        int size = signatoryModels.size();
        signatoryModel.setUserInList(++size);
        signatoryModel.setCheckEdit("false");
        signatoryModels.add(signatoryModel);
        
        addAccountModel.setListOfSignatory(signatoryModels);
        httpSession.setAttribute("Signatory", addAccountModel);
    }

    /**
     * Add an account with signatories to the request wrapper.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void addAccountWithSignatoriesToRequest(AddAccountModel addAccountModel) {
        RequestWrapper requestWrapper = (RequestWrapper) httpSession.getAttribute("RequestWrapper");
        List<AddAccountModel> addAccountModelList = requestWrapper.getListOfAddAccount();
        
        if (addAccountModelList == null) {
            addAccountModelList = new ArrayList<>();
        }

        int size = addAccountModelList.size();
        addAccountModel.setUserInList(++size);
        addAccountModel.setSignatoryErrorModel(null);
        addAccountModel.setCheckSignatoryList("false");

        List<SignatoryModel> listOfSignatory = addAccountModel.getListOfSignatory();
        if (listOfSignatory != null && !listOfSignatory.isEmpty()) {
            for (int i = 0; i < listOfSignatory.size(); i++) {
                SignatoryModel signatoryModel = listOfSignatory.get(i);
                signatoryModel.setUserInAccount(addAccountModel.getUserInList());
                listOfSignatory.set(i, signatoryModel);
            }
        }

        addAccountModelList.add(addAccountModel);
        requestWrapper.setListOfAddAccount(addAccountModelList);
        requestWrapper.setAccountCheck("true");
        
        httpSession.setAttribute("Signatory", addAccountModel);
        httpSession.setAttribute("RequestWrapper", requestWrapper);
    }
}
