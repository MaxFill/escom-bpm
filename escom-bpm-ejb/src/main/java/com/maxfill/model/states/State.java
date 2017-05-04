package com.maxfill.model.states;

import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.rights.Right;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *  Состояния 
 * @author mfilatov
 */
@Entity
@Table(name = "states")
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
@DiscriminatorColumn(name="REF_TYPE")
public class State extends BaseDict<State, State, State, StateLog>{    
    private static final long serialVersionUID = 311429207470166273L;       

    @TableGenerator(
        name="stateIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="STATE_ID", allocationSize = 1)
    
    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=TABLE, generator="stateIdGen")
    @Column(name = "ID")
    @XmlElement(name = "ID")
    private Integer id;

    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "state")   
    private List<Doc> docsList;
    
    @XmlTransient
    @OneToMany(mappedBy = "state")
    private List<Right> rightList; 

    @XmlTransient
    @ManyToMany(mappedBy = "statesList")
    private List<Metadates> metadatesList;
      
    @XmlTransient
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<StateLog> itemLogs = new ArrayList<>();
    
    public State() {
    }

    @Override
    public List<StateLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<StateLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    public List<Right> getRightList() {
        return rightList;
    }
    public void setRightList(List<Right> rightList) {
        this.rightList = rightList;
    }
    
    public List<Doc> getDocsList() {
        return docsList;
    }
    public void setDocsList(List<Doc> docsList) {
        this.docsList = docsList;
    }
    
    public List<Metadates> getMetadatesList() {
        return metadatesList;
    }
    public void setMetadatesList(List<Metadates> metadatesList) {
        this.metadatesList = metadatesList;
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
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof State)) {
            return false;
        }
        State other = (State) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.model.docs.DocsState[ id=" + id + " ]";
    }
    
}
