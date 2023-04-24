/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubMetadataRepositorySourceResolverProviderTest {
    @Test
    void givenGitHubMetadataRepositorySourceResolverProviderWhenCallingResolverThenLocalMetadataRepositorySourceResolverConstructorReferenceShouldBeReturned() {
        assertThat(new GitHubMetadataRepositorySourceResolverProvider().resolver()).isNotNull();
    }

    @Test
    void givenGitHubMetadataRepositorySourceResolverProviderWhenCallingPriorityThenOneShouldBeReturned() {
        assertThat(new GitHubMetadataRepositorySourceResolverProvider().priority()).isOne();
    }
}
