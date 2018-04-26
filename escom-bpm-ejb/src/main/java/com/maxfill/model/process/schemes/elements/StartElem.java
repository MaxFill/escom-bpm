package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Вход в процесс" - точка входа
 */
public class StartElem extends WorkflowConnectedElement{
    private static final String STYLE_NAME = "ui-diagram-start";
    private static final long serialVersionUID = -7870980216646008269L;

    public StartElem(String caption, int x, int y) {
        super(caption, x, y);
    }

    @Override
    public String getStyle() {
        return STYLE_NAME;
    }

    @Override
    public String toString() {
        return "StartElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
