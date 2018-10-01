
package com.maxfill.escom.beans.docs;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.partner.Partner;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/* Расширение поиска Документов  */
public class DocsSearche extends SearcheModel{
    private static final long serialVersionUID = 3953544990596152030L;
    
    private String numberSearche;  

    private Date dateDocStart;
    private Date dateDocEnd;
    private boolean dateDocSearche = false;
    private List<DocType> docTypes;
    private Partner partnerSearche;
    private Company companySearche;
    
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){
        paramEQ.put("company", companySearche);
        if (StringUtils.isNotBlank(numberSearche)){
            paramLIKE.put("regNumber", numberSearche);
        }
        if (dateDocSearche){
            Date[] dateArray = new Date[2];
            dateArray[0] = dateDocStart;
            dateArray[1] = dateDocEnd;
            paramDATE.put("itemDate", dateArray);
        }
        if (!docTypes.isEmpty()){
            paramIN.put("docType", docTypes);
        }
        if (partnerSearche != null){
           paramEQ.put("partner", partnerSearche); 
        }        
    }        
       
    /* GETS & SETS */

    public Company getCompanySearche() {
        return companySearche;
    }
    public void setCompanySearche(Company companySearche) {
        this.companySearche = companySearche;
    }
        
    public List<DocType> getDocTypes() {
        return docTypes;
    }
    public void setDocTypes(List<DocType> docTypes) {
        this.docTypes = docTypes;
    }
        
    public Date getDateDocStart() {
        return dateDocStart;
    }
    public void setDateDocStart(Date dateDocStart) {
        this.dateDocStart = dateDocStart;
    }

    public Date getDateDocEnd() {
        return dateDocEnd;
    }
    public void setDateDocEnd(Date dateDocEnd) {
        this.dateDocEnd = dateDocEnd;
    }
    
    public boolean isDateDocSearche() {
        return dateDocSearche;
    }
    public void setDateDocSearche(boolean dateDocSearche) {
        this.dateDocSearche = dateDocSearche;
    }
    
    public String getNumberSearche() {
        return numberSearche;
    }
    public void setNumberSearche(String numberSearche) {
        this.numberSearche = numberSearche;
    }

    public Partner getPartnerSearche() {
        return partnerSearche;
    }
    public void setPartnerSearche(Partner partnerSearche) {
        this.partnerSearche = partnerSearche;
    }    
    
}
