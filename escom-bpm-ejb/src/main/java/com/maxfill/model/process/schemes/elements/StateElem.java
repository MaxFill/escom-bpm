package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Состояние"
 */
public class StateElem extends WorkflowConnectedElement{
    private static final long serialVersionUID = 8873088059837269820L;

    private String styleType;

    public StateElem(String caption, int x, int y) {
        super(caption, x, y);
    }

    @Override
    public String getStyle() {
        return "ui-diagram-" + styleType + "-state";
    }

    public void setStyleType(String styleType) {
        this.styleType = styleType;
    }
    public String getStyleType() {
        return styleType;
    }

    @Override
    public String toString() {
        return "StateElem{" +
                "styleType='" + styleType + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}