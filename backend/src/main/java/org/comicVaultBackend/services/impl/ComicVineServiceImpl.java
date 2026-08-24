package org.comicVaultBackend.services.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.comicVaultBackend.services.ComicVineService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ComicVineServiceImpl implements ComicVineService {
    private final RestClient restClient;

    private String apiKey;

    public ComicVineServiceImpl(
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://comicvine.gamespot.com/api")
                .build();
    }

    @Override
    public Optional<ComicVineIssueInfo> searchIssue(String issueName, String apiKey) {
        if (issueName == null || issueName.isBlank()) {
            return Optional.empty();
        }
        this.apiKey = apiKey;
        ParsedIssue parsed = parseIssueName(issueName);
        // Normal case: "Title #number"
        if (parsed != null) {
            Optional<IssueSummary> issue = findIssueByVolume(
                    parsed
            );
            if (issue.isPresent()) {
                return getIssueDetails(issue.get().id());
            }
        }
        // Fallback: let ComicVine search the complete title.
        return searchIssueByTitle(issueName)
                .flatMap(issue -> getIssueDetails(issue.id()));
    }

    private Optional<IssueSummary> findIssueByVolume(ParsedIssue parsed) {
        ComicVineResponse<List<Volume>> volumes = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes/")
                        .queryParam("api_key", this.apiKey)
                        .queryParam("format", "json")
                        .queryParam("limit", 10)
                        .queryParam("filter", "name:" + parsed.seriesName() + " " + parsed.issueNumber())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (volumes == null || volumes.results() == null) {
            return Optional.empty();
        }

        // Prefer an exact volume name match.
        Volume volume = volumes.results()
                .stream()
                .filter(v -> v.name() != null)
                .filter(v -> v.name().equalsIgnoreCase(parsed.seriesName()))
                .findFirst()
                .orElseGet(() -> volumes.results().stream().findFirst().orElse(null));
        if (volume == null) {
            return Optional.empty();
        }

        ComicVineResponse<List<IssueSummary>> issues = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/issues/")
                        .queryParam("api_key", this.apiKey)
                        .queryParam("format", "json")
                        .queryParam("limit", 10)
                        .queryParam(
                                "filter",
                                "volume:" + volume.id()
                                        + ",issue_number:" + parsed.issueNumber()
                        )
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (issues == null || issues.results() == null) {
            return Optional.empty();
        }
        return issues.results()
                .stream()
                .findFirst();
    }

    private Optional<IssueSummary> searchIssueByTitle(String title) {
        ComicVineResponse<List<IssueSummary>> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/")
                        .queryParam("api_key", this.apiKey)
                        .queryParam("format", "json")
                        .queryParam("resources", "issue")
                        .queryParam("query", title)
                        .queryParam("limit", 10)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (response == null || response.results() == null) {
            return Optional.empty();
        }
        return response.results()
                .stream()
                .findFirst();
    }

    private Optional<ComicVineIssueInfo> getIssueDetails(int issueId) {
        ComicVineResponse<IssueDetails> details = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/issue/4000-" + issueId + "/")
                        .queryParam("api_key", this.apiKey)
                        .queryParam("format", "json")
                        .queryParam(
                                "field_list",
                                "id,name,description,cover_date,volume,publisher"
                        )
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (details == null || details.results() == null) {
            return Optional.empty();
        }

        IssueDetails result = details.results();
        String series = null;

        if (result.volume() != null) {
            series = result.volume().name();
        }

        String category = getCategory(
                result.publisher() != null
                        ? result.publisher().name()
                        : null
        );

        return Optional.of(
                new ComicVineIssueInfo(
                        result.description(),
                        getYear(result.coverDate()),
                        category,
                        series
                )
        );
    }

    private String getCategory(String publisher) {
        if (publisher == null || publisher.isBlank()) {
            return "other-comics";
        }
        String normalized = publisher.toLowerCase();
        if (normalized.contains("marvel")) {
            return "marvel";
        }
        if (normalized.contains("dc")) {
            return "dc";
        }
        return "other-comics";
    }

    private Integer getYear(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return LocalDate.parse(date).getYear();
    }

    private ParsedIssue parseIssueName(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        var matcher = Pattern
                .compile("^(.+?)\\s*#?\\s*([0-9]+)\\s*$")
                .matcher(input.trim());
        if (!matcher.matches()) {
            return null;
        }
        return new ParsedIssue(
                matcher.group(1).trim(),
                matcher.group(2)
        );
    }

    private record ParsedIssue(
            String seriesName,
            String issueNumber

    ) {
    }

    public record ComicVineIssueInfo(
            String description,
            Integer year,
            String category,
            String series
    ) {
    }

    public record ComicVineResponse<T>(
            String error,
            int limit,
            int offset,
            @JsonProperty("number_of_page_results")
            int numberOfPageResults,
            @JsonProperty("number_of_total_results")
            int numberOfTotalResults,
            @JsonProperty("status_code")
            int statusCode,
            T results
    ) {
    }

    public record Volume(
            int id,
            String name
    ) {
    }

    public record IssueSummary(
            int id,
            @JsonProperty("issue_number")
            String issueNumber,
            String name
    ) {
    }

    public record IssueDetails(
            int id,
            String name,
            String description,
            @JsonProperty("cover_date")
            String coverDate,
            VolumeInfo volume,
            Publisher publisher

    ) {
    }

    public record VolumeInfo(
            int id,
            String name
    ) {
    }

    public record Publisher(
            int id,
            String name
    ) {
    }
}
