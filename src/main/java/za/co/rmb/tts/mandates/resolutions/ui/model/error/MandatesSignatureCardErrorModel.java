package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "mandatesSignatureCardErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class MandatesSignatureCardErrorModel {
  private String accountName;
  private String accountNo;

  //Used by MandatesSignatureCard.xsl (singular flow)
  private String signatoryFullName;
  private String signatoryIdNumber;

  //Used by combined pages (MandatesResolutionsSignatureCard*.xsl)
  private String fullName;
  private String idNumber;
  private String capacity;
  private String group;
}