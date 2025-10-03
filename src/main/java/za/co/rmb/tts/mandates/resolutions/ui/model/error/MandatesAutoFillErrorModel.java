package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "mandatesAutoFillErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class MandatesAutoFillErrorModel {
  private String accountName;
  private String accountNo;
  private String signatoryFullName;
  private String signatoryIdNumber;
  private String signatoryInstruction;
}