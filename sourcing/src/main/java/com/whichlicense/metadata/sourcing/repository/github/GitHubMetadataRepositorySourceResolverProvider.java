/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.whichlicense.metadata.sourcing.MetadataSourceResolver;
import com.whichlicense.metadata.sourcing.MetadataSourceResolverProvider;

import java.util.function.UnaryOperator;

public final class GitHubMetadataRepositorySourceResolverProvider implements MetadataSourceResolverProvider {
    @Override
    public UnaryOperator<MetadataSourceResolver> resolver() {
        return GitHubMetadataRepositorySourceResolver::new;
    }
}
