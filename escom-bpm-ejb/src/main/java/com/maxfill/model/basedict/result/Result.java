package com.maxfill.model.basedict.result;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.process.options.RunOptions;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Шаблоны результатов выполнения (задач)
 * @author maksim
 */
@Entity
@Table(name = "results")
@DiscriminatorColumn(name = "REF_TYPE")
public class Result extends BaseDict<Result, Result, Result, ResultLog, ResultStates> {    
    private static final long serialVersionUID = -3383003884974574336L;
    
    @TableGenerator(
            name = "IdResultGen",
            table = "SYS_ID_GEN",
            pkColumnName = "GEN_KEY",
            valueColumnName = "GEN_VALUE",
            pkColumnValue = "RESULT_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = TABLE, generator = "IdResultGen")
    @Column(name = "Id")
    private Integer id;    

    @Size(max = 1024)
    @Column(name = "ConditonRun")
    private String conditonJson;    //условия, которые должны быть выполнены перед запуском задачи
     
    @JoinColumn(name = "RunOption", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private RunOptions runOptions;  //опции, которые нужно передать в процесс при выполнении задачи
    
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ResultStates state;
    
    /* *** GETS & SETS *** */
        
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }  

    public RunOptions getRunOptions() {
        return runOptions;
    }
    public void setRunOptions(RunOptions runOptions) {
        this.runOptions = runOptions;
    }
        
    @Override
    public ResultStates getState() {
        return state;
    }
    @Override
    public void setState(ResultStates state) {
        this.state = state;
    }

    public String getConditonJson() {
        return conditonJson;
    }
    public void setConditonJson(String conditonJson) {
        this.conditonJson = conditonJson;
    }
            
    /* *** *** */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
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
        final Result other = (Result) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Result{" + "id=" + id + '}';
    }    
    
}
