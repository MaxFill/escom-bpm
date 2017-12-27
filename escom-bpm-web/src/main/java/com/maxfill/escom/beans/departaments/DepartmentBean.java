package com.maxfill.escom.beans.departaments;

import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomMsgUtils.getMessageLabel;
import com.maxfill.facade.StaffFacade;

import java.text.MessageFormat;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

/* Сервисный бин "Подразделения" */
@Named
@SessionScoped
public class DepartmentBean extends BaseTreeBean<Department, Company>{
    private static final long serialVersionUID = -690060212424991825L;
    
    @Inject
    private CompanyBean ownerBean;
    @Inject
    private StaffBean staffBean;
    
    @EJB
    private DepartmentFacade itemFacade;
    @EJB
    private StaffFacade staffFacade;
    
    @Override
    public DepartmentFacade getItemFacade() {
        return itemFacade;
    }
    
    @Override
    public void moveGroupToGroup(BaseDict dropItem, Department dragItem) {
        itemFacade.detectParentOwner(dragItem, dropItem);
        getItemFacade().edit(dragItem);
    }
    
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Company){
            getOwnerBean().getItemFacade().actualizeRightItem(dropItem, currentUser);
        } else {
            getItemFacade().actualizeRightItem(dropItem, currentUser);
        }
    }
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(Department department){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(department.getDetailItems());
        dependency.add(department.getChildItems());
        return dependency;
    } 
    
    /* Вставка скопированного объекта */
    @Override
    public void preparePasteItem(Department pasteItem, Department sourceItem, BaseDict target){
        super.preparePasteItem(pasteItem, sourceItem, target);
        if (target == null){
            if (sourceItem.getOwner() != null){
                target = sourceItem.getOwner();
            } else {
                target = sourceItem.getParent();
            }
        }
        itemFacade.detectParentOwner(pasteItem, target);    
    }       
    
    /* Формирование кода подразделения  */
    public void makeCode(Department department){  
        itemFacade.makeCode(department);        
    } 
        
    /* Формирование контента подразделения  */     
    @Override
    public List<BaseDict> makeGroupContent(BaseDict department, Integer viewMode) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент подразделения
        List<Department> departments = itemFacade.findActualChilds((Department)department);
        departments.stream().forEach(depart -> addChildItemInContent(depart, cnt));        
        //загружаем в контент штатные единицы
        List<BaseDict> staffs = staffFacade.findItemByOwner((Department)department);
        staffs.stream().forEach(staff -> addDetailItemInContent(staff, cnt));
        return cnt;
    }    
    
    @Override
    public List<Company> getGroups(Department item) {
        List<Company> groups = null;
        if (item.getOwner() != null){
            groups = item.getOwner().getChildItems();
        }
        return groups;
    }
    
    /* Формирует число ссылок на department в связанных объектах  */
    @Override
    public void doGetCountUsesItem(Department department,  Map<String, Integer> rezult){
        rezult.put("Staffs", staffFacade.findItemByOwner(department).size());
    }    
    
    /* Проверка возможности удаления Подразделения */
    @Override
    protected void checkAllowedDeleteItem(Department department, Set<String> errors){
        if (CollectionUtils.isNotEmpty(staffFacade.findStaffByDepartment(department)) ) {
            Object[] messageParameters = new Object[]{department.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return ownerBean;
    }
    
    @Override
    public Class<Company> getOwnerClass() {
        return Company.class;
    }

    @Override
    public BaseExplBean getDetailBean() {
        return staffBean;
    }

}