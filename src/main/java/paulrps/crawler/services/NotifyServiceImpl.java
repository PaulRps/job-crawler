package paulrps.crawler.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import paulrps.crawler.domain.dto.EmailNotificationMessageDto;
import paulrps.crawler.domain.dto.TrackDataDto;
import paulrps.crawler.domain.dto.TrackEventDto;
import paulrps.crawler.domain.dto.WebPageDataDto;
import paulrps.crawler.domain.entity.User;
import paulrps.crawler.util.Constants;
import paulrps.crawler.util.EmailTrackObjectMessageFormatter;
import paulrps.crawler.util.JobNotifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyServiceImpl implements NotifyService {

  private final @NonNull JobService jobService;
  private final @NonNull @Qualifier("EmailNotifier") JobNotifier emailNotifier;
  private final @NonNull UserService userService;
  private final @NonNull TrackObjectService trackObjectService;
  private final @NonNull EmailTrackObjectMessageFormatter trackObjectMessageFormatter;

  @Override
  public void notifyJobsAllUsers() {
    log.info("SENDING JOB OPENINGS NOTIFICATION TO ALL USERS");
    Map<User, List<WebPageDataDto>> userJobOpenningsMap = jobService.getAll();

    userJobOpenningsMap.entrySet().stream()
        .filter(e -> !e.getValue().isEmpty())
        .forEach(
            e ->
                emailNotifier.sendTo(
                    e.getKey(),
                    EmailNotificationMessageDto.builder()
                        .subject("Job Crawler Summary")
                        .body(e.getValue())
                        .build()));
    log.info("SENT JOB OPENINGS NOTIFICATION TO ALL USERS");
  }

  @Override
  public void notifyJobsByUserEmail(String email) {
    log.info("SENDING JOB OPENINGS NOTIFICATION TO {}", email);
    List<WebPageDataDto> data = jobService.getByUserEmail(email);

    if (!data.isEmpty()) {
      emailNotifier.sendTo(
          User.builder().email(email).build(),
          EmailNotificationMessageDto.builder().subject("Job Crawler Summary").body(data).build());
      log.info("SENT JOB OPENINGS NOTIFICATION TO {}", email);
    }
  }

  @Override
  public void notifyTrackObjectAllUsers() {
    log.info("notifing all users for tracking objects");
    List<User> users = userService.findAll();

    users.forEach(
        user -> {
          List<TrackDataDto> trackData =
              trackObjectService.getTrackObjByUserEmail(user.getEmail()).stream()
                  .filter(obj -> obj.isHasChanged())
                  .collect(Collectors.toList());
          log.info("found {} track objects to notify", trackData.size());
          if (!trackData.isEmpty())
            emailNotifier.sendTo(
                user, "Tracking Summary", trackObjectMessageFormatter.formatBody(trackData));
        });
  }

  private String tempTrackObjFormater(List<TrackDataDto> trackData) {
    StringBuilder msg = new StringBuilder("Objects: ");
    msg.append(Constants.LINE_SEPARATOR);
    trackData.forEach(
        obj ->
            msg.append(obj.getTrackingCode())
                .append(Constants.LINE_SEPARATOR)
                .append(tempTrackEvntsFormatter(obj.getEvents()))
                .append(Constants.LINE_SEPARATOR));
    return msg.toString();
  }

  private String tempTrackEvntsFormatter(List<TrackEventDto> events) {
    StringBuilder evnts = new StringBuilder("");
    events.forEach(
        evt ->
            evnts
                .append(Constants.TAB_SPACE)
                .append(evt.getDtEvent())
                .append(Constants.LINE_SEPARATOR)
                .append(Constants.TAB_SPACE)
                .append(evt.getDescription())
                .append(Constants.LINE_SEPARATOR));
    return evnts.toString();
  }
}
