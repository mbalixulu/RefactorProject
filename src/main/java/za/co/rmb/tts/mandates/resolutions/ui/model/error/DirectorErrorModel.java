package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "directorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectorErrorModel {

  private String name;
  private String surname;
  private String designation;
  private String instruction;
}
