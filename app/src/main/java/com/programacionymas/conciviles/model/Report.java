package com.programacionymas.conciviles.model;

import android.content.ContentValues;

import com.programacionymas.conciviles.io.MyApiAdapter;
import com.programacionymas.conciviles.io.response.NewReportResponse;
import com.programacionymas.conciviles.io.sqlite.MyDbContract.ReportEntry;

import rx.Observable;

public class Report {

/*
"id":25,
"user_id":6,
"work_front_id":2, "area_id":4, "responsible_id":7,
"aspect":"Positivo",
"critical_risks_id":13,
"potential":"Medio", "state":"Abierto",
"image":"http:\/\/app.mauriciogutierrez.xyz\/images\/report\/25.jpg",
"image_action":"http:\/\/app.mauriciogutierrez.xyz\/images\/action\/default.png",
"planned_date":"2017-03-20", "deadline":null, "inspections":1,
"description":"Colocare una abierta", "actions":"Seguir", "observations":"",
"created_at":"2017-03-20 23:37:30",
"work_front_name":"Concreto",
"area_name":"HSE",
"responsible_name":"Gutierrez Mauricio",
"critical_risks_name":"Manipulaci\u00f3n de alimentos"
*/

    private int id;
    private int user_id;
    private String aspect;
    private String potential;
    private String state;
    private String image;
    private String image_action;
    private String planned_date;
    private String deadline;
    private int inspections;
    private String description;
    private String actions;
    private String observations;
    private String created_at;
    private String work_front_name;
    private String area_name;
    private String responsible_name;
    private String critical_risks_name;

    // the next IDs are not needed in the presentation (when the API is read)
    // but it is important to publish new reports to the API
    private int critical_risks_id;
    private int work_front_id;
    private int area_id;
    private int responsible_id;
    private int inform_id;
    // base 64 images (only used for the offline storage)
    private String image_base64;
    private String image_action_base64;

    // local updates are recognized based on this value
    private boolean offline_edited;
    private int _id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
    }

    public String getPotential() {
        return potential;
    }

    public void setPotential(String potential) {
        this.potential = potential;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageAction() {
        return image_action;
    }

    public void setImageAction(String image_action) {
        this.image_action = image_action;
    }

    public String getPlannedDate() {
        return planned_date;
    }

    public void setPlannedDate(String planned_date) {
        this.planned_date = planned_date;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public int getInspections() {
        return inspections;
    }

    public void setInspections(int inspections) {
        this.inspections = inspections;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getWorkFrontName() {
        return work_front_name;
    }

    public void setWorkFrontName(String work_front_name) {
        this.work_front_name = work_front_name;
    }

    public String getAreaName() {
        return area_name;
    }

    public void setAreaName(String area_name) {
        this.area_name = area_name;
    }

    public String getResponsibleName() {
        return responsible_name;
    }

    public void setResponsibleName(String responsible_name) {
        this.responsible_name = responsible_name;
    }

    public String getCriticalRisksName() {
        return critical_risks_name;
    }

    public void setCriticalRisksName(String critical_risks_name) {
        this.critical_risks_name = critical_risks_name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        if (getId() == 0)
            values.putNull(ReportEntry.COLUMN_ID);
        else
            values.put(ReportEntry.COLUMN_ID, getId());

        values.put(ReportEntry.COLUMN_INFORM_ID, getInformId()); // it is not received from the API

        values.put(ReportEntry.COLUMN_USER_ID, getUserId());
        values.put(ReportEntry.COLUMN_ASPECT, getAspect());
        values.put(ReportEntry.COLUMN_POTENTIAL, getPotential());
        values.put(ReportEntry.COLUMN_STATE, getState());
        values.put(ReportEntry.COLUMN_IMG, getImage());
        values.put(ReportEntry.COLUMN_IMG_ACTION, getImageAction());
        values.put(ReportEntry.COLUMN_PLANNED_DATE, getPlannedDate());
        values.put(ReportEntry.COLUMN_DEADLINE, getDeadline());
        values.put(ReportEntry.COLUMN_INSPECTIONS, getInspections());
        values.put(ReportEntry.COLUMN_DESCRIP, getDescription());
        values.put(ReportEntry.COLUMN_ACTIONS, getActions());
        values.put(ReportEntry.COLUMN_OBS, getObservations());

        values.put(ReportEntry.COLUMN_CREATED_AT, getCreatedAt());
        values.put(ReportEntry.COLUMN_WORK_FRONT_NAME, getWorkFrontName());
        values.put(ReportEntry.COLUMN_AREA_NAME, getAreaName());
        values.put(ReportEntry.COLUMN_RESPONSIBLE_NAME, getResponsibleName());
        values.put(ReportEntry.COLUMN_CRIT_RISKS_NAME, getCriticalRisksName());

        // IDs (used for the offline functionality)
        values.put(ReportEntry.COLUMN_WORK_FRONT_ID, getWorkFrontId());
        values.put(ReportEntry.COLUMN_AREA_ID, getAreaId());
        values.put(ReportEntry.COLUMN_RESPONSIBLE_ID, getResponsibleId());
        values.put(ReportEntry.COLUMN_CRIT_RISKS_ID, getCriticalRisksId());

        if (wasOfflineEdited()) // this column is by default false
            values.put(ReportEntry.COLUMN_OFFLINE_EDITED, true);

        // offline image storage using base64 strings
        if (getImage() == null || getImage().isEmpty())
            values.put(ReportEntry.COLUMN_OFFLINE_IMG, getImageBase64());
        if (getImageAction() == null || getImageAction().isEmpty())
            values.put(ReportEntry.COLUMN_OFFLINE_IMG_ACTION, getImageActionBase64());

        return values;
    }

    public int getCriticalRisksId() {
        return critical_risks_id;
    }

    public void setCriticalRisksId(int critical_risks_id) {
        this.critical_risks_id = critical_risks_id;
    }

    public int getWorkFrontId() {
        return work_front_id;
    }

    public void setWorkFrontId(int work_front_id) {
        this.work_front_id = work_front_id;
    }

    public int getAreaId() {
        return area_id;
    }

    public void setAreaId(int area_id) {
        this.area_id = area_id;
    }

    public int getResponsibleId() {
        return responsible_id;
    }

    public void setResponsibleId(int responsible_id) {
        this.responsible_id = responsible_id;
    }

    public int getInformId() {
        return inform_id;
    }

    public void setInformId(int inform_id) {
        this.inform_id = inform_id;
    }

    public boolean wasOfflineEdited() {
        return offline_edited;
    }

    public void setOfflineEdited(boolean offline_edited) {
        this.offline_edited = offline_edited;
    }

    public String getImageBase64() {
        return image_base64;
    }

    public void setImageBase64(String image_base64) {
        this.image_base64 = image_base64;
    }

    public String getImageActionBase64() {
        return image_action_base64;
    }

    public void setImageActionBase64(String image_action_base64) {
        this.image_action_base64 = image_action_base64;
    }

    public Observable<NewReportResponse> postToServer() {
        Observable<NewReportResponse> observable;

        if ((image_base64 == null || image_base64.isEmpty()) && (image_action_base64 == null || image_action_base64.isEmpty())) {
            observable = MyApiAdapter.getApiService().postNewReport(
                    user_id, description, work_front_id, area_id, responsible_id,
                    planned_date, deadline,
                    state, actions, aspect, potential, String.valueOf(inspections),
                    critical_risks_id, observations, inform_id
            );
        } else {
            observable = MyApiAdapter.getApiService().postNewReportWithImages(
                    user_id, description, image_base64, work_front_id, area_id, responsible_id,
                    planned_date, deadline,
                    state, actions, image_action_base64, aspect, potential, String.valueOf(inspections),
                    critical_risks_id, observations, inform_id
            );
        }

        return observable;
    }

    public Observable<NewReportResponse> updateInServer() {
        Observable<NewReportResponse> observable;

        if ((image_base64 == null || image_base64.isEmpty()) && (image_action_base64 == null || image_action_base64.isEmpty())) {
            observable = MyApiAdapter.getApiService().updateNewReport(
                    id, description, work_front_id, area_id, responsible_id,
                    planned_date, deadline,
                    state, actions, aspect, potential, String.valueOf(inspections),
                    critical_risks_id, observations
            );
        } else {
            observable = MyApiAdapter.getApiService().updateNewReportWithImages(
                    id, description, image_base64, work_front_id, area_id, responsible_id,
                    planned_date, deadline,
                    state, actions, image_action_base64, aspect, potential, String.valueOf(inspections),
                    critical_risks_id, observations
            );
        }

        return observable;
    }

    public int getRowId() {
        return _id;
    }

    public void setRowId(int _id) {
        this._id = _id;
    }
}
