package com.maxfill.model.process.types;

import com.google.gson.Gson;
import com.maxfill.model.BaseDict;
import com.maxfill.model.Results;
import com.maxfill.model.process.Process;
import com.maxfill.model.process.templates.ProcTempl;
import com.maxfill.model.task.result.Result;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.TABLE;

/**
 * Сущность "Вид процесса"
 */
@Entity
@Table(name = "processesTypes")
@DiscriminatorColumn(name = "REF_TYPE")
public class ProcessType extends BaseDict<ProcessType, ProcessType, Process, ProcessTypeLog, ProcessTypeStates> implements Results{
    private static final long serialVersionUID = 3021369175241244174L;

    @TableGenerator(
            name="ProcessesTypesIdGen",
            table="SYS_ID_GEN",
            pkColumnName="GEN_KEY",
            valueColumnName="GEN_VALUE",
            pkColumnValue="ProcessesTypes_ID", allocationSize  = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="ProcessesTypesIdGen")
    @Column(name = "Id")
    private Integer id;

    @Column(name = "AvaibleResults")
    private String avaibleResultsJSON;
    
    @Column(name = "DefaultTaskName")
    private String defaultTaskName;
    
    /* Список шаблонов */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<ProcTempl> templates = new ArrayList<>();

    /* Процессы */
    @OneToMany
    @JoinColumn(name = "owner")
    private List<Process> detailItems = new ArrayList<>();

    /* Текущее состояние */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ProcessTypeStates state;

    /* Лог */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<ProcessTypeLog> itemLogs = new ArrayList<>();

    public ProcessType() {
    }
    
    /* GETS & SETS */

    public String getDefaultTaskName() {
        return defaultTaskName;
    }
    public void setDefaultTaskName(String defaultTaskName) {
        this.defaultTaskName = defaultTaskName;
    }
    
    public List<ProcTempl> getTemplates() {
        return templates;
    }
    public void setTemplates(List<ProcTempl> templates) {
        this.templates = templates;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public List <Process> getDetailItems() {
        return detailItems;
    }
    @Override
    public void setDetailItems(List <Process> detailItems) {
        this.detailItems = detailItems;
    }

    @Override
    public ProcessTypeStates getState() {
        return state;
    }
    @Override
    public void setState(ProcessTypeStates state) {
        this.state = state;
    }

    @Override
    public void setResults(List<Result> taskResults) {
        Gson gson = new Gson();
        String json = gson.toJson(taskResults.stream().map(r->r.getId()).collect(Collectors.toList()));
        avaibleResultsJSON = json;
    }
    
    @Override
    public String getAvaibleResultsJSON() {
        return avaibleResultsJSON;
    }
    @Override
    public void setAvaibleResultsJSON(String avaibleResultsJSON) {
        this.avaibleResultsJSON = avaibleResultsJSON;
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
        if (!(object instanceof ProcessType)) {
            return false;
        }
        ProcessType other = (ProcessType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProcessType[ id=" + id + " ]";
    }
}
