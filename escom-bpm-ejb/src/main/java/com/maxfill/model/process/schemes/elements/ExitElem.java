package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Сущность "Элемент схемы процесса "Выход из процесса" - точка выхода
 */

public class ExitElem extends BaseConnectedElement{
    private static final String STYLE_NAME = "ui-diagram-exit";
    private static final long serialVersionUID = 8512962777531919513L;

    public ExitElem(String caption, int x, int y, Set<AnchorElem> anchors) {
        super(caption, x, y, anchors);
    }

    @Override
    public String getStyle() {
        return STYLE_NAME;
    }
}
