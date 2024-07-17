package com.jd.workflow.jsf.analyzer;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * 项目名称：parent
 * 类 名 称：MavenJarLocation
 * 类 描 述：maven 坐标
 * 创建时间：2022-06-16 20:16
 * 创 建 人：wangxiaofei8
 */
@Data
public class MavenJarLocation {
    private String groupId;

    private String artifactId;

    private String version;
    public MavenJarLocation() {
    }

    public MavenJarLocation(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String toFolder(){
        return groupId + "_" + artifactId + "_" + version;
    }

    public String toString(){
        return groupId + ":" + artifactId + ":" + version;
    }

    /**
     * @hidden
     * @return
     */
    public boolean isSnapshot(){
        return version.endsWith("SNAPSHOT") || version.endsWith("snapshot");
    }
    public static MavenJarLocation from(String path){
        if(StringUtils.isEmpty(path) || !path.contains(":")){
            return null;
        }
        String[] split = path.split(":");
        MavenJarLocation mavenJarLocation = new MavenJarLocation();
        mavenJarLocation.setGroupId(split[0]);
        mavenJarLocation.setArtifactId(split[1]);
        mavenJarLocation.setVersion(split[2]);
        return mavenJarLocation;
    }
}
