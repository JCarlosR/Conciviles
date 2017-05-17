package com.programacionymas.conciviles.io.response;

import java.util.ArrayList;

public class NewInformResponse {

    private boolean success;
    private ArrayList<String> errors;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void setErrors(ArrayList<String> errors) {
        this.errors = errors;
    }

    public String getFirstError() {
        return errors.get(0);
    }
}
