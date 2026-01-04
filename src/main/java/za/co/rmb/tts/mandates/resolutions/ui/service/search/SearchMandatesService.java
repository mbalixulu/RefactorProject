package za.co.rmb.tts.mandates.resolutions.ui.service.search;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestTableWrapper;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestStagingDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestTableDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling search and display of mandate/resolution requests.
 * 
 * This service supports the following screens/features:
 * - Draft requests table display
 * - In-progress requests table display
 * - Completed requests table display
 * - On-hold requests table display
 * - Request filtering and pagination
 * - Export requests to CSV
 * 
 * Controller methods that delegate to this service:
 * - draftRequests()
 * - inProgressRequests()
 * - completedRequests()
 * - onHoldRequests()
 * - requestTable()
 * - requestTableOnHold()
 * - requestTableCompleted()
 * - exportCSV()
 * - exportRequests()
 * 
 * Extracted from MandatesResolutionUIController for separation of concerns.
 */
@Service
public class SearchMandatesService {

    private final HttpSession httpSession;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mandates-resolutions-dao}")
    private String mandatesResolutionsDaoURL;

    public SearchMandatesService(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    /**
     * Build a table wrapper with draft requests filtered by user role.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public RequestTableWrapper buildDraftRequestsTable(List<RequestStagingDTO> allDrafts) {
        RequestTableWrapper wrapper = new RequestTableWrapper();
        List<RequestTableDTO> rows = new ArrayList<>();
        RequestDTO requestDTO = new RequestDTO();
        UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");

        if ("ADMIN".equalsIgnoreCase(user.getUserRole())) {
            requestDTO.setSubStatus("Admin");
        } else {
            requestDTO.setSubStatus("User");
        }

        for (RequestStagingDTO src : allDrafts) {
            RequestTableDTO r = new RequestTableDTO();
            if ("ADMIN".equalsIgnoreCase(user.getUserRole())) {
                r.setRequestId(src.getStagingId());
                r.setCompanyName(src.getCompanyName());
                r.setRegistrationNumber(src.getCompanyRegistrationNumber());
                r.setStatus(src.getRequestStatus());
                r.setSubStatus(cleanSubStatus(src.getRequestSubStatus()));
                r.setType(src.getRequestType());
                r.setCreated(String.valueOf(src.getCreated()));
                rows.add(r);
            } else if ("USER".equalsIgnoreCase(user.getUserRole())
                       && src.getCreator().equalsIgnoreCase(user.getUsername())) {
                r.setRequestId(src.getStagingId());
                r.setCompanyName(src.getCompanyName());
                r.setRegistrationNumber(src.getCompanyRegistrationNumber());
                r.setStatus(src.getRequestStatus());
                r.setSubStatus(cleanSubStatus(src.getRequestSubStatus()));
                r.setType(src.getRequestType());
                r.setCreated(String.valueOf(src.getCreated()));
                rows.add(r);
            }
        }

        wrapper.setRequest(rows);
        wrapper.setRequestDTO(requestDTO);
        return wrapper;
    }

    /**
     * Clean substatus by removing "Pending" suffix.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public String cleanSubStatus(String subStatus) {
        if (subStatus == null || subStatus.trim().isEmpty()) {
            return subStatus;
        }
        String cleaned = subStatus.trim();
        if (cleaned.endsWith(" Pending")) {
            cleaned = cleaned.substring(0, cleaned.length() - " Pending".length()).trim();
        }
        return cleaned;
    }

    /**
     * Get all draft requests from the DAO.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public List<RequestStagingDTO> getAllDrafts() {
        String url = mandatesResolutionsDaoURL + "/api/request-staging";
        ResponseEntity<RequestStagingDTO[]> response = 
            restTemplate.getForEntity(url, RequestStagingDTO[].class);
        
        if (response.getBody() != null) {
            return List.of(response.getBody());
        }
        return new ArrayList<>();
    }

    /**
     * Filter requests by status for table display.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public List<RequestTableDTO> filterRequestsByStatus(List<RequestTableDTO> allRequests, String status) {
        List<RequestTableDTO> filtered = new ArrayList<>();
        for (RequestTableDTO req : allRequests) {
            if (status.equalsIgnoreCase(req.getStatus())) {
                filtered.add(req);
            }
        }
        return filtered;
    }
}
