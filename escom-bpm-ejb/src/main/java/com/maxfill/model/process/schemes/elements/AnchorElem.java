package com.maxfill.model.process.schemes.elements;

import java.io.Serializable;

public class AnchorElem implements Serializable{
    private static final long serialVersionUID = 8128988436740920824L;

    private String position;
    private Boolean type;

    public AnchorElem(String position, Boolean type) {
        this.position = position;
        this.type = type;
    }

    public String getPosition() {
        return position;
    }

    public Boolean getType() {
        return type;
    }

    public Boolean isSource(){
        return type;
    }

    public Boolean isTarget(){
        return !type;
    }
}
