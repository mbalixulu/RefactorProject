package za.co.rmb.tts.mandates.resolutions.ui.model.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "exportErrorModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExportErrorModel {

  private String status;
  private String fromDate;
  private String toDate;
}
