package com.maxfill.model.process.schemes.elements;

import com.maxfill.dictionary.DictWorkflowElem;
import com.maxfill.utils.EscomUtils;

/**
 * Сущность "Элемент схемы процесса "Логическое ветвление"
 */
public class LogicElem extends WFConnectedElement{
    private static final long serialVersionUID = 1857271531554809843L;

    public LogicElem() {
    }

    public LogicElem(String caption, int x, int y) {
        this.caption = caption;
        this.posX = x;
        this.posY = y;
        this.uid = EscomUtils.generateGUID();
    }

    @Override
    public String getStyle() {
        return DictWorkflowElem.STYLE_LOGIC;
    }

    @Override
    public String getBundleKey() {
        return "Logic";
    }
    
    /* *** *** */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        LogicElem logicElem = (LogicElem) o;

        return uid.equals(logicElem.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public String toString() {
        return "LogicElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}