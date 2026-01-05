package za.co.rmb.tts.mandates.resolutions.ui.service;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import za.co.rmb.tts.mandates.resolutions.ui.model.dto.RequestDTO;

/**
 * Service for managing file uploads, downloads, and document storage operations.
 * 
 * <p>This service handles file operations including validation, session storage,
 * and persistence to the document management system.</p>
 */
@Service
public class FileManagementService {

  private static final long MAX_FILE_BYTES = 10L * 1024L * 1024L; // 10MB

  @Value("${tts.dms.base-url}")
  private String ttsDmsBaseUrl;

  @Autowired
  @Qualifier("defaultRestTemplate")
  private RestTemplate restTemplate;

  /**
   * Session file wrapper for storing uploaded files in HTTP session.
   */
  public static final class SessionFile implements java.io.Serializable {
    private final String name;
    private final String contentType;
    private final long size;
    private final byte[] bytes;

    /**
     * Creates a new session file.
     *
     * @param name        the file name
     * @param contentType the content type
     * @param size        the file size in bytes
     * @param bytes       the file content
     */
    public SessionFile(String name, String contentType, long size, byte[] bytes) {
      this.name = name;
      this.contentType = contentType;
      this.size = size;
      this.bytes = bytes;
    }

    public String getName() {
      return name;
    }

    public String getContentType() {
      return contentType;
    }

    public long getSize() {
      return size;
    }

    public byte[] getBytes() {
      return bytes;
    }
  }

  /**
   * Retrieves or initializes the list of uploaded files from session.
   *
   * @param session the HTTP session
   * @return the list of session files
   */
  @SuppressWarnings("unchecked")
  public List<SessionFile> getOrInitSessionFiles(HttpSession session) {
    Object obj = session.getAttribute("uploadedFiles");
    if (obj instanceof List<?> list && !list.isEmpty()
        && list.get(0) instanceof SessionFile) {
      return (List<SessionFile>) obj;
    }
    List<SessionFile> fresh = new ArrayList<>();
    session.setAttribute("uploadedFiles", fresh);
    return fresh;
  }

  /**
   * Adds an uploaded file to the session and updates the request DTO.
   * 
   * <p>This method stores the file content in the session and adds the filename
   * to the RequestDTO's resolution documents list for UI display.</p>
   *
   * @param file    the uploaded multipart file
   * @param session the HTTP session
   * @throws Exception if file reading or storage fails
   */
  public void addUploadedFileToSession(MultipartFile file, HttpSession session) throws Exception {
    String name = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
        ? "uploaded-file"
        : file.getOriginalFilename();
    String ct = (file.getContentType() == null || file.getContentType().isBlank())
        ? "application/octet-stream"
        : file.getContentType();

    getOrInitSessionFiles(session).add(
        new SessionFile(name, ct, file.getSize(), file.getBytes()));

    RequestDTO dto = (RequestDTO) session.getAttribute("requestData");
    if (dto == null) {
      dto = new RequestDTO();
    }
    if (dto.getResolutionDocs() == null) {
      dto.setResolutionDocs(new ArrayList<>());
    }
    dto.getResolutionDocs().add(name);
    dto.setEditable(true);
    session.setAttribute("requestData", dto);
  }

  /**
   * Persists all session files to the TTS Document Management System.
   * 
   * <p>Uploads all files stored in the session and clears the session list
   * after attempting uploads.</p>
   *
   * @param requestId the request ID to associate with the documents
   * @param creator   the creator username
   * @param session   the HTTP session
   */
  public void persistSessionFilesToTts(Long requestId, String creator, HttpSession session) {
    if (requestId == null) {
      return;
    }
    List<SessionFile> files = getOrInitSessionFiles(session);
    if (files.isEmpty()) {
      return;
    }

    RestTemplate rt = new RestTemplate();

    for (SessionFile sf : files) {
      try {
        // Build "meta" JSON inline (matches TTS DocumentUploadRequest)
        var meta = new java.util.LinkedHashMap<String, Object>();
        meta.put("name", sf.name);
        meta.put("type", sf.contentType);
        meta.put("tags", "MR");
        meta.put("refType", "MR_REQUEST");
        meta.put("refId", requestId);
        meta.put("creator", (creator == null || creator.isBlank()) ? "UI_USER" : creator);

        MultipartBodyBuilder mbb = new MultipartBodyBuilder();
        mbb.part("file", new ByteArrayResource(sf.bytes) {
          @Override
          public String getFilename() {
            return sf.name;
          }
        }).contentType(MediaType.parseMediaType(sf.contentType));
        mbb.part("meta", meta, MediaType.APPLICATION_JSON);

        MultiValueMap<String, HttpEntity<?>> body = mbb.build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<String> resp = rt.postForEntity(
            ttsDmsBaseUrl,
            new HttpEntity<>(body, headers),
            String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
          System.err.println(
              "TTS upload failed for " + sf.name + " -> " + resp.getStatusCode());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Clear after attempting uploads
    files.clear();
    session.setAttribute("uploadedFiles", files);
  }

  /**
   * Validates file size against maximum allowed size.
   *
   * @param file the file to validate
   * @return true if file size is within limits, false otherwise
   */
  public boolean isFileSizeValid(MultipartFile file) {
    return file != null && file.getSize() <= MAX_FILE_BYTES;
  }

  /**
   * Gets the maximum allowed file size in bytes.
   *
   * @return maximum file size
   */
  public long getMaxFileBytes() {
    return MAX_FILE_BYTES;
  }

  /**
   * Formats file size in human-readable format (MB, KB).
   *
   * @param bytes the size in bytes
   * @return formatted size string
   */
  public String formatFileSize(long bytes) {
    if (bytes >= 1024 * 1024) {
      return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    } else if (bytes >= 1024) {
      return String.format("%.2f KB", bytes / 1024.0);
    } else {
      return bytes + " bytes";
    }
  }
}
