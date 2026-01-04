package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;

/**
 * Service for transforming and mapping data between different representations.
 * 
 * <p>This service handles complex data transformations, especially for parsing
 * HTTP request parameters into structured DTOs.</p>
 */
@Service
public class DataTransformationService {

  @Autowired
  private UtilityService utilityService;

  /**
   * Parses account information from HTTP request parameters.
   * 
   * <p>This method extracts account details including account names, numbers,
   * and nested signatory information from HTTP request parameters following
   * the pattern: accountName_X, accountNo_X, fullName_X_Y, idNumber_X_Y, etc.</p>
   *
   * @param request the HTTP servlet request containing form parameters
   * @return list of Account objects populated from request parameters
   */
  public List<RequestDTO.Account> parseAccountsFromRequest(HttpServletRequest request) {
    Pattern accNamePat = Pattern.compile("^accountName_(\\d+)$");
    Pattern accNoPat = Pattern.compile("^accountNo_(\\d+)$");
    Pattern fullNamePat = Pattern.compile("^fullName_(\\d+)_(\\d+)$");
    Pattern idNumPat = Pattern.compile("^idNumber_(\\d+)_(\\d+)$");
    Pattern instrPat = Pattern.compile("^instruction_(\\d+)_(\\d+)$");
    Pattern capacityPat = Pattern.compile("^capacity_(\\d+)_(\\d+)$");
    Pattern groupPat = Pattern.compile("^group_(\\d+)_(\\d+)$");

    SortedSet<Integer> accountIdxs = new TreeSet<>();
    for (String paramName : request.getParameterMap().keySet()) {
      Matcher accNameM = accNamePat.matcher(paramName);
      Matcher accNoM = accNoPat.matcher(paramName);
      if (accNameM.matches()) {
        accountIdxs.add(Integer.parseInt(accNameM.group(1)));
      }
      if (accNoM.matches()) {
        accountIdxs.add(Integer.parseInt(accNoM.group(1)));
      }
    }

    List<RequestDTO.Account> out = new ArrayList<>();
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
      a.setSignatories(new ArrayList<>());

      SortedSet<Integer> sortedIdx = new TreeSet<>();
      for (String paramName : request.getParameterMap().keySet()) {
        Matcher fullNameMatcher = fullNamePat.matcher(paramName);
        Matcher idNumMatcher = idNumPat.matcher(paramName);
        Matcher instrMatcher = instrPat.matcher(paramName);
        Matcher capacityMatcher = capacityPat.matcher(paramName);
        Matcher groupMatcher = groupPat.matcher(paramName);

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
          sig.setInstruction(in.trim());
        }
        if (cp != null) {
          sig.setCapacity(cp.trim());
        }
        if (gp != null) {
          sig.setGroup(gp.trim());
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

  /**
   * Parses director information from HTTP request parameters.
   * 
   * <p>This method extracts director details including name, surname, designation,
   * and instructions from HTTP request parameters following the pattern:
   * directorName_X, directorSurname_X, directorDesignation_X, directorInstruction_X.</p>
   *
   * @param request the HTTP servlet request containing form parameters
   * @return list of Director objects populated from request parameters
   */
  public List<RequestDTO.Director> parseDirectorsFromRequest(HttpServletRequest request) {
    Pattern namePat = Pattern.compile("^directorName_(\\d+)$");
    Pattern surPat = Pattern.compile("^directorSurname_(\\d+)$");
    Pattern desPat = Pattern.compile("^directorDesignation_(\\d+)$");
    Pattern insPat = Pattern.compile("^directorInstruction_(\\d+)$");

    SortedSet<Integer> idxs = new TreeSet<>();
    for (String p : request.getParameterMap().keySet()) {
      Matcher m1 = namePat.matcher(p);
      Matcher m2 = surPat.matcher(p);
      Matcher m3 = desPat.matcher(p);
      Matcher m4 = insPat.matcher(p);
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
        idxs.add(Integer.parseInt(m4.group(1)));
      }
    }

    List<RequestDTO.Director> out = new ArrayList<>();
    for (Integer i : idxs) {
      RequestDTO.Director d = new RequestDTO.Director();
      String nm = request.getParameter("directorName_" + i);
      String sn = request.getParameter("directorSurname_" + i);
      String dg = request.getParameter("directorDesignation_" + i);
      String in = request.getParameter("directorInstruction_" + i);
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

  /**
   * Merges account data by position, only updating non-blank fields.
   * 
   * <p>This method performs a position-based merge, preserving existing values
   * in the target when the source has blank values.</p>
   *
   * @param target the target RequestDTO to merge into
   * @param parsed the parsed account list from request
   */
  public void mergeAccounts(RequestDTO target, List<RequestDTO.Account> parsed) {
    if (parsed == null || parsed.isEmpty()) {
      return;
    }
    if (target.getAccounts() == null) {
      target.setAccounts(new ArrayList<>());
    }

    for (int i = 0; i < parsed.size(); i++) {
      RequestDTO.Account src = parsed.get(i);
      while (target.getAccounts().size() <= i) {
        target.getAccounts().add(createBlankAccount());
      }
      RequestDTO.Account dst = target.getAccounts().get(i);

      if (utilityService.nz(src.getAccountName()).length() > 0) {
        dst.setAccountName(utilityService.nz(src.getAccountName()));
      }
      String srcNo = getAccountNumber(src);
      if (utilityService.nz(srcNo).length() > 0) {
        setAccountNumber(dst, srcNo);
      }

      if (src.getSignatories() != null && !src.getSignatories().isEmpty()) {
        if (dst.getSignatories() == null) {
          dst.setSignatories(new ArrayList<>());
        }
        while (dst.getSignatories().size() < src.getSignatories().size()) {
          dst.getSignatories().add(createBlankSignatory());
        }

        for (int s = 0; s < src.getSignatories().size(); s++) {
          RequestDTO.Signatory ss = src.getSignatories().get(s);
          RequestDTO.Signatory dd = dst.getSignatories().get(s);
          if (utilityService.nz(ss.getFullName()).length() > 0) {
            dd.setFullName(utilityService.nz(ss.getFullName()));
          }
          if (utilityService.nz(ss.getIdNumber()).length() > 0) {
            dd.setIdNumber(utilityService.nz(ss.getIdNumber()));
          }
          if (utilityService.nz(ss.getInstruction()).length() > 0) {
            dd.setInstruction(utilityService.nz(ss.getInstruction()));
          }
          if (utilityService.nz(ss.getCapacity()).length() > 0) {
            dd.setCapacity(utilityService.nz(ss.getCapacity()));
          }
          if (utilityService.nz(ss.getGroup()).length() > 0) {
            dd.setGroup(utilityService.nz(ss.getGroup()));
          }
        }
      }
    }
  }

  /**
   * Merges director data by position, only updating non-blank fields.
   * 
   * <p>This method performs a position-based merge for directors,
   * preserving existing values when the source has blank values.</p>
   *
   * @param target the target RequestDTO to merge into
   * @param parsed the parsed director list from request
   */
  public void mergeDirectorsByPosition(RequestDTO target, List<RequestDTO.Director> parsed) {
    if (parsed == null || parsed.isEmpty()) {
      return;
    }
    if (target.getDirectors() == null) {
      target.setDirectors(new ArrayList<>());
    }
    List<RequestDTO.Director> dst = target.getDirectors();
    for (int i = 0; i < parsed.size(); i++) {
      while (dst.size() <= i) {
        dst.add(createBlankDirector());
      }
      RequestDTO.Director s = parsed.get(i);
      RequestDTO.Director d = dst.get(i);
      if (utilityService.nz(s.getName()).length() > 0) {
        d.setName(utilityService.nz(s.getName()));
      }
      if (utilityService.nz(s.getSurname()).length() > 0) {
        d.setSurname(utilityService.nz(s.getSurname()));
      }
      if (utilityService.nz(s.getDesignation()).length() > 0) {
        d.setDesignation(utilityService.nz(s.getDesignation()));
      }
      if (utilityService.nz(s.getInstruction()).length() > 0) {
        d.setInstruction(utilityService.nz(s.getInstruction()));
      }
    }
  }

  /**
   * Creates a blank Account object with empty values.
   *
   * @return a new blank Account instance
   */
  private RequestDTO.Account createBlankAccount() {
    RequestDTO.Account a = new RequestDTO.Account();
    a.setAccountName("");
    setAccountNumber(a, "");
    a.setSignatories(new ArrayList<>());
    return a;
  }

  /**
   * Creates a blank Director object with empty values.
   *
   * @return a new blank Director instance
   */
  private RequestDTO.Director createBlankDirector() {
    RequestDTO.Director d = new RequestDTO.Director();
    d.setName("");
    d.setSurname("");
    d.setDesignation("");
    d.setInstruction("");
    return d;
  }

  /**
   * Creates a blank Signatory object with empty values.
   *
   * @return a new blank Signatory instance
   */
  private RequestDTO.Signatory createBlankSignatory() {
    RequestDTO.Signatory s = new RequestDTO.Signatory();
    s.setFullName("");
    s.setIdNumber("");
    s.setInstruction("");
    s.setCapacity("");
    s.setGroup("");
    return s;
  }

  /**
   * Retrieves the last non-blank value for a parameter name from the request.
   *
   * @param request   the HTTP servlet request
   * @param paramName the parameter name
   * @return the last non-blank value or null
   */
  private String lastNonBlank(HttpServletRequest request, String paramName) {
    String[] arr = request.getParameterValues(paramName);
    if (arr == null || arr.length == 0) {
      return null;
    }
    String last = null;
    for (String v : arr) {
      if (v != null && !v.isBlank()) {
        last = v;
      }
    }
    return last;
  }

  /**
   * Sets the account number on an Account object.
   * 
   * <p>This method exists to centralize account number setting logic.</p>
   *
   * @param account the account object
   * @param number  the account number to set
   */
  private void setAccountNumber(RequestDTO.Account account, String number) {
    account.setAccountNumber(number);
  }

  /**
   * Gets the account number from an Account object.
   * 
   * <p>This method exists to centralize account number retrieval logic.</p>
   *
   * @param account the account object
   * @return the account number
   */
  private String getAccountNumber(RequestDTO.Account account) {
    return account.getAccountNumber();
  }
}
