package com.maxfill.model.process.schemes.templates;

import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Справочник шаблонов схем согласования
 * @author maksim
 */
@Entity
@Table(name = "scheme_templates")
@DiscriminatorColumn(name = "REF_TYPE")
public class SchemeTemplates extends BaseDict<SchemeTemplates, SchemeTemplates, SchemeTemplates, SchemeTemplatesLog, SchemeTemplatesStates>{    
    private static final long serialVersionUID = 221516839841703825L;
    
    @TableGenerator(
            name = "SchemeTemplatesIdGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "SCHEMETEMPL_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "SchemeTemplatesIdGen")
    @Column(name = "Id")
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<SchemeTemplatesLog> itemLogs = new ArrayList<>();

    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private SchemeTemplatesStates state;

    public SchemeTemplates() {
    }
    
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
    public List<SchemeTemplatesLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<SchemeTemplatesLog> itemLogs) {
        this.itemLogs = itemLogs;
    }

    @Override
    public SchemeTemplatesStates getState() {
        return state;
    }
    @Override
    public void setState(SchemeTemplatesStates state) {
        this.state = state;
    }

    /* *** *** */
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchemeTemplates other = (SchemeTemplates) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SchemeTemplates{" + "id=" + id + '}';
    }        
    
}
