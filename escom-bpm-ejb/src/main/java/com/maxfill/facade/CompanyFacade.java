
package com.maxfill.facade;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyLog;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang.StringUtils;

/* Комапнии  */
@Stateless
public class CompanyFacade extends BaseDictFacade<Company, Company, CompanyLog> {
    protected static final Logger LOG = Logger.getLogger(CompanyFacade.class.getName());
    
    @EJB
    private UserFacade userFacade;
    
    public CompanyFacade() {
        super(Company.class, CompanyLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return Company.class.getSimpleName().toLowerCase();
    }            

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_COMPANY;
    }       
    
    /* Ищет компанию с указанным названием, и если не найдена то создаёт новую  */
    public Company onGetCompanyByName(String companyName){
        if (StringUtils.isBlank(companyName)){
            return null;
        }
        for (Company company : findAll()){
            if (Objects.equals(company.getName(), companyName)){
                return company;
            }
        }
        Company company = createItem(userFacade.getAdmin());
        company.setName(companyName);
        create(company);        
        LOG.log(Level.INFO, "Create company = {0}", companyName);
        return company;
    }

    @Override
    public void replaceItem(Company oldItem, Company newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     
}
