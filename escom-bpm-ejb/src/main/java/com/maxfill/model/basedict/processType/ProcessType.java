package com.maxfill.model.basedict.processType;

import com.google.gson.Gson;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.Results;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.procTempl.ProcTempl;
import com.maxfill.model.basedict.process.options.RunOptions;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.userGroups.UserGroups;
import com.maxfill.model.basedict.processType.roles.ProcessRole;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.TABLE;
import javax.validation.constraints.Size;

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
    
    @Basic(optional = false)
    @Column(name = "ShowReports")
    private boolean showReports = true; 
    
    @Column(name = "NameReports")
    private String nameReports;
    
    @Basic(optional = false)
    @Column(name = "InheritRunOptions")
    private boolean inheritRunOptions = true; 
    
    @Basic(optional = false)
    @Column(name = "InheritTaskOptions")
    private boolean inheritTaskOptions = true;
    
    @Column(name = "RunOptions")
    private String runOptionsJSON;
    
    @Column(name = "DefaultTaskName")
    private String defaultTaskName;        
        
    @Column(name="DeltaDeadLine")
    private Integer defaultDeltaDeadLine = 0;      //срок исполнения в секундах
    
    @Basic(optional = false)
    @Size(max=50)
    @Column(name = "Guide")
    private String guide;
     
    @Basic(optional = false)
    @Size(max=10)
    @Column(name = "Code")
    private String code;
        
    @JoinColumn(name = "RoleInProc", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private UserGroups defaultTaskRole;     //роль по умолчанию для задач процесса
        
    @JoinColumn(name = "Numerator", referencedColumnName = "Id")
    @ManyToOne
    private NumeratorPattern numerator;
    
    /* Список шаблонов */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<ProcTempl> templates = new ArrayList<>();

    /* Роли процесса */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "procType")
    private List<ProcessRole> processRoles = new ArrayList<>();
     
    /* Процессы */
    @OneToMany
    @JoinColumn(name = "owner")
    private List<Process> detailItems = new ArrayList<>();

    /* Текущее состояние */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ProcessTypeStates state;

    public ProcessType() {
    }
    
    @Override
    public void setResults(List<Result> taskResults) {
        Gson gson = new Gson();
        String json = gson.toJson(taskResults.stream().map(r->r.getId()).collect(Collectors.toList()));
        avaibleResultsJSON = json;
    }
    
    public void setRunOptions(List<RunOptions> options){
        Gson gson = new Gson();        
        setRunOptionsJSON(gson.toJson(options.stream().map(o->o.getId()).collect(Collectors.toList()), List.class));
    }
        
    /* GETS & SETS */

    public NumeratorPattern getNumerator() {
        return numerator;
    }
    public void setNumerator(NumeratorPattern numerator) {
        this.numerator = numerator;
    }

    public String getGuide() {
        return guide;
    }
    public void setGuide(String guide) {
        this.guide = guide;
    }

    public UserGroups getDefaultTaskRole() {
        return defaultTaskRole;
    }
    public void setDefaultTaskRole(UserGroups defaultTaskRole) {
        this.defaultTaskRole = defaultTaskRole;
    }    

    public List<ProcessRole> getProcessRoles() {
        return processRoles;
    }
    public void setProcessRoles(List<ProcessRole> processRoles) {
        this.processRoles = processRoles;
    }
        
    @Override
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
          
    public boolean isShowReports() {
        return showReports;
    }
    public void setShowReports(boolean showReports) {
        this.showReports = showReports;
    }

    public String getNameReports() {
        return nameReports;
    }
    public void setNameReports(String nameReports) {
        this.nameReports = nameReports;
    }
    
    public boolean isInheritRunOptions() {
        return inheritRunOptions;
    }
    public void setInheritRunOptions(boolean inheritRunOptions) {
        this.inheritRunOptions = inheritRunOptions;
    }

    public boolean isInheritTaskOptions() {
        return inheritTaskOptions;
    }
    public void setInheritTaskOptions(boolean inheritTaskOptions) {
        this.inheritTaskOptions = inheritTaskOptions;
    }
        
    public Integer getDefaultDeltaDeadLine() {
        return defaultDeltaDeadLine;
    }
    public void setDefaultDeltaDeadLine(Integer defaultDeltaDeadLine) {
        this.defaultDeltaDeadLine = defaultDeltaDeadLine;
    }
    
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

    public String getRunOptionsJSON() {
        return runOptionsJSON;
    }
    public void setRunOptionsJSON(String runOptionsJSON) {
        
        this.runOptionsJSON = runOptionsJSON;
    }     

    @Override
    public String getResult() {
        return "";
    }
    @Override
    public void setResult(String result) {        
    }    
    
    @Override
    public String getResultIcon() {
        return "";
    }
    @Override
    public void setResultIcon(String resultIcon) {   
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
