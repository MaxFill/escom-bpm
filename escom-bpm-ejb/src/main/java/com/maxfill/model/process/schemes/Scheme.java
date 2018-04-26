package com.maxfill.model.process.schemes;

import com.maxfill.model.Dict;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.ProcessLog;
import com.maxfill.model.process.schemes.elements.*;
import com.maxfill.model.process.schemes.task.Task;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* Класс сущности "Схемы процессов" */
@Entity
@Table(name = "schemes")
public class Scheme implements Serializable, Dict{
    private static final long serialVersionUID = -2748097439732321325L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    /* Ссылка на процесс */
    @NotNull
    @JoinColumn(name = "Process", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Process process;

    /* Список поручений */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "scheme")
    private final List<Task> tasks = new ArrayList<>();

    @Transient
    private final List<WorkflowConnectedElement> elements = new ArrayList <>();

    /* Список коннекторов */
    @Transient
    private final List<ConnectorElem> connectors = new ArrayList<>();

    /* GETS & SETS */

    public List <WorkflowConnectedElement> getElements() {
        return elements;
    }


    public List <Task> getTasks() {
        return tasks;
    }

    public List <ConnectorElem> getConnectors() {
        return connectors;
    }

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Process getProcess() {
        return process;
    }
    public void setProcess(Process process) {
        this.process = process;
    }
    
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Scheme)) {
            return false;
        }
        Scheme other = (Scheme) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Scheme [ id=" + id;
    }
}
