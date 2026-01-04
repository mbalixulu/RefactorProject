package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
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
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestDetails;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;

/**
 * Service responsible for audit trail and status management.
 * This service handles request status updates and status checking operations.
 */
@Service
public class AuditTrailService {

  @Autowired
  private HttpSession httpSession;

  RestTemplate restTemplate = new RestTemplate();

  @Value("${mandates-resolutions-dao}")
  private String mandatesResolutionsDaoURL;

  /**
   * Updates the status of a request.
   *
   * @param requestId Request ID to update
   * @param processOutcome Process outcome value
   * @param subStatus New sub-status
   * @param status New status
   * @param currentUser Current user making the update
   */
  public void statusUpdated(Long requestId, String processOutcome, String subStatus,
                            String status, String currentUser) {
    String url = mandatesResolutionsDaoURL + "/api/request/" + requestId;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("processOutcome", processOutcome);
    requestBody.put("subStatus", subStatus);
    requestBody.put("status", status);
    requestBody.put("updator", currentUser);

    HttpEntity<Map<String, Object>> entity =
        new HttpEntity<>(requestBody, headers);

    ResponseEntity<RequestDTO> response = restTemplate.exchange(
        url,
        HttpMethod.PUT,
        entity,
        RequestDTO.class
    );
    RequestDTO dto = response.getBody();
    RequestDetails requestDetails = (RequestDetails) httpSession.getAttribute("RequestDetails");
    requestDetails.setStatus(dto.getStatus());
    requestDetails.setSubStatus(dto.getSubStatus());
    requestDetails.setUpdatorRequest(dto.getUpdator());
    requestDetails.setUpdatedReq(String.valueOf(dto.getUpdated()));
    httpSession.setAttribute("RequestDetails", requestDetails);
  }

  /**
   * Checks and sets appropriate flags based on request status.
   * Used to determine which verification or approval workflow to display.
   *
   * @param status Current status to check
   */
  public void statusCheck(String status) {
    RequestDetails requestDetails = (RequestDetails) httpSession.getAttribute("RequestDetails");
    if ("Hogan Verification Pending".equalsIgnoreCase(status)) {
      requestDetails.setCheckHoganVarificationPending("true");
      requestDetails.setCheckHoldRecord("Verification On Hold for Hogan");
      requestDetails.setCheckUnHoldRecord("Hogan Verification Pending");
    } else {
      requestDetails.setCheckHoganVarificationPending("false");
    }

    if ("Windeed Verification Pending".equalsIgnoreCase(status)) {
      requestDetails.setCheckWindeedVarificationPending("true");
      requestDetails.setCheckHoldRecord("Verification On Hold for Windeed");
      requestDetails.setCheckUnHoldRecord("Windeed Verification Pending");
    } else {
      requestDetails.setCheckWindeedVarificationPending("false");
    }

    if ("Hanis Verification Pending".equalsIgnoreCase(status)) {
      requestDetails.setCheckHanisVarificationPending("true");
      requestDetails.setCheckHoldRecord("Verification On Hold for Hanis");
      requestDetails.setCheckUnHoldRecord("Hanis Verification Pending");
    } else {
      requestDetails.setCheckHanisVarificationPending("false");
    }

    if ("Admin Approval Pending".equalsIgnoreCase(status)) {
      requestDetails.setCheckAdminApprovePending("true");
      requestDetails.setCheckHoldRecord("Admin Approval On Hold");
      requestDetails.setCheckUnHoldRecord("Admin Approval Pending");
    } else {
      requestDetails.setCheckAdminApprovePending("false");
    }

    if ("Hogan Update Pending".equalsIgnoreCase(status)) {
      requestDetails.setCheckHoganUpdatePending("true");
      requestDetails.setCheckHoldRecord("Update on Hold for Hogan");
      requestDetails.setCheckUnHoldRecord("Hogan Update Pending");
    } else {
      requestDetails.setCheckHoganUpdatePending("false");
    }

    if ("Documentum Update Pending".equalsIgnoreCase(status)) {
      requestDetails.setCheckDocumentUpdatePending("true");
      requestDetails.setCheckHoldRecord("Update on Hold for Documentum");
      requestDetails.setCheckUnHoldRecord("Documentum Update Pending");
    } else {
      requestDetails.setCheckDocumentUpdatePending("false");
    }

    if ("Verification On Hold for Hogan".equalsIgnoreCase(status)) {
      requestDetails.setCheckHoganVarificationPending("true");
      requestDetails.setCheckHoldRecord("Verification On Hold for Hogan");
      requestDetails.setCheckUnHoldRecord("Hogan Verification Pending");
    }

    if ("Verification On Hold for Windeed".equalsIgnoreCase(status)) {
      requestDetails.setCheckWindeedVarificationPending("true");
      requestDetails.setCheckHoldRecord("Verification On Hold for Windeed");
      requestDetails.setCheckUnHoldRecord("Windeed Verification Pending");
    }

    if ("Verification On Hold for Hanis".equalsIgnoreCase(status)) {
      requestDetails.setCheckHanisVarificationPending("true");
      requestDetails.setCheckHoldRecord("Verification On Hold for Hanis");
      requestDetails.setCheckUnHoldRecord("Hanis Verification Pending");
    }

    if ("Admin Approval On Hold".equalsIgnoreCase(status)) {
      requestDetails.setCheckAdminApprovePending("true");
      requestDetails.setCheckHoldRecord("Admin Approval On Hold");
      requestDetails.setCheckUnHoldRecord("Admin Approval Pending");
    }

    if ("Update on Hold for Hogan".equalsIgnoreCase(status)) {
      requestDetails.setCheckHoganUpdatePending("true");
      requestDetails.setCheckHoldRecord("Update on Hold for Hogan");
      requestDetails.setCheckUnHoldRecord("Hogan Update Pending");
    }

    if ("Update on Hold for Documentum".equalsIgnoreCase(status)) {
      requestDetails.setCheckDocumentUpdatePending("true");
      requestDetails.setCheckHoldRecord("Update on Hold for Documentum");
      requestDetails.setCheckUnHoldRecord("Documentum Update Pending");
    }

    httpSession.setAttribute("RequestDetails", requestDetails);
  }
}
