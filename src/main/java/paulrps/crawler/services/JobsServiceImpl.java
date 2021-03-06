package paulrps.crawler.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import paulrps.crawler.domain.dto.WebPageDataDto;
import paulrps.crawler.domain.entity.JobFilter;
import paulrps.crawler.domain.entity.User;
import paulrps.crawler.domain.enums.ParserTypeEnum;
import paulrps.crawler.util.WebPageParserFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JobsServiceImpl implements paulrps.crawler.services.JobService {
  private final @NonNull paulrps.crawler.services.UserService userService;
  private final @NonNull JobFilterService jobFilterService;

  @Override
  public List<WebPageDataDto> getByUserEmail(String email) {
    User user = userService.findByEmail(email);
    List<WebPageDataDto> jobs = new ArrayList<>();
    Map<Integer, String> jobType = new TreeMap<>();
    jobType.put(
        ParserTypeEnum.GITHUB_BACKEND_ISSUES.getId(),
        ParserTypeEnum.GITHUB_BACKEND_ISSUES.getUrl());

    JobFilter jobFilter = jobFilterService.findByUserId(user.getId());
    Optional.ofNullable(jobFilter)
        .ifPresent(
            filters -> {
              filters.getJobOpeningSources().stream()
                  .filter(webPageId -> jobType.containsKey(webPageId))
                  .forEach(
                      webPageId -> {
                        ParserTypeEnum parserTypeEnum = ParserTypeEnum.getOne(webPageId);

                        WebPageParserFactory.getInstance(parserTypeEnum)
                            .parseData(parserTypeEnum.getUrl())
                            .stream()
                            .filter(data -> filterData((WebPageDataDto) data, filters))
                            .forEach(job -> jobs.add((WebPageDataDto) job));
                      });
            });

    return jobs;
  }

  @Override
  public Map<User, List<WebPageDataDto>> getAll() {
    Map<User, List<WebPageDataDto>> jobOpenningsMap = new LinkedHashMap<>();

    List<User> allUsers = userService.findAll();
    Map<String, List<JobFilter>> activeUsersMap =
        jobFilterService.findAll().stream()
            .filter(JobFilter::getIsActive)
            .collect(Collectors.groupingBy(JobFilter::getUserId));

    Optional.ofNullable(allUsers)
        .ifPresent(
            users ->
                users.stream()
                    .filter(user -> activeUsersMap.containsKey(user.getId()))
                    .forEach(user -> jobOpenningsMap.put(user, getByUserEmail(user.getEmail()))));

    return jobOpenningsMap;
  }

  private boolean filterData(WebPageDataDto data, JobFilter jobFilter) {
    Map<String, String> dataMap = data.getDataMap();
    long countMatch =
        jobFilter.getJobKeyWords().stream()
            .filter(keyWord -> dataMap.containsKey(keyWord.toLowerCase()))
            .count();

    return (BigDecimal.valueOf(countMatch)
                .divide(BigDecimal.valueOf(jobFilter.getJobKeyWords().size())))
            .compareTo(BigDecimal.valueOf(0.5))
        >= 0;
  }
}
