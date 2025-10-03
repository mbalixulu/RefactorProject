package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

/** Simple view DTO for directors on View/Edit pages */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectorDTO {
  @XmlElement(name = "authorityId")
  private Long authorityId;

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "surname")
  private String surname;

  @XmlElement(name = "designation")
  private String designation;

  @XmlElement(name = "instruction")
  private String instruction;
}

