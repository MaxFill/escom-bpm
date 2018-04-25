package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Вход в процесс" - точка входа
 */
public class StartElem extends BaseConnectedElement{
    private static final String STYLE_NAME = "ui-diagram-start";
    private static final long serialVersionUID = -7870980216646008269L;

    public StartElem(String caption, int x, int y, Set<AnchorElem> anchors) {
        super(caption, x, y, anchors);
    }

    @Override
    public String getStyle() {
        return STYLE_NAME;
    }
}
