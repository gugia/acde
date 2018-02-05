package com.unitslink.acde;

/**
 * Created by gugia on 2018/2/5.
 */

public class Location {

    private String id;

    private String name = "carshow";

    private String type = "Point";

    private Double[] coordinates = {0.0, 0.0};

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Double[] coordinates) {
        this.coordinates = coordinates;
    }
}
