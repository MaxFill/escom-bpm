
package com.maxfill.facade;

import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.departments.DepartamentLog;
import com.maxfill.model.departments.Department;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    public DepartmentFacade() {
        super(Department.class, DepartamentLog.class);
    }

    @Override
    public String getFRM_NAME() {
        return Department.class.getSimpleName().toLowerCase();
    }  

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DEPARTAMENTS;
    }     
    
    /* Ищет подразделение с указанным названием в заданной компании и если не найдено, то создаёт новое.  */
    public Department onGetDepartamentByName(Company company, String departName){
        if (StringUtils.isBlank(departName) || company == null){        
            return null;
        } else {
            for(Department department : company.getDepartmentsList()){
                if (Objects.equals(department.getName(), departName)){
                    return department;
                }
            }
            Department department = createItem(userFacade.getAdmin());
            department.setName(departName);
            department.setOwner(company);
            create(department);
            company.getDepartmentsList().add(department);
            LOG.log(Level.INFO, "Create department = {0}", departName);
            return department;
        }
    }

    @Override
    public void replaceItem(Department oldItem, Department newItem) {
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
