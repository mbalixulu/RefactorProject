package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
@XmlRootElement(name = "request")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequestDTO implements Serializable {

  @XmlElement(name = "registrationNumber")
  private String registrationNumber;

  @XmlElement(name = "companyName")
  private String companyName;

  @XmlElement(name = "companyAddress")
  private String companyAddress;

  @XmlElement(name = "documentumTool")
  private List<String> documentumTools;

  @XmlElementWrapper(name = "directors")
  @XmlElement(name = "director")
  private List<Director> directors;

  @XmlElement(name = "errorCode")
  private String errorCode;

  @XmlElement(name = "errorMessage")
  private String errorMessage;

  @XmlElement(name = "editable")
  private boolean editable;

  @XmlElementWrapper(name = "signatories")
  @XmlElement(name = "signatory")
  private List<Signatory> signatories;

  @XmlElementWrapper(name = "accounts")
  @XmlElement(name = "account")
  private List<Account> accounts;

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Director {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "surname")
    private String surname;

    @XmlElement(name = "designation")
    private String designation;
  }

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Account {

    @XmlElement(name = "accountName")
    private String accountName;

    @XmlElement(name = "accountNo")
    private String accountNo;

    @XmlElementWrapper(name = "signatories")
    @XmlElement(name = "signatory")
    private List<Signatory> signatories;
  }

  @Data
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Signatory {

    @XmlElement(name = "fullName")
    private String fullName;

    @XmlElement(name = "idNumber")
    private String idNumber;

    @XmlElement(name = "instruction")
    private String instruction;
  }
}