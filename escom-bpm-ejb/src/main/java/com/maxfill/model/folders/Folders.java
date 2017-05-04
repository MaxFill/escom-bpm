package com.maxfill.model.folders;

import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.users.User;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Папки архива
 * @author Maxim
 */
@Entity
@Table(name = "folders")
@NamedQueries({
    @NamedQuery(name = "Folders.findAll", query = "SELECT f FROM Folders f")})
@DiscriminatorColumn(name="REF_TYPE")
public class Folders extends BaseDict<Folders, Folders, Doc, FoldersLog>{
    private static final long serialVersionUID = -7531636538666889579L;

    @TableGenerator(
        name="idGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="FOLDER_ID", allocationSize = 1)
 
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    @GeneratedValue(strategy=TABLE, generator="idGen")
    private Integer id;
        
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    private List<Folders> foldersList;    
    
    @JoinColumn(name = "Moderator", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private User moderator;        
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsModeration")
    private boolean isModeration;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "IsLocked")
    private boolean isLocked = false;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<Doc> docsList = new ArrayList<>();
    
    @Size(max = 2147483647)
    @Column(name = "AccessDocs")
    private String accessDocs;    
    
    @NotNull
    @Column(name = "IsInheritsAccessDocs")
    private boolean isInheritsAccessDocs;
        
    @JoinColumn(name = "DocTypeDefault", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private DocType docTypeDefault;    
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<FoldersLog> itemLogs = new ArrayList<>();
        
    public Folders(){
    }

    @Override
    public List<FoldersLog> getItemLogs() {
        return itemLogs;
    }
    @Override
    public void setItemLogs(List<FoldersLog> itemLogs) {
        this.itemLogs = itemLogs;
    }       
    
    public String getRegNumber(){
        if (getNumber() == null){
            return "";
        }
        return getNumber().toString();
    }
    
    //@return вычисление значка папки (модерируемая-немодерируемая)
    public String getStateIcon(){
        String stateIcon = "ui-icon-folder-open";
        if(isModeration == true){
          stateIcon = "ui-icon-person";
        }
        return stateIcon;
    }       
    
    /**
     * Возвращает название для заголовка наследования прав к документам
     * @return Строка
     */
    public String getInheritsAccessDocName(){
        if (isInheritsAccessDocs){
            return "Права для документов этой папки наследуются от родительской папки";
        } else{
            return "Для документов этой папки установлены специальные права";
        }
    }
    
    public Folders(Integer id) {  
        this.id = id;
    }
               
    @Override
    public String getIconName() {
        return "folder_open";
    }
    
    public boolean getIsModeration() {return isModeration;}
    public void setIsModeration(boolean isModeration) {       this.isModeration = isModeration;    }
    
    public boolean getIsLocked() {return isLocked;}
    public void setIsLocked(boolean isLocked) {        this.isLocked = isLocked;    }
    
    public List<Doc> getDocsList() {
        return docsList;    
    }
    public void setDocsList(List<Doc> docsList) {        this.docsList = docsList;    }
    
    public User getModerator() { return moderator;    }
    public void setModerator(User moderator) {        this.moderator = moderator;    }
    
    public List<Folders> getFoldersList() {        return foldersList;    }
    public void setFoldersList(List<Folders> foldersList) {     this.foldersList = foldersList;   }
    
    public String getAccessDocs() { return accessDocs; }
    public void setAccessDocs(String accessDocs) { this.accessDocs = accessDocs; }
    
    public boolean getIsInheritsAccessDocs() {
        return isInheritsAccessDocs;
    }
    public void setIsInheritsAccessDocs(boolean isInheritsAccessDocs) {
        this.isInheritsAccessDocs = isInheritsAccessDocs;
    }

    public DocType getDocTypeDefault() {
        return docTypeDefault;
    }
    public void setDocTypeDefault(DocType docTypeDefault) {
        this.docTypeDefault = docTypeDefault;
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
        if (!(object instanceof Folders)) {
            return false;
        }
        Folders other = (Folders) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxfill.escombpm2.datamodel.Folders[ id=" + id + " ]";
    }   


}
