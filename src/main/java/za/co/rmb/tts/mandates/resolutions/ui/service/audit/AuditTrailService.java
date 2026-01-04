package za.co.rmb.tts.mandates.resolutions.ui.service.audit;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import za.co.rmb.tts.mandates.resolutions.ui.model.CommentModel;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.CommentDto;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.UserDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling audit trail and request management workflows.
 * 
 * This service supports the following screens/features:
 * - View request details and history
 * - Comments management (add, view comments)
 * - Approval/rejection workflow
 * - Hold/unhold functionality for requests
 * - Request reassignment
 * - Status updates and tracking
 * 
 * Controller methods that delegate to this service:
 * - adminView()
 * - updateAdminView()
 * - adminReassign()
 * - statusRejected()
 * - approveRejectRequest()
 * - holdUnholdRequest()
 * 
 * Extracted from MandatesResolutionUIController for separation of concerns.
 */
@Service
public class AuditTrailService {

    private final HttpSession httpSession;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mandates-resolutions-dao}")
    private String mandatesResolutionsDaoURL;

    public AuditTrailService(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    /**
     * Fetch request details by request ID from the DAO.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public RequestDTO getRequestById(Long requestId) {
        String url = mandatesResolutionsDaoURL + "/api/request/" + requestId;
        ResponseEntity<RequestDTO> response = restTemplate.getForEntity(url, RequestDTO.class);
        return response.getBody();
    }

    /**
     * Fetch all comments for a request.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public List<CommentModel> getCommentsForRequest(Long requestId) {
        String url = mandatesResolutionsDaoURL + "/api/comments/request/" + requestId;
        try {
            ResponseEntity<CommentDto[]> response = restTemplate.getForEntity(url, CommentDto[].class);
            if (response.getBody() != null) {
                List<CommentModel> comments = new ArrayList<>();
                for (CommentDto dto : response.getBody()) {
                    CommentModel model = new CommentModel();
                    model.setCommentId(dto.getCommentId());
                    model.setCommentText(dto.getCommentText());
                    model.setCreator(dto.getCreator());
                    model.setCreated(dto.getCreated());
                    comments.add(model);
                }
                return comments;
            }
        } catch (Exception e) {
            // Return empty list if fetching comments fails
        }
        return new ArrayList<>();
    }

    /**
     * Add a comment to a request.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void addCommentToRequest(Long requestId, String commentText) {
        UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
        String creator = user != null ? user.getUsername() : "Unknown";

        CommentDto comment = new CommentDto();
        comment.setRequestId(requestId);
        comment.setCommentText(commentText);
        comment.setCreator(creator);
        comment.setCreated(LocalDateTime.now());

        String url = mandatesResolutionsDaoURL + "/api/comments";
        restTemplate.postForEntity(url, comment, CommentDto.class);
    }

    /**
     * Update request status (for approval/rejection).
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void updateRequestStatus(Long requestId, String status, String subStatus, String outcome) {
        RequestDTO request = new RequestDTO();
        request.setRequestId(requestId);
        request.setStatus(status);
        request.setSubStatus(subStatus);
        
        UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
        if (user != null) {
            request.setUpdator(user.getUsername());
        }

        String url = mandatesResolutionsDaoURL + "/api/request/" + requestId;
        restTemplate.exchange(url, HttpMethod.PUT, 
            new org.springframework.http.HttpEntity<>(request), 
            Void.class);
    }

    /**
     * Reassign a request to another user.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void reassignRequest(Long requestId, String assignedUser) {
        RequestDTO request = new RequestDTO();
        request.setRequestId(requestId);
        request.setAssignedUser(assignedUser);

        UserDTO user = (UserDTO) httpSession.getAttribute("currentUser");
        if (user != null) {
            request.setUpdator(user.getUsername());
        }

        String url = mandatesResolutionsDaoURL + "/api/request/" + requestId;
        restTemplate.exchange(url, HttpMethod.PUT, 
            new org.springframework.http.HttpEntity<>(request), 
            Void.class);
    }

    /**
     * Put a request on hold.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void holdRequest(Long requestId, String currentSubStatus) {
        updateRequestStatus(requestId, "On Hold", toHoldLabel(currentSubStatus), null);
    }

    /**
     * Remove a request from hold.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public void unholdRequest(Long requestId, String holdLabel) {
        String pendingSubStatus = fromHoldLabel(holdLabel);
        updateRequestStatus(requestId, "In Progress", pendingSubStatus, null);
    }

    /**
     * Convert a pending substatus to its hold label.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    private String toHoldLabel(String currentSubStatus) {
        if (currentSubStatus == null || currentSubStatus.isBlank()) {
            return "Verification On Hold for Hogan";
        }
        
        String canonical = canonical(currentSubStatus);
        return switch (canonical) {
            case "Windeed Verification Pending" -> "Verification On Hold for Windeed";
            case "Hogan Verification Pending" -> "Verification On Hold for Hogan";
            case "Hanis Verification Pending" -> "Verification On Hold for Hanis";
            case "Admin Approval Pending" -> "Admin Approval On Hold";
            case "Hogan Update Pending" -> "Update on Hold for Hogan";
            case "Documentum Update Pending" -> "Update on Hold for Documentum";
            default -> "Admin Approval On Hold";
        };
    }

    /**
     * Convert a hold label back to its pending substatus.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    private String fromHoldLabel(String holdLabel) {
        if (holdLabel == null) {
            return "Hogan Verification Pending";
        }
        
        String s = holdLabel.trim();
        return switch (s) {
            case "Verification On Hold for Windeed" -> "Windeed Verification Pending";
            case "Verification On Hold for Hogan" -> "Hogan Verification Pending";
            case "Verification On Hold for Hanis" -> "Hanis Verification Pending";
            case "Admin Approval On Hold" -> "Admin Approval Pending";
            case "Update on Hold for Hogan" -> "Hogan Update Pending";
            case "Update on Hold for Documentum" -> "Documentum Update Pending";
            default -> "Hogan Verification Pending";
        };
    }

    /**
     * Canonicalize substatus values.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    private String canonical(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return t;
        }

        // Legacy/typo fixes
        if (t.equalsIgnoreCase("Hannis Verification Pending")) {
            return "Hanis Verification Pending";
        }
        if (t.equalsIgnoreCase("Admin Verification Pending")) {
            return "Admin Approval Pending";
        }
        if (t.equalsIgnoreCase("Submitted")) {
            return "Hogan Verification Pending";
        }

        return t;
    }

    /**
     * Get the next substatus in the approval workflow.
     * Extracted from MandatesResolutionUIController for separation of concerns.
     */
    public String getNextSubStatus(String current, boolean approve) {
        String c = canonical(current);
        
        if (approve) {
            return switch (c) {
                case "Hogan Verification Pending" -> "Windeed Verification Pending";
                case "Windeed Verification Pending" -> "Hanis Verification Pending";
                case "Hanis Verification Pending" -> "Admin Approval Pending";
                case "Admin Approval Pending" -> "Hogan Update Pending";
                case "Hogan Update Pending" -> "Documentum Update Pending";
                case "Documentum Update Pending" -> "Request Updated Successfully";
                case "Request Updated Successfully" -> "Request Updated Successfully";
                default -> c;
            };
        } else {
            return "Rejected";
        }
    }
}
