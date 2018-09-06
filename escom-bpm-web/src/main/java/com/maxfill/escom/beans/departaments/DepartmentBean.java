package com.maxfill.escom.beans.departaments;

import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.model.departments.Department;
import com.maxfill.model.departments.DepartmentFacade;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.staffs.StaffBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import java.text.MessageFormat;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import com.maxfill.model.staffs.Staff;
import javax.persistence.criteria.Order;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.TreeNode;

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
    
    @Override
    public DepartmentFacade getFacade() {
        return itemFacade;
    }
    
    @Override
    public void moveGroupToGroup(BaseDict dropItem, Department dragItem) {
        itemFacade.detectParentOwner(dragItem, dropItem, dropItem);
        getFacade().edit(dragItem);
    }
    
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Company){
            getOwnerBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
        } else {
            getFacade().actualizeRightItem(dropItem, getCurrentUser());
        }
    }
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(Department department){
        List<List<?>> dependency = new ArrayList<>();
        List<Staff> staffs = staffFacade.findStaffByDepartment(department);
        if (!staffs.isEmpty()) {
            dependency.add(staffs);
        }
        List<Department> departments = itemFacade.findChildDepartments(department);
        if (!departments.isEmpty()) {
            dependency.add(departments);
        }
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
        itemFacade.detectParentOwner(pasteItem, target, target);
    }

    /* Формирование кода подразделения  */
    public void makeCode(Department department){  
        itemFacade.makeCode(department);
    }
        
    /* Формирование контента подразделения  */     
    @Override
    public List<BaseDict> makeGroupContent(BaseDict department, Integer viewMode, int first, int pageSize, String sortField, String sortOrder) {
        List<BaseDict> cnt = new ArrayList();
        List<Order> orders = new ArrayList<>();
        //загружаем в контент подразделения
        List<Department> departments = itemFacade.findActualChilds((Department)department);
        departments.stream().forEach(depart -> addChildItemInContent(depart, cnt));
        //загружаем в контент штатные единицы
        List<Staff> staffs = staffFacade.findActualDetailItems((Department)department, first, pageSize, sortField,  sortOrder);
        staffs.stream().forEach(staff -> addDetailItemInContent(staff, cnt));
        return cnt;
    }

    /**
     * Формирует список компаний, в которых находится данное подразделение
     * @param item
     * @return
     */
    @Override
    public List<Company> getGroups(Department item) {
        List<Company> groups = new ArrayList <>();;
        if (item.getOwner() != null){
            groups.add(item.getOwner());
        }
        return groups;
    }
    
    /* Формирует число ссылок на department в связанных объектах  */
    @Override
    public void doGetCountUsesItem(Department department,  Map<String, Integer> rezult){
        Long count = staffFacade.getCountDetails(department);
        rezult.put("Staffs", count.intValue());
    }

    /**
     * Проверка возможности удаления Подразделения
     * Подразделение можно удалить только если в нём нет штатных единиц. Наличие вложенных подразделений не важно.
     * При удалении вложенных подразделений произойдётаналогичная проверка
     * @param department
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(Department department, Set<String> errors){
        if (CollectionUtils.isNotEmpty(staffFacade.findStaffByDepartment(department)) ) {
            Object[] messageParameters = new Object[]{department.getName()};
            String error = MessageFormat.format(getMessageLabel("DeleteObjectHaveChildItems"), messageParameters);
            errors.add(error);
        }
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return ownerBean;
    }
    
    @Override
    public Class<Company> getOwnerClass() {
        return Company.class;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return staffBean;
    }

    @Override
    protected void doExpandTreeNode(TreeNode node){
        node.setExpanded(true);
    }
}