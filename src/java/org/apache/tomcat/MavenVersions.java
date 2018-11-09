package org.apache.tomcat;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.Assert;

public class MavenVersions {

    public static void main(String... args) {
        String s0 = "2.0";
        String s1 = "3.0-b01";
        String s2 = "3.0.0-b01";
        String s3 = "3.0";
        String s4 = "3.0.0";
        String s5 = "3.0.1-b04";
        String s6 = "3.0.1";
        String s7 = "3.0.2";

        DefaultArtifactVersion v0 = new DefaultArtifactVersion(s0);
        DefaultArtifactVersion v1 = new DefaultArtifactVersion(s1);
        DefaultArtifactVersion v2 = new DefaultArtifactVersion(s2);
        DefaultArtifactVersion v3 = new DefaultArtifactVersion(s3);
        DefaultArtifactVersion v4 = new DefaultArtifactVersion(s4);
        DefaultArtifactVersion v5 = new DefaultArtifactVersion(s5);
        DefaultArtifactVersion v6 = new DefaultArtifactVersion(s6);
        DefaultArtifactVersion v7 = new DefaultArtifactVersion(s7);

        Assert.assertTrue(v0.compareTo(v1) < 0);
        Assert.assertTrue(v1.compareTo(v2) == 0);
        Assert.assertTrue(v2.compareTo(v3) < 0);
        Assert.assertTrue(v3.compareTo(v4) == 0);
        Assert.assertTrue(v4.compareTo(v5) < 0);
        Assert.assertTrue(v5.compareTo(v6) < 0);
        Assert.assertTrue(v6.compareTo(v7) < 0);

    }
}
