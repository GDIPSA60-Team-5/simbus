package com.example.springbackend.dto;

public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String newPassword, String currentPassword) {
        this.newPassword = newPassword;
        this.currentPassword = currentPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
