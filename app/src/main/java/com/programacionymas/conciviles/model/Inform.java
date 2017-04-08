package com.programacionymas.conciviles.model;

/*
{
    "user_id":6,
    "created_at":"2017-03-20 23:20:58",
    "user_name":"Mauricio Gutierrez R",
    "from_date_format":"20\/03\/2017",
    "to_date_format":"21\/03\/2017"
}
*/
public class Inform {

    private int id;
    private int user_id;
    private String from_date_format;
    private String to_date_format;
    private String created_at;
    private String user_name;
    private boolean isEditable;

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getFromDate() {
        return from_date_format;
    }

    public void setFromDate(String from_date_format) {
        this.from_date_format = from_date_format;
    }

    public String getToDate() {
        return to_date_format;
    }

    public void setToDate(String to_date_format) {
        this.to_date_format = to_date_format;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }
}
