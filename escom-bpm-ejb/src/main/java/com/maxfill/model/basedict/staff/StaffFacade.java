package com.maxfill.model.basedict.staff;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.company.CompanyFacade;
import com.maxfill.model.basedict.department.DepartmentFacade;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.company.Company;
import com.maxfill.model.basedict.department.Department;
import com.maxfill.model.basedict.post.Post;
import com.maxfill.model.basedict.user.User;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.user.User_;
import com.maxfill.services.worktime.WorkTimeFacade;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class StaffFacade extends BaseDictFacade<Staff, Department, StaffLog, StaffStates>{
    
    @EJB
    private CompanyFacade companyFacade;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private WorkTimeFacade workTimeFacade;

    public StaffFacade() {
        super(Staff.class, StaffLog.class, StaffStates.class);
    }

    /* Дополнения при выполнении поиска через форму поиска */
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, Map<String, Object> addParams){
        String postName = (String) addParams.get("postName");
        if (StringUtils.isNotBlank(postName)){
            Join<Staff, Post> postJoin = root.join(Staff_.post);
            predicates.add(builder.like(postJoin.<String>get("name"), postName));
        }
        String secondName = (String) addParams.get("secondName");
        if (StringUtils.isNotBlank(secondName)){
            Join<Staff, User> userJoin = root.join(Staff_.employee);
            predicates.add(builder.like(userJoin.<String>get(User_.secondName), secondName));
        }
    }

    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        //если sataff относится к подразделению
        if (item.getOwner() != null) {
            Rights rights = departmentFacade.getRightForChild(item.getOwner()); //получаем права из спец.прав подразделения
            if (rights != null){
                return rights;
            }
        } else {
            //если staff относится напрямую к компании
            Staff staff = (Staff) item;
            Company company = staff.getCompany();
            if(company != null) {
                Rights rights = companyFacade.getRightForChild(company); //получаем права из спец.прав компании
                if (rights != null){
                    return rights;
                }
            }
        }
        return getDefaultRights(item);
    }

    @Override
    protected Stream<Staff> filtrationTrashResult(Stream<Staff> trashes){
        return trashes
                .filter(item ->
                        (item.getCompany() == null || (item.getCompany() != null && !item.getCompany().isDeleted())) &&
                        (item.getOwner() == null || (item.getOwner() != null && !item.getOwner().isDeleted()))
                );                
    }
       
    /* Создание новой штатной единицы  */
    public Staff createStaff(Department department, Post post, User user){
        if (department == null || post == null || user == null){
            return null;
        }
        String name = post.getName() + " " + user.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        Staff staff = createItem(userFacade.getAdmin(), null, department, params);
        staff.setPost(post);
        staff.setEmployee(user);         
        create(staff);
        return staff; 
    }

    @Override
    public void create(Staff staff) {
        if (staff.getCompany() == null && staff.getOwner() != null){
            staff.setCompany(staff.getOwner().getCompany());
        }
        super.create(staff); 
    }

    @Override
    public void edit(Staff staff) {
        if (staff.getCompany() == null && staff.getOwner() != null){
            staff.setCompany(staff.getOwner().getCompany());
        }
        super.edit(staff);
    }    
    
    /**
     * Действие перед физическим удалением объекта
     * @param staff 
     */
    @Override
    protected void beforeRemoveItem(Staff staff){ 
        CriteriaBuilder builder = em.getCriteriaBuilder(); 
        CriteriaUpdate<User> update = builder.createCriteriaUpdate(User.class);    
        Root root = update.from(User.class);
        Expression<Staff> nullStaff = builder.nullLiteral(itemClass);
        update.set(User_.staff, nullStaff);
        Predicate predicate = builder.equal(root.get(User_.staff), staff);
        update.where(predicate);
        Query query = em.createQuery(update);
        query.executeUpdate();
        
        workTimeFacade.removeWorkTimeByStaff(staff);
    }
        
    @Override
    public void detectParentOwner(Staff staff, BaseDict parent, BaseDict target){
        if (target instanceof Company){
            staff.setCompany((Company)target);
            staff.setOwner(null); //теперь нет связи с подразделением
        } else
            if (target instanceof Department){
                staff.setOwner((Department)target);
                staff.setCompany(null);
            }
    }
    
    /* Обновление штатной единицы */
    public void updateStaff(Staff staff, Department department, Post post, User user){
        boolean isChange = false;
        if (department != null && !Objects.equals(department, staff.getOwner())){
            staff.setOwner(department);
            isChange = true;
        }
        if (post != null && !Objects.equals(post, staff.getPost())){
            staff.setPost(post);
            isChange = true;
        }
        if (user != null && !Objects.equals(user, staff.getEmployee())){
            staff.setEmployee(user);
            isChange = true;
        }
        if (isChange){
            edit(staff);
        }
    }
    
    /* Возвращает список занятых (не вакантных) штатных единиц с сотрудниками и должностями */
    public List<Staff> findActualStaff(){
        em.getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.isNotNull(c.get("employee"));
        Predicate crit2 = builder.isNotNull(c.get("post"));
        Predicate crit3 = builder.equal(c.get("actual"), true);
        Predicate crit4 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2, crit3, crit4));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = em.createQuery(cq);
        List<Staff> result = q.getResultList(); 
        return result;
    }
    
    /* Поиск штатных единиц по должности */
    public List<Staff> findStaffByPost(Post post){
        em.getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("post"), post);
        cq.select(c).where(builder.and(crit1));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = em.createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Поиск штатных единиц по пользователю */
    public List<Staff> findStaffsByUser(User user){
        em.getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("employee"), user);
        cq.select(c).where(builder.and(crit1));
        Query q = em.createQuery(cq);       
        return q.getResultList(); 
    }

    /**
     * Возвращает штатную eдиницу указанного пользователя
     * @param user
     * @return
     */
    public Staff findStaffByUser(User user){        
        List<Staff> staffs = findStaffsByUser(user);
        return staffs.stream().findFirst().orElse(null);       
    }
    
    /**
     * Отбор актуальных штатных единиц, входящих в компанию
     * @param company
     * @param currentUser
     * @return
     */
    public List<Staff> findActualStaffByCompany(Company company, User currentUser){
        em.getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("company"), company);
        Predicate crit2 = builder.equal(c.get("actual"), true);
        Predicate crit3 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<Staff> query = em.createQuery(cq);       
        return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }
    
    /**
     * Отбор штатных единиц (кроме удалённых в корзину), не входящих в подразделения, а напрямую принадлежащих компании 
     * @param company
     * @param currentUser
     * @return
     */
    public List<Staff> findStaffInOnlyCompany(Company company, User currentUser){
        em.getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("company"), company);
        Predicate crit2 = builder.isNull(c.get("owner"));   
        Predicate crit3 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        cq.orderBy(builder.asc(c.get("name")));
        TypedQuery<Staff> query = em.createQuery(cq);       
        return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }

    /**
     * Отбор штатных единиц (кроме удалённых в корзину), входящих в указанное подразделение
     * @param department
     * @return
     */
    public List<Staff> findStaffByDepartment(Department department){
        em.getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get(Staff_.owner), department);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = em.createQuery(cq);       
        return q.getResultList();
    }

    /**
     * Возвращает компанию, в которой числится staff
     * @param staff
     * @return 
     */
    public Company findCompanyForStaff(Staff staff){
        if (staff == null) return null;
        if (staff.getCompany() != null && staff.getOwner() == null){
            return staff.getCompany();
        }
        Department dept = staff.getOwner();
        if (dept != null){
            return dept.getCompany();
        }
        return null;
    }
    
    /* ***  ПРОЧЕЕ * *** */
    
    /**
     * Замена штатной единицы в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(Staff oldItem, Staff newItem) {
        int count = 0;
        return count;
    }
    
    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_STAFFS;
    }   
}
