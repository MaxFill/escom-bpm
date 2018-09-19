package com.maxfill.escom.beans.companies;

import com.maxfill.dictionary.DictExplForm;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.companies.Company;
import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.Department;
import com.maxfill.model.departments.DepartmentFacade;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.process.ProcessFacade;
import com.maxfill.model.staffs.Staff;
import java.text.MessageFormat;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.*;
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
    private DocFacade docsFacade;    
    @EJB
    private ProcessFacade processFacade;
    
    @Override
    public CompanyFacade getFacade() {
        return itemsFacade;
    }

    @Override
    public List<Company> getGroups(Company item) {
        return null;
    }   

    @Override
    public void loadChilds(BaseDict item, TreeNode node){
        if ("ui-icon-folder-collapsed".equals(item.getIconTree())){
            node.setExpanded(true);
            node.getChildren().clear();            
            switch (item.getClass().getSimpleName()){
                case DictObjectName.DEPARTAMENT:{                    
                    departmentFacade.findActualChilds((Department)item, getCurrentUser())
                            .forEach(itemChild -> addItemInTree(node, itemChild, "tree"));
                    break;
                }
                case DictObjectName.COMPANY:{                    
                    departmentFacade.findActualDetailItems((Company)item, 0, 0, "name", "ASCENDING", getCurrentUser())
                            .forEach(itemChild -> addItemInTree(node, itemChild, DictObjectName.COMPANY));
                    break;
                }
            }            
            item.setIconTree("ui-icon-folder-open");
        }
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
        List<Staff> staffs = staffFacade.findStaffByCompany(company, null, getCurrentUser()); //копируются все штединицы, кроме удалённых в корзину
        if (!staffs.isEmpty()) {
            dependency.add(staffs);
        }
        return dependency;
    } 
    
    /* Формирование контента компании */
    @Override
    public List<BaseDict> makeGroupContent(BaseDict company, BaseTableBean tableBean, Integer viewMode, int first, int pageSize, String sortField, String sortOrder){
        List<BaseDict> cnt = new ArrayList();
        if (Objects.equals(viewMode, DictExplForm.SELECTOR_MODE)) {
            cnt.addAll(departmentFacade.findActualDetailItems((Company) company, first, pageSize, sortField, sortOrder, getCurrentUser()));
        } else {
            cnt.addAll(staffFacade.findStaffByCompany((Company) company, null, getCurrentUser()));
        }
        return cnt;
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
        if (processFacade.findCountCompanyLinks(company) > 0 ) {
            Object[] messageParameters = new Object[]{company.getName()};
            String error = MessageFormat.format(getMessageLabel("CompanyUsedInProcesses"), messageParameters);
            errors.add(error);
        }
        if (docsFacade.findCountCompanyLinks(company) > 0 ) {
            Object[] messageParameters = new Object[]{company.getName()};
            String error = MessageFormat.format(getMessageLabel("CompanyUsedInDocs"), messageParameters);
            errors.add(error);
        }
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

}