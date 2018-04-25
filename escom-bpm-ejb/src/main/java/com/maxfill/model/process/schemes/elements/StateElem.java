package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Состояние"
 */
public class StateElem extends BaseConnectedElement{
    private static final long serialVersionUID = 8873088059837269820L;

    public StateElem(String caption, int x, int y, Set<AnchorElem> anchors) {
        super(caption, x, y, anchors);
    }

    @Override
    public String getStyle() {
        return "ui-diagram-" + typeName + "-state";
    }


}