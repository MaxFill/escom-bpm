package com.maxfill.escom.beans.companies;

import com.maxfill.model.companies.Company;
import com.maxfill.facade.CompanyFacade;
import com.maxfill.escom.beans.BaseCardBean;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Компании юр лица
 *
 * @author Maxim
 */
@Named
@ViewScoped
public class CompanyCardBean extends BaseCardBean<Company> {
    private static final long serialVersionUID = -4023333706435214537L;    
            
    @EJB
    private CompanyFacade itemsFacade;
    
    @Override
    public CompanyFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    protected void onAfterCreateItem(Company item){
    }        

}