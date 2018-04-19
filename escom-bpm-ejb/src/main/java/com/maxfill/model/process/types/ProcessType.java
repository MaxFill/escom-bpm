package com.maxfill.model.process.types;

import com.maxfill.model.BaseDict;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.TABLE;

@Entity
@Table(name = "processesTypes")
@DiscriminatorColumn(name = "REF_TYPE")
public class ProcessType extends BaseDict<ProcessType, ProcessType, ProcessType, ProcessTypeLog, ProcessTypeStates>{
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

    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ProcessTypeStates state;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<ProcessTypeLog> itemLogs = new ArrayList<>();

    /* GETS & SETS */

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
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
    public List <ProcessTypeLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List <ProcessTypeLog> itemLogs) {
        this.itemLogs = itemLogs;
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
