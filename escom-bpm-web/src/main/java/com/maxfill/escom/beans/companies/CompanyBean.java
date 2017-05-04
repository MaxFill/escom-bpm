package com.maxfill.escom.beans.companies;

import com.maxfill.model.companies.Company;
import com.maxfill.facade.CompanyFacade;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.Department;
import com.maxfill.facade.DepartmentFacade;
import com.maxfill.model.staffs.Staff;
import com.maxfill.facade.StaffFacade;
import com.maxfill.model.rights.Rights;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Компании юр лица
 *
 * @author Maxim
 */
@Named
@ViewScoped
public class CompanyBean extends BaseTreeBean<Company, Company> {
    private static final long serialVersionUID = -5912904511478529363L;
    private static final String BEAN_NAME = "companyBean";    
            
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

    /**
     * ДЕРЕВО: добавление узла в дерево при его формировании
     *
     * @param parentNode Узел дерева, в который добавляется объект
     * @param item
     * @return
     */
    @Override
    protected TreeNode addItemInTree(TreeNode parentNode, BaseDict item) {           
        TreeNode rezNode = null;
        String typeNode = "tree";
        
        if (explorerBean.isItemTreeType(item)){ //подразделение
        } else {
            if (explorerBean.isItemRootType(item)){ //компания
                typeNode = explorerBean.getTypeRoot();
            }
        }    
        if (sessionBean.preloadCheckRightView(item)) {
            TreeNode newNode;

            synchronized (this) {
                newNode = new DefaultTreeNode(typeNode, item, parentNode);
                //newNode.setExpanded(true);
            }
            List<Department> childs = new ArrayList<>();
            if (explorerBean.isItemTreeType(item)){ //если подразделение, то берем дочерние подразделения
                childs = departmentFacade.findChilds((Department)item);
            } else {
                if (explorerBean.isItemRootType(item)){ //если это компания, то берём подчинённые подразделения
                    childs = departmentFacade.findDetailItems((Company)item);
                }
            }    
            childs.stream().forEach(itemChild -> addItemInTree(newNode, itemChild));
            rezNode = newNode;
        }
        return rezNode;
    } 
    
    /**
    * КОНТЕНТ: формирование контента компании
     * @param company
     * @return 
    */     
    @Override
    public List<BaseDict> makeGroupContent(Company company) {
        Rights docRights = getChildRights();
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент подразделения
        List<Department> departments = departmentFacade.findDetailItems(company);        
        departments.stream()
                .forEach(fl -> addDepartmentInCnt(fl, cnt, docRights)
        );        
        //загружаем в контент штатные единицы
        List<Staff> staffs = staffFacade.findStaffByCompany(company, null);
        staffs.stream().
                forEach(staff -> addStaffInCnt(staff, cnt, docRights)
        );
        return cnt;
    }
    
    /**
     * КОНТЕНТ: добавляет подразделение в контент
     * @param department
     * @param cnts
     * @param defChildRight
     * @return 
     */ 
    private void addDepartmentInCnt(BaseDict department, List<BaseDict> cnts, Rights defChildRight) {
        //Rights rights = makeRightChild(folder, defDocRight);
        //settingRightForChild(folder, rights); //сохраняем права к документам
        cnts.add(department);
    }

    /**
     * КОНТЕНТ: добавляет штатную единицу в контент
     * @param staff
     * @param cnts
     * @param defChildRight
     */ 
    public void addStaffInCnt(Staff staff, List<BaseDict> cnts, Rights defChildRight) {
        /*
        Rights rd = defDocRight;
        if (doc.isInherits() && doc.getAccess() != null) { //установлены специальные права и есть в базе данные по правам
            rd = (Rights) JAXB.unmarshal(new StringReader(doc.getAccess()), Rights.class); //Демаршаллинг прав из строки! 
        }
        doc.setRightItem(rd);
        doc.setRightMask(rightService.getAccessMask(doc.getState(), rd, getCurrentUser())); //получаем маску доступа для текущего пользователя  
        */
        cnts.add(staff);
    }    
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME; 
    }
     
    /**
     * Формирует число ссылок на company в связанных объектах 
     * @param company
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(Company company,  Map<String, Integer> rezult){
        rezult.put("Departaments", company.getDepartmentsList().size() - 1);
    }    
    
    /**
     * Проверка возможности удаления company
     * @param company
     */
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