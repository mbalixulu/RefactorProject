package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@XmlRootElement(name = "commentModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class CommentModel {

  private String name;
  private String createdDate;
  private String commentedText;
}
