package za.co.rmb.tts.mandates.resolutions.ui.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service class providing page generation and error page rendering utilities.
 * Handles creation of XML-formatted error pages and response entities.
 */
@Service
public class PageGenerationService {

  /**
   * Generates a simple error page with a message.
   */
  public String generateErrorPage(String message) {
    return "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "id=\"\" heading=\" \" template=\"error\" version=\"1\">"
        + "<error xsi:type=\"validationError\">"
        + "<code>0</code>"
        + "<message>" + escapeXml(message) + "</message>"
        + "</error>"
        + "</page>";
  }

  /**
   * Generates an error page for file upload errors.
   */
  public String generateFileUploadErrorPage(String message) {
    return "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "id=\"\" heading=\" \" template=\"error\" version=\"1\">"
        + "<error xsi:type=\"validationError\">"
        + "<code>FILE_UPLOAD_ERROR</code>"
        + "<message>" + escapeXml(message) + "</message>"
        + "</error>"
        + "</page>";
  }

  /**
   * Renders a simple error page with custom heading, message, and back URL.
   */
  public String renderSimpleErrorPage(String heading, String message, String backUrl) {
    StringBuilder xml = new StringBuilder();
    xml.append("<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
    xml.append("id=\"ERROR\" heading=\"").append(escapeXml(heading)).append("\" ");
    xml.append("template=\"simple-error\" version=\"1\">");
    xml.append("<error xsi:type=\"validationError\">");
    xml.append("<code>USER_ERROR</code>");
    xml.append("<message>").append(escapeXml(message)).append("</message>");
    if (backUrl != null && !backUrl.isBlank()) {
      xml.append("<backUrl>").append(escapeXml(backUrl)).append("</backUrl>");
    }
    xml.append("</error>");
    xml.append("</page>");
    return xml.toString();
  }

  /**
   * Creates a ResponseEntity with validation error message.
   */
  public ResponseEntity<String> validationErrorResponse(String message) {
    String errorPage = generateErrorPage(message);
    return ResponseEntity.ok(errorPage);
  }

  /**
   * Generates error page content for simple errors.
   */
  public String errorPageContent(String message) {
    return "<page xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + " id=\"\" heading=\" \" template=\"error\" version=\"1\">"
        + "<error xsi:type=\"validationError\">"
        + "<code>0</code>"
        + "<message>" + escapeXml(message) + "</message>"
        + "</error>"
        + "</page>";
  }

  /**
   * Escapes XML special characters to prevent injection.
   */
  private String escapeXml(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  /**
   * Builds XML page path for template files.
   */
  public String xmlPagePath(String pageName) {
    return "/templates/xml/" + pageName + ".xml";
  }

  /**
   * Builds XSL page path for stylesheet files.
   */
  public String xslPagePath(String pageName) {
    return "/templates/xsl/" + pageName + ".xsl";
  }
}
