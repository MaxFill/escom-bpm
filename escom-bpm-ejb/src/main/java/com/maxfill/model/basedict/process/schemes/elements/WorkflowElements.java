package com.maxfill.model.basedict.process.schemes.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс списка элементов графической модели процесса
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkflowElements implements Serializable{
    private static final long serialVersionUID = 5898399111315803093L;

    @XmlElement(name = "start")
    private StartElem startElem ;
        
    @XmlElement(name = "tasks")
    private Map<String, TaskElem> tasks = new HashMap <>();
    
    @XmlElement(name = "connectors")
    private List<ConnectorElem> connectors = new ArrayList <>();

    @XmlElement(name = "conditions")
    private Map<String, ConditionElem> conditions = new HashMap <>();

    @XmlElement(name = "enters")
    private Map<String, EnterElem> enters = new HashMap <>();

    @XmlElement(name = "exits")
    private Map<String, ExitElem> exits = new HashMap <>();

    @XmlElement(name = "logics")
    private Map<String, LogicElem> logics = new HashMap <>();

    @XmlElement(name = "states")
    private Map<String, StatusElem> states = new HashMap <>();
    
    @XmlElement(name = "timers")
    private Map<String, TimerElem> timers = new HashMap <>();
    
    @XmlElement(name = "messages")
    private Map<String, MessageElem> messages = new HashMap <>();
     
    @XmlElement(name = "procedures")
    private Map<String, ProcedureElem> procedures = new HashMap <>();
    
    @XmlElement(name = "subprocesses")
    private Map<String, SubProcessElem> subprocesses = new HashMap <>();
    
    @XmlElement(name = "loops")
    private Map<String, LoopElem> loops = new HashMap <>();
    
    public WorkflowElements() {
    }
    
    /* GETS & SETS */

    public Map<String, LoopElem> getLoops() {
        return loops;
    }
    public void setLoops(Map<String, LoopElem> loops) {
        this.loops = loops;
    }
    
    public Map<String, MessageElem> getMessages() {
        return messages;
    }
    public void setMessages(Map<String, MessageElem> messages) {
        this.messages = messages;
    }

    public Map<String, ProcedureElem> getProcedures() {
        return procedures;
    }
    public void setProcedures(Map<String, ProcedureElem> procedures) {
        this.procedures = procedures;
    }    

    public Map<String, SubProcessElem> getSubprocesses() {
        return subprocesses;
    }
    public void setSubprocesses(Map<String, SubProcessElem> subprocesses) {
        this.subprocesses = subprocesses;
    }
    
    public StartElem getStartElem() {
        return startElem;
    }
    public void setStartElem(StartElem startElem) {
        this.startElem = startElem;
    }

    public Map<String, TimerElem> getTimers() {
        return timers;
    }
    public void setTimers(Map<String, TimerElem> timers) {
        this.timers = timers;
    }
    
    public Map<String, TaskElem> getTasks() {
        return tasks;
    }
    public void setTasks(Map<String, TaskElem> tasks) {
        this.tasks = tasks;
    }    
    
    public List <ConnectorElem> getConnectors() {
        return connectors;
    }
    public void setConnectors(List <ConnectorElem> connectors) {
        this.connectors = connectors;
    }

    public Map<String, ConditionElem> getConditions() {
        return conditions;
    }
    public void setConditions(Map<String, ConditionElem> conditions) {
        this.conditions = conditions;
    }

    public Map<String, EnterElem> getEnters() {
        return enters;
    }
    public void setEnters(Map<String, EnterElem> enters) {
        this.enters = enters;
    }

    public Map<String, ExitElem> getExits() {
        return exits;
    }
    public void setExits(Map<String, ExitElem> exits) {
        this.exits = exits;
    }

    public Map<String, LogicElem> getLogics() {
        return logics;
    }
    public void setLogics(Map<String, LogicElem> logics) {
        this.logics = logics;
    }

    public Map<String, StatusElem> getStates() {
        return states;
    }
    public void setStates(Map<String, StatusElem> states) {
        this.states = states;
    }

}
