/*
 * Copyright 2014 Yaroslav Mytkalyk
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docd.purefm.commandline;

import com.docd.purefm.file.Permissions;

/**
 * chmod command - changes file mod bits
 *
 * This manual page documents the GNU version of chmod. chmod changes the file mode bits of each
 * given file according to mode, which can be either a symbolic representation of changes to make,
 * or an octal number representing the bit pattern for the new mode bits.
 *
 * The format of a symbolic mode is [ugoa...][[+-=][perms...]...], where perms is either zero or
 * more letters from the set rwxXst, or a single letter from the set ugo. Multiple symbolic modes
 * can be given, separated by commas.
 *
 * A combination of the letters ugoa controls which users' access to the file will be changed: the
 * user who owns it (u), other users in the file's group (g), other users not in the file's group
 * (o), or all users (a). If none of these are given, the effect is as if a were given, but bits
 * that are set in the umask are not affected.
 *
 * The operator + causes the selected file mode bits to be added to the existing file mode bits of
 * each file; - causes them to be removed; and = causes them to be added and causes unmentioned bits
 * to be removed except that a directory's unmentioned set user and group ID bits are not affected.
 *
 * @author Doctoror
 */
public final class CommandChmod extends BusyboxCommand {

    /**
     * Builds chmod command
     *
     * @param path Path to file to apply permissions to
     * @param permissions Permissions to apply to path
     */
    public CommandChmod(final String path, final Permissions permissions) {
        super("chmod " + CommandLineUtils.toOctalPermission(permissions) +
                " " + CommandLineUtils.getCommandLineString(path));
    }
}
