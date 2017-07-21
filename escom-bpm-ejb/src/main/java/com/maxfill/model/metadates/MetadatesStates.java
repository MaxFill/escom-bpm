package com.maxfill.model.metadates;

import com.maxfill.model.states.State;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "metadatesStates")
public class MetadatesStates implements Serializable{
    private static final long serialVersionUID = -1927983121344122543L;    
       
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    
    @JoinColumn(name = "MetadateObj", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    @NotNull
    private Metadates metadateObj;
    
    @JoinColumn(name = "StateSource", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    @NotNull
    private State stateSource;
    
    @JoinColumn(name = "StateTarget", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    @NotNull
    private State stateTarget;
    
    @Column(name = "MoveType")
    private Integer moveType;

    public MetadatesStates() {
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Metadates getMetadateObj() {
        return metadateObj;
    }
    public void setMetadateObj(Metadates metadateObj) {
        this.metadateObj = metadateObj;
    }

    public State getStateSource() {
        return stateSource;
    }
    public void setStateSource(State stateSource) {
        this.stateSource = stateSource;
    }

    public State getStateTarget() {
        return stateTarget;
    }
    public void setStateTarget(State stateTarget) {
        this.stateTarget = stateTarget;
    }

    public Integer getMoveType() {
        return moveType;
    }
    public void setMoveType(Integer moveType) {
        this.moveType = moveType;
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
        if (!(object instanceof MetadatesStates)) {
            return false;
        }
        MetadatesStates other = (MetadatesStates) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "MetadatesStates[ id=" + id + " ]";
    }    
}
