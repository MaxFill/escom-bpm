package com.maxfill.escom.beans.staffs;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.departments.Department;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;

import javax.ejb.EJB;
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

    /**
     * Проверка перед созданием штатной единицы
     * @param newItem
     * @param parent
     * @param errors
     */
    @Override
    protected void prepCreate(Staff newItem, BaseDict parent, Set <String> errors) {
        if (newItem.getOwner() == null && newItem.getCompany() != null) {
            Company company = newItem.getCompany();
            companyBean.getFacade().actualizeRightItem(company, getCurrentUser());
            Boolean isAllowedAddDetail = companyBean.getFacade().isHaveRightAddDetail(company); //можно ли создавать штатные единицы
            if (!isAllowedAddDetail){
                String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("RightAddDetailsNo"), new Object[]{company.getName(), EscomMsgUtils.getBandleLabel(getFacade().getMetadatesObj().getBundleName())});
                errors.add(error);
            }
        }
        super.prepCreate(newItem, parent, errors);
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return ownerBean;
    }
    
    @Override
    public BaseDetailsBean getGroupBean() {
        return ownerBean;
    }
    
    @Override
    public StaffFacade getFacade() {
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
        getFacade().edit(staff);
    }
      
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Department){
            getOwnerBean().getFacade().actualizeRightItem(dropItem, getCurrentUser());
        } else {
            companyBean.getFacade().actualizeRightItem(dropItem, getCurrentUser());
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
        getFacade().edit(dragItem);
    }

    /**
     * Проверка возможности удаления Штатной единицы
     * @param staff
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(Staff staff, Set<String> errors){
        super.checkAllowedDeleteItem(staff, errors);
    }
    
    @Override
    public void doGetCountUsesItem(Staff staff,  Map<String, Integer> rezult){
        rezult.put("Users", userFacade.findUsersByStaff(staff).size());
    }  

    @Override
    public BaseDetailsBean getDetailBean() {
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

}
