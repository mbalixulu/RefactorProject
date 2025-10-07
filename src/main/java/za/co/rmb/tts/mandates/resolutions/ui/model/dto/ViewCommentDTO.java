package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.Serializable;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ViewCommentDTO implements Serializable {
  @XmlElement(name = "creator")
  private String creator;

  // formatted as "yyyy-MM-dd HH:mm"
  @XmlElement(name = "created")
  private String created;

  @XmlElement(name = "text")
  private String text;
}
