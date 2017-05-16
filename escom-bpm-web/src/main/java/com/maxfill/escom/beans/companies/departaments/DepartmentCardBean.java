package com.maxfill.escom.beans.companies.departaments;

import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.Staff;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.utils.SysParams;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.event.ValueChangeEvent;

/**
 * Бин для карточки подразделения
 * @author mfilatov
 */
@Named
@ViewScoped
public class DepartmentCardBean extends BaseCardBeanGroups<Department, Company>{
    private static final long serialVersionUID = 3589793075526575359L;
                
    @EJB
    private DepartmentFacade itemsFacade;     

    @EJB
    private StaffFacade staffFacade;

    private List<Staff> staffs;

    @Override
    public DepartmentFacade getItemFacade() {
        return itemsFacade;
    }

    /* Формирование кода подразделения  */
    public void makeCode(){
        Department department = getEditedItem();
        Company company = findCompany(department);
        String counterName = itemsFacade.getFRM_NAME();
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
    
    public String getCompanyName(){
        if (getEditedItem() == null){
            return "";
        }
        Company company = findCompany(getEditedItem());
        return company.getName();
    }
    
    /* Обработка события выбора руководителя на карточке  */
    public void onChiefSelected(SelectEvent event){
        List<Staff> items = (List<Staff>) event.getObject();
        if (items.isEmpty()){return;}
        Staff item = items.get(0);
        onItemChange();
        getEditedItem().setChief(item );
        if (!staffs.contains(item)){
            staffs.add(item);
        }
    }
    public void onChiefSelected(ValueChangeEvent event){        
        Staff staff = (Staff) event.getNewValue();
        getEditedItem().setChief(staff);
        onItemChange();
    }  
    
    public List<Staff> getStaffs() {
        if (staffs == null){
            staffs = staffFacade.findActualStaff().stream()
                    .filter(item -> sessionBean.preloadCheckRightView(item))
                    .collect(Collectors.toList());
        }
        return staffs;
    }

    @Override
    public List<Company> getGroups(Department item) {
        List<Company> groups = null;
        if (item.getOwner() != null){
            groups = item.getOwner().getChildItems();
        }
        return groups;        
    }

    @Override
    protected void afterCreateItem(Department item) {        
        makeCode();
    }
        
    @Override
    public Class<Department> getItemClass() {
        return Department.class;
    }
}
