package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ExportCsvView {
  private List<ListOfValuesDTO> statuses;  //from /api/lov?type=Dropdown&subType=RequestStatus
}
