package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Выход из процесса" - точка выхода
 */

public class ExitElem extends WorkflowConnectedElement{
    private static final String STYLE_NAME = "ui-diagram-exit";
    private static final long serialVersionUID = 8512962777531919513L;

    public ExitElem(String caption, int x, int y) {
        super(caption, x, y);
    }

    @Override
    public String getStyle() {
        return STYLE_NAME;
    }

    @Override
    public String toString() {
        return "ExitElem{" +
                "caption='" + caption + '\'' +
                '}';
    }
}
