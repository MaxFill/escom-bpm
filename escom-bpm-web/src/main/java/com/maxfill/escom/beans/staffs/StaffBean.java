package com.maxfill.escom.beans.staffs;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.staff.StaffFacade;
import com.maxfill.model.basedict.staff.Staff;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import static com.maxfill.escom.utils.MsgUtils.getMessageLabel;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.post.Post;
import com.maxfill.model.basedict.process.ProcessFacade;
import com.maxfill.model.basedict.task.TaskFacade;
import com.maxfill.model.basedict.user.User;
import javax.ejb.EJB;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
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
    private ProcessFacade processFacade;
    @EJB
    private TaskFacade taskFacade;
    
    /**
     * Возвращает все штатные единицы, входящие в компанию, доступные для просмотра текущему пользователю
     * @param company
     * @return 
     */
    public List<Staff> findStaffsInCompany(Company company){        
        return staffFacade.findActualStaffInCompany(company, getCurrentUser());
    }
            
    /**
     * Возвращает штатные единицы, не входящие в подразделения, а напрямую принадлежащие компании, доступные для просмотра текущему пользователю
     * @param company
     * @return 
     */
    public List<Staff> findStaffsByCompany(Company company){        
        return staffFacade.findStaffInOnlyCompany(company, getCurrentUser());
    }
    
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
            companyBean.getLazyFacade().actualizeRightItem(company, getCurrentUser());
            Boolean isAllowedAddDetail = companyBean.getLazyFacade().isHaveRightAddDetail(company); //можно ли создавать штатные единицы
            if (!isAllowedAddDetail){
                String error = MessageFormat.format(MsgUtils.getMessageLabel("RightAddDetailsNo"), new Object[]{company.getName(), MsgUtils.getBandleLabel(getLazyFacade().getMetadatesObj().getBundleName())});
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
    public StaffFacade getLazyFacade() {
        return staffFacade;
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
        staffFacade.detectParentOwner(pasteItem, target, target);
    }
    
    @Override
    public boolean addItemToGroup(Staff staff, BaseDict group){ 
        //поскольку шт.ед. может быть только в одном подразделении, то выполняем перемещение
        moveItemToGroup(group, staff); 
        return true;
    }

    public void moveItemToGroup(BaseDict group, Staff staff){
        staffFacade.detectParentOwner(staff,group, group);
        getLazyFacade().edit(staff);
    }
      
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        if (dropItem instanceof Department){
            getOwnerBean().getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
        } else {
            companyBean.getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
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
        getLazyFacade().edit(dragItem);
    }

    /**
     * Проверка возможности удаления Штатной единицы
     * @param staff
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(Staff staff, Set<String> errors){
        super.checkAllowedDeleteItem(staff, errors);
        if (processFacade.findCountStaffLinks(staff) > 0 ) {
            Object[] messageParameters = new Object[]{staff.getName()};
            String error = MessageFormat.format(getMessageLabel("StaffUsedInProcesses"), messageParameters);
            errors.add(error);
        }
        if (taskFacade.findCountStaffLinks(staff) > 0 ) {
            Object[] messageParameters = new Object[]{staff.getName()};
            String error = MessageFormat.format(getMessageLabel("StaffUsedInTasks"), messageParameters);
            errors.add(error);
        }
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
        return new StaffSearche();
    }

    public void makeName(Staff staff){
        StringBuilder sb = new StringBuilder();
        User user = staff.getEmployee();
        if (user != null && StringUtils.isNotEmpty(user.getShortFIO())) {
            sb.append(user.getShortFIO()).append(" ");
        } else {
            sb.append(MsgUtils.getBandleLabel("Vacant")).append(" ");
        }
        Post post = staff.getPost();
        if (post != null && StringUtils.isNoneBlank(post.getName())) {
            sb.append(post.getName());
        }
        staff.setName(sb.toString());
    }
    
    public List<Staff> findActualStaff(){
        return staffFacade.findActualStaff();
    }
}
