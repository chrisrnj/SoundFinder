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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Commands {
    private static final @NotNull Back back = new Back();
    public static boolean delayMessages = true;
    private static boolean firstBaseAsking = true;
    private static double delayMultiplier = 1.0;

    public static void exit(@NotNull String[] command) {
        if (command.length <= 1 || !command[1].equalsIgnoreCase("!delete")) {
            System.out.println("Deleting sounds...");
            try {
                PathUtils.deleteAll(SoundFinder.SOUNDS_FOLDER);
            } catch (Throwable t) { // Must exit the program even if it fails deletion.
                System.out.println("Unable to delete '" + SoundFinder.SOUNDS_FOLDER.getFileName().toString() + "' folder:");
                t.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("Goodbye, happy coding!");
        System.exit(0);
    }

    public static void help(@NotNull String[] command) throws Back {
        if (command.length > 1) {
            switch (command[1]) {
                case "exit", "end", "close", "stop" -> System.out.println("""
                        Showing help of exit command >>
                          Aliases: end, close, stop
                          Description: Exits the script and deletes sounds folder
                          Arguments:
                            !delete: Prevents the deletion of sounds folder.
                          Usage: > exit [args]""");
                case "start", "begin" -> System.out.println("""
                        Showing help of start command >>
                          Alias: begin
                          Description: Generates a list of sounds based on jsons in sounds folder
                          Usage: > start""");
                case "help", "command", "commands" -> System.out.println("""
                        Showing help of help command >>
                          Alias: command, commands
                          Description: Shows the list of commands or detailed help of a specific command
                          Arguments:
                            <cmd>: Shows the details of a command and its arguments.
                          Usage: > help [args]""");
                default -> System.out.println("Command not found! Type \"help\" to see the list of commands.");
            }
            throw back;
        }

        System.out.println("List of available commands (Arguments enclosed in [] are optional):");
        System.out.println("- exit [!delete] -> Exits the script and deletes 'sounds' folder");
        System.out.println("- help [cmd]     -> Shows the list of commands or detailed help of a specific command");
        System.out.println("- start          -> Generates a list of sounds based on jsons in sounds folder");
        throw back;
    }

    public static void start(@NotNull Scanner input) throws Back {
        List<String> availableVersions = getAvailableVersions();

        if (availableVersions.isEmpty()) {
            System.out.println("It looks like there are no sound files in " + SoundFinder.SOUNDS_FOLDER.getFileName().toString() + " folder.");
            System.out.println("Please add sound files or restart the program to extract the default sounds.");
            tryAndSleep(5000);
            throw back;
        }
        // If there is only one version, using it as base and asking to remove denominator.
        else if (availableVersions.size() == 1) {
            allSet(input, availableVersions.get(0), availableVersions, removeVersionDenominator(input, availableVersions));
            throw back;
        }

        // Asking for the base.
        String base = base(input, availableVersions);
        // Asking if any versions should be excluded from the constructor.
        exclude(input, availableVersions);
        // All set, asking for confirmation and creating enum.
        // If user has excluded every other version and left one, then ask if denominator should be removed.
        allSet(input, base, availableVersions, removeVersionDenominator(input, availableVersions));
        throw back;
    }

    private static @NotNull List<String> getAvailableVersions() {
        var list = new ArrayList<String>();
        list.add("1.8");
        return list;
    }

    private static @NotNull String base(@NotNull Scanner input, @NotNull List<String> availableVersions) throws Back {
        System.out.println("Please input the version you would like to use as base for the enum" +
                (firstBaseAsking ? ", that is the version that will be used to create the names of the enums, and to" +
                        " compare to sound files of other versions." : "."));
        System.out.println("Type 'back' to go back to command prompt.");
        System.out.println("Available sound versions: " + availableVersions);
        System.out.print("> ");
        firstBaseAsking = false;

        String base = input.nextLine();

        checkThrowBack(base);

        if (!availableVersions.contains(base)) {
            System.out.println("\nUnknown version '" + base + "'\n");
            tryAndSleep(1000);
            return base(input, availableVersions);
        }

        return base;
    }

    private static void exclude(@NotNull Scanner input, @NotNull List<String> availableVersions) throws Back {
        if (availableVersions.size() == 1) {
            System.out.println("\nLooks like there is only one version left. Using it.\n");
            tryAndSleep(2000);
            return;
        }
        System.out.println("\nType in the versions you would not like to have its sounds in the enum. One at a time, please.");
        System.out.println("Type 'done' when you're done, or 'back' to go back to prompt.");
        System.out.println("Current sound versions: " + availableVersions);
        System.out.print("> ");

        String excluding = input.nextLine();

        checkThrowBack(excluding);

        if (excluding.equalsIgnoreCase("done") || excluding.equalsIgnoreCase("ready")) {
            return;
        } else if (!availableVersions.remove(excluding)) {
            System.out.println("\nUnknown version '" + excluding + "'");
            tryAndSleep(1000);
        }

        exclude(input, availableVersions);
    }

    private static boolean removeVersionDenominator(@NotNull Scanner input, @NotNull List<String> availableVersions) throws Back {
        if (availableVersions.size() == 1) {
            System.out.println("Only one version was detected: " + availableVersions.get(0));
            System.out.println("Since you're creating an enum with only one version of sounds, would you like to remove the version denominator of the string at the enum constructor?");
            System.out.print("Y/N > ");

            String response = input.nextLine();

            checkThrowBack(response);
            if (response.equalsIgnoreCase("Y") || response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("yeah") || response.equalsIgnoreCase("ye") || response.equalsIgnoreCase("yea")) {
                return true;
            } else if (response.equalsIgnoreCase("N") || response.equalsIgnoreCase("no") || response.equalsIgnoreCase("naur") || response.equalsIgnoreCase("nah") || response.equalsIgnoreCase("nope")) {
                return false;
            } else {
                System.out.println("I'll take that as a no.");
                tryAndSleep(1300);
                return false;
            }
        }
        return false;
    }

    private static void allSet(@NotNull Scanner input, @NotNull String base, @NotNull List<String> versions, boolean noDenominator) throws Back {
        System.out.println("\n\nWe are all set! Please confirm the options:");
        tryAndSleep(2500 * delayMultiplier);
        System.out.println("\n- Base for enum names: " + base);
        tryAndSleep(800 * delayMultiplier);
        System.out.println("- Versions to add sound names to constructor: " + versions);

        if (versions.size() == 1) {
            tryAndSleep(800 * delayMultiplier);
            System.out.println("- Remove version denominator from sound names in enum constructor: " + (noDenominator ? "yes" : "no"));
        }

        tryAndSleep(10000 * delayMultiplier);
        System.out.println("\nType 'confirm' to confirm the options, or 'cancel' to discard and go back to command prompt.");
        System.out.print("> ");

        String confirmation = input.nextLine();

        checkThrowBack(confirmation);
        if (confirmation.equalsIgnoreCase("confirm") || confirmation.equalsIgnoreCase("ok") || confirmation.equalsIgnoreCase("proceed") || confirmation.equalsIgnoreCase("check") || confirmation.equalsIgnoreCase("yes") || confirmation.equalsIgnoreCase("okay")) {
            System.out.println("\nPLEASE WAIT WHILE SOUNDS ARE WRITTEN TO 'output.txt'...");
            tryAndSleep(4000);
            System.err.println("FAILED! THIS PROGRAM IS NOT FINISHED AND THIS FUNCTIONALITY IS NOT AVAILABLE YET.");
            tryAndSleep(6000);
            throw back;
        } else {
            System.out.println("\nI couldn't catch that, I'm going to ask again.");
            tryAndSleep(2000 * delayMultiplier);
            delayMultiplier -= 0.2;
            if (delayMultiplier <= 0) {
                delayMultiplier = 0;
            }
            allSet(input, base, versions, noDenominator);
        }
    }

    private static void tryAndSleep(double time) {
        if (!delayMessages) return;
        try {
            Thread.sleep((long) time);
        } catch (InterruptedException ignored) {
        }
    }

    private static void checkThrowBack(@NotNull String lineInput) throws Back {
        if (lineInput.equalsIgnoreCase("back") || lineInput.equalsIgnoreCase("prompt") || lineInput.equalsIgnoreCase("cancel")) {
            throw back;
        }
    }
}
