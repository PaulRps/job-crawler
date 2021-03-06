package paulrps.crawler.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ParserTypeEnum {
  GITHUB_BACKEND_ISSUES(1, "https://github.com/backend-br/vagas/issues"),
  CORREIOS_TRACK_OBJ(
      2, "https://www2.correios.com.br/sistemas/rastreamento/ctrl/ctrlRastreamento.cfm");

  private Integer id;
  private String url;

  public static ParserTypeEnum getOne(final Integer id) {
    return Stream.of(ParserTypeEnum.values())
        .filter(wpe -> wpe.id.equals(id))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(String.format("WebPageEnum (%d) not found", id)));
  }
}
