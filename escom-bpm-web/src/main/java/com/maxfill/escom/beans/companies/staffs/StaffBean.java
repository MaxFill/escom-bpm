package com.maxfill.escom.beans.companies.staffs;

import com.maxfill.facade.StaffFacade;
import com.maxfill.model.staffs.StaffModel;
import com.maxfill.model.staffs.Staff;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.departments.Department;
import com.maxfill.facade.DocFacade;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Штатные единицы
 *
 * @author mfilatov
 */
@Named
@ViewScoped
public class StaffBean extends BaseExplBeanGroups<Staff, Department> {
    private static final long serialVersionUID = 2554984851643471496L;
    private static final String BEAN_NAME = "staffBean";    

    @EJB
    private StaffFacade itemsFacade;
    @EJB
    private DocFacade docFacade;
    
    private Staff currentStaff;

    public StaffBean() {
    }
    
    @Override
    public void onInitBean() {
        super.onInitBean();
        //TODO добавить поиск штатной единицы для текущего пользователя
        currentStaff = itemsFacade.find(1);
    }

    @Override
    protected BaseDataModel createModel() {
        return new StaffModel();
    }
      
    @Override
    protected String getBeanName() {
        return BEAN_NAME; 
    }
    
    @Override
    public StaffFacade getItemFacade() {
        return itemsFacade;
    }
    
    /**
     * Проверка возможности удаления штатной единицы.
     * @param staff
     */
    @Override
    protected void checkAllowedDeleteItem(Staff staff, Set<String> errors){
        if (!docFacade.findDocsByManager(staff).isEmpty()){
            Object[] messageParameters = new Object[]{staff.getStaffFIO()};
            String message = EscomBeanUtils.getMessageLabel("StaffUsedInDocs");
            String error = MessageFormat.format(message, messageParameters);
            errors.add(error);
        }
        super.checkAllowedDeleteItem(staff, errors);
    }
    
    /**
     * Формирует число ссылок на объект в связанных объектах 
     * @param staff
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(Staff staff,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByManager(staff).size());
        rezult.put("Users", sessionBean.getUserFacade().findUsersByStaff(staff).size());
    }
    
    public Staff getCurrentStaff() {
        return currentStaff;
    }
    public void setCurrentStaff(Staff currentStaff) {
        this.currentStaff = currentStaff;
    }    

    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

    /**
     * Формирование списка групп, в которые входит данная штатная единица
     * @param item
     * @return 
     */
    @Override
    public List<Department> getGroups(Staff item) {
        List<Department> groups = new ArrayList<>();
        if (item.getOwner() != null){
            Department department = item.getOwner();
            groups.add(department);
        }
        return groups;
    }

    @Override
    public Class<Staff> getItemClass() {
        return Staff.class;
    }

    @Override
    public Class<Department> getOwnerClass() {
        return Department.class;
    }
    
    @FacesConverter("staffConvertor")
    public static class staffConvertor implements Converter {

        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
            if (value != null && value.trim().length() > 0) {
                try {
                    StaffBean bean = EscomBeanUtils.findBean("staffBean", fc);
                    Object searcheObj = bean.getItemFacade().find(Integer.parseInt(value));
                    return searcheObj;
                } catch (NumberFormatException e) {
                    throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
                }
            } else {
                return null;
            }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if (object != null) {
                return String.valueOf(((Staff) object).getId());
            } else {
                return "";
            }
        }
    }
}
