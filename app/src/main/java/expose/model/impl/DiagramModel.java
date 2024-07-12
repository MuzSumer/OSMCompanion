/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.impl;


import expose.model.meta.UniversalModel;

public class DiagramModel implements UniversalModel {


    private String id = "";
    public String getId() { return id; }
    public void setId(String value) { id = value; }


    private String date = "";
    public String getDate() { return date; }
    public void setDate(String value) { date = value; }


    /* --------------------------------windvolt-------------------------------- */

    private String type = "0";
    public String getType() { return type; }
    public void setType(String value) { type = value; }

    private String state = "0";
    public String getState() { return state; }
    public void setState(String value) { state = value; }


    /* --------------------------------windvolt-------------------------------- */

    private String title = "'title'";
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }

    private String subject = "'subject'";
    public String getSubject() { return subject; }
    public void setSubject(String value) { subject = value; }


    /* --------------------------------windvolt-------------------------------- */

    private String symbol = "";
    public String getSymbol() { return symbol; }
    public void setSymbol(String value) { symbol = value; }


    /* --------------------------------windvolt-------------------------------- */

    private String content = "";
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }


    private String targets = "";
    public String getTargets() { return targets; }
    public void setTargets(String value) { targets = value; }

    private String tags = "";
    public String getTags() { return tags; }
    public void setTags(String value) { tags = value; }

    private String specs = "";
    public String getSpecs() { return specs; }
    public void setSpecs(String value) { specs = value; }


    private String location = "";
    public String getLocation() { return location; }
    public void setLocation(String value) { location = value; }

    private String coordinates = "";
    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String value) { coordinates = value; }
    /* --------------------------------windvolt-------------------------------- */

    public String getField(String name) {

        switch (name) {
            case UniversalModel.N_ID:
                return getId();

            case UniversalModel.N_TYPE:
                return getType();

            case UniversalModel.N_DATE:
                return getDate();

            case UniversalModel.N_STATE:
                return getState();

            case UniversalModel.N_SYMBOL:
                return getSymbol();

            case UniversalModel.N_TITLE:
                return getTitle();

            case UniversalModel.N_SUBJECT:
                return getSubject();

            case UniversalModel.N_CONTENT:
                return getContent();

            case UniversalModel.N_TARGETS:
                return getTargets();

            case UniversalModel.N_TAGS:
                return getTags();

            case UniversalModel.N_SPECS:
                return getSpecs();

            case UniversalModel.N_LOCATION:
                return getLocation();

            case UniversalModel.N_COORDINATES:
                return getCoordinates();

        }

        return "";
    }

    public void setField(String name, String value) {

        switch (name) {
            case UniversalModel.N_ID:
                setId(value);
                break;

            case UniversalModel.N_TYPE:
                setType(value);
                break;

            case UniversalModel.N_DATE:
                setDate(value);
                break;

            case UniversalModel.N_STATE:
                setState(value);
                break;

            case UniversalModel.N_SYMBOL:
                setSymbol(value);
                break;

            case UniversalModel.N_TITLE:
                setTitle(value);
                break;

            case UniversalModel.N_SUBJECT:
                setSubject(value);
                break;

            case UniversalModel.N_CONTENT:
                setContent(value);
                break;

            case UniversalModel.N_TARGETS:
                setTargets(value);
                break;

            case UniversalModel.N_TAGS:
                setTags(value);
                break;

            case UniversalModel.N_SPECS:
                setSpecs(value);
                break;

            case UniversalModel.N_LOCATION:
                setLocation(value);
                break;

            case UniversalModel.N_COORDINATES:
                setCoordinates(value);
                break;

        }
    }


}