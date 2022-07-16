/*
 * Sound Finder - Tool used to create SoundType enum for PlayMoreSounds.
 * Copyright (C) 2022  Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.soundfinder.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public record Version(@NotNull String version) implements Comparable<Version> {
    private static final @NotNull Pattern validVersion = Pattern.compile("^\\d+(\\.\\d+)+$");
    private static final @NotNull Pattern versionSeparator = Pattern.compile("\\.");

    public Version(@NotNull String version) {
        if (!validVersion.matcher(version).matches())
            throw new IllegalArgumentException("'" + version + "' is not a valid version");

        this.version = version;
    }

    @Override
    public int compareTo(@NotNull Version version) {
        String[] versionNodes = versionSeparator.split(version());
        String[] greaterNodes = versionSeparator.split(version.version());
        int length = Math.max(versionNodes.length, greaterNodes.length);

        for (int i = 0; i < length; ++i) {
            int versionNode = i < versionNodes.length ? Integer.parseInt(versionNodes[i]) : 0;
            int greaterNode = i < greaterNodes.length ? Integer.parseInt(greaterNodes[i]) : 0;

            if (versionNode < greaterNode)
                return -1;
            else if (versionNode != greaterNode)
                return 1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version that)) return false;

        return compareTo(that) == 0;
    }

    @Override
    public @NotNull String toString() {
        return version;
    }
}
