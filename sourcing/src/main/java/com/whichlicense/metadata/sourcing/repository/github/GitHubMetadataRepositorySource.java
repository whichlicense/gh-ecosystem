/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.whichlicense.metadata.sourcing.MetadataOrigin;
import com.whichlicense.metadata.sourcing.MetadataOrigin.RawURL;
import com.whichlicense.metadata.sourcing.repository.MetadataRepositorySource;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public record GitHubMetadataRepositorySource(String username, String repository, String commit, Path path, MetadataOrigin origin) implements MetadataRepositorySource {
    public GitHubMetadataRepositorySource(String username, String repository, String commit, Path path, URL url) {
        this(username, repository, commit, path, new RawURL(Objects.requireNonNull(url)));
    }

    public GitHubMetadataRepositorySource {
        Objects.requireNonNull(username);
        Objects.requireNonNull(repository);
        Objects.requireNonNull(commit);
        Objects.requireNonNull(path);
        Objects.requireNonNull(origin);
    }
}
