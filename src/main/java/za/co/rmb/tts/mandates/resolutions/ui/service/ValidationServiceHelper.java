package za.co.rmb.tts.mandates.resolutions.ui.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.model.DirectorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.SignatoryModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.DirectorErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SearchResultsErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.error.SignatoryErrorModel;
import za.co.rmb.tts.mandates.resolutions.ui.util.ScreenValidation;

/**
 * Service for validating user input and business logic across different screens.
 * 
 * <p>This service centralizes validation logic previously embedded in controller methods,
 * ensuring consistent validation rules and improving code maintainability.</p>
 */
@Service
public class ValidationServiceHelper {

  @Autowired
  private ScreenValidation screenValidation;

  /**
   * Validates director form data.
   *
   * @param admin             the form data containing director information
   * @param checkInstructions whether to check for instructions field
   * @return a DirectorErrorModel containing validation errors, or null if no errors
   */
  public DirectorErrorModel validateDirectorDetails(Map<String, String> admin,
                                                     boolean checkInstructions) {
    DirectorErrorModel errorModel = new DirectorErrorModel();
    boolean hasError = false;

    if (admin.get("name") == null || admin.get("name").isBlank()) {
      errorModel.setName("Name can't be empty !");
      hasError = true;
    }

    if (admin.get("designation") == null || admin.get("designation").isBlank()) {
      errorModel.setDesignation("Designation can't be empty !");
      hasError = true;
    }

    if (admin.get("surname") == null || admin.get("surname").isBlank()) {
      errorModel.setSurname("Surname can't be empty !");
      hasError = true;
    }

    if (checkInstructions) {
      if (admin.get("instructions") == null || admin.get("instructions").isBlank()
          || "Please select".equalsIgnoreCase(admin.get("instructions"))) {
        errorModel.setInstruction("Instruction can't be empty or Please select !");
        hasError = true;
      }
    }

    return hasError ? errorModel : null;
  }

  /**
   * Validates signatory form data.
   *
   * @param user the form data containing signatory information
   * @return a SignatoryErrorModel containing validation errors, or null if no errors
   */
  public SignatoryErrorModel validateSignatoryDetails(Map<String, String> user) {
    SignatoryErrorModel errorModel = new SignatoryErrorModel();
    boolean hasError = false;

    if (user.get("fullName") == null || user.get("fullName").isBlank()) {
      errorModel.setFullName("Full Name can't be empty !");
      hasError = true;
    }

    if (user.get("idNumber") == null || user.get("idNumber").isBlank()) {
      errorModel.setIdNumber("Id number can't be empty !");
      hasError = true;
    } else if (!screenValidation.validateSaIdNumber(user.get("idNumber"))) {
      errorModel.setIdNumber("Provide Valid SA Id Number !");
      hasError = true;
    }

    if (user.get("accountRef1") == null || user.get("accountRef1").isBlank()
        || "Please select".equalsIgnoreCase(user.get("accountRef1"))) {
      errorModel.setInstruction("Instruction can't be empty or Please select !");
      hasError = true;
    }

    return hasError ? errorModel : null;
  }

  /**
   * Validates search results form data before proceeding to account details.
   *
   * @param user           the form data containing search results
   * @param requestWrapper the request wrapper containing director models
   * @return a SearchResultsErrorModel containing validation errors, or null if no errors
   */
  public SearchResultsErrorModel validateSearchResults(Map<String, String> user,
                                                        RequestWrapper requestWrapper) {
    SearchResultsErrorModel errorModel = new SearchResultsErrorModel();
    boolean hasError = false;

    if (user.get("companyName") == null || user.get("companyName").isBlank()) {
      errorModel.setCompanyName("Campany Name can't be empty !");
      hasError = true;
    }

    if (user.get("companyAddress") == null || user.get("companyAddress").isBlank()) {
      errorModel.setCompanyAddress("Company Address can't be empty !");
      hasError = true;
    }

    if ((user.get("toolOne") == null || user.get("toolOne").isBlank())
        && (user.get("toolTwo") == null || user.get("toolTwo").isBlank())
        && (user.get("toolThree") == null || user.get("toolThree").isBlank())
        && (user.get("toolFour") == null || user.get("toolFour").isBlank())
        && (user.get("toolFive") == null || user.get("toolFive").isBlank())) {
      errorModel.setToolOne("At least One Waiver tool need for the Request !");
      hasError = true;
    }

    List<DirectorModel> directors = requestWrapper.getDirectorModels();
    if (directors == null || directors.isEmpty()) {
      requestWrapper.setCheckDirectorEmpty("true");
      hasError = true;
    } else {
      requestWrapper.setCheckDirectorEmpty("false");
    }

    if (user.get("mandateResolution") == null || user.get("mandateResolution").isBlank()
        || "Please select".equalsIgnoreCase(user.get("mandateResolution"))) {
      errorModel.setRequestType("Request Type can't be empty and Please select !");
      hasError = true;
    } else {
      if ("false".equalsIgnoreCase(user.get("check1"))) {
        errorModel.setCheckStyleOne("Select the Check box !");
        hasError = true;
      }
      if ("false".equalsIgnoreCase(user.get("check2"))) {
        errorModel.setCheckStyleTwo("Select the Check box !");
        hasError = true;
      }
    }

    return hasError ? errorModel : null;
  }

  /**
   * Validates account details including account name, number, and signatories.
   *
   * @param user               the form data containing account details
   * @param listOfSignatory    the list of signatories for this account
   * @return a SignatoryErrorModel containing validation errors, or null if no errors
   */
  public SignatoryErrorModel validateAccountDetails(Map<String, String> user,
                                                     List<SignatoryModel> listOfSignatory) {
    SignatoryErrorModel errorModel = new SignatoryErrorModel();
    boolean hasError = false;

    if (user.get("accountName") == null || user.get("accountName").isBlank()) {
      errorModel.setAccountName("Account Name can't be empty !");
      hasError = true;
    }

    if (user.get("accountNo") == null || user.get("accountNo").isBlank()) {
      errorModel.setAccountNumber("Account Number can't be empty !");
      hasError = true;
    }

    if (listOfSignatory == null || listOfSignatory.isEmpty()) {
      hasError = true;
      // Note: checkSignatoryList flag should be set by caller
    }

    return hasError ? errorModel : null;
  }

  /**
   * Validates company registration number input.
   *
   * @param companyRegNumber the registration number to validate
   * @return a SearchResultsErrorModel containing validation errors, or null if no errors
   */
  public SearchResultsErrorModel validateCompanyRegistration(String companyRegNumber) {
    SearchResultsErrorModel errorModel = new SearchResultsErrorModel();
    boolean hasError = false;

    if (companyRegNumber == null || companyRegNumber.isBlank() || companyRegNumber.isEmpty()) {
      errorModel.setRegiNumber("Company Registration Number can't be empty !");
      hasError = true;
    }

    return hasError ? errorModel : null;
  }
}
