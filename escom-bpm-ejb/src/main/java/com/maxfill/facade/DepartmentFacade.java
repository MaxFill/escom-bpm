
package com.maxfill.facade;

import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.departments.DepartamentLog;
import com.maxfill.model.departments.Department;
import com.maxfill.model.users.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Maxim
 */
@Stateless
public class DepartmentFacade extends BaseDictFacade<Department, Company, DepartamentLog> {
    protected static final Logger LOG = Logger.getLogger(DepartmentFacade.class.getName());

    @EJB
    private UserFacade userFacade;
    @EJB
    private StaffFacade staffFacade;
    
    public DepartmentFacade() {
        super(Department.class, DepartamentLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return Department.class.getSimpleName().toLowerCase();
    }  
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DEPARTAMENTS;
    }
       
    @Override
    public void pasteItem(Department pasteItem, BaseDict target , Set<String> errors){
        detectParentOwner(pasteItem, target);
        doCopyPasteChilds(pasteItem, errors);
        doCopyPasteDetails(pasteItem, errors);
        doPaste(pasteItem, errors);
    }

    /* Копирования дочерних объектов - подразделений */
    private void doCopyPasteChilds(Department department, Set<String> errors){
        department.getChildItems().stream().forEach(item -> pasteItem(item, department, errors));
    }
    
    /* Копирования подчинённых объектов - штатных единиц */
    private void doCopyPasteDetails(Department department, Set<String> errors) {
        department.getDetailItems().stream().forEach(staff -> staffFacade.pasteItem(staff, department, errors));
    }
    
    @Override
    protected void detectParentOwner(Department item, BaseDict target){
        if (target instanceof Company){
            item.setOwner((Company)target);
            item.setParent(null);
        } else
        if (target instanceof Department){
            item.setOwner(null);
            item.setParent((Department)target);
        }
    }
    
    public void moveGroupToGroup(BaseDict dropItem, Department dragItem) {
        detectParentOwner(dragItem, dropItem);
        edit(dragItem);
    }
    
    /**
     * Ищет подразделение с указанным названием в заданной компании и если не найдено, то создаёт новое.
     * @param company
     * @param departName
     * @return 
     */
    public Department onGetDepartamentByName(Company company, String departName){
        if (StringUtils.isBlank(departName) || company == null){        
            return null;
        } else {
            for(Department department : company.getDepartmentsList()){
                if (Objects.equals(department.getName(), departName)){
                    return department;
                }
            }
            Department department = createItem(company, userFacade.getAdmin());
            department.setName(departName);
            create(department);
            company.getDepartmentsList().add(department);
            LOG.log(Level.INFO, "Create department = {0}", departName);
            return department;
        }
    }

    @Override
    public Map<String, Integer> replaceItem(Department oldItem, Department newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Отбор всех подразделений, относящихся к указанной компании
     * @param company
     * @return 
     */
    public List<Department> findDepartmentByCompany(Company company){
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Department> cq = builder.createQuery(Department.class);
        Root<Department> c = cq.from(Department.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("owner"), company));

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<Department> q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
            
    /**
     * Возвращает подразделения, относящиеся к компании и находящиеся на верхнем уровне
     * @param owner
     * @return 
     */
    @Override
    public List<Department> findDetailItems(Company owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Department> cq = builder.createQuery(Department.class);
        Root<Department> c = cq.from(Department.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(c.get("parent")));
        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("actual"), true));
        if (owner != null){                    
            criteries.add(builder.equal(c.get("owner"), owner));
        }

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
}
