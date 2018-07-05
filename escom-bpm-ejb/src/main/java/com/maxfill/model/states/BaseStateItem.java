package com.maxfill.model.states;

import java.io.Serializable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@MappedSuperclass
public abstract class BaseStateItem implements Serializable{
    private static final long serialVersionUID = -8950754819239886861L;
    
    @Transient
    @XmlTransient
    private Integer id;
    
    @XmlTransient
    @JoinColumn(name = "CurrentState", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private State currentState;
    
    @XmlTransient
    @JoinColumn(name = "PreviousState", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private State previousState;

    public BaseStateItem() {
    }
    
    public BaseStateItem(State currentState, State previousState) {
        this.currentState = currentState;
        this.previousState = previousState;
    }

    /* GETS & SETS */
    
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
           
    public State getCurrentState() {
        return currentState;
    }
    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public State getPreviousState() {
        return previousState;
    }
    public void setPreviousState(State previousState) {
        this.previousState = previousState;
    }    
    
}
