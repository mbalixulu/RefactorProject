package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RequestExportDto {
  private String status;     
  private LocalDate fromDate;
  private LocalDate toDate;
  private String type;       //optional: Mandates | Resolutions | Both
}