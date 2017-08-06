package com.maxfill.escom.beans.staffs;

import com.maxfill.facade.StaffFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.departments.Department;
import com.maxfill.facade.DocFacade;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.rights.Rights;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.primefaces.model.TreeNode;

/* Сервисный бин "Штатные единицы" */
 
@Named
@SessionScoped
public class StaffBean extends BaseExplBeanGroups<Staff, Department> {
    private static final long serialVersionUID = 2554984851643471496L; 

    @Inject
    private DepartmentBean ownerBean;
    @Inject
    private CompanyBean companyBean;
    
    @EJB
    private StaffFacade itemsFacade;
    @EJB
    private DocFacade docFacade;
    
    private Staff currentStaff;            
    
    @Override
    public void onInitBean() {
        super.onInitBean();
        //TODO добавить поиск штатной единицы для текущего пользователя
        currentStaff = itemsFacade.find(1);
    }

    @Override
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item); //получаем свои права 
        } 
 
        //если sataff относиться к подразделению
        if (item.getOwner() != null) {
            return ownerBean.getRightForChild(item.getOwner()); //получаем права из спец.прав подразделения
        }
        
        //если staff относиться напрямую к компании
        Staff staff = (Staff)item;
        Company company = staff.getCompany();
        if (company != null) {
            return companyBean.getRightForChild(company); //получаем права из спец.прав компании 
        } 

        return getDefaultRights(item);
    } 
    
    @Override
    public BaseExplBean getOwnerBean() {
        return ownerBean;
    }
    
    @Override
    public StaffFacade getItemFacade() {
        return itemsFacade;
    }
    
    @Override
    public void preparePasteItem(Staff pasteItem, Staff sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        detectParentOwner(pasteItem, target);
    }
    
    @Override
    public boolean addItemToGroup(Staff staff, BaseDict group){ 
        //поскольку шт.ед. может быть только в одном подразделении, то выполняем перемещение
        moveItemToGroup(group, staff); 
        return true;
    }

    public void moveItemToGroup(BaseDict group, Staff staff){
        detectParentOwner(staff, group);
        getItemFacade().edit(staff);
    }
      
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Department){
            getOwnerBean().actualizeRightItem(dropItem);
        } else {
            companyBean.actualizeRightItem(dropItem);
        }
    }
    
    @Override
    public void moveItemToGroup(BaseDict dropItem, Staff dragItem, TreeNode sourceNode) {    
        if (dropItem instanceof Department){            
            dragItem.setOwner((Department)dropItem);
        } else if (dropItem instanceof Company){
            dragItem.setOwner(null);
            dragItem.setCompany((Company)dropItem);
        }                
        getItemFacade().edit(dragItem);
    }
    
    @Override
    public void detectParentOwner(Staff staff, BaseDict target){
        if (target instanceof Company){
            staff.setCompany((Company)target);
            staff.setOwner(null); //теперь нет связи с подразделением
        } else
            if (target instanceof Department){
                staff.setOwner((Department)target);
                staff.setCompany(null);
            }
    }
    
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
    
    @Override
    public void doGetCountUsesItem(Staff staff,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByManager(staff).size());
        rezult.put("Users", userFacade.findUsersByStaff(staff).size());
    }  

    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

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

    @Override
    public SearcheModel initSearcheModel() {
        return new StaffsSearche();
    }

    
    /* GETS & SETS */
    
    public Staff getCurrentStaff() {
        return currentStaff;
    }
    public void setCurrentStaff(Staff currentStaff) {
        this.currentStaff = currentStaff;
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
