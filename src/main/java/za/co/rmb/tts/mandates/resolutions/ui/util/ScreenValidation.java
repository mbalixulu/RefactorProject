package za.co.rmb.tts.mandates.resolutions.ui.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

@Component
public class ScreenValidation {

  public static boolean validateSaIdNumber(String idNumber) {

    if (idNumber == null || !idNumber.matches("\\d{13}")) {
      return false;
    }

    String dobString = idNumber.substring(0, 6);
    try {
      int yearPrefix = LocalDate.now().getYear() / 100 * 100;

      if (Integer.parseInt(dobString.substring(0, 2)) > LocalDate.now().getYear() % 100) {
        yearPrefix -= 100;
      }

      String fullDobString = yearPrefix / 100 + dobString;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
      LocalDate dob = LocalDate.parse(fullDobString, formatter);
      if (dob.isAfter(LocalDate.now())) {
        return false;
      }
    } catch (DateTimeParseException e) {
      return false;
    }

    if (!isValidLuhn(idNumber)) {
      return false;
    }

    String genderSequence = idNumber.substring(6, 10);
    if (!genderSequence.matches("\\d{4}")) {
      return false;
    }


    char nationalityDigit = idNumber.charAt(10);
    if (nationalityDigit != '0' && nationalityDigit != '1') {
      return false;
    }

    return true;
  }

  private static boolean isValidLuhn(String idNumber) {
    int sum = 0;
    boolean alternate = false;
    for (int i = idNumber.length() - 1; i >= 0; i--) {
      int digit = Character.getNumericValue(idNumber.charAt(i));
      if (alternate) {
        digit *= 2;
        if (digit > 9) {
          digit = (digit % 10) + 1;
        }
      }
      sum += digit;
      alternate = !alternate;
    }
    return (sum % 10 == 0);
  }
}
