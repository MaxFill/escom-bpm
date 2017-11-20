package com.maxfill.facade;

import com.maxfill.model.companies.Company;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.BaseDict;
import com.maxfill.model.departments.DepartamentLog;
import com.maxfill.model.departments.Department;
import com.maxfill.model.departments.DepartmentStates;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.services.numerators.department.DepartmentNumeratorService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;

@Stateless
public class DepartmentFacade extends BaseDictFacade<Department, Company, DepartamentLog, DepartmentStates> {

    @EJB
    private UserFacade userFacade;
    @EJB
    private DepartmentNumeratorService departmentNumeratorService;
        
    public DepartmentFacade() {
        super(Department.class, DepartamentLog.class, DepartmentStates.class);
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
            for(Department department : company.getDetailItems()){
                if (Objects.equals(department.getName(), departName)){
                    return department;
                }
            }
            Map<String, Object> params = new HashMap<>();
            params.put("name", departName);
            Department department = createItem(userFacade.getAdmin(), company, params);            
            create(department);
            company.getDetailItems().add(department);
            return department;
        }
    }

    /* Определяет owner и parent для объекта  */
    @Override
    public void detectParentOwner(Department item, BaseDict target){
        if (target instanceof Company){
            item.setOwner((Company)target);
            item.setParent(null);
        } else
        if (target instanceof Department){
            item.setOwner(null);
            item.setParent((Department)target);
        }
    }
    
    @Override
    public void replaceItem(Department oldItem, Department newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /* Отбор всех подразделений, относящихся к указанной компании */
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
    
    /* Отбор дочерних подразделений */
    public List<Department> findChildDepartments(Department department){
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Department> cq = builder.createQuery(Department.class);
        Root<Department> c = cq.from(Department.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("parent"), department));

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<Department> q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Возвращает подразделения, относящиеся к компании и находящиеся на верхнем уровне  */
    @Override
    public List<Department> findActualDetailItems(Company owner){
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
    
    @Override
    public void setSpecAtrForNewItem(Department department, Map<String, Object> params) {
        makeCode(department);
    }
    
    /* Формирование кода подразделения  */
    public void makeCode(Department department){        
        NumeratorPattern numeratorPattern = getMetadatesObj().getNumPattern();
        String number = departmentNumeratorService.doRegistrNumber(department, numeratorPattern, null, new Date());
        Company company = findCompany(department);
        StringBuilder sb = new StringBuilder();
        sb.append(company.getCode()).append(SysParams.CODE_SEPARATOR).append(number);
        department.setCode(sb.toString());
    }
}
