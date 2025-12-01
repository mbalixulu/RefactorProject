package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;

import za.co.rmb.tts.mandates.resolutions.ui.model.error.DirectorErrorModel;

@Data
@XmlRootElement(name = "directorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class DirectorModel {

  private String buttonCheck;
  private String name;
  private String surname;
  private String designation;
  private int userInList;
  private String pageCheck;
  private String instructions;
  private DirectorErrorModel directorErrorModel;
  private String checkDraft;
}
