package com.maxfill.escom.beans.docs;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.utils.DateUtils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/* Расширение для поиска Документов  */
public final class DocsSearche extends SearcheModel{
    private static final long serialVersionUID = 3953544990596152030L;
    
    private String numberSearche;  

    private String dateDocPeriod;
    private Date dateDocStart;
    private Date dateDocEnd;
    
    private List<DocType> selectedDocTypes; //выбранные виды документов
    private Partner partnerSearche;
    private Company companySearche;
       
    private List<Partner> partners = null; //список контрагентов критериев поиска
    private List<DocType> docTypes = null; //список видов документов критериев поиска
    private List<Company> companies = null; //список компаний критериев поиска
    
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams){
        if (companySearche != null){
            paramEQ.put("company", companySearche);
        }
        if (StringUtils.isNotBlank(numberSearche)){
            paramLIKE.put("regNumber", numberSearche);
        }
        if (dateDocPeriod != null){
            Date[] dateArray = new Date[2];
            dateArray[0] = dateDocStart;
            dateArray[1] = dateDocEnd;
            paramDATE.put("itemDate", dateArray);
        }
        if (!selectedDocTypes.isEmpty()){
            List<Integer> docTypeIds = selectedDocTypes.stream().map(item -> item.getId()).collect(Collectors.toList());
            addParams.put("docTypes", docTypeIds);
        }
        if (partnerSearche != null){
           paramEQ.put("partner", partnerSearche); 
        }  
        
    }        
      
    public void changeDateDocument(String period){
        dateDocPeriod = period;  
        if (dateDocPeriod != null){
            dateDocStart = DateUtils.periodStartDate(dateDocPeriod, dateDocStart);
            dateDocEnd = DateUtils.periodEndDate(dateDocPeriod, dateDocEnd);
        }
    }
    
    /* GETS & SETS */

    public Company getCompanySearche() {
        return companySearche;
    }
    public void setCompanySearche(Company companySearche) {
        this.companySearche = companySearche;
    }

    public List<DocType> getSelectedDocTypes() {
        return selectedDocTypes;
    }
    public void setSelectedDocTypes(List<DocType> selectedDocTypes) {
        this.selectedDocTypes = selectedDocTypes;
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

    public String getDateDocPeriod() {
        return dateDocPeriod;
    }
    public void setDateDocPeriod(String dateDocPeriod) {
        this.dateDocPeriod = dateDocPeriod;
    }        

    public List<Partner> getPartners() {
        if (partners == null){
            partners = explBean.getPartnerBean().findAll();
        }
        return partners;
    }

    public List<DocType> getDocTypes() {
        if (docTypes == null){
            docTypes = explBean.getDocTypeBean().findAll();
        }
        return docTypes;
    }

    public List<Company> getCompanies() {
        if (companies == null){
            companies = explBean.getCompanyBean().findAll();
        }
        return companies;
    }
}
