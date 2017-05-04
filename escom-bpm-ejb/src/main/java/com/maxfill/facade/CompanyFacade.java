
package com.maxfill.facade;

import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.staffs.Staff;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyLog;
import com.maxfill.model.users.User;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    @EJB
    private DepartmentFacade departmentFacade;
    
    public CompanyFacade() {
        super(Company.class, CompanyLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return Company.class.getSimpleName().toLowerCase();
    }
            
    @Override
    public void pasteItem(Company pasteItem, BaseDict target, Set<String> errors){
        doCopyPasteDetails(pasteItem,  errors);
        doCopyStaffs(pasteItem,  errors);
        doPaste(pasteItem, errors);
    }
    
    /* Копирования подчинённых объектов - подразделений */
    private void doCopyPasteDetails(Company company, Set<String> errors) {
        company.getDetailItems().stream().forEach(department -> departmentFacade.pasteItem(department, company,  errors));
    }
    
    /* Копирования подчинённых объектов - штатных единиц */
    private void doCopyStaffs(Company company, Set<String> errors) {
        List<Staff> staffs = staffFacade.findStaffByCompany(company, null);
        staffs.stream().forEach(staff -> staffFacade.pasteItem(staff, company, errors));
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_COMPANY;
    }
 
    /**
     * Ищет компанию с указанным названием, и если не найдена то создаёт новую
     * @param companyName
     * @return 
     */
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
