package com.maxfill.escom.beans.staffs;

import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.staffs.StaffFacade;
import com.maxfill.model.departments.Department;
import com.maxfill.model.posts.Post;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.users.User;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
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
    private void makeName() {        
        staffBean.makeName(getEditedItem());        
        onItemChange();         
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

}