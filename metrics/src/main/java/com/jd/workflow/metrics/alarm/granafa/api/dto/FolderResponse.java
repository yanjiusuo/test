package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

@Data
public class FolderResponse {
    String id;
    String uid;
    String title;
    String url;
    boolean hasAcl;
    boolean canSave;
    boolean canEdit;
    boolean canAdmin;
    String createdBy;
    String created;
    String updatedBy;
    String updated;
    Integer version;
}
