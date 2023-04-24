/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.whichlicense.metadata.sourcing.MetadataSource;
import com.whichlicense.metadata.sourcing.MetadataSourceResolver;
import com.whichlicense.metadata.sourcing.internal.ArchiveHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.SEVERE;

public record GitHubMetadataRepositorySourceResolver(MetadataSourceResolver next) implements MetadataSourceResolver {
    @Override
    public boolean handles(URL url) {
        return url.getHost().equals("github.com") && parts(url).count() == 2;
    }

    @Override
    public MetadataSource handle(URL url) {
        try {
            var parts = parts(url).toList();
            var username = parts.get(0);
            var repository = parts.get(1);
            return exists(username, repository) ? new GitHubMetadataRepositorySource(
                    username, repository, "", ArchiveHelper.resolveRoot(
                    new URL("https://github.com/%s/%s/archive/refs/heads/main.zip"
                            .formatted(username, repository))), url) : null;
        } catch (MalformedURLException exception) {
            Logger.getLogger("whichlicense.sourcing.github")
                    .log(SEVERE, "Failed to create archive download URL for %s"
                            .formatted(url), exception);
            return null;
        }
    }

    private Stream<String> parts(URL url) {
        return Stream.of(url.getPath().split("/")).filter(p -> !p.isBlank());
    }

    boolean exists(String username, String repository) {
        var logger = Logger.getLogger("whichlicense.sourcing.github");
        try {
            var api = new URL("https://api.github.com/repos/%s/%s".formatted(username, repository));
            var connection = (HttpURLConnection) api.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            logger.finest("Checking if %s/%s exists".formatted(username, repository));
            return connection.getResponseCode() == 200;
        } catch (IOException exception) {
            logger.log(SEVERE, "Unable to verify the existence of %s/%s on github.com", exception);
            return false;
        }
    }
}
