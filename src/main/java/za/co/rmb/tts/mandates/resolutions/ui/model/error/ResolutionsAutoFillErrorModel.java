package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "resolutionsAutoFillErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResolutionsAutoFillErrorModel {
  private String directorName;
  private String directorSurname;
  private String directorDesignation;
  private String directorInstruction;
}