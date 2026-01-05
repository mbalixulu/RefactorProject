package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestStagingDTO;

/**
 * Service for data transformation operations including parsing request parameters
 * and converting between different data formats.
 */
@Service
public class DataTransformationService {

  /**
   * Parse directors from request parameters with suffix pattern.
   */
  public List<RequestDTO.Director> parseDirectorsFromParamsSuffix(Map<String, String[]> params) {
    Pattern p = Pattern.compile("^directors\\[(\\d+)\\]\\.(name|surname|designation)$");
    SortedSet<Integer> indices = new TreeSet<>();
    
    for (String key : params.keySet()) {
      Matcher m = p.matcher(key);
      if (m.matches()) {
        indices.add(Integer.parseInt(m.group(1)));
      }
    }
    
    List<RequestDTO.Director> result = new ArrayList<>();
    for (Integer i : indices) {
      RequestDTO.Director d = new RequestDTO.Director();
      String[] nameArr = params.get("directors[" + i + "].name");
      String[] surnameArr = params.get("directors[" + i + "].surname");
      String[] desigArr = params.get("directors[" + i + "].designation");
      
      if (nameArr != null && nameArr.length > 0) {
        d.setName(nameArr[0]);
      }
      if (surnameArr != null && surnameArr.length > 0) {
        d.setSurname(surnameArr[0]);
      }
      if (desigArr != null && desigArr.length > 0) {
        d.setDesignation(desigArr[0]);
      }
      result.add(d);
    }
    return result;
  }

  /**
   * Parse directors from request parameters with generic pattern.
   */
  public List<RequestDTO.Director> parseDirectorsFromParamsGeneric(Map<String, String[]> params) {
    Pattern namePat = Pattern.compile("^directorName_(\\d+)$");
    Pattern surPat = Pattern.compile("^directorSurname_(\\d+)$");
    Pattern desPat = Pattern.compile("^directorDesignation_(\\d+)$");
    Pattern insPat = Pattern.compile("^directorInstruction_(\\d+)$");
    
    SortedSet<Integer> idxs = new TreeSet<>();
    for (String p : params.keySet()) {
      Matcher m1 = namePat.matcher(p);
      Matcher m2 = surPat.matcher(p);
      Matcher m3 = desPat.matcher(p);
      Matcher m4 = insPat.matcher(p);
      
      if (m1.matches()) idxs.add(Integer.parseInt(m1.group(1)));
      if (m2.matches()) idxs.add(Integer.parseInt(m2.group(1)));
      if (m3.matches()) idxs.add(Integer.parseInt(m3.group(1)));
      if (m4.matches()) idxs.add(Integer.parseInt(m4.group(1)));
    }
    
    List<RequestDTO.Director> out = new ArrayList<>();
    for (Integer i : idxs) {
      RequestDTO.Director d = new RequestDTO.Director();
      String[] nm = params.get("directorName_" + i);
      String[] sn = params.get("directorSurname_" + i);
      String[] dg = params.get("directorDesignation_" + i);
      String[] in = params.get("directorInstruction_" + i);
      
      if (nm != null && nm.length > 0) d.setName(nm[0].trim());
      if (sn != null && sn.length > 0) d.setSurname(sn[0].trim());
      if (dg != null && dg.length > 0) d.setDesignation(dg[0].trim());
      if (in != null && in.length > 0) d.setInstruction(in[0].trim());
      
      out.add(d);
    }
    return out;
  }

  /**
   * Parse documentum tools from request parameters.
   */
  public List<String> parseDocumentumToolsFromParams(Map<String, String[]> params) {
    List<String> tools = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      String key = "toolFive";
      switch (i) {
        case 1: key = "toolOne"; break;
        case 2: key = "toolTwo"; break;
        case 3: key = "toolThree"; break;
        case 4: key = "toolFour"; break;
        case 5: key = "toolFive"; break;
      }
      
      String[] vals = params.get(key);
      if (vals != null && vals.length > 0 && vals[0] != null && !vals[0].isBlank()) {
        tools.add(vals[0].trim());
      }
    }
    return tools;
  }

  /**
   * Parse accounts from request staging parameters.
   */
  public List<RequestStagingDTO.AccountDraft> parseAccountsFromParams(
      Map<String, String> params, HttpServletRequest request) {
    List<RequestStagingDTO.AccountDraft> accounts = new ArrayList<>();
    // Implementation would parse account-related parameters
    // This is a placeholder for the actual implementation
    return accounts;
  }

  /**
   * Parse directors from standard request parameters.
   */
  public List<RequestDTO.Director> parseDirectorsFromParams(Map<String, String[]> params) {
    Pattern namePat = Pattern.compile("^directorName_(\\d+)$");
    Pattern surPat = Pattern.compile("^directorSurname_(\\d+)$");
    Pattern desPat = Pattern.compile("^directorDesignation_(\\d+)$");
    Pattern insPat = Pattern.compile("^directorInstruction_(\\d+)$");
    
    SortedSet<Integer> idxs = new TreeSet<>();
    for (String p : params.keySet()) {
      Matcher m1 = namePat.matcher(p);
      Matcher m2 = surPat.matcher(p);
      Matcher m3 = desPat.matcher(p);
      Matcher m4 = insPat.matcher(p);
      
      if (m1.matches()) idxs.add(Integer.parseInt(m1.group(1)));
      if (m2.matches()) idxs.add(Integer.parseInt(m2.group(1)));
      if (m3.matches()) idxs.add(Integer.parseInt(m3.group(1)));
      if (m4.matches()) idxs.add(Integer.parseInt(m4.group(1)));
    }
    
    List<RequestDTO.Director> out = new ArrayList<>();
    for (Integer i : idxs) {
      RequestDTO.Director d = new RequestDTO.Director();
      String[] nm = params.get("directorName_" + i);
      String[] sn = params.get("directorSurname_" + i);
      String[] dg = params.get("directorDesignation_" + i);
      String[] in = params.get("directorInstruction_" + i);
      
      if (nm != null && nm.length > 0) d.setName(nm[0].trim());
      if (sn != null && sn.length > 0) d.setSurname(sn[0].trim());
      if (dg != null && dg.length > 0) d.setDesignation(dg[0].trim());
      if (in != null && in.length > 0) d.setInstruction(in[0].trim());
      
      out.add(d);
    }
    return out;
  }
}
