package com.maxfill.model.process.schemes.elements;

import com.maxfill.utils.EscomUtils;

import java.io.Serializable;

/**
 * Абстрактный класс элементов графической модели процесса
 */
public abstract class WorkflowElement implements Serializable{
    protected String uid;
    protected String caption;

    public WorkflowElement(String caption) {
        this.caption = caption;
        uid = EscomUtils.generateGUID();
    }

    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        WorkflowElement that = (WorkflowElement) o;

        return uid.equals(that.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    public abstract String getStyle();
}
