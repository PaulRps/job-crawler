package paulrps.crawler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class TrackObject {
  @Id private String id;
  private String userId;
  private String trackCode;
  private String description;
  private Integer carrier;
  private Long lastEventsAmount = 0l;
  private Boolean isActive = true;

  public boolean hasChanged(Long current) {
    return current > lastEventsAmount;
  }
}
