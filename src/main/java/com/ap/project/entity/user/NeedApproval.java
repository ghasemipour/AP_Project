package com.ap.project.entity.user;

import com.ap.project.Enums.ApprovalStatus;

public interface NeedApproval {
    public void changeStatus(ApprovalStatus status);
}
