/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github.internal;

import com.whichlicense.configuration.ReadableKeyedConfiguration;
import com.whichlicense.metadata.sourcing.internal.ArchiveHelper;
import com.whichlicense.metadata.sourcing.repository.github.exceptions.GitHubApiForbiddenException;
import com.whichlicense.metadata.sourcing.repository.github.exceptions.GitHubApiUnauthorizedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class GithubArchiveHelper {
    public static Path resolveRoot(URL url, ReadableKeyedConfiguration configuration) {
        Objects.requireNonNull(url);
        try {
            var fileName = url.getFile().lastIndexOf("/") + 1;
            var tempDir = Files.createTempDirectory("whichlicense-archive-");
            var tempArchiveFile = tempDir.resolve(url.getFile().substring(fileName));

            var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            configuration.hasString("github.token", token ->
                    connection.setRequestProperty("Authorization", "token " + token));
            connection.setRequestProperty("User-Agent", "whichlicense");

            var code = connection.getResponseCode();
            if (code == 401) throw new GitHubApiUnauthorizedException();
            if (code == 403) throw new GitHubApiForbiddenException();
            if (code != 200) return null;

            Files.copy(connection.getInputStream(), tempArchiveFile, REPLACE_EXISTING);
            Logger.getLogger("whichlicense.sourcing.archive")
                    .finest("Archive input source temporarily downloaded to: " + tempArchiveFile);
            return ArchiveHelper.resolveRoot(tempArchiveFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
