package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "approveRejectErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApproveRejectErrorModel {
  private String confirmationCheckMandate; // inline error under the checkbox
  private String commentbox;               // inline error under the comment (reject flow)
  private String commentboxValue;          // echo back user's typed comment
}
