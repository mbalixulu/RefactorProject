package za.co.rmb.tts.mandates.resolutions.ui.model;

import java.util.List;

import lombok.Data;

@Data
public class PdfInstruction {
  private String instructionType;
  private List<SignatoryEntry> entries;
}