package io.github._4drian3d.unsignedvelocity.updatechecker;

public record Version(int major, int minor, int patch) implements Comparable<Version> {

    public static Version parse(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid version format");
        }
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        return new Version(major, minor, patch);
    }

    public boolean isEqualOrLowerThan(Version version) {
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
        return Integer.compare(this.patch, other.patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
