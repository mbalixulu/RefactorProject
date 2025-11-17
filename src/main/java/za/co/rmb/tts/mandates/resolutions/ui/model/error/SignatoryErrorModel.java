package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "signatoryErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignatoryErrorModel {

  private String fullName;
  private String idNumber;
  private String instruction;
  private String accountNumber;
  private String accountName;
  private String capacity;
  private String group;
  private String checkDocConfirm;
}
