/*
 * Copyright (c) 2023 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository https://github.com/whichlicense/gh-ecosystem.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.whichlicense.metadata.sourcing.repository.github;

import com.whichlicense.configuration.KeyedConfiguration;

import java.util.function.Consumer;

public record ConfigurationMock() implements KeyedConfiguration {
    @Override
    public boolean getBoolean(String key) {
        return false;
    }

    @Override
    public void hasBoolean(String key, Consumer<Boolean> consumer) {

    }

    @Override
    public int getInteger(String key) {
        return 0;
    }

    @Override
    public void hasInteger(String key, Consumer<Integer> consumer) {

    }

    @Override
    public long getLong(String key) {
        return 0;
    }

    @Override
    public void hasLong(String key, Consumer<Long> consumer) {

    }

    @Override
    public String getString(String key) {
        return null;
    }

    @Override
    public void hasString(String key, Consumer<String> consumer) {

    }

    @Override
    public void setBoolean(String key, boolean value) {

    }

    @Override
    public void setInteger(String key, int value) {

    }

    @Override
    public void setLong(String key, long value) {

    }

    @Override
    public void setString(String key, String value) {

    }
}
