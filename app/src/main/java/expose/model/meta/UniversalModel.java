/*
    This file is part of windvolt.

    created 2020 by Max Sumer
*/
package expose.model.meta;

public interface UniversalModel {

    final String N_ID = "id";
    final String N_TYPE = "type";
    final String N_DATE = "date";
    final String N_STATE = "state";
    final String N_SYMBOL = "symbol";
    final String N_TITLE = "title";
    final String N_SUBJECT = "subject";
    final String N_CONTENT = "content";
    final String N_TARGETS = "targets";
    final String N_TAGS = "tags";
    final String N_SPECS = "specs";
    final String N_LOCATION = "location";
    final String N_COORDINATES = "coordinates";

    String getId();
    void setId(String value);

    String getType();
    void setType(String value);

    String getDate();
    void setDate(String value);

    String getState();
    void setState(String value);

    String getSymbol();
    void setSymbol(String value);

    String getTitle();
    void setTitle(String value);

    String getSubject();
    void setSubject(String value);

    String getContent();
    void setContent(String value);

    String getTargets();
    void setTargets(String value);

    String getTags();
    void setTags(String value);

    String getSpecs();
    void setSpecs(String value);

    String getLocation();
    void setLocation(String value);

    String getCoordinates();
    void setCoordinates(String value);


    String getField(String name);
    void setField(String name, String value);
}
