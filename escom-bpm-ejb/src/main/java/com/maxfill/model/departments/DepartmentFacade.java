package com.maxfill.model.departments;

import com.maxfill.model.companies.CompanyFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.staffs.StaffFacade;
import com.maxfill.model.companies.Company;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.BaseDict;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.Staff_;
import com.maxfill.model.users.User;
import com.maxfill.services.numerators.department.DepartmentNumeratorService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import org.apache.commons.lang.StringUtils;

@Stateless
public class DepartmentFacade extends BaseDictFacade<Department, Company, DepartamentLog, DepartmentStates>{

    @EJB
    private DepartmentNumeratorService departmentNumeratorService;
    @EJB
    private CompanyFacade companyFacade;
    @EJB
    private StaffFacade staffFacade;

    public DepartmentFacade() {
        super(Department.class, DepartamentLog.class, DepartmentStates.class);
    }

    @Override
    public List<BaseDict> findDetailItems(Department owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);
        Predicate crit = builder.equal(c.get(Staff_.owner), owner);
        cq.select(c).where(builder.and(crit));
        cq.orderBy(builder.asc(c.get("name")));
        Query query = getEntityManager().createQuery(cq);
        return query.getResultList();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DEPARTAMENTS;
    }

    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getParent() != null) {
            return getRightItem(item.getParent(), user); //получаем права от родительского подразделения
        }

        if (item.getOwner() != null) {
            Rights childRight = companyFacade.getRightForChild(item.getOwner()); //получаем права из спец.прав
            if (childRight != null){
                return childRight;
            }
        }

        return getDefaultRights(item);
    }

    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((Department) item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        if (item.getOwner() != null) {
            return getRightForChild(item.getOwner()); //получаем права от владельца
        }

        return staffFacade.getDefaultRights();
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
            Department department = createItem(userFacade.getAdmin(), null, company, params);            
            create(department);
            company.getDetailItems().add(department);
            return department;
        }
    }

    /* Определяет owner и parent для объекта  */
    @Override
    public void detectParentOwner(Department item, BaseDict parent, BaseDict target){
        if (target instanceof Company){
            item.setOwner((Company)target);
            item.setParent(null);
        } else
        if (target instanceof Department){
            item.setOwner(null);
            item.setParent((Department)target);
        } else {
            super.detectParentOwner(item, parent, target);
        }
    }

    /**
     * Отбор всех подразделений, относящихся к указанной компании, кроме удалённых в корзину
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
     * Отбор дочерних подразделений, кроме удалённых в корзину
     * @param department
     * @return
     */
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

    /**
     * Отбирает подразделения, относящиеся к компании и находящиеся на верхнем уровне
     * @param owner
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param currentUser
     * @return
     */
    @Override
    public List<Department> findActualDetailItems(Company owner, int first, int pageSize, String sortField, String sortOrder, User currentUser){
        first = 0;
        pageSize = configuration.getMaxResultCount();
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Department> cq = builder.createQuery(Department.class);
        Root<Department> root = cq.from(Department.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(root.get("parent")));
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get("actual"), true));
        if (owner != null){                    
            criteries.add(builder.equal(root.get("owner"), owner));
        }

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(root).where(builder.and(predicates));        
        TypedQuery<Department> query = getEntityManager().createQuery(cq);       
        query.setFirstResult(first);
        query.setMaxResults(pageSize);
        return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }
    
    @Override
    public Long findCountActualDetails(Company owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(Department.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = builder.createQuery(Long.class);
        Root<Department> root = cq.from(Department.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.isNull(root.get("parent")));
        criteries.add(builder.equal(root.get("deleted"), false));
        criteries.add(builder.equal(root.get("actual"), true));
        if (owner != null){                    
            criteries.add(builder.equal(root.get("owner"), owner));
        }

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(builder.count(root)).where(builder.and(predicates));               

        Query query = getEntityManager().createQuery(cq);  
        return (Long) query.getSingleResult();
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

    /**
     * Замена подразделения на другое в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(Department oldItem, Department newItem) {
        int count = replaceDepartments(oldItem, newItem);                   // замена в подразделениях
        count = count + replaceDepartamentsInStaffs(oldItem, newItem);  // замена в штатных единицах
        return count;
    }

    /**
     * Выполняет замену подразделения в подразделениях
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceDepartments(Department oldItem, Department newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Department> update = builder.createCriteriaUpdate(Department.class);
        Root root = update.from(Department.class);
        update.set(Department_.parent, newItem);
        Predicate predicate = builder.equal(root.get(Department_.parent), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Выполняет замены подразделения в штатных единицах
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceDepartamentsInStaffs(Department oldItem, Department newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Staff> update = builder.createCriteriaUpdate(Staff.class);
        Root root = update.from(Staff.class);
        update.set(Staff_.owner, newItem);
        Predicate predicate = builder.equal(root.get(Staff_.owner), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
