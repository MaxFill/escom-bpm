package com.maxfill.model.process.schemes.elements;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

/**
 * Абстрактный класс элементов графической модели процесса, имеющих коннекторы
 */
@XmlTransient
public abstract class WorkflowConnectedElement extends WorkflowElement{

    @XmlElement(name = "posY")
    protected int posX;

    @XmlElement(name = "posY")
    protected int posY;

    @XmlElement(name = "anchors")
    protected Set<AnchorElem> anchors = new HashSet<>();

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
