package com.whichlicense.metadata.sourcing.repository.github.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Details(
        @JsonProperty("default_branch") String defaultBranch,
        @JsonProperty("branches_url") String branchesURL,
        @JsonProperty("archive_url") String archiveURL
) {
}
