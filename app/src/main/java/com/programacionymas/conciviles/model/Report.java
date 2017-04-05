package com.programacionymas.conciviles.model;

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
}
