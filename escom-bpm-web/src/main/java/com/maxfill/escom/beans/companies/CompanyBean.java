package com.maxfill.escom.beans.companies;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.model.companies.Company;
import com.maxfill.facade.CompanyFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.facade.StaffFacade;
import com.maxfill.escom.utils.EscomBeanUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
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

/* Компании юр лица */
@Named
@SessionScoped
public class CompanyBean extends BaseTreeBean<Company, Company> {
    private static final long serialVersionUID = -5912904511478529363L;

    @Inject
    private DepartmentBean departmentBean;
    @Inject
    private StaffBean staffBean;
    
    @EJB
    private CompanyFacade itemsFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private StaffFacade staffFacade;    
    
    @Override
    public CompanyFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    public List<Company> getGroups(Company item) {
        return null;
    }

    @Override
    protected TreeNode addItemInTree(TreeNode parentNode, BaseDict item) {           
        TreeNode rezNode = null;
        String typeNode = "tree";
        
        if (preloadCheckRightView(item)){
            List<Department> childs = new ArrayList<>();
            
            switch (item.getClass().getSimpleName()){
                case DictObjectName.DEPARTAMENT:{
                    typeNode = "tree";
                    childs = departmentFacade.findChilds((Department)item);
                    break;
                }
                case DictObjectName.COMPANY:{
                    typeNode = DictObjectName.COMPANY;
                    childs = departmentFacade.findDetailItems((Company)item);
                    break;
                }
            }
            TreeNode newNode = new DefaultTreeNode(typeNode, item, parentNode);    
            childs.stream().forEach(itemChild -> addItemInTree(newNode, itemChild));
            rezNode = newNode;
        }
        return rezNode;
    } 
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(Company company){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(company.getDetailItems());
        List<Staff> staffs = staffFacade.findStaffByCompany(company, null);
        dependency.add(staffs);
        return dependency;
    } 
    
    /* Формирование контента компании */     
    @Override
    public List<BaseDict> makeGroupContent(BaseDict company, Integer viewMode) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент подразделения
        List<Department> departments = departmentFacade.findDetailItems((Company)company);        
        departments.stream().forEach(department -> addDetailItemInContent(department, cnt));        
        //загружаем в контент штатные единицы
        List<Staff> staffs = staffFacade.findStaffByCompany((Company)company, null);
        staffs.stream().forEach(staff -> addStaffInCnt(staff, cnt));
        return cnt;
    }

    /* Добавляет штатную единицу в контент  */ 
    public void addStaffInCnt(Staff staff, List<BaseDict> cnts) {
        if (staffBean.preloadCheckRightView(staff)){
            cnts.add(staff);
        }
    }           
     
    /* Формирует число ссылок на company в связанных объектах   */
    @Override
    public void doGetCountUsesItem(Company company,  Map<String, Integer> rezult){
        rezult.put("Departaments", company.getDepartmentsList().size() - 1);
    }
    
    /* Проверка возможности удаления company */
    @Override
    protected void checkAllowedDeleteItem(Company company, Set<String> errors){
        if (departmentFacade.findDepartmentByCompany(company).size() >1){ //одно подразделение служебное и его не надо учитывать!
            Object[] messageParameters = new Object[]{company.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("CompanyUsedInDepartaments"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public Class<Company> getItemClass() {
        return Company.class;
    }

    @Override
    public Class<Company> getOwnerClass() {
        return null;
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseExplBean getDetailBean() {
        return departmentBean;
    }
    
    @FacesConverter("companyConvertor")
    public static class companyConvertor implements Converter {

        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
            if (value != null && value.trim().length() > 0) {
                try {
                    CompanyBean bean = EscomBeanUtils.findBean("companyBean", fc);
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
                return String.valueOf(((Company) object).getId());
            } else {
                return "";
            }
        }
    }
}