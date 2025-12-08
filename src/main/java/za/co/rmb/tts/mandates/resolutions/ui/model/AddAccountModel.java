package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import lombok.Data;

import za.co.rmb.tts.mandates.resolutions.ui.model.error.SignatoryErrorModel;

@Data
@XmlRootElement(name = "addAccountModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddAccountModel {

  private String accountName;
  private String accountNumber;
  private SignatoryErrorModel signatoryErrorModel;
  private String checkSignatoryList;
  private int userInList;
  private String buttonCheck;
  private String checkRemoveSignatory;
  private List<SignatoryModel> listOfSignatory;
  private String checkDelete;
  private String editButton;
  private Long accountId;
  private String checkDeleteButton;

}
