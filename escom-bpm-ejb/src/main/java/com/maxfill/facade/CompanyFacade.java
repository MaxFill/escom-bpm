package com.maxfill.facade;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyLog;
import com.maxfill.model.companies.CompanyStates;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class CompanyFacade extends BaseDictFacade<Company, Company, CompanyLog, CompanyStates> {
    protected static final Logger LOG = Logger.getLogger(CompanyFacade.class.getName());
    
    @EJB
    private UserFacade userFacade;
    
    public CompanyFacade() {
        super(Company.class, CompanyLog.class, CompanyStates.class);
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
        Map<String, Object> params = new HashMap<>();
        params.put("name", companyName);
        Company company = createItem(userFacade.getAdmin(), null, params);
        create(company);
        return company;
    }

    @Override
    public void replaceItem(Company oldItem, Company newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     
}
