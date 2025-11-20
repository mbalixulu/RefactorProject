package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "searchResultsErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResultsErrorModel {
  private String companyName;
  private String companyAddress;
  private String companyWaiver;
  private String fullName;
  private String surname;
  private String designation;
  private String regiNumber;
  private String requestType;
  private String checkStyleOne;
  private String checkStyleTwo;
  private String toolOne;
}
