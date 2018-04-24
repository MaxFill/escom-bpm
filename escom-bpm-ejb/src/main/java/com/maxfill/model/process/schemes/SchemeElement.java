package com.maxfill.model.process.schemes;

public interface SchemeElement {
    Scheme getScheme();
    void setScheme(Scheme scheme);         
    Integer getId();
    String getCaption();
    void setCaption(String caption);
}
