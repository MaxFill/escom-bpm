package com.maxfill.model.process.schemes.elements;

import com.maxfill.model.process.schemes.Scheme;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Абстрактный класс элементов графической модели процесса
 */
public abstract class BaseElement implements Serializable{
    protected static final AtomicInteger NUMBER_ID = new AtomicInteger(0);
    protected Scheme scheme;
    protected final Integer id;
    protected String caption;

    public BaseElement(String caption) {
        this.id = NUMBER_ID.incrementAndGet();
        this.caption = caption;
    }

    public Integer getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Scheme getScheme() {
        return scheme;
    }
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }

}
