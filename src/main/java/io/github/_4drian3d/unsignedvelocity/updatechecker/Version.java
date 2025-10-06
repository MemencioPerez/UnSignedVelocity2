package io.github._4drian3d.unsignedvelocity.updatechecker;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Version(int major, int minor, int patch, Optional<String> preRelease) implements Comparable<Version> {

    public static Version parse(String version) {
        String[] parts = version.split("[.-]");
        if (parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException("Invalid version format");
        }
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        Optional<String> preRelease = parts.length == 4 ? Optional.of(parts[3]) : Optional.empty();
        return new Version(major, minor, patch, preRelease);
    }

    public boolean isHigherThan(Version version) {
        return this.compareTo(version) > 0;
    }

    public boolean isLowerThan(Version version) {
        return this.compareTo(version) < 0;
    }

    @Override
    public int compareTo(Version other) {
        if (this.major != other.major) {
            return Integer.compare(this.major, other.major);
        }
        if (this.minor != other.minor) {
            return Integer.compare(this.minor, other.minor);
        }
        if (this.patch != other.patch) {
            return Integer.compare(this.patch, other.patch);
        }
        if (this.preRelease.isPresent() && other.preRelease.isPresent()) {
            return comparePreRelease(this.preRelease.get(), other.preRelease.get());
        } else if (this.preRelease.isPresent()) {
            return -1;
        } else if (other.preRelease.isPresent()) {
            return 1;
        }
        return 0;
    }

    private int comparePreRelease(String preRelease1, String preRelease2) {
        String[] parts1 = preRelease1.split("\\.");
        String[] parts2 = preRelease2.split("\\.");

        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int comparison = comparePreReleasePart(parts1[i], parts2[i]);
            if (comparison != 0) {
                return comparison;
            }
        }

        return Integer.compare(parts1.length, parts2.length);
    }

    private int comparePreReleasePart(String part1, String part2) {
        boolean isNumeric1 = part1.matches("\\d+");
        boolean isNumeric2 = part2.matches("\\d+");

        if (isNumeric1 && isNumeric2) {
            return Integer.compare(Integer.parseInt(part1), Integer.parseInt(part2));
        } else if (isNumeric1) {
            return -1;
        } else if (isNumeric2) {
            return 1;
        } else {
            return part1.compareTo(part2);
        }
    }

    @Override
    public @NotNull String toString() {
        return major + "." + minor + "." + patch + preRelease.map(s -> "-" + s).orElse("");
    }
}
