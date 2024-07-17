package com.jd.workflow.webhook;
import lombok.Data;

@Data
public class Project {

    private String avatar_url;
    private String default_branch;
    private String description;
    private String git_http_url;
    private String git_ssh_url;
    private String homepage;
    private String http_url;
    private long id;
    private String name;
    private String namespace;
    private String path_with_namespace;
    private String ssh_url;
    private String url;
    private int visibility_level;
    private String web_url;
}
