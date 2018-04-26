package com.maxfill.model.process.schemes.elements;

import java.util.HashSet;
import java.util.Set;

/**
 * Абстрактный класс элементов графической модели процесса, имеющих коннекторы
 */
public abstract class WorkflowConnectedElement extends WorkflowElement{
    protected int posX;
    protected int posY;
    protected Set<AnchorElem> anchors = new HashSet <>();

    public WorkflowConnectedElement(String caption, int posX, int posY) {
        super(caption);
        this.posX = posX;
        this.posY = posY;
    }

    public Set<AnchorElem> getAnchors() {
        return anchors;
    }
    public void setAnchors(Set <AnchorElem> anchors) {
        this.anchors = anchors;
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

    public Set<String> validate(){
        Set <String> errors = new HashSet<>();
        return errors;
    }

    public AnchorElem getAnchorsById(String id){
        AnchorElem anchorElem = null;
        for (AnchorElem elem : anchors){
            if (elem.getUid().equals(id)){
                anchorElem = elem;
                break;
            }
        }
        return anchorElem;
    }
}
