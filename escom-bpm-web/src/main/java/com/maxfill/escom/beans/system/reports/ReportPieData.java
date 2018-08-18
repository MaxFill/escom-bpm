package com.maxfill.escom.beans.system.reports;

import java.io.Serializable;

public class ReportPieData implements Serializable{
    private static final long serialVersionUID = -7639025669321318282L;
    
    private String key;
    private Number value;

    public ReportPieData(String key, Number value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public Number getValue() {
        return value;
    }
    public void setValue(Number value) {
        this.value = value;
    }
    
    
}
