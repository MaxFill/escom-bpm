
package com.maxfill.facade;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyLog;
import com.maxfill.model.staffs.Staff;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Maxim
 */
@Stateless
public class CompanyFacade extends BaseDictFacade<Company, Company, CompanyLog> {
    protected static final Logger LOG = Logger.getLogger(CompanyFacade.class.getName());
    
    @EJB
    private UserFacade userFacade;
    @EJB
    private StaffFacade staffFacade;
    
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
 
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(Company company){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(company.getDetailItems());
        List<Staff> staffs = staffFacade.findStaffByCompany(company, null);
        dependency.add(staffs);
        return dependency;
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
        Company company = createItem(null, userFacade.getAdmin());
        company.setName(companyName);
        create(company);        
        LOG.log(Level.INFO, "Create company = {0}", companyName);
        return company;
    }

    @Override
    public Map<String, Integer> replaceItem(Company oldItem, Company newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

     
}
