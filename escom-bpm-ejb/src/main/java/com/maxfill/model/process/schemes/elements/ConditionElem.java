package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Условие"
 */
public class ConditionElem extends WorkflowConnectedElement{
    private static final String STYLE_NAME = "ui-diagram-condition";
    private static final long serialVersionUID = 7842115174869991399L;

    public ConditionElem(String caption, int x, int y) {
        super(caption, x, y);
    }

    @Override
    public String getStyle() {
        return STYLE_NAME;
    }

    @Override
    public String toString() {
        return "ConditionElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
