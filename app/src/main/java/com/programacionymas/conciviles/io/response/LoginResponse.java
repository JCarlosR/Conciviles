package com.programacionymas.conciviles.io.response;

public class LoginResponse {

    private boolean success;
    private int user_id;
    private boolean is_admin;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public boolean isAdmin() {
        return is_admin;
    }

    public void setIsAdmin(boolean is_admin) {
        this.is_admin = is_admin;
    }
}
