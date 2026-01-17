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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public final class SoundFinder {
    static final @NotNull Path SOUNDS_FOLDER;
    static final @NotNull String SOUNDS_FOLDER_NAME;
    private static final @NotNull Version MAX_SOUNDS_VERSION = new Version("1.21.11");
    private static final @NotNull Version MIN_SOUNDS_VERSION = new Version("1.7");
    private static final @NotNull String[] soundResources = new String[]{"sounds 1.7.json", "sounds 1.7.10.json",
            "sounds 1.8.json", "sounds 1.8.9.json", "sounds 1.9.json", "sounds 1.9.4.json", "sounds 1.10.json",
            "sounds 1.10.2.json", "sounds 1.11.json", "sounds 1.11.2.json", "sounds 1.12.json", "sounds 1.12.2.json",
            "sounds 1.13.json", "sounds 1.13.2.json", "sounds 1.14.json", "sounds 1.14.4.json", "sounds 1.15.json",
            "sounds 1.15.2.json", "sounds 1.16.json", "sounds 1.16.5.json", "sounds 1.17.json", "sounds 1.17.1.json",
            "sounds 1.18.json", "sounds 1.18.2.json", "sounds 1.19.json", "sounds 1.19.2.json", "sounds 1.19.3.json",
            "sounds 1.19.4.json", "sounds 1.20.json", "sounds 1.20.1.json", "sounds 1.20.2.json", "sounds 1.20.3.json",
            "sounds 1.20.4.json", "sounds 1.20.5.json", "sounds 1.20.6.json", "sounds 1.21.json", "sounds 1.21.1.json",
            "sounds 1.21.2.json", "sounds 1.21.3.json", "sounds 1.21.4.json", "sounds 1.21.5.json",
            "sounds 1.21.6.json", "sounds 1.21.8.json", "sounds 1.21.9.json", "sounds 1.21.10.json",
            "sounds 1.21.11.json"};
    private static boolean firstPrompt = true;

    static {
        try {
            // Asserting creation of folder and getting it on the right path,
            // since this location might be already used by a regular file.
            SOUNDS_FOLDER = PathUtils.getDirectory(Path.of("./sounds"));
            SOUNDS_FOLDER_NAME = SOUNDS_FOLDER.getFileName().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SoundFinder() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && (args[0].equalsIgnoreCase("speedrun") || args[0].equalsIgnoreCase("nodelay") || args[0].equalsIgnoreCase("nosleep"))) {
            Commands.delayMessages = false;
        }
        System.out.print("Extracting sounds... ");

        try {
            extractSounds();
        } catch (IOException e) {
            throw new IOException("Could not extract sounds", e);
        }

        System.out.println("\nWelcome to Sound Finder!" + "\n" + "\nUsing all sounds from " + MIN_SOUNDS_VERSION + " to " + MAX_SOUNDS_VERSION + "." + "\nIf you'd like to use sounds from another version, add a new sounds json into the folder '" + SOUNDS_FOLDER_NAME + "'.");

        prompt(new Scanner(System.in));
    }

    private static void prompt(@NotNull Scanner input) {
        System.out.println("\nPlease type a command." + (firstPrompt ? " (Type \"help\" to see the list of commands)" : ""));
        System.out.print("> ");
        firstPrompt = false;

        String[] command = input.nextLine().trim().replaceAll(" +", " ").toLowerCase().split(" ");

        System.out.print('\n');

        try {
            switch (command[0]) {
                case "end", "exit", "close", "stop" -> Commands.exit(command);
                case "start", "begin" -> Commands.start(input);
                case "help", "commands" -> Commands.help(command);
                default -> {
                    System.out.println("Command not found! Type \"help\" to see the list of commands.");
                    prompt(input);
                }
            }
        } catch (Back b) {
            prompt(input);
        } catch (Throwable t) {
            System.out.println("Unable to execute command '" + command[0] + "':");
            t.printStackTrace();
        }
    }

    private static void extractSounds() throws IOException {
        boolean fail = false;
        int existingResources = 0;

        for (String resource : soundResources) {
            Path resourcePath = SOUNDS_FOLDER.resolve(resource);

            if (Files.notExists(resourcePath)) {
                try (InputStream sound = SoundFinder.class.getClassLoader().getResourceAsStream("sounds/" + resource)) {
                    if (sound == null) {
                        if (!fail) {
                            fail = true;
                            System.out.print('\n');
                        }
                        System.err.println("Could not find 'sounds/" + resource + "' sound resource.");
                        continue;
                    }

                    Files.copy(sound, resourcePath);
                }
            } else {
                existingResources++;
            }
        }

        if (existingResources == soundResources.length) {
            System.out.println("Sounds were already found on destination folder.");
            return;
        }

        if (fail) {
            System.out.println("Extraction Done.");
        } else {
            System.out.println("Done");
        }
    }
}