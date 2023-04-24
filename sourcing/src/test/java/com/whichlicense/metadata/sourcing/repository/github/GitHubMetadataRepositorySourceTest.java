/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.whichlicense.metadata.sourcing.MetadataOrigin;
import com.whichlicense.metadata.sourcing.MetadataOrigin.RawPath;
import com.whichlicense.testing.nullable.NullSubstituteSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubMetadataRepositorySourceTest {
    static final Path PATH = Paths.get(".");
    static final URL URL;

    static {
        try {
            URL = new URL("https://github.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    static final Arguments URL_ARGS = Arguments.of("username", "repository", "commit", PATH, URL);
    static final Arguments ORIGIN_ARGS = Arguments.of("username", "repository", "commit", PATH, new RawPath(PATH));

    @ParameterizedTest
    @NullSubstituteSource("URL_ARGS")
    void givenNullableArgumentsWhenCallingTheConstructorThenThrowNullPointerException(String username, String repository, String commit, Path path, URL url) {
        assertThatThrownBy(() -> new GitHubMetadataRepositorySource(username, repository, commit, path, url))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @NullSubstituteSource("ORIGIN_ARGS")
    void givenNullableArgumentsWhenCallingTheConstructorThenThrowNullPointerException(String username, String repository, String commit, Path path, MetadataOrigin origin) {
        assertThatThrownBy(() -> new GitHubMetadataRepositorySource(username, repository, commit, path, origin))
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
