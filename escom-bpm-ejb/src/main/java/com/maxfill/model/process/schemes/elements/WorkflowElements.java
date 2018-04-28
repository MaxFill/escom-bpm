package com.maxfill.model.process.schemes.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс списка элементов графической модели процесса
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowElements implements Serializable{
    private static final long serialVersionUID = 5898399111315803093L;

    @XmlElement(name = "connectors")
    private List<ConnectorElem> connectors = new ArrayList <>();

    @XmlElement(name = "conditions")
    private List<ConditionElem> conditions = new ArrayList <>();

    @XmlElement(name = "starts")
    private List<StartElem> starts = new ArrayList <>();

    @XmlElement(name = "exits")
    private List<ExitElem> exits = new ArrayList <>();

    @XmlElement(name = "logics")
    private List<LogicElem> logics = new ArrayList <>();

    @XmlElement(name = "states")
    private List<StateElem> states = new ArrayList <>();

    public WorkflowElements() {
    }

    public List <ConnectorElem> getConnectors() {
        return connectors;
    }
    public void setConnectors(List <ConnectorElem> connectors) {
        this.connectors = connectors;
    }

    public List <ConditionElem> getConditions() {
        return conditions;
    }
    public void setConditions(List <ConditionElem> conditions) {
        this.conditions = conditions;
    }

    public List <StartElem> getStarts() {
        return starts;
    }
    public void setStarts(List <StartElem> starts) {
        this.starts = starts;
    }

    public List <ExitElem> getExits() {
        return exits;
    }
    public void setExits(List <ExitElem> exits) {
        this.exits = exits;
    }

    public List <LogicElem> getLogics() {
        return logics;
    }
    public void setLogics(List <LogicElem> logics) {
        this.logics = logics;
    }

    public List <StateElem> getStates() {
        return states;
    }
    public void setStates(List <StateElem> states) {
        this.states = states;
    }
}
