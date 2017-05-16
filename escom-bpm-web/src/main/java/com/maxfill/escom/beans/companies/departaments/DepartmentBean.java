package com.maxfill.escom.beans.companies.departaments;

import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.rights.Rights;
import com.maxfill.escom.utils.EscomBeanUtils;
import static com.maxfill.escom.utils.EscomBeanUtils.getMessageLabel;
import com.maxfill.facade.StaffFacade;
import java.text.MessageFormat;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import org.apache.commons.collections.CollectionUtils;

/* Подразделения */
@Named
@ViewScoped
public class DepartmentBean extends BaseTreeBean<Department, Company>{
    private static final long serialVersionUID = -690060212424991825L;
    private static final String BEAN_NAME = "departmentBean";
    
    @EJB
    private DepartmentFacade itemFacade;
    @EJB
    private StaffFacade staffFacade;
    
    public DepartmentBean() {
    }
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME; 
    }
    
    @Override
    public void onInitBean(){            
        super.onInitBean();
    }     
    
    @Override
    public DepartmentFacade getItemFacade() {
        return itemFacade;
    }

    /* КОНТЕНТ: формирование контента подразделения  */     
    @Override
    public List<BaseDict> makeGroupContent(Department department) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент подразделения
        List<Department> departments = itemFacade.findChilds(department);
        departments.stream().forEach(depart -> addDepartmentInCnt(depart, cnt));        
        //загружаем в контент штатные единицы
        List<BaseDict> staffs = staffFacade.findItemByOwner(department);
        staffs.stream().forEach(staff -> addStaffInCnt(staff, cnt));
        return cnt;
    }
    
    /* КОНТЕНТ: добавляет подразделение в контент  */ 
    private void addDepartmentInCnt(BaseDict department, List<BaseDict> cnts) {
        //Rights rights = makeRightChild(folder, defDocRight);
        //settingRightForChild(folder, rights); //сохраняем права к документам
        cnts.add(department);
    }

    /* КОНТЕНТ: добавляет штатную единицу в контент */ 
    public void addStaffInCnt(BaseDict staff, List<BaseDict> cnts) {
        /*
        Rights rd = defDocRight;
        if (doc.isInherits() && doc.getAccess() != null) { //установлены специальные права и есть в базе данные по правам
            rd = (Rights) JAXB.unmarshal(new StringReader(doc.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        }
        doc.setRightItem(rd);
        doc.setRightMask(rightService.getAccessMask(doc.getState(), rd, getCurrentUser())); //получаем маску доступа для текущего пользователя  
        */
        cnts.add(staff);
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
        if (CollectionUtils.isNotEmpty(department.getDetailItems()) ) {
            Object[] messageParameters = new Object[]{department.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }      

    @Override
    public Class<Department> getItemClass() {
        return Department.class;
    }
    
    @Override
    public Class<Company> getOwnerClass() {
        return Company.class;
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