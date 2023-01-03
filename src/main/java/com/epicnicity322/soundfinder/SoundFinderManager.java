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

package com.epicnicity322.soundfinder;

import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.soundfinder.util.Back;
import com.epicnicity322.soundfinder.util.Version;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class SoundFinderManager {
    private static final @NotNull Comparator<String> versionStringComparator = Comparator.comparing(Version::new);

    private SoundFinderManager() {
    }

    public static void printOutput(@NotNull Base base, @NotNull TreeMap<String, TreeMap<String, ArrayList<String>>> versions,
                                   boolean denominator) throws IOException, Back {
        var soundVersionName = new TreeMap<String, TreeMap<String, String>>();

        for (Map.Entry<String, ArrayList<String>> sound : base.soundNames().entrySet()) {
            // Looping through all versions to look for matching sounds.
            for (Map.Entry<String, TreeMap<String, ArrayList<String>>> version : versions.entrySet()) {
                Set<Map.Entry<String, ArrayList<String>>> sound2EntrySet = version.getValue().entrySet();
                boolean checkAgain = true;

                // Firstly, do a check of matching names.
                for (var sound2 : sound2EntrySet) {
                    if (sound.getKey().equals(sound2.getKey())) {
                        soundVersionName.computeIfAbsent(sound.getKey().toUpperCase().replace('.', '_'), s -> new TreeMap<>(versionStringComparator))
                                .put(version.getKey(), sound2.getKey());
                        checkAgain = false;
                        break;
                    }
                }

                // Then check if sound has the same ogg locations.
                if (checkAgain) {
                    for (var sound2 : sound2EntrySet) {
                        if (!sound.getValue().isEmpty() && !sound2.getValue().isEmpty() && sound.getValue().containsAll(sound2.getValue())) {
                            soundVersionName.computeIfAbsent(sound.getKey().toUpperCase().replace('.', '_'), s -> new TreeMap<>(versionStringComparator))
                                    .put(version.getKey(), sound2.getKey());
                            break;
                        }
                    }
                }
            }
        }

        if (soundVersionName.isEmpty()) {
            System.out.println("No sounds were found.");
            throw new Back();
        }

        var builder = new StringBuilder();
        boolean firstIteration = true;

        for (Map.Entry<String, TreeMap<String, String>> soundEntry : soundVersionName.entrySet()) {
            if (firstIteration) {
                firstIteration = false;
            } else {
                builder.append("),\n");
            }

            builder.append(soundEntry.getKey()).append("(");

            if (denominator) {
                String previousSound = "";
                String previousVersion = "";
                String minVersion = "";
                Set<Map.Entry<String, String>> entrySet = soundEntry.getValue().entrySet();
                int i = 0;
                var versionsAndSound = new LinkedHashMap<String, String>();

                for (Map.Entry<String, String> versionEntry : soundEntry.getValue().entrySet()) {
                    String version = versionEntry.getKey();
                    String sound = versionEntry.getValue();

                    // If there's only one version.
                    if (entrySet.size() == 1) {
                        versionsAndSound.put(version + "-" + version, sound);
                        // Finish it up on last version entry.
                    } else if (++i == entrySet.size()) {
                        if (previousSound.equals(sound)) {
                            versionsAndSound.put(minVersion + "-" + version, previousSound);
                        } else {
                            versionsAndSound.put(minVersion + "-" + previousVersion, previousSound);
                            versionsAndSound.put(version + "-" + version, sound);
                        }
                    } else {
                        if (minVersion.isEmpty()) minVersion = version;
                        if (previousSound.isEmpty()) {
                            previousSound = sound;
                        } else {
                            if (!previousSound.equals(sound)) {
                                versionsAndSound.put(minVersion + "-" + previousVersion, previousSound);
                                previousSound = sound;
                                minVersion = version;
                            }
                        }

                        previousVersion = version;
                    }
                }

                boolean firstVersion = true;
                for (Map.Entry<String, String> versionAndSound : versionsAndSound.entrySet()) {
                    if (firstVersion) {
                        firstVersion = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append('"').append(versionAndSound.getKey()).append(' ').append(versionAndSound.getValue()).append('"');
                }
            } else {
                // No denominator has only one version and only one sound name.
                for (Map.Entry<String, String> versionEntry : soundEntry.getValue().entrySet()) {
                    builder.append('"').append(versionEntry.getValue()).append('"');
                }
            }
        }

        builder.append(");");

        Path output = Path.of("./output.txt");

        if (Files.deleteIfExists(output)) {
            System.out.println("Previous 'output.txt' deleted.");
        }

        PathUtils.write(builder.toString(), output);
    }

    public static @NotNull TreeMap<String, TreeMap<String, ArrayList<String>>> getAvailableVersions() throws IOException {
        var map = new TreeMap<String, TreeMap<String, ArrayList<String>>>(versionStringComparator);
        var parser = new JSONParser();

        if (!Files.isDirectory(SoundFinder.SOUNDS_FOLDER)) return map;

        try (Stream<Path> sounds = Files.list(SoundFinder.SOUNDS_FOLDER)) {
            sounds.filter(file -> file.toString().endsWith(".json")).forEach(jsonPath -> {
                // Getting and validating version. Files that don't match will be ignored.
                String jsonName = jsonPath.getFileName().toString();
                int extensionIndex = jsonName.lastIndexOf('.');
                int spaceIndex = jsonName.lastIndexOf(' ');
                if (extensionIndex == -1 || spaceIndex == -1) return;
                String version = jsonName.substring(spaceIndex + 1, extensionIndex);
                if (!Version.validVersion.matcher(version).matches()) return;

                // Parsing file as json.
                final JSONObject json;
                try (FileReader reader = new FileReader(jsonPath.toFile())) {
                    json = (JSONObject) parser.parse(reader);
                } catch (ParseException | IOException e) {
                    System.err.println("Unable to parse '" + jsonName + "' as a json.");
                    return;
                }

                // Finally adding the sound names and ogg file locations to the version map.
                map.put(version, getSoundNamesAndOggArray(json));
            });
        }

        return map;
    }

    private static @NotNull TreeMap<String, ArrayList<String>> getSoundNamesAndOggArray(@NotNull JSONObject json) {
        var soundNames = new TreeMap<String, ArrayList<String>>();

        for (Object entry : json.entrySet()) {
            var soundEntry = (Map.Entry<?, ?>) entry;
            var soundOggs = new ArrayList<String>();
            var child = (JSONObject) soundEntry.getValue();

            if (child.get("sounds") instanceof JSONArray oggLocationArray) {
                for (Object oggLocation : oggLocationArray) {
                    // Some sounds have specific pitch and volume, so looking for ogg under "name" key.
                    if (oggLocation instanceof JSONObject obj) {
                        if (obj.containsKey("name")) soundOggs.add(obj.get("name").toString());
                    } else {
                        soundOggs.add(oggLocation.toString());
                    }
                }
            } else {
                // Does not have "sounds" ogg locations array, aka: not a real sound.
                // Should not happen if it's a default sound resource.
                continue;
            }
            soundNames.put(soundEntry.getKey().toString(), soundOggs);
        }

        return soundNames;
    }

}