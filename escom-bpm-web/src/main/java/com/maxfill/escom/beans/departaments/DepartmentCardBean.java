package com.maxfill.escom.beans.departaments;

import com.maxfill.model.departments.Department;
import com.maxfill.facade.treelike.DepartmentFacade;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.model.companies.Company;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.states.State;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import javax.inject.Inject;

/* Контролер формы "Карточка подразделения"  */
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
    public DepartmentFacade getFacade() {
        return itemsFacade;
    }

    /**
     * Формирует наименование компании для отображения на карточке подразделения
     * @return
     */
    public String getCompanyName(){
        if (getEditedItem() == null) return "";
        Company company = itemsFacade.findCompany(getEditedItem());
        return company.getName() + " Id=" + company.getId();
    }         
    
    public void makeCode(){
        departmentBean.makeCode(getEditedItem());
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return departmentBean;
    }

}
