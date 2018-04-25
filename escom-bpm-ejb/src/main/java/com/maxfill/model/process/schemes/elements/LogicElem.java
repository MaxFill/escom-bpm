package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Логическое ветвление"
 */
public class LogicElem extends BaseConnectedElement{
    private static final String STYLE_NAME = "ui-diagram-logic";
    private static final long serialVersionUID = 1857271531554809843L;

    public LogicElem(String caption, int x, int y, Set<AnchorElem> anchors) {
        super(caption, x, y, anchors);
    }

    @Override
    public String getStyle() {
        return STYLE_NAME;
    }

}