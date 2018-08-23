package com.maxfill.escom.beans.companies;

import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.model.states.State;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import com.maxfill.utils.DateUtils;
import org.omnifaces.cdi.ViewScoped;
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

    private Date beginTime;
    
    @Override
    public void doPrepareOpen(Company company){
        Integer time = company.getBeginTime();
        if (time != null){
            beginTime = new Date(time);
            beginTime = DateUtils.convertHourFromUTCToLocalTimeZone(beginTime);
        }
    }       
 
    @Override
    protected void onBeforeSaveItem(Company company){
        beginTime = DateUtils.convertHourToUTCTimeZone(beginTime);
        Long time = beginTime.getTime();
        company.setBeginTime(time.intValue());
        super.onBeforeSaveItem(company);
    }
    
    @Override
    public List<State> getStateForChild(){
        return departmentBean.getMetadatesObj().getStatesList();
    }
    
    @Override
    public CompanyFacade getFacade() {
        return itemsFacade;
    }

    @Override
    protected BaseTreeBean getTreeBean() {
        return companyBean;
    }
    
    public Date getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }
}