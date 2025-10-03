package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import lombok.Data;

import org.springframework.web.multipart.MultipartFile;

@Data
public class PdfUploadRequestDto {
  private MultipartFile signatoriesMandateDoc;
  private String companyRegNumber;
}