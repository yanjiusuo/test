package com.jd.workflow.console.dto;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Version implements Comparable<Version> {
    private static final String VERSION_PARSE_ERROR = "Invalid version string! Could not parse segment %s within %s.";
    private  int major;
    private  int minor;
    private  int reportVersion;


    public Version(int... parts) {
        Assert.notNull(parts, "Parts must not be null!");
        Assert.isTrue(parts.length > 0 && parts.length < 5, String.format("Invalid parts length. 0 < %s < 5", parts.length));
        this.major = parts[0];
        this.minor = parts.length > 1 ? parts[1] : 0;
        this.reportVersion = parts.length > 2 ? parts[2] : 0;

        Assert.isTrue(this.major >= 0, "Major version must be greater or equal zero!");
        Assert.isTrue(this.minor >= 0, "Minor version must be greater or equal zero!");
        Assert.isTrue(this.reportVersion >= 0, "Bugfix version must be greater or equal zero!");
    }

    public static Version parse(String version) {
        Assert.hasText(version, "Version must not be null o empty!");
        String[] parts = version.trim().split("\\.");
        int[] intParts = new int[parts.length];

        for(int i = 0; i < parts.length; ++i) {
            String input = i == parts.length - 1 ? parts[i].replaceAll("\\D.*", "") : parts[i];
            if (StringUtils.hasText(input)) {
                try {
                    intParts[i] = Integer.parseInt(input);
                } catch (IllegalArgumentException var6) {
                    throw new IllegalArgumentException(String.format("Invalid version string! Could not parse segment %s within %s.", input, version), var6);
                }
            }
        }

        return new Version(intParts);
    }

    public static Version javaVersion() {
        return parse(System.getProperty("java.version"));
    }

    public boolean isGreaterThan(Version version) {
        return this.compareTo(version) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Version version) {
        return this.compareTo(version) >= 0;
    }

    public boolean is(Version version) {
        return this.equals(version);
    }

    public boolean isLessThan(Version version) {
        return this.compareTo(version) < 0;
    }

    public boolean isLessThanOrEqualTo(Version version) {
        return this.compareTo(version) <= 0;
    }

    public int compareTo(Version that) {
        if (this.major != that.major) {
            return this.major - that.major;
        } else if (this.minor != that.minor) {
            return this.minor - that.minor;
        } else if (this.reportVersion != that.reportVersion) {
            return this.reportVersion - that.reportVersion;
        } else {
            return  0;
        }
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Version)) {
            return false;
        } else {
            Version that = (Version)obj;
            return this.major == that.major && this.minor == that.minor && this.reportVersion == that.reportVersion;
        }
    }

    public int hashCode() {
        int result = 17;
         result = result + 31 * this.major;
        result += 31 * this.minor;
        result += 31 * this.reportVersion;
        return result;
    }
    public void increase(int pos){
        if(pos == 0){
            major++;
            minor =0 ;
            reportVersion = 0;
        }else if(pos == 1){
            minor++;
            reportVersion = 0;
        }else{
            reportVersion++;
        }
    }
    public String toString() {
        List<Integer> digits = new ArrayList();
        digits.add(this.major);
        digits.add(this.minor);
        //if (this.reportVersion != 0) {
            digits.add(this.reportVersion);
        //}



        return StringUtils.collectionToDelimitedString(digits, ".");
    }
}