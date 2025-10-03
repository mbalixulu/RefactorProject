package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
  private int loginId;
  private String userId;
  private String firstName;
  private String lastName;
  private String email;
  private String passKey;
  private String userRole;
  private String userType;
  private String creator;
  private String created;
  private String updator;
  private String updated;
}