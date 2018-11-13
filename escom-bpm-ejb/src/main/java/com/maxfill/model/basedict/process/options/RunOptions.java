package com.maxfill.model.basedict.process.options;

import com.maxfill.model.Dict;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * Справочник возможных опций запуска процессов
 * @author maksim
 */
@Entity
@Table(name = "runOptions")
public class RunOptions implements Serializable, Dict{
    private static final long serialVersionUID = 8670664769545051294L;
    
    @TableGenerator(
        name = "runOptionsIdGen",
        table = "SYS_ID_GEN",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "RunOptions_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "runOptionsIdGen")
    private Integer id;
    
    @Column(name = "Name")
    private String name;
    
    @Column(name = "BundleName")
    private String bundleName;

    @Column(name = "Icon")
    private String iconName = "ui-icon-play";
    
    public RunOptions() {
    }
    
    public RunOptions(String name, String bundleName) {
        this.name = name;
        this.bundleName = bundleName;
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

    public String getIconName() {
        return iconName;
    }
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    } 

    public String getBundleName() {
        return bundleName;
    }
    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }
        
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
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
        final RunOptions other = (RunOptions) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "RunOptions{" + "id=" + id + ", bundleName=" + bundleName + '}';
    }
    
}
