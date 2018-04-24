package com.maxfill.model.process.schemes.elements;

import com.maxfill.model.process.schemes.Scheme;
import com.maxfill.model.process.schemes.SchemeElement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class State implements SchemeElement{
    private static final AtomicInteger NUMBER_ID = new AtomicInteger(0);
    private Scheme scheme;    
    private final Integer id;
    private String caption;

    public State(String caption) {
        this.id = NUMBER_ID.incrementAndGet();
        this.caption = caption;
    }
    
    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getCaption() {
        return caption;
    }
    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public Scheme getScheme() {
        return scheme;
    }
    @Override
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }
}