package za.co.rmb.tts.mandates.resolutions.ui.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "waveModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class WaveModel {

  private int name;
  private String label;
  private String waveTool;
}
