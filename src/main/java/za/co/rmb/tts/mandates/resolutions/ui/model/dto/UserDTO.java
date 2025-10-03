package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

  @JsonAlias("user_id")
  private Long userId;

  @JsonAlias("employee_number")
  private String employeeNumber;

  @JsonAlias("first_name")
  private String firstName;

  @JsonAlias("last_name")
  private String lastName;

  private String email;

  private String username;

  @JsonAlias("passkey")
  private String passKey;

  @JsonAlias("user_role")
  private String userRole;

  @JsonAlias("user_type")
  private String userType;

  private String manager;

  @JsonAlias("manager_email")
  private String managerEmail;

  //Audit
  private String creator;
  private String created;
  private String updator;
  private String updated;

  //(Old convenience; safe to keep or remove)
  public String getUiUserId() {
    if (username != null && !username.isBlank()) return username;
    if (employeeNumber != null && !employeeNumber.isBlank()) return employeeNumber;
    return null;
  }
}