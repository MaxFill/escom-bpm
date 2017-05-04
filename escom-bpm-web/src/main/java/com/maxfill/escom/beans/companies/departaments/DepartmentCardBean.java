package com.maxfill.escom.beans.companies.departaments;

import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.companies.Company;
import com.maxfill.model.staffs.Staff;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.dictionary.SysParams;
import org.primefaces.event.SelectEvent;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Формирование кода подразделения
     */
    public void makeCode(){
        Department department = getEditedItem();
        Company company = department.getOwner();
        String counterName = itemsFacade.getFRM_NAME();
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = numeratorService.doRegistrNumber(department, counterName, numeratorPattern, null, new Date());
        StringBuilder sb = new StringBuilder();
        sb.append(company.getCode()).append(SysParams.CODE_SEPARATOR).append(number);
        department.setCode(sb.toString());
    }    
        
    /**
     * Выбор руководителя      
     * @param event
     */
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

    public List<Staff> getStaffs() {
        if (staffs == null){
            staffs = staffFacade.findAll().stream()
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
    protected void onAfterCreateItem(Department item) {        
        makeCode();
    }
        
}
