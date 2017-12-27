package com.maxfill.escom.beans.staffs;

import com.maxfill.escom.utils.EscomMsgUtils;
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
    public BaseExplBean getOwnerBean() {
        return ownerBean;
    }
    
    @Override
    public BaseExplBean getGroupBean() {
        return ownerBean;
    }
    
    @Override
    public StaffFacade getItemFacade() {
        return itemsFacade;
    }
    
    @Override
    public void preparePasteItem(Staff pasteItem, Staff sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        if (target == null){
            if (sourceItem.getCompany() != null){
                target = sourceItem.getCompany();
            } else {
                target = sourceItem.getOwner();
            }
        }
        itemsFacade.detectParentOwner(pasteItem, target);
    }
    
    @Override
    public boolean addItemToGroup(Staff staff, BaseDict group){ 
        //поскольку шт.ед. может быть только в одном подразделении, то выполняем перемещение
        moveItemToGroup(group, staff); 
        return true;
    }

    public void moveItemToGroup(BaseDict group, Staff staff){
        itemsFacade.detectParentOwner(staff, group);
        getItemFacade().edit(staff);
    }
      
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Department){
            getOwnerBean().getItemFacade().actualizeRightItem(dropItem, currentUser);
        } else {
            companyBean.getItemFacade().actualizeRightItem(dropItem, currentUser);
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
    protected void checkAllowedDeleteItem(Staff staff, Set<String> errors){
        if (!docFacade.findDocsByManager(staff).isEmpty()){
            Object[] messageParameters = new Object[]{staff.getStaffFIO()};
            String message = EscomMsgUtils.getMessageLabel("StaffUsedInDocs");
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

}
