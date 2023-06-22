/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.whichlicense.metadata.sourcing.MetadataSourceResolverProvider;
import com.whichlicense.metadata.sourcing.repository.github.GitHubMetadataRepositorySourceResolverProvider;

module whichlicense.sourcing.github {
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires whichlicense.configuration;
    requires transitive whichlicense.sourcing;
    exports com.whichlicense.metadata.sourcing.repository.github;
    exports com.whichlicense.metadata.sourcing.repository.github.exceptions;
    opens com.whichlicense.metadata.sourcing.repository.github.internal to com.fasterxml.jackson.databind;
    provides MetadataSourceResolverProvider with GitHubMetadataRepositorySourceResolverProvider;
}
