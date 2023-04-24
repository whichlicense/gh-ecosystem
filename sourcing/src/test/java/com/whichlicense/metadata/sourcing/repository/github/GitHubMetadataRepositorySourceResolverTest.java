/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubMetadataRepositorySourceResolverTest {
    static final GitHubMetadataRepositorySourceResolver RESOLVER = new GitHubMetadataRepositorySourceResolver(null);

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com/whichlicense/core-libs", "https://github.com/whichlicense/core-libs/"})
    void givenGitHubMetadataRepositorySourceResolverWhenCallingHandlesWithValidGitHubProjectUrlThenTrueShouldBeReturned(String url) throws MalformedURLException {
        assertThat(RESOLVER.handles(new URL(url))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com", "https://github.com/", "https://github.com/whichlicense",
            "https://github.com/whichlicense/", "https://github.com/whichlicense/core-libs/test",
            "https://github.com/whichlicense/core-libs/test/"})
    void givenGitHubMetadataRepositorySourceResolverWhenCallingHandlesWithInvalidGitHubProjectUrlThenTrueShouldBeReturned(String url) throws MalformedURLException {
        assertThat(RESOLVER.handles(new URL(url))).isFalse();
    }

    @Test
    void givenGitHubMetadataRepositorySourceResolverAndValidArgumentsWhenCallingExistsThenTrueShouldBeReturned() {
        assertThat(RESOLVER.exists("whichlicense", "core-libs")).isTrue();
    }

    @Test
    void givenGitHubMetadataRepositorySourceResolverAndValidArgumentsWhenCallingExistsThenFalseShouldBeReturned() {
        assertThat(RESOLVER.exists("whichlicense", "unknown")).isFalse();
    }
}
