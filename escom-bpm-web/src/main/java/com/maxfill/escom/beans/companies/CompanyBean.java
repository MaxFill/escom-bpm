package com.maxfill.escom.beans.companies;

import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.Department;
import com.maxfill.model.departments.DepartmentFacade;
import com.maxfill.model.staffs.Staff;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.inject.Named;
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
    
    @Override
    public CompanyFacade getFacade() {
        return itemsFacade;
    }

    @Override
    public List<Company> getGroups(Company item) {
        return null;
    }

    @Override
    public TreeNode addItemInTree(TreeNode parentNode, BaseDict item, String typeNode) {
        TreeNode rezNode = null;

        if (itemsFacade.preloadCheckRightView(item, getCurrentUser())){
            List<Department> childs = new ArrayList<>();
            
            switch (item.getClass().getSimpleName()){
                case DictObjectName.DEPARTAMENT:{
                    typeNode = "tree";
                    childs = departmentFacade.findActualChilds((Department)item);
                    break;
                }
                case DictObjectName.COMPANY:{
                    typeNode = DictObjectName.COMPANY;
                    childs = departmentFacade.findActualDetailItems((Company)item, 0, 0, "name", "ASCENDING");
                    break;
                }
            }
            TreeNode newNode = new DefaultTreeNode(typeNode, item, parentNode);
            doExpandTreeNode(newNode);
            String finalTypeNode = typeNode;
            childs.stream().forEach(itemChild -> addItemInTree(newNode, itemChild, finalTypeNode));
            rezNode = newNode;
        }
        return rezNode;
    }

    /* Удаление подчинённых объектов из корзины */
    @Override
    protected void deleteDetails(Company company) {
        List<Department> departments = getFacade().findDepartmentByCompany(company);
        if (!departments.isEmpty()) {
            departments.stream().forEach(child -> departmentBean.deleteItem(child));
        }
        List<Staff> staffs = getFacade().findStaffByCompany(company);
        if (!staffs.isEmpty()){
            staffs.stream().forEach((staff -> staffBean.deleteItem(staff)));
        }
    }

    /* Перемещение в корзину подчинённых объектов  */
    @Override
    protected void moveDetailItemsToTrash(Company company, Set<String> errors) {
        List<Department> departments = getFacade().findDepartmentByCompany(company);
        if (!departments.isEmpty()) {
            departments.stream().forEach(child -> departmentBean.moveToTrash(child, errors));
        }
        List<Staff> staffs = getFacade().findStaffByCompany(company);
        if (!staffs.isEmpty()){
            staffs.stream().forEach((staff -> staffBean.moveToTrash(staff, errors)));
        }
    }

    /* Восстановление подчинённых объектов из корзины */
    @Override
    protected void restoreDetails(Company company) {
        List<Department> departments = getFacade().findDepartmentByCompany(company);
        if (departments != null){
            departments.stream().forEach(item -> departmentBean.doRestoreItemFromTrash(item));
        }
        List<Staff> staffs = getFacade().findStaffByCompany(company);
        if (!staffs.isEmpty()){
            staffs.stream().forEach((staff -> staffBean.doRestoreItemFromTrash(staff)));
        }
    }

    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(Company company){
        List<List<?>> dependency = new ArrayList<>();
        List<Department> departments = departmentFacade.findDepartmentByCompany(company);
        if (!departments.isEmpty()) {
            dependency.add(departments); //копируются все подразделения, кроме удалённых в корзину
        }
        List<Staff> staffs = staffFacade.findStaffByCompany(company, null); //копируются все штединицы, кроме удалённых в корзину
        if (!staffs.isEmpty()) {
            dependency.add(staffs);
        }
        return dependency;
    } 
    
    /* Формирование контента компании */
    @Override
    public List<BaseDict> makeGroupContent(BaseDict company, Integer viewMode, int first, int pageSize, String sortField, String sortOrder){
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент подразделения
        List<Department> departments = departmentFacade.findActualDetailItems((Company)company, first, pageSize, sortField,  sortOrder);        
        departments.stream().forEach(department -> addDetailItemInContent(department, cnt));        
        //загружаем в контент штатные единицы
        List<Staff> staffs = staffFacade.findStaffByCompany((Company)company, null);
        staffs.stream().forEach(staff -> addStaffInCnt(staff, cnt));
        return cnt;
    }

    /* Добавляет штатную единицу в контент  */ 
    public void addStaffInCnt(Staff staff, List<BaseDict> cnts) {
        if (staffBean.getFacade().preloadCheckRightView(staff, getCurrentUser())){
            cnts.add(staff);
        }
    }           
     
    /* Формирует число ссылок на Компанию в подчинённых объектах   */
    @Override
    public void doGetCountUsesItem(Company company,  Map<String, Integer> rezult){
        rezult.put("Departaments", company.getDetailItems().size());
        rezult.put("Staffs", company.getDetailItems().size());
    }

    /**
     * Проверка возможности удаления Компании в корзину
     * Компанию без всяких проверок можно поместить в корзину
     * @param company
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(Company company, Set<String> errors){
        /* 12/04/2018г. Думаю что нет причины блокировать удаление компании, даже если она последняя...
        if (!departmentFacade.findDepartmentByCompany(company).isEmpty()){
            Object[] messageParameters = new Object[]{company.getName()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("CompanyUsedInDepartaments"), messageParameters);
            errors.add(error);
        }
        if (!staffFacade.findStaffByCompany(company, null).isEmpty()){
            Object[] messageParameters = new Object[]{company.getName()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("CompanyUsedInStaffs"), messageParameters);
            errors.add(error);
        }
        */
    }

    @Override
    public Class<Company> getOwnerClass() {
        return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return departmentBean;
    }

    @Override
    protected void doExpandTreeNode(TreeNode node){
        node.setExpanded(true);
    }
}