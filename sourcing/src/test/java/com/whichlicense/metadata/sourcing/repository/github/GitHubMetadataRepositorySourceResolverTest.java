/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whichlicense.configuration.KeyedConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubMetadataRepositorySourceResolverTest {
    static final GitHubMetadataRepositorySourceResolver RESOLVER = new GitHubMetadataRepositorySourceResolver(null);
    static final KeyedConfiguration CONFIG = new ConfigurationMock();

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com/whichlicense/core-libs", "https://github.com/whichlicense/core-libs/",
            "https://github.com/whichlicense/core-libs/commit/0536e94bddb63f1bf786ea8963a23b7ce39e3726",
            "https://github.com/whichlicense/core-libs/tree/sourcing", "https://github.com/whichlicense/core-libs/releases/tag/v0.2.1"})
    void givenGitHubMetadataRepositorySourceResolverWhenCallingHandlesWithValidGitHubProjectUrlThenTrueShouldBeReturned(String url) throws MalformedURLException {
        assertThat(RESOLVER.handles(new URL(url), CONFIG)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://github.com", "https://github.com/", "https://github.com/whichlicense",
            "https://github.com/whichlicense/"})
    void givenGitHubMetadataRepositorySourceResolverWhenCallingHandlesWithInvalidGitHubProjectUrlThenTrueShouldBeReturned(String url) throws MalformedURLException {
        assertThat(RESOLVER.handles(new URL(url), CONFIG)).isFalse();
    }

    @Test
    void givenGitHubMetadataRepositorySourceResolverAndValidArgumentsWhenCallingMetadataThenTheMetadataShouldBeReturned() {
        System.out.println(RESOLVER.lookup("whichlicense", "core-libs", new ObjectMapper(), CONFIG));
        assertThat(RESOLVER.lookup("whichlicense", "core-libs", new ObjectMapper(), CONFIG)).isNotNull();
    }

    @Test
    void givenGitHubMetadataRepositorySourceResolverAndValidArgumentsWhenCallingMetadataThenNullShouldBeReturned() {
        assertThat(RESOLVER.lookup("whichlicense", "unknown", new ObjectMapper(), CONFIG)).isNull();
    }
}
