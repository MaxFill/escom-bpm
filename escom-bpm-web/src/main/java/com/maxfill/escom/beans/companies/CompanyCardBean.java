package com.maxfill.escom.beans.companies;

import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.escom.beans.BaseCardTree;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.model.states.State;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
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
        beginTime = new Date(time);
        beginTime = convertHourToUTCTimeZone(beginTime);
    }
    
    public Date convertHourToUTCTimeZone(Date inputDate)  {        
        Date result = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inputDate);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            String dateString = ""+((hours>9)?""+hours:"0"+hours)+":"+((hours>9)?""+minutes:"0"+minutes)+"";
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));        
            result = sdf.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(CompanyCardBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
     /**
      * convert a date with hour format (HH:mm) from UTC time zone to local time zone
      */
     public Date convertHourFromUTCToLocalTimeZone(Date inputDate) throws ParseException {
      if(inputDate == null){
       return null;
      }
      Date localFromGmt = new Date(inputDate.getTime() - TimeZone.getDefault().getOffset(inputDate.getTime()));

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(localFromGmt);
      int hours = calendar.get(Calendar.HOUR_OF_DAY);
      int minutes = calendar.get(Calendar.MINUTE);
      String dateString = ""+((hours>9)?""+hours:"0"+hours)+":"+((hours>9)?""+minutes:"0"+minutes)+"";
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

      return sdf.parse(dateString);
     }
 
    @Override
    protected void onBeforeSaveItem(Company company){
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