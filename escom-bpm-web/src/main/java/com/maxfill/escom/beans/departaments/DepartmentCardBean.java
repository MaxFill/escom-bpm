package com.maxfill.escom.beans.departaments;

import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.companies.Company;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.states.State;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

/* Карточка подразделения  */
@Named
@ViewScoped
public class DepartmentCardBean extends BaseCardTree<Department>{
    private static final long serialVersionUID = 3589793075526575359L;
                
    @Inject
    private DepartmentBean departmentBean;
    @Inject
    private StaffBean staffBean;
            
    @EJB
    private DepartmentFacade itemsFacade;     

    @Override
    public List<State> getStateForChild(){
        return staffBean.getMetadatesObj().getStatesList();
    }
    
    @Override
    public DepartmentFacade getItemFacade() {
        return itemsFacade;
    }
       
    /* Формирование кода подразделения  */
    public void makeCode(){
        Department department = getEditedItem();
        Company company = findCompany(department);
        String counterName = itemsFacade.getFRM_NAME();
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(department, counterName, numeratorPattern, null, new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(company.getCode()).append(SysParams.CODE_SEPARATOR).append(number);
        department.setCode(sb.toString());
    }    
    
    /* Возвращает компанию, в которой находится подразделение */
    public Company findCompany(Department item){        
        Company company = null;
        if (item.getParent() != null){
            company = findCompany(item.getParent());
        }
        if (company == null){
            company = item.getOwner();
        }    
        return company;
    }
    
    public String getCompanyName(){
        if (getEditedItem() == null){
            return "";
        }
        Company company = findCompany(getEditedItem());
        return company.getName();
    }    
    
    /* Проверка на наличие у объекта корректных прав доступа */
    /*
    @Override
    protected void checkRightsChilds(Department item, Boolean isInheritsAccessChilds, Set<String> errors){ 
        //Подразделение может наследовать права доступа для дочерних объектов от компании
    }
    */
    @Override
    protected void afterCreateItem(Department item) {        
        makeCode();
    }
        
    @Override
    public Class<Department> getItemClass() {
        return Department.class;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return departmentBean;
    }
}
