package za.co.rmb.tts.mandates.resolutions.ui.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestDTO;
import za.co.rmb.tts.mandates.resolutions.ui.model.RequestWrapper;

/**
 * Service for generating XML pages and building HTTP responses.
 * 
 * <p>This service handles page generation logic and response building
 * that was previously embedded in controller methods.</p>
 */
@Service
public class PageGenerationService {

  private static final String XSL_PAGE_PATH = "/templates/xsl/";

  @Autowired
  private XSLTProcessorService xsltProcessor;

  /**
   * Generates the XSL page path for a given page name.
   *
   * @param pageName the name of the page
   * @return the full XSL page path
   */
  public String xslPagePath(String pageName) {
    return XSL_PAGE_PATH + pageName + ".xsl";
  }

  /**
   * Generates an error page with the given message.
   *
   * @param message the error message
   * @return XML error page content
   */
  public String generateErrorPage(String message) {
    return """
               <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     id="" heading="Error" template="error" version="1">
                 <error xsi:type="validationError">
                   <code>ERR001</code>
                   <message>"""
           + xmlEscape(message)
           + "</message>\n"
           + "</error>\n"
           + "</page>";
  }

  /**
   * Renders a create request page with inline error message.
   *
   * @param registrationNumber the company registration number
   * @param message            the error message
   * @param code               the error code
   * @return ResponseEntity containing the generated page
   */
  public ResponseEntity<String> renderCreateRequestWithInline(String registrationNumber,
                                                               String message, String code) {
    RequestDTO dto = new RequestDTO();
    dto.setRegistrationNumber(registrationNumber == null ? "" : registrationNumber.trim());
    dto.setErrorMessage(message);
    dto.setErrorCode(code);
    RequestWrapper wrapper = new RequestWrapper();
    wrapper.setRequest(dto);
    String page = xsltProcessor.generatePage(xslPagePath("CreateRequest"), wrapper);
    return ResponseEntity.ok(page);
  }

  /**
   * Renders a simple error page with heading, message, and optional back URL.
   *
   * @param heading the page heading
   * @param message the error message
   * @param backUrl optional back URL (can be null)
   * @return XML error page content
   */
  public String renderSimpleErrorPage(String heading, String message, String backUrl) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
    sb.append("      id=\"error-page\" heading=\"").append(xmlEscape(heading)).append("\"\n");
    sb.append("      template=\"error\" version=\"1\">\n");
    sb.append("  <error xsi:type=\"validationError\">\n");
    sb.append("    <code>ERR001</code>\n");
    sb.append("    <message>").append(xmlEscape(message)).append("</message>\n");
    if (backUrl != null && !backUrl.isBlank()) {
      sb.append("    <backUrl>").append(xmlEscape(backUrl)).append("</backUrl>\n");
    }
    sb.append("  </error>\n");
    sb.append("</page>");
    return sb.toString();
  }

  /**
   * Generates a static error page with a predefined structure.
   *
   * @param message the error message to display
   * @return XML error page content
   */
  public String errorPage(String message) {
    return """
               <page xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     id="" heading=" " template="error" version="1">
                 <error xsi:type="validationError">
                   <code>0</code>
                   <message>"""
           + message
           + "</message>\n"
           + "</error>\n"
           + "</page>";
  }

  /**
   * Escapes XML special characters in a string.
   *
   * @param s the string to escape
   * @return XML-escaped string
   */
  private String xmlEscape(String s) {
    if (s == null || s.isEmpty()) {
      return s;
    }
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
