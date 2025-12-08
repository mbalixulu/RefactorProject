package za.co.rmb.tts.mandates.resolutions.ui.model.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CommentDto {

  private Long commentId;
  private Long requestId;
  private String commentText;
  private LocalDateTime updated;
  private String updator;
  private LocalDateTime created;
  private String creator;
}
