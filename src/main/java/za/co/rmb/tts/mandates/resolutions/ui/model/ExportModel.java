package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@XmlRootElement(name = "exportModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExportModel {
  private String status;
  private String fromDate;
  private String toDate;
  private String buttonCheck;
}
