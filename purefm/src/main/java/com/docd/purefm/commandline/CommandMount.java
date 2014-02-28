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

import com.docd.purefm.Environment;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Doctoror
 *
 * mount - mount a filesystem
 *
 * No arguments lists mount points
 */
public final class CommandMount extends Command {

    public CommandMount(final String filter) {
        super(buildCommand(filter));
    }

    private static String buildCommand(final String filter) {
        if (Environment.isUtilAvailable("grep")) {
            return "mount | grep " + filter;
        }
        return "mount";
    }

    public static final class MountOutput {
        public final String device;
        public final String mountPoint;
        public final String fileSystem;
        public final String[] options;

        MountOutput(final String output) {
            final String[] result = output.split("\\s");
            if (result.length < 4) {
                throw new IllegalArgumentException("Mount: invalid output");
            }
            device = result[0];
            mountPoint = result[1];
            fileSystem = result[2];
            options = result[3].split(",");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final MountOutput that = (MountOutput) o;

            if (device != null ? !device.equals(that.device) : that.device != null) return false;
            if (fileSystem != null ? !fileSystem.equals(that.fileSystem) : that.fileSystem != null)
                return false;
            if (mountPoint != null ? !mountPoint.equals(that.mountPoint) : that.mountPoint != null)
                return false;
            if (!Arrays.equals(options, that.options)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = device != null ? device.hashCode() : 0;
            result = 31 * result + (mountPoint != null ? mountPoint.hashCode() : 0);
            result = 31 * result + (fileSystem != null ? fileSystem.hashCode() : 0);
            result = 31 * result + (options != null ? Arrays.hashCode(options) : 0);
            return result;
        }

        @Override
        public String toString() {
            return device + " " + mountPoint + " " + fileSystem + " " + Arrays.toString(options);
        }
    }

    @NotNull
    public static Set<MountOutput> listMountpoints(final String filter) {
        final Set<MountOutput> result = new HashSet<MountOutput>();
        final List<String> listed = CommandLine.executeForResult(ShellHolder.getShell(),
                new CommandMount(filter));
        if (listed != null) {
            for (final String line : listed) {
                try {
                    result.add(new MountOutput(line));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
