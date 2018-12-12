package com.maxfill.model.basedict.process;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxfill.model.Results;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.WithDatesPlans;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.process.reports.ProcReport;
import com.maxfill.model.basedict.process.schemes.Scheme;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.result.Result;
import com.maxfill.model.basedict.staff.Staff;
import org.apache.commons.lang.StringUtils;
import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.persistence.GenerationType.TABLE;

/* Класс сущности "Процессы" */
@Entity
@Table(name = "processes")
@DiscriminatorColumn(name = "REF_TYPE")
public class Process extends BaseDict<ProcessType, Process, Process, ProcessLog, ProcessStates> implements WithDatesPlans, Results{
    private static final long serialVersionUID = 8735448948976387594L;

    @TableGenerator(
        name = "processIdGen",
        table = "SYS_ID_GEN",
        pkColumnName = "GEN_KEY",
        valueColumnName = "GEN_VALUE",
        pkColumnValue = "PROCESS_ID", allocationSize = 1)

    @Id
    @Basic(optional = false)
    @Column(name = "Id")
    @GeneratedValue(strategy = TABLE, generator = "processIdGen")
    private Integer id;

    /* Владелец "Виды процесов" */
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private ProcessType owner;

    @JoinColumn(name = "Company", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Company company;

    @XmlTransient
    @JoinColumn(name = "Curator", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff curator;

    @XmlTransient
    @JoinColumn(name = "Inspector", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Staff inspector;
        
    @XmlTransient
    @JoinColumn(name = "Document", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Doc document;       
    
    @Size(max = 50)
    @Column(name = "RegNumber")
    private String regNumber;

    @Column(name = "ProcessDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date itemDate;
        
    @Column(name = "RoleJson", length = 2048)
    private String roleJson;
     
    @Column(name = "BeginDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date beginDate;

    @Column(name = "PlanExecDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date planExecDate;

    @Column(name = "FactExecDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date factExecDate;
    
    @Column(name="DeltaDeadLine")
    private Integer deltaDeadLine = 0; //срок исполения в секундах
    
    @Column(name="DeadLineType")
    private String deadLineType = "delta"; //вид установки срока исполнения    
     
    @Column(name = "Result")
    private String result; 
      
    @Column(name = "ResultIcon")
    private String resultIcon = "";
        
    /* Ссылка на визуальный элемент схемы процесса */
    @Column(name = "LinkUID")
    private String linkUID;
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent")
    private final List<Process> childItems = new ArrayList<>();
    
    /* Список документов */
    @JoinTable(name = "docsInProcesses", joinColumns = {
        @JoinColumn(name = "ProcessId", referencedColumnName = "Id")}, inverseJoinColumns = {
        @JoinColumn(name = "DocumentId", referencedColumnName = "Id")})
    @ManyToMany(fetch = FetchType.LAZY)
    private List<Doc> docs = new ArrayList<>();    
    
    /* Состояние */
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private ProcessStates state;

    /* Схема процесса */
    @XmlTransient
    @JoinColumn(name = "Scheme", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval=true)
    private Scheme scheme;

    /* Отчёты по исполнению */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval=true, mappedBy = "process")
    private Set<ProcReport> reports = new HashSet<>();
    
    @Transient
    @XmlTransient
    private String statusName;
    
    public Process() {
    }        

    @Override
    public String getCompanyName() {
        return company != null ? company.getName() : "";
    }
    
    @Override
    public String getCuratorName(){
        return curator != null ? curator.getName() : "";
    }

    @Override
    public String getIconName() {
        if (getParent() == null){
            return "process";
        }
        return "subproc";
    }
    
    @XmlTransient
    @Override
    public String getStatusName(){
       return statusName; 
    }
    @Override
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
        
    /* GETS & SETS */

    @Override
    public String getResultIcon() {
        return resultIcon;
    }
    @Override
    public void setResultIcon(String resultIcon) {
        this.resultIcon = resultIcon;
    }
    
    public Staff getInspector() {
        return inspector;
    }
    public void setInspector(Staff inspector) {
        this.inspector = inspector;
    }
        
    @Override
    public Date getItemDate() {
        return itemDate;
    }
    @Override
    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }
    
    @Override
    public String getTypeName(){
        return owner != null ? owner.getName() : "";
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
    public ProcessType getOwner() {
        return owner;
    }
    @Override
    public void setOwner(ProcessType owner) {
        this.owner = owner;
    }

    public Staff getCurator() {
        return curator;
    }
    public void setCurator(Staff curator) {
        this.curator = curator;
    }

    public Doc getDocument() {
        return document;
    }
    public void setDocument(Doc document) {
        this.document = document;
    }

    @Override
    public String getResult() {
        return result;
    }
    @Override
    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String getAvaibleResultsJSON() {
        return "";
    }
    @Override
    public void setAvaibleResultsJSON(String avaibleResultsJSON) {        
    }

    @Override
    public void setResults(List<Result> taskResults) {    
    }
    
    @Override
    public Process getParent() {
        return super.getParent();
    }
    @Override
    public void setParent(Process parent) {
        super.setParent(parent); 
    }
            
    @Override
    public ProcessStates getState() {
        return state;
    }
    @Override
    public void setState(ProcessStates state) {
        this.state = state;
    }

    @Override
    public Date getBeginDate() {
        return beginDate;
    }
    @Override
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public Date getPlanExecDate() {
        return planExecDate;
    }
    @Override
    public void setPlanExecDate(Date planExecDate) {
        this.planExecDate = planExecDate;
    }

    @Override
    public Date getFactExecDate() {
        return factExecDate;
    }
    @Override
    public void setFactExecDate(Date factExecDate) {
        this.factExecDate = factExecDate;
    }

    public Integer getDeltaDeadLine() {
        return deltaDeadLine;
    }
    public void setDeltaDeadLine(Integer deltaDeadLine) {
        this.deltaDeadLine = deltaDeadLine;
    }

    public String getDeadLineType() {
        return deadLineType;
    }
    public void setDeadLineType(String deadLineType) {
        this.deadLineType = deadLineType;
    }
    
    public Scheme getScheme() {
        return scheme;
    }
    public void setScheme(Scheme scheme) {
        this.scheme = scheme;
    }    

    public String getLinkUID() {
        return linkUID;
    }
    public void setLinkUID(String linkUID) {
        this.linkUID = linkUID;
    }
    
    public List<Doc> getDocs() {
        return docs;
    }
    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }           
        
    @Override
    public List<Process> getChildItems() {
        return childItems;
    }

    @Override
    public List<Process> getDetailItems() {
        return null;
    }
    
    @Override
    public String getRoleJson() {
        return roleJson;
    }
    @Override
    public void setRoleJson(String roleJson) {
        this.roleJson = roleJson;
    }
    
    @Override
    public Map<String, Set<Integer>> getRoles() {
        if (roles == null){
            roles = new HashMap<>();
            if (StringUtils.isBlank(getRoleJson())) return roles;
            try {
                ObjectMapper mapper = new ObjectMapper();            
                roles = mapper.readValue(roleJson, new TypeReference<HashMap<String, HashSet<Integer>>>() {});
                setRoles(roles);
            } catch (IOException ex) {
                Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return roles;
    }

    public Set<ProcReport> getReports() {
        return reports;
    }
    public void setReports(Set<ProcReport> reports) {
        this.reports = reports;
    }

    @Override
    public String getRegNumber() {
        return regNumber;
    }
    @Override
    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
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
        if (!(object instanceof Process)) {
            return false;
        }
        Process other = (Process) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Process [ id=" + id + " ] [" + getName() + "]";
    }

}