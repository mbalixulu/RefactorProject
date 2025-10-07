package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import lombok.Data;

@Data
public class ExtractedPdfDataDTO {
  private Long id;
  private String pdfSessionId;
  private String companyRegNumber;
  private String extractedJson;
}