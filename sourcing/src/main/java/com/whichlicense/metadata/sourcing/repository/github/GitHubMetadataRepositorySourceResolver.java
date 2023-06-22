/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whichlicense.configuration.ReadableKeyedConfiguration;
import com.whichlicense.metadata.sourcing.MetadataSource;
import com.whichlicense.metadata.sourcing.MetadataSourceResolver;
import com.whichlicense.metadata.sourcing.repository.github.exceptions.GitHubApiForbiddenException;
import com.whichlicense.metadata.sourcing.repository.github.exceptions.GitHubApiUnauthorizedException;
import com.whichlicense.metadata.sourcing.repository.github.internal.Details;
import com.whichlicense.metadata.sourcing.repository.github.internal.GithubArchiveHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.logging.Level.SEVERE;

public record GitHubMetadataRepositorySourceResolver(MetadataSourceResolver next) implements MetadataSourceResolver {
    private static final String TAGS = "https://api.github.com/repos/%s/%s/git/ref/tags{/tag}";
    private static final String TAGS_FOR_SHA = "https://api.github.com/repos/%s/%s/git/refs/tags";

    @Override
    public boolean handles(URL url, ReadableKeyedConfiguration configuration) {
        return url.getHost().equals("github.com") && parts(url).count() >= 2 && !url.toString().endsWith(".zip");
    }

    //TODO support whichlicense/core-libs :: ContextualMetadataSourceCompleter
    // -> error out on the CLI if more than one source
    // -> show and autocomplete on the frontend
    @Override
    public MetadataSource handle(URL originURL, ReadableKeyedConfiguration configuration) {
        var parts = parts(originURL).toList();
        var username = parts.get(0);
        var repository = parts.get(1);

        var mapper = new ObjectMapper();

        try {
            if (lookup(username, repository, mapper, configuration) instanceof Details(
                    var defaultBranch, var branches, var archives
            )) {
                var tagsForSha = TAGS_FOR_SHA.formatted(username, repository);
                if (parts.size() < 4) {
                    if (lookupShaForBranch(branches, defaultBranch, mapper, configuration) instanceof String sha
                            && constructDownloadURL(archives, sha) instanceof String downloadURL) {
                        var tags = lookupTagsForSha(tagsForSha, sha, mapper, configuration);
                        return new GitHubMetadataRepositorySource(username, repository, defaultBranch, tags,
                                sha, GithubArchiveHelper.resolveRoot(new URL(downloadURL), configuration), originURL);
                    } else return null;
                } else {
                    return switch (parts.get(2)) {
                        case "commit" -> {
                            //TODO lookup the referenced branch instead of assuming the default one
                            if (constructDownloadURL(archives, parts.get(3)) instanceof String downloadURL) {
                                var tags = lookupTagsForSha(tagsForSha, parts.get(3), mapper, configuration);
                                yield new GitHubMetadataRepositorySource(username, repository, defaultBranch, tags,
                                        parts.get(3), GithubArchiveHelper.resolveRoot(new URL(downloadURL), configuration), originURL);
                            } else yield null;
                        }
                        case "tree" -> {
                            //TODO fallback to commit checking if branch does not exist
                            var branch = remaining(parts, 3);
                            if (lookupShaForBranch(branches, branch, mapper, configuration) instanceof String sha
                                    && constructDownloadURL(archives, sha) instanceof String downloadURL) {
                                var tags = lookupTagsForSha(tagsForSha, sha, mapper, configuration);
                                yield new GitHubMetadataRepositorySource(username, repository, branch, tags,
                                        sha, GithubArchiveHelper.resolveRoot(new URL(downloadURL), configuration), originURL);
                            } else yield null;
                        }
                        case "releases" -> {
                            var tags = TAGS.formatted(username, repository);
                            if (parts.size() >= 5 && Objects.equals(parts.get(3), "tag")
                                    && lookupShaForTag(tags, parts.get(4), mapper, configuration) instanceof String sha
                                    && constructDownloadURL(archives, sha) instanceof String downloadURL) {
                                yield new GitHubMetadataRepositorySource(username, repository, defaultBranch, Set.of(parts.get(4)),
                                        sha, GithubArchiveHelper.resolveRoot(new URL(downloadURL), configuration), originURL);
                            } else yield null;
                        }
                        default -> null;
                    };
                }
            } else return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private Stream<String> parts(URL url) {
        return Stream.of(url.getPath().split("/")).filter(p -> !p.isBlank());
    }

    @SuppressWarnings("SameParameterValue")
    private String remaining(List<String> parts, int index) {
        return String.join("/", parts.subList(index, parts.size()));
    }

    Details lookup(String username, String repository, ObjectMapper mapper, ReadableKeyedConfiguration configuration) {
        var logger = Logger.getLogger("whichlicense.sourcing.github");
        try {
            var api = new URL("https://api.github.com/repos/%s/%s".formatted(username, repository));
            var connection = (HttpURLConnection) api.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            configuration.hasString("github.token", token ->
                    connection.setRequestProperty("Authorization", "token " + token));
            connection.setRequestProperty("User-Agent", "whichlicense");

            logger.finest("Checking if \"%s/%s\" exists".formatted(username, repository));

            var code = connection.getResponseCode();
            if (code == 401) throw new GitHubApiUnauthorizedException();
            if (code == 403) throw new GitHubApiForbiddenException();
            if (code != 200) return null;

            return mapper.readValue(connection.getInputStream(), Details.class);
        } catch (IOException exception) {
            logger.log(SEVERE, "Unable to verify the existence of \"%s/%s\" on github.com"
                    .formatted(username, repository), exception);
            return null;
        }
    }

    String lookupShaForBranch(String branchesURL, String branch, ObjectMapper mapper, ReadableKeyedConfiguration configuration) {
        var logger = Logger.getLogger("whichlicense.sourcing.github");
        try {
            var api = new URL(branchesURL.replaceFirst("\\{/branch}", "/" + branch));
            var connection = (HttpURLConnection) api.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            configuration.hasString("github.token", token ->
                    connection.setRequestProperty("Authorization", "token " + token));
            connection.setRequestProperty("User-Agent", "whichlicense");

            var code = connection.getResponseCode();
            if (code == 401) throw new GitHubApiUnauthorizedException();
            if (code == 403) throw new GitHubApiForbiddenException();
            if (code != 200) return null;
            var node = mapper.readTree(connection.getInputStream());

            logger.finest("Checking if branch \"%s\" exists".formatted(branch));
            if (!Objects.equals(node.findValue("name")
                    .asText(null), branch)) return null;

            logger.finest("Trying to find commit->sha for branch \"%s\"".formatted(branch));
            return node.findPath("commit").findValue("sha").asText(null);
        } catch (IOException exception) {
            logger.log(SEVERE, "Failed to lookup commit sha for branch \"%s\""
                    .formatted(branch), exception);
            return null;
        }
    }

    String lookupShaForTag(String tagsURL, String tag, ObjectMapper mapper, ReadableKeyedConfiguration configuration) {
        var logger = Logger.getLogger("whichlicense.sourcing.github");
        try {
            var api = new URL(tagsURL.replaceFirst("\\{/tag}", "/" + tag));
            var connection = (HttpURLConnection) api.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            configuration.hasString("github.token", token ->
                    connection.setRequestProperty("Authorization", "token " + token));
            connection.setRequestProperty("User-Agent", "whichlicense");

            var code = connection.getResponseCode();
            if (code == 401) throw new GitHubApiUnauthorizedException();
            if (code == 403) throw new GitHubApiForbiddenException();
            if (code != 200) return null;
            var node = mapper.readTree(connection.getInputStream());

            logger.finest("Trying to find object->sha for tag \"%s\"".formatted(tag));
            return node.findPath("object").findValue("sha").asText(null);
        } catch (IOException exception) {
            logger.log(SEVERE, "Failed to lookup commit sha for tag \"%s\""
                    .formatted(tag), exception);
            return null;
        }
    }

    Set<String> lookupTagsForSha(String tagsForShaURL, String sha, ObjectMapper mapper, ReadableKeyedConfiguration configuration) {
        try {
            var api = new URL(tagsForShaURL);
            var connection = (HttpURLConnection) api.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            configuration.hasString("github.token", token ->
                    connection.setRequestProperty("Authorization", "token " + token));
            connection.setRequestProperty("User-Agent", "whichlicense");

            var code = connection.getResponseCode();
            if (code == 401) throw new GitHubApiUnauthorizedException();
            if (code == 403) throw new GitHubApiForbiddenException();
            if (code != 200) return Collections.emptySet();
            var node = mapper.readTree(connection.getInputStream());

            return StreamSupport.stream(node.spliterator(), true)
                    .filter(n -> Objects.equals(n.findPath("object")
                            .findValue("sha").asText(), sha))
                    .map(n -> n.findValue("ref").asText()
                            .replaceFirst("refs/tags/", ""))
                    .filter(tag -> !Objects.equals(tag, "status"))
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return Collections.emptySet();
        }
    }

    String constructDownloadURL(String archiveURL, String sha) {
        return archiveURL.replaceFirst("\\{/ref}", "/" + sha)
                .replaceFirst("\\{archive_format}", "zipball");
    }
}
