package za.co.rmb.tts.mandates.resolutions.ui.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.SubmissionPayload;

/**
 * Service for building submission payloads from UI data.
 * 
 * <p>This service handles the complex transformation of UI RequestDTO objects
 * into backend SubmissionPayload objects, applying business rules and mappings.</p>
 */
@Service
public class SubmissionPayloadService {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionPayloadService.class);

  private static final String TYPE_MANDATES = "Mandates";
  private static final String TYPE_RESOLUTIONS = "Resolutions";
  private static final String TYPE_BOTH = "Both";

  @Autowired
  private UtilityService utilityService;

  @Autowired
  private WorkflowManagementService workflowManagementService;

  /**
   * Builds a submission payload from UI request data.
   * 
   * <p>This method transforms a RequestDTO into a SubmissionPayload, applying
   * all necessary business rules, type normalization, and data validation.</p>
   *
   * @param ui              the UI request DTO containing user input
   * @param requestTypeRaw  the raw request type string from UI
   * @return a complete SubmissionPayload ready for backend submission
   */
  public SubmissionPayload buildSubmissionPayload(RequestDTO ui, String requestTypeRaw) {
    SubmissionPayload out = new SubmissionPayload();

    // --- Company ---
    SubmissionPayload.Company c = new SubmissionPayload.Company();
    c.setRegistrationNumber(utilityService.nz(ui.getRegistrationNumber()));
    c.setName(utilityService.nz(ui.getCompanyName()));
    c.setAddress(utilityService.nz(ui.getCompanyAddress()));
    c.setCreator(utilityService.nz(ui.getLoggedInUsername()));
    c.setUpdator(utilityService.nz(ui.getLoggedInUsername()));
    out.setCompany(c);

    // --- Request (normalize "1|2|3" -> "Mandates|Resolutions|Both") ---
    String typeNorm = mapRequestType(requestTypeRaw);
    if (typeNorm == null) {
      String t = utilityService.nz(requestTypeRaw).toLowerCase();
      if (t.contains("both")) {
        typeNorm = TYPE_BOTH;
      } else if (t.contains("resol")) {
        typeNorm = TYPE_RESOLUTIONS;
      } else if (t.contains("mandate")) {
        typeNorm = TYPE_MANDATES;
      } else {
        typeNorm = TYPE_MANDATES; // default
      }
    }

    // Ensure a non-blank creator (also used for assignedUser)
    String creator = utilityService.nz(ui.getLoggedInUsername());
    if (creator.isBlank()) {
      creator = utilityService.nz(ui.getLoggedInEmail());
    }
    if (creator.isBlank()) {
      creator = "UI_USER";
    }

    SubmissionPayload.Request r = new SubmissionPayload.Request();
    r.setSla(3);

    // IMPORTANT: exact strings expected by DAO validation
    r.setStatus("In Progress");
    r.setType(typeNorm);

    // SubStatus: use UI value if already allowed, else choose an initial valid one
    String uiSub = utilityService.nz(ui.getSubStatus());
    String startSub = workflowManagementService.isAllowedSubstatus(uiSub)
        ? uiSub
        : workflowManagementService.initialSubStatusForType(typeNorm);
    r.setSubStatus(startSub);

    // Outcome should be null on create; DAO allows null for processOutcome
    r.setCreator(creator);
    r.setAssignedUser(creator);
    out.setRequest(r);

    // --- Accounts + nested signatories ---
    List<SubmissionPayload.Account> payloadAccounts = buildAccountsPayload(ui, creator);
    out.setAccounts(payloadAccounts);

    // --- Authorities (Directors) ---
    List<SubmissionPayload.Authority> payloadAuthorities =
        buildAuthoritiesPayload(ui, typeNorm, creator);
    out.setAuthorities(payloadAuthorities);

    int accCount = out.getAccounts() == null ? 0 : out.getAccounts().size();
    int sigCount = out.getAccounts() == null
        ? 0
        : out.getAccounts().stream()
            .mapToInt(a -> a.getSignatories() == null ? 0 : a.getSignatories().size())
            .sum();

    logger.info(
        "buildSubmissionPayload -> type={}, status={}, subStatus={}, accounts={}, "
            + "signatories(total)={}, authorities(directors)={}",
        typeNorm, r.getStatus(), r.getSubStatus(), accCount, sigCount,
        payloadAuthorities.size());

    return out;
  }

  /**
   * Builds the accounts section of the submission payload.
   * 
   * <p>Processes both nested accounts with signatories and legacy flat signatory lists.</p>
   *
   * @param ui      the UI request DTO
   * @param creator the creator username
   * @return list of payload accounts
   */
  private List<SubmissionPayload.Account> buildAccountsPayload(RequestDTO ui, String creator) {
    List<SubmissionPayload.Account> payloadAccounts = new ArrayList<>();

    if (ui.getAccounts() != null && !ui.getAccounts().isEmpty()) {
      for (RequestDTO.Account a : ui.getAccounts()) {
        if (a == null) {
          continue;
        }

        SubmissionPayload.Account pa = new SubmissionPayload.Account();
        pa.setAccountName(utilityService.nz(a.getAccountName()));
        pa.setAccountNumber(utilityService.nz(a.getAccountNumber()));
        pa.setCreator(creator);
        pa.setUpdator(creator);

        List<SubmissionPayload.Signatory> ps = new ArrayList<>();
        if (a.getSignatories() != null) {
          for (RequestDTO.Signatory s : a.getSignatories()) {
            if (s == null || isEmptySignatory(s)) {
              continue;
            }

            SubmissionPayload.Signatory ns = new SubmissionPayload.Signatory();
            ns.setFullName(utilityService.nz(s.getFullName()));
            ns.setIdNumber(utilityService.nz(s.getIdNumber()));
            ns.setInstructions(
                workflowManagementService.normalizeInstruction(
                    utilityService.nz(s.getInstruction())));
            ns.setCapacity(utilityService.nz(s.getCapacity()));
            ns.setGroupCategory(utilityService.nz(s.getGroup()));
            ns.setInstructionsDate(LocalDateTime.now());
            ns.setCreator(creator);
            ns.setUpdator(creator);
            ps.add(ns);
          }
        }
        pa.setSignatories(ps);

        if (!utilityService.nz(pa.getAccountName()).isEmpty()
            || !utilityService.nz(pa.getAccountNumber()).isEmpty()
            || (ps != null && !ps.isEmpty())) {
          payloadAccounts.add(pa);
        }
      }
    }

    // Fallback: legacy flat signatories -> single "Unknown" account
    if (payloadAccounts.isEmpty() && ui.getSignatories() != null
        && !ui.getSignatories().isEmpty()) {
      SubmissionPayload.Account pa = new SubmissionPayload.Account();
      pa.setAccountName("Unknown");
      pa.setAccountNumber("");
      pa.setCreator(creator);
      pa.setUpdator(creator);

      List<SubmissionPayload.Signatory> ps = new ArrayList<>();
      for (RequestDTO.Signatory s : ui.getSignatories()) {
        if (s == null || isEmptySignatory(s)) {
          continue;
        }

        SubmissionPayload.Signatory ns = new SubmissionPayload.Signatory();
        ns.setFullName(utilityService.nz(s.getFullName()));
        ns.setIdNumber(utilityService.nz(s.getIdNumber()));
        ns.setInstructions(
            workflowManagementService.normalizeInstruction(
                utilityService.nz(s.getInstruction())));
        ns.setCapacity(utilityService.nz(s.getCapacity()));
        ns.setGroupCategory(utilityService.nz(s.getGroup()));
        ns.setInstructionsDate(LocalDateTime.now());
        ns.setCreator(creator);
        ns.setUpdator(creator);
        ps.add(ns);
      }
      pa.setSignatories(ps);
      payloadAccounts.add(pa);
    }

    return payloadAccounts;
  }

  /**
   * Builds the authorities (directors) section of the submission payload.
   * 
   * <p>Determines whether to include authorities based on request type and
   * whether director input was provided.</p>
   *
   * @param ui       the UI request DTO
   * @param typeNorm the normalized request type
   * @param creator  the creator username
   * @return list of payload authorities
   */
  private List<SubmissionPayload.Authority> buildAuthoritiesPayload(RequestDTO ui,
                                                                     String typeNorm,
                                                                     String creator) {
    List<SubmissionPayload.Authority> payloadAuthorities = new ArrayList<>();

    boolean hasDirectorsInput =
        ui.getDirectors() != null && ui.getDirectors().stream().anyMatch(d -> d != null
            && !(utilityService.nz(d.getName()).isEmpty()
                 && utilityService.nz(d.getSurname()).isEmpty()
                 && utilityService.nz(d.getDesignation()).isEmpty()));

    boolean includeAuthorities =
        TYPE_RESOLUTIONS.equals(typeNorm)
        || TYPE_BOTH.equals(typeNorm)
        || (TYPE_MANDATES.equals(typeNorm) && hasDirectorsInput);

    if (includeAuthorities && ui.getDirectors() != null) {
      for (RequestDTO.Director d : ui.getDirectors()) {
        if (d == null) {
          continue;
        }
        boolean allBlank = utilityService.nz(d.getName()).isEmpty()
            && utilityService.nz(d.getSurname()).isEmpty()
            && utilityService.nz(d.getDesignation()).isEmpty();
        if (allBlank) {
          continue;
        }

        SubmissionPayload.Authority a = new SubmissionPayload.Authority();
        a.setFirstname(utilityService.nz(d.getName()));
        a.setSurname(utilityService.nz(d.getSurname()));
        a.setDesignation(utilityService.nz(d.getDesignation()));
        a.setInstructions(
            workflowManagementService.normalizeInstruction(
                utilityService.nz(d.getInstruction())));
        a.setCreator(creator);
        a.setUpdator(creator);
        payloadAuthorities.add(a);
      }
    }

    return payloadAuthorities;
  }

  /**
   * Maps raw request type strings to normalized backend enum values.
   *
   * @param raw the raw request type string
   * @return normalized type or null if no mapping found
   */
  private String mapRequestType(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String r = raw.trim();
    if ("1".equals(r) || "Mandate".equalsIgnoreCase(r) || "Mandates".equalsIgnoreCase(r)) {
      return TYPE_MANDATES;
    }
    if ("2".equals(r) || "Resolution".equalsIgnoreCase(r)
        || "Resolutions".equalsIgnoreCase(r)) {
      return TYPE_RESOLUTIONS;
    }
    if ("3".equals(r) || "Both".equalsIgnoreCase(r)
        || "Mandate And Resolution".equalsIgnoreCase(r)
        || "Mandate and Resolution".equalsIgnoreCase(r)) {
      return TYPE_BOTH;
    }
    return null;
  }

  /**
   * Checks if a signatory has all blank/empty fields.
   *
   * @param s the signatory to check
   * @return true if all fields are blank, false otherwise
   */
  private boolean isEmptySignatory(RequestDTO.Signatory s) {
    return utilityService.nz(s.getFullName()).isEmpty()
        && utilityService.nz(s.getIdNumber()).isEmpty()
        && utilityService.nz(s.getInstruction()).isEmpty()
        && utilityService.nz(s.getCapacity()).isEmpty()
        && utilityService.nz(s.getGroup()).isEmpty();
  }
}
