package com.maxfill.escom.beans.staffs;

import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.StaffFacade;
import com.maxfill.model.departments.Department;
import com.maxfill.model.posts.Post;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.users.User;
import com.maxfill.utils.DateUtils;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/* Контролер формы "Штатная единица" */
@Named
@ViewScoped
public class StaffCardBean extends BaseCardBeanGroups <Staff, Department>{
    private static final long serialVersionUID = -977912654006193660L;

    @EJB
    private StaffFacade itemFacade;
    @Inject
    private StaffBean staffBean;
    
    private Date beginTime;
    private Date endTime;
    private Company company;
    
    @Override
    public void doPrepareOpen(Staff staff){
        if (staff.isInheritsWorkTime()){
            company = staffFacade.findCompanyForStaff(staff);
            beginTime = new Date (company.getBeginTime());
            Integer time = company.getBeginTime();
            if (time != null){
                beginTime = new Date(time);
                beginTime = DateUtils.convertHourFromUTCToLocalTimeZone(beginTime); 
                staff.setWorkTime(company.getWorkTime());
                staff.setBeginTime(time);
            }
        } else {
            Integer time = staff.getBeginTime();
            if (time != null){
                beginTime = new Date(time);
                beginTime = DateUtils.convertHourFromUTCToLocalTimeZone(beginTime);                
            }    
        }        
    }
    
    @Override
    protected void onBeforeSaveItem(Staff staff){
        if (!staff.isInheritsWorkTime()){
            beginTime = DateUtils.convertHourToUTCTimeZone(beginTime);
            Long time = beginTime.getTime();
            staff.setBeginTime(time.intValue());
        }
        super.onBeforeSaveItem(staff);
    }
    
    @Override
    public StaffFacade getFacade() {
        return itemFacade;
    }

    @Override
    protected void onAfterSaveItem(Staff item){
        User user = getEditedItem().getEmployee();
        if (user == null) return;
        if (user.getStaff() == null){
            user.setStaff(getEditedItem());
        }
        userFacade.edit(user);        
    }
     
    /* Печать карточки объекта */
    @Override
    protected void doPreViewItemCard(ArrayList <Object> dataReport, Map <String, Object> parameters, String reportName) {
        super.doPreViewItemCard(dataReport, parameters, DictPrintTempl.REPORT_STAFF_CARD);
    }

    /* Обработка события на выбор сотрудника   */
    public void onEmployeeSelected(SelectEvent event) {
        if (event.getObject() instanceof String) return;
        List <User> items = (List <User>) event.getObject();
        if(items.isEmpty()) return;
        User user = items.get(0);
        checkUserStaff(user);        
    }
    public void onEmployeeSelected(ValueChangeEvent event) {
        User user = (User) event.getNewValue();
        checkUserStaff(user);        
    }

    /**
     * Проверка пользователя на заполненность ссылки на Staff. Если ссылка не заполнена, то она заполняется
     * @param user
     * @param staff 
     */
    private void checkUserStaff(User user){
        getEditedItem().setEmployee(user);
        makeName();
    }
    
    /* Событие изменение на форме поля выбора должности  */
    public void onPostSelected(SelectEvent event) {
        if (event.getObject() instanceof String) return;
        List <Post> items = (List <Post>) event.getObject();
        if(items.isEmpty()) return;
        Post item = items.get(0);        
        if (item != null) {
            getEditedItem().setPost(item);
            onItemChange();
        }
        makeName();
    }
    public void onPostSelected(ValueChangeEvent event) {
        Post post = (Post) event.getNewValue();
        getEditedItem().setPost(post);
        makeName();
    }

    /* Формирование наименования шт. единицы */
    public void makeName() {        
        staffBean.makeName(getEditedItem());        
        onItemChange(); 
        PrimeFaces.current().ajax().update("mainFRM:mainTabView:visibleName");
    }

    public void onInheritsWTchange(){
        Staff staff = getEditedItem();
        if (!staff.isInheritsWorkTime()){            
            staff.setWorkTime(company.getWorkTime());
            staff.setBeginTime(company.getBeginTime());
        } 
    }    
    
    @Override
    public List <Department> getGroups(Staff item) {
        List <Department> groups = new ArrayList <>();
        if(item.getOwner() != null) {
            Department department = item.getOwner();
            groups.add(department);
        }
        return groups;
    }

    @Override
    public Staff getEditedItem() {
        return super.getEditedItem(); //To change body of generated methods, choose Tools | Templates.
    }

    public Date getBeginTime() {
        return beginTime;
    }
    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {   
        Staff staff = getEditedItem();
        if (staff.isInheritsWorkTime()){
            endTime = DateUtils.addHour(beginTime, company.getWorkTime());            
        } else {            
            Integer worktime = staff.getWorkTime();
            endTime = DateUtils.addHour(beginTime, worktime);            
        }         
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
}