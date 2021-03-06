package com.maxfill.model.basedict.partner;

import com.maxfill.model.basedict.partner.contacts.PartnersContacts;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.partnerGroups.PartnerGroups;
import com.maxfill.model.basedict.partnerTypes.PartnerTypes;
import com.maxfill.dictionary.SysParams;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;

/* Контрагенты */
@Entity
@Table(name = "partners")
@DiscriminatorColumn(name="REF_TYPE")
public class Partner extends BaseDict<PartnerGroups, Partner, Partner, PartnersLog, PartnerStates>{     
    private static final long serialVersionUID = 9082567804115998647L;
        
    @TableGenerator(
        name="PartnerIdGen", 
        table="SYS_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="PARTNER_ID", allocationSize = 1)
          
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=TABLE, generator="PartnerIdGen")
    @Column(name = "ID")
    private Integer id;
    
    @JoinColumn(name = "Owner", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private PartnerGroups owner;
        
    @JoinColumn(name = "Type", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private PartnerTypes type;
    
    @Column(name = "Code")
    private String code;
    
    @Basic(optional = false)
    @Column(name = "FullName")
    @Size(max = 512)
    private String fullName;     

    @Column(name = "Email")
    @Size(max = 256)
    private String email;    
    
    @Column(name = "Phone")
    @Size(max = 256)
    private String phone;
    
    @Column(name = "Inn")
    @Size(max = 32)
    private String inn;
    
    @Column(name = "Kpp")
    @Size(max = 32)
    private String kpp;
    
    @Column(name = "Ogrn")
    @Size(max = 32)
    private String ogrn;
    
    @Column(name = "Okpo")
    @Size(max = 32)
    private String okpo; //для ИП 10 симв, для ЮЛ 8
   
    @Column(name = "Okato")
    @Size(max = 32)
    private String okato;
     
    @Column(name = "Address")
    @Size(max = 255)
    private String address;
        
    @JoinTable(name = "partnersInGroup", 
            joinColumns = {@JoinColumn(name = "PartnerId", referencedColumnName = "ID")}, 
            inverseJoinColumns = {@JoinColumn(name = "GroupId", referencedColumnName = "Id")})
    @ManyToMany
    private List<PartnerGroups> partnersGroupsList = new ArrayList<>();
    
    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH}, mappedBy = "partner")
    private List<Doc> docsList = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "partner")
    private List<PartnersContacts> partnersContactsList = new ArrayList<>();
    
    @Transient
    @XmlTransient
    private Integer templRegNumber; //номер регистрационный, сохраняется только на время регистрации для отката
       
    @XmlTransient
    @JoinColumn(name = "State", referencedColumnName = "Id")
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private PartnerStates state;
    
    @Transient
    @XmlTransient
    private Integer tempId;
    
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    
    public Partner() {
        tempId = COUNT.incrementAndGet();
    }
    
    /* GETS & SETS */

    public String getOgrn() {
        return ogrn;
    }
    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
        
    public Integer getTempId() {
        return tempId;
    }
    public void setTempId(Integer tempId) {
        this.tempId = tempId;
    }
    
    @Override
    public PartnerStates getState() {
        return state;
    }
    @Override
    public void setState(PartnerStates state) {
        this.state = state;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
        
    @Override
    public String getNameEndElipse(){
        StringBuilder builder = new StringBuilder();        
        builder.append(StringUtils.abbreviate(getName(), SysParams.LENGHT_NAME_ELIPSE));
        if (type != null){
           builder.append(" ").append(type.getName());
        }
        return builder.toString();
    }    
        
    public List<PartnerGroups> getPartnersGroupsList() {
        return partnersGroupsList;
    }
    public void setPartnersGroupsList(List<PartnerGroups> partnersGroupsList) {
        this.partnersGroupsList = partnersGroupsList;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Doc> getDocsList() {
        return docsList;
    }
    public void setDocsList(List<Doc> docsList) {
        this.docsList = docsList;
    }
    
    public List<PartnersContacts> getPartnersContactsList() {
        return partnersContactsList;
    }
    public void setPartnersContactsList(List<PartnersContacts> partnersContactsList) {
        this.partnersContactsList = partnersContactsList;
    }

    public Integer getTemplRegNumber() {
        return templRegNumber;
    }
    public void setTemplRegNumber(Integer templRegNumber) {
        this.templRegNumber = templRegNumber;
    }

    public String getInn() {
        return inn;
    }
    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getKpp() {
        return kpp;
    }
    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getOkpo() {
        return okpo;
    }
    public void setOkpo(String okpo) {
        this.okpo = okpo;
    }

    public String getOkato() {
        return okato;
    }
    public void setOkato(String okato) {
        this.okato = okato;
    }
         
    @Override
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public PartnerTypes getType() {
        return type;
    }
    public void setType(PartnerTypes type) {
        this.type = type;
    }    
    
    @Override
    public PartnerGroups getOwner() {
        return owner;
    }
    @Override
    public void setOwner(PartnerGroups owner) {
        this.owner = owner;
    }
    
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Partner)) {
            return false;
        }
        Partner other = (Partner) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Partner[ id=" + id + " ]";
    }   

}
