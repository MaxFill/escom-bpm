package com.maxfill.escom.beans.staffs;

import com.maxfill.dictionary.DictEditMode;
import com.maxfill.dictionary.DictFrmName;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.beans.companies.CompanyBean;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.beans.departaments.DepartmentBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.staff.Staff;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.SelectEvent;
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
    private BaseDict newItem;
    private int typeEdit; //режим редактирования записи
    private String style = "width: auto;";
    
    @Override
    protected void initBean(){ 
    }
    
    /* *** МЕТОДЫ РАБОТЫ С ДИАГРАММОЙ *** */
    
    /**
     * Инициализация диаграммы
     */
    public void initDiagram(){
        if (selectedCompany == null) return;        
 
        rootNode = new DefaultOrganigramNode("root", selectedCompany, null);
        
        rootNode.setCollapsible(true);
        rootNode.setDroppable(true);        
        rootNode.setSelectable(true);
        
        //загрузить штатные единицы компании
        staffBean.findStaffsByCompany(selectedCompany)
                .forEach(staff->addStaffNode(rootNode, staff));
        
        //загрузить подразделения компании
        departmentBean.findDetailItems(selectedCompany, 0, 0, "", "")        
                .forEach(dep->loadDivision(rootNode, dep));

        if (selectedItem == null){
            selection = rootNode;
        } else {
            selection = findNodeByData(rootNode, selectedItem);
        }
    }
    
    /**
     * Загрузка в диаграмму подразделения department
     * @param parent
     * @param department
     * @return 
     */
    private OrganigramNode loadDivision(OrganigramNode parent, Department department) {
        OrganigramNode divisionNode = addDepartmentNode(parent, department);        
        
        departmentBean.getLazyFacade()
                .findActualChilds(department, getCurrentUser())
                .forEach(child->loadDivision(divisionNode, child));
        
        staffBean.findDetailItems(department, 0, 0, "", "")
                .forEach(staff->addStaffNode(divisionNode, staff));
        
        return divisionNode;
    }    
    
    private OrganigramNode addDepartmentNode(OrganigramNode parent, Department department){
        OrganigramNode divisionNode = new DefaultOrganigramNode("division", department, parent);
        divisionNode.setDroppable(true);
        divisionNode.setDraggable(isCanEdit());
        divisionNode.setSelectable(true);
        return divisionNode;
    }
    
    private OrganigramNode addStaffNode(OrganigramNode parent, Staff staff){
        OrganigramNode staffNode = new DefaultOrganigramNode(staff.isVacant() ? "vacant":"employee", staff, parent);
        staffNode.setDroppable(false);
        staffNode.setDraggable(isCanEdit());
        staffNode.setSelectable(true);
        return staffNode;
    }
    
    /**
     * Обработка события drag & drop
     * @param event 
     */
    public void nodeDragDropListener(OrganigramNodeDragDropEvent event) {
        //BaseDict sourceItem = (BaseDict)event.getSourceOrganigramNode().getData();
        BaseDict targetItem = (BaseDict)event.getTargetOrganigramNode().getData();
        BaseDict movedItem = (BaseDict)event.getOrganigramNode().getData();
        moveItem(movedItem, targetItem);
    }
    
    /**
     * Обработка события выделения элемента в диаграмме
     * @param event 
     */
    public void nodeSelectListener(OrganigramNodeSelectEvent event) {
        selectedItem = (BaseDict)event.getOrganigramNode().getData();        
    }
    
    /**
     * Обработка свёртывания подразделений
     */
    public void onCollapseAll(){
        rootNode.getChildren().forEach(node->node.setExpanded(false));
    }
    
    /**
     * Обработка развёртывания подразделений
     */
    public void onExpandAll(){
        rootNode.getChildren().forEach(node->node.setExpanded(true));
    }
    
    /**
     * Осуществляет поиск в дереве rootNode элемента, с заданным значением selectedItem
     * @param node
     * @param selectedItem
     * @return 
     */
    private OrganigramNode findNodeByData(OrganigramNode node, BaseDict selectedItem){
        if (Objects.equals((BaseDict)node.getData(), selectedItem)){
            return node;
        } else {
            return node.getChildren().stream()
                    .filter(n->findNodeByData(n, selectedItem) != null)
                    .findFirst()
                    .orElse(null);
        }        
    }
    
    /**
     * Удаление в диаграмме выделенного объекта
     */
    private void removeSelectedNode(){
        OrganigramNode parent = selection.getParent();
        if (parent != null){
            parent.getChildren().remove(selection);
        }            
        selection = parent; //новым текущим элементом будет родитель удалённого
        selectedItem = (BaseDict)selection.getData();
    }
    
    /* *** ПЕРЕМЕЩЕНИЯ * *** */
    
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
    
    /**
     * Перемещение объекта в корзину
     */
    public void onMoveToTrash(){ 
        Set<String> errors = new HashSet<>();
        if (selectedItem instanceof Staff){
            staffBean.moveToTrash((Staff)selectedItem, errors);
        } else 
            if (selectedItem instanceof Department){
                departmentBean.moveToTrash((Department)selectedItem, errors);
            } else {
                companyBean.moveToTrash((Company)selectedItem, errors);
            }
        
        if (!errors.isEmpty()) {
            MsgUtils.showErrors(errors);
        } else { 
            removeSelectedNode();
        }
    }         
    
    /* *** КАРТОЧКА *** */
    
    /**
     * Метод вызывается перед открытием карточки объекта
     */
    public void onBeforOpenItem(){        
    }
    
    /**
     * Метод вызывается после закрытия карточки объекта
     * @param event 
     */
    public void onAfterCloseItemCard(SelectEvent event){
        if (event.getObject() == null) return;
        if (SysParams.EXIT_NEED_UPDATE.equals((String) event.getObject())){
            switch (typeEdit){
                case DictEditMode.EDIT_MODE:{
                    updateSelectedItem();
                    break;
                }
                case DictEditMode.INSERT_MODE:{
                    insertNewItem();
                    break;
                }
            }
        } 
    }
    
    /**
     * Открытие карточки для просмотра
     */
    public void onViewItem(){
        if (selectedItem == null) return;
        typeEdit = DictEditMode.VIEW_MODE;
        if (selectedItem instanceof Staff){
            staffBean.prepViewItem((Staff) selectedItem, this.getParamsMap(), new HashSet<>());
        } else 
            if (selectedItem instanceof Department){
                departmentBean.prepViewItem((Department) selectedItem, this.getParamsMap(), new HashSet<>());
            } else {
                companyBean.prepViewItem((Company) selectedItem, this.getParamsMap(), new HashSet<>());
            }
    }     
    
    /* *** РЕДАКТИРОВАНИЕ *** */
    
    /**
     * Открытие карточки для редактирования
     */
    public void onEditItem(){
        if (selectedItem == null) return;
        typeEdit = DictEditMode.EDIT_MODE;
        if (selectedItem instanceof Staff){
            staffBean.prepEditItem((Staff) selectedItem, this.getParamsMap());
        } else 
            if (selectedItem instanceof Department){
                departmentBean.prepEditItem((Department) selectedItem, this.getParamsMap());
            } else {
                companyBean.prepEditItem((Company) selectedItem, this.getParamsMap());
            }
    }              
    
    /**
     * Создание подразделения
     */
    public void onCreateDepartment(){
        Department parent = null;
        if (selectedItem instanceof Department){
            parent = (Department)selectedItem;
        } 
        typeEdit = DictEditMode.INSERT_MODE;
        newItem = departmentBean.createItemAndOpenCard(parent, selectedCompany, new HashMap<>(), this.getParamsMap());
    }
    
    /**
     * Создание штатной единицы
     */
    public void onCreateStaff(){
        typeEdit = DictEditMode.INSERT_MODE;
        newItem = staffBean.createItemAndOpenCard(null, selectedItem, new HashMap<>(), this.getParamsMap());
    }
    
    /**
     * Обновление изменённого объекта
     * @param item 
     */
    private void updateSelectedItem(){
        if (selectedItem instanceof Staff){
            selectedItem = staffBean.findItem(selectedItem.getId());
            selection.setType(((Staff)selectedItem).isVacant()? "vacant":"employee");
        } else 
            if (selectedItem instanceof Department){
                selectedItem = departmentBean.findItem(selectedItem.getId());
            } else 
                if (selectedItem instanceof Company){
                    selectedItem = companyBean.findItem(selectedItem.getId());
                }
        selection.setData(selectedItem);
        
        //если объект после редактирования стал не актуальным, то его нужно удалить из диаграммы
        if (!selectedItem.isActual()){
            removeSelectedNode();
        }
    }
    
    /**
     * Вставка в диаграмму нового объекта     
     */
    private void insertNewItem(){
        if (newItem == null) return;
        if (newItem instanceof Staff){
            selection = addStaffNode(selection, (Staff) newItem);
        } else 
            if(newItem instanceof Department){
                selection = addDepartmentNode(selection, (Department) newItem);
            }
        newItem = null;        
    }
    
    /* *** ПРОЧИЕ МЕТОДЫ *** */
        
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
    
    public boolean isCanEdit(){
        return !isReadOnly();
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
