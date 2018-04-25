package com.maxfill.model.process.schemes.elements;

import java.util.Set;

/**
 * Абстрактный класс элементов графической модели процесса, имеющих коннекторы
 */
public abstract class BaseConnectedElement extends BaseElement{
    protected int posX;
    protected int posY;
    protected final Set<AnchorElem> anchors;

    public BaseConnectedElement(String caption, int posX, int posY, Set<AnchorElem> anchors) {
        super(caption);
        this.posX = posX;
        this.posY = posY;
        this.anchors = anchors;
    }

    public Set<AnchorElem> getAnchors() {
        return anchors;
    }

    public int getPosX() {
        return posX;
    }
    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }
    public void setPosY(int posY) {
        this.posY = posY;
    }

    public abstract String getStyle();

}
