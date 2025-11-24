package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import za.co.rmb.tts.mandates.resolutions.ui.model.error.SignatoryErrorModel;

@Data
@XmlRootElement(name = "signatoryModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignatoryModel {

  private String fullName;
  private String idNumber;
  private String instruction;
  private int userInList;
  private int userInAccount;
  private String buttonCheck;
  private String capacity;
  private String group;
  private String checkDocConfirm;
  private SignatoryErrorModel signatoryErrorModel;
  private String checkRemoveOption;
  private String checkEdit;
}
