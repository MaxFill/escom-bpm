package com.maxfill.escom.beans.departaments;

import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.rights.Rights;
import java.text.MessageFormat;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
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
    public Rights getRightItem(BaseDict item) {
        if (item == null) return null;
        
        if (!item.isInherits()) {
            return getActualRightItem(item); //получаем свои права 
        } 
        
        if (item.getParent() != null) {
            return getRightItem(item.getParent()); //получаем права от родительского подразделения
        } 
        
        if (item.getOwner() != null) {
            Rights childRight = ownerBean.getRightForChild(item.getOwner()); //получаем права из спец.прав 
            if (childRight != null){
                return childRight;
            }
        }
        
        return getDefaultRights(item);
    } 
    
    @Override
    public DepartmentFacade getItemFacade() {
        return itemFacade;
    }

    /* Определяет owner и parent для объекта  */
    @Override
    public void detectParentOwner(Department item, BaseDict target){
        if (target instanceof Company){
            item.setOwner((Company)target);
            item.setParent(null);
        } else
        if (target instanceof Department){
            item.setOwner(null);
            item.setParent((Department)target);
        }
    }
    
    @Override
    public void moveGroupToGroup(BaseDict dropItem, Department dragItem) {
        detectParentOwner(dragItem, dropItem);
        getItemFacade().edit(dragItem);
    }
    
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Company){
            getOwnerBean().actualizeRightItem(dropItem);
        } else {
            actualizeRightItem(dropItem);
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
        detectParentOwner(pasteItem, target);    
    }   
    
    @Override
    public void setSpecAtrForNewItem(Department department, Map<String, Object> params) {
        makeCode(department);
    }
    
    /* Формирование кода подразделения  */
    public void makeCode(Department department){
        Company company = findCompany(department);
        String counterName = getItemFacade().getFRM_NAME();
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(department, counterName, numeratorPattern, null, new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(company.getCode()).append(SysParams.CODE_SEPARATOR).append(number);
        department.setCode(sb.toString());
    } 
    
    /* Возвращает компанию, в которой находится подразделение */
    public Company findCompany(Department item){        
        Company company = null;
        if (item.getParent() != null){
            company = findCompany(item.getParent());
        }
        if (company == null){
            company = item.getOwner();
        }    
        return company;
    }
    
    /* Формирование контента подразделения  */     
    @Override
    public List<BaseDict> makeGroupContent(BaseDict department, Integer viewMode) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент подразделения
        List<Department> departments = itemFacade.findChilds((Department)department);
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
    public Class<Department> getItemClass() {
        return Department.class;
    }
    
    @Override
    public Class<Company> getOwnerClass() {
        return Company.class;
    }

    @Override
    public BaseExplBean getDetailBean() {
        return staffBean;
    }
    
    @FacesConverter("departmentConvertor")
    public static class departmentConvertor implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {  
                 DepartmentBean bean = EscomBeanUtils.findBean("departmentBean", fc);
                 Object searcheObj = bean.getItemFacade().find(Integer.parseInt(value));
                 return searcheObj;
             } catch(NumberFormatException e) {
                 throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
             }
         }
         else {
             return null;
         }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if(object != null) {
                return String.valueOf(((Department)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}