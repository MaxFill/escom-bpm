package com.maxfill.escom.beans.companies;

import com.maxfill.model.companies.Company;
import com.maxfill.facade.treelike.CompanyFacade;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.model.states.State;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/* Бин карточки "Компания"  */
@Named
@ViewScoped
public class CompanyCardBean extends BaseCardTree<Company> {
    private static final long serialVersionUID = -4023333706435214537L;    
    
    @Inject
    private CompanyBean companyBean;
    @Inject
    private DepartmentBean departmentBean;
    
    @EJB
    private CompanyFacade itemsFacade;

    @Override
    public List<State> getStateForChild(){
        return departmentBean.getMetadatesObj().getStatesList();
    }
    
    @Override
    public CompanyFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return companyBean;
    }
}