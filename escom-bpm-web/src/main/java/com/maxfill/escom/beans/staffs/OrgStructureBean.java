package com.maxfill.escom.beans.staffs;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.staff.Staff;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.organigram.OrganigramNodeDragDropEvent;
import org.primefaces.event.organigram.OrganigramNodeSelectEvent;
import org.primefaces.model.DefaultOrganigramNode;
import org.primefaces.model.OrganigramNode;

/**
 * Контролер формы Оргструктура
 * @author maksim
 */
@Named
@ViewScoped
public class OrgStructureBean extends BaseViewBean{
    private static final long serialVersionUID = -5622929754379822031L;

    @Inject
    private CompanyBean companyBean;
    @Inject
    private DepartmentBean departmentBean;
    @Inject
    private StaffBean staffBean;
        
    private Company selectedCompany;
    private List<Company> companies;
    
    private OrganigramNode rootNode;
    private OrganigramNode selection;
    
    private BaseDict selectedItem;
    
    private String style = "width: auto;";
    
    @Override
    protected void initBean(){ 
    }
    
    private void initDiagram(){
        if (selectedCompany == null) return;        
 
        rootNode = new DefaultOrganigramNode("root", selectedCompany, null);
        selection = rootNode;
        rootNode.setCollapsible(true);
        rootNode.setDroppable(true);        
        
        //загрузить штатные единицы компании
        staffBean.findStaffsByCompany(selectedCompany)
                .forEach(staff->loadStaff(rootNode, staff));
        
        //загрузить подразделения компании
        departmentBean.findDetailItems(selectedCompany, 0, 0, "", "")        
                .forEach(dep->loadDivision(rootNode, dep));

    }
     
    private OrganigramNode loadDivision(OrganigramNode parent, Department department) {
        OrganigramNode divisionNode = new DefaultOrganigramNode("division", department, parent);
        divisionNode.setDroppable(true);
        divisionNode.setDraggable(true);
        divisionNode.setSelectable(true);
        
        staffBean.findDetailItems(department, 0, 0, "", "")
                .forEach(staff->loadStaff(divisionNode, staff));
        
        departmentBean.getLazyFacade()
                .findActualChilds(department, getCurrentUser())
                .forEach(child->loadDivision(divisionNode, child));
        return divisionNode;
    }    
    
    private void loadStaff(OrganigramNode parent, Staff staff){
        OrganigramNode staffNode = new DefaultOrganigramNode("employee", staff, parent);
        staffNode.setDroppable(false);
        staffNode.setDraggable(true);
        staffNode.setSelectable(true);
    }
    
    public void nodeDragDropListener(OrganigramNodeDragDropEvent event) {
        //BaseDict sourceItem = (BaseDict)event.getSourceOrganigramNode().getData();
        BaseDict targetItem = (BaseDict)event.getTargetOrganigramNode().getData();
        BaseDict movedItem = (BaseDict)event.getOrganigramNode().getData();
        moveItem(movedItem, targetItem);
    }
    
    public void nodeSelectListener(OrganigramNodeSelectEvent event) {
        selectedItem = (BaseDict)event.getOrganigramNode().getData();        
    }
 
    public void onClearDiagram(){
        initDiagram();
    }
    
    /**
     * Перемещение объекта movedItem из sourceItem в targetItem
     * @param movedItem
     * @param targetItem 
     */
    private void moveItem(BaseDict movedItem, BaseDict targetItem){        
        if (movedItem instanceof Staff){
            moveStaff((Staff)movedItem, targetItem);
        } else {
            moveDepartment((Department)movedItem, targetItem);
        }
    }
    
    /**
     * Перемещение штатной единицы
     * @param movedItem
     * @param targetItem 
     */
    private void moveStaff(Staff movedItem, BaseDict targetItem){
        if (targetItem instanceof Company){
            movedItem.setOwner(null);
            movedItem.setCompany((Company)targetItem);
        } else {
            movedItem.setOwner((Department)targetItem);
        }
        staffBean.getLazyFacade().edit(movedItem);
    }
    
    /**
     * Перемещение подразделения
     * @param movedItem
     * @param targetItem 
     */
    private void moveDepartment(Department movedItem, BaseDict targetItem){
        if (targetItem instanceof Department){
            movedItem.setParent((Department)targetItem);
        } else {
            movedItem.setOwner((Company)targetItem);
        }
        departmentBean.getLazyFacade().edit(movedItem);
    }
    
    public void onMoveStaffToTrash(){
        selection.getParent().getChildren().remove(selection);
        selection.clearParent();
        selection = null;
        //TODO
    }
    
    public void onMoveDepartToTrash(){
        selection.getParent().getChildren().remove(selection);
        selection.clearParent();
        selection = null;
        //TODO
    }
    
    /* *** *** */
        
    @Override
    public String getFormName() {
        return DictFrmName.FRM_ORGSTRUCTURE;

    }

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("OrgStructure");        
    }

    @Override
    public boolean isReadOnly() {
        return super.isReadOnly(); 
    }
    
    /* GETS & SETS */

    public OrganigramNode getRootNode() {
        return rootNode;
    }
    public void setRootNode(OrganigramNode rootNode) {
        this.rootNode = rootNode;
    }
 
    public OrganigramNode getSelection() {
        return selection;
    }
    public void setSelection(OrganigramNode selection) {
        this.selection = selection;
    }
    
    public List<Company> getCompanies() {
        if (companies == null){
            companies = companyBean.findAll();
        }
        return companies;
    }

    public Company getSelectedCompany() {
        return selectedCompany;
    }
    public void setSelectedCompany(Company selectedCompany) {
        this.selectedCompany = selectedCompany;
    }
    
    public String getStyle() {
        return style;
    } 
    public void setStyle(String style) {
        this.style = style;
    }
    
}
