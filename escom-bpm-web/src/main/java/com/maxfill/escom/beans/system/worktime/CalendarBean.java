package com.maxfill.escom.beans.system.worktime;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.services.worktime.WorkTimeCalendar;
import com.maxfill.services.worktime.WorkTimeFacade;
import com.maxfill.services.worktime.WorkTimeService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;

/**
 * Контролер формы "Календарь рабочего времени"
 */
@Named
@ViewScoped
public class CalendarBean extends BaseViewBean {
    private static final long serialVersionUID = -2515586022679502172L;

    @EJB
    private WorkTimeFacade workTimeFacade; 
    @EJB
    private WorkTimeService workTimeService;
    @EJB
    private CompanyFacade companyFacade;
    
    private WorkTimeCalendar selected;
    
    private CalendarDay calendarDay;
    private Company company;
    
    private final Calendar current = Calendar.getInstance();
    
    private Date dtStart;
    private Date dtEnd;
    
    private final LazyScheduleModel eventModel = new LazyScheduleModel() {
        private static final long serialVersionUID = 1297279688835547588L;
            @Override
            public void loadEvents(Date start, Date end) {
                makeStartEndDates();
                Calendar calStart = new GregorianCalendar();
                calStart.setTime(dtStart);
                System.out.println(" Start Date: " + calStart.get(Calendar.YEAR) + "-" +
                                   calStart.get(Calendar.MONTH) + "-" +
                                   calStart.get(Calendar.DAY_OF_MONTH));
                Calendar calEnd = new GregorianCalendar();
                calEnd.setTime(dtEnd);
                System.out.println(" End Date: " + calEnd.get(Calendar.YEAR) + "-" +
                                   calEnd.get(Calendar.MONTH) + "-" +
                                   calEnd.get(Calendar.DAY_OF_MONTH));
                prepareEvents();
	    }
    };
    
    private final TimeZone utc=TimeZone.getTimeZone("UTC");
    private final String tzname = utc.getID();
    
    @Override
    protected void initBean(){        
        company = companyFacade.findAll().get(0);                
    }
  
    private void prepareEvents(){
        int days = current.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar calendar = Calendar.getInstance();
        while(days > 0){            
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), days, 0, 0, 0);
            Date date = calendar.getTime();
            selected = workTimeService.getWorkTimeDate(date, null, company);            
            eventModel.addEvent(new CalendarDay(selected));
            days--;
        }
    }
    
    private void makeStartEndDates(){
        Calendar calendar = (Calendar)current.clone();        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        dtStart = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        dtEnd = calendar.getTime();
    }
    
    public void onChangeMonth(String type) {
        switch(type){
            case "next":{
                current.add(Calendar.MONTH, 1);
                break;
            }
            case "previous":{
                current.add(Calendar.MONTH, -1);
                break;
            }
        }
        modelRefresh();
    }
    
    public String getMounthName(){        
        StringBuilder sb = new StringBuilder();
        sb.append(new SimpleDateFormat("MMMM").format(current.getTime()));
        sb.append(", ");
        sb.append(new SimpleDateFormat("YYYY").format(current.getTime()));        
        return sb.toString();
    }
    
    @Override
    public String getFormHeader() {
        return getLabelFromBundle("CalendarWorkTime");
    }
      
    public void onOpenDay(){        
        sessionBean.openDialogFrm(DictFrmName.FRM_WORKTIME, getParamsMap());
    }
    
    /**
     * Обработка события после закрытия карточки настроек дня
     * @param event
     */
    public void onDlgClose(SelectEvent event){
        if (event.getObject() == null) return;        
        String action = (String) event.getObject();

        switch (action){
            case SysParams.EXIT_NEED_UPDATE:{
                workTimeService.update(selected);
                modelRefresh();
                break;
            }
            case SysParams.EXIT_NOTHING_TODO:{
                break;
            }
        }
    }    
    
    public void onEventSelect(SelectEvent selectEvent) {
        calendarDay = (CalendarDay) selectEvent.getObject();
        selected = calendarDay.getWtc();
        PrimeFaces.current().executeScript("document.getElementById('mainFRM:btnOpen').click();");
    }     
    
    public void modelRefresh(){
        eventModel.clear();
        //PrimeFaces.current().ajax().update("mainFRM");
    }
    
    @Override
    public String getFormName() {
        return DictFrmName.FRM_SCHEDULER;
    }
    
    /* GETS & SETS */

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public String getClientTimeZone(){
        return tzname;
    }
    
    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public WorkTimeCalendar getSelected() {
        return selected;
    }
    public void setSelected(WorkTimeCalendar selected) {
        this.selected = selected;
    }
       
}