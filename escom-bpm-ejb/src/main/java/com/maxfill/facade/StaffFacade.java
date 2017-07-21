package com.maxfill.facade;

import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.Staff_;
import com.maxfill.model.staffs.StaffLog;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.posts.Post;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.users.User;
import com.maxfill.model.users.User_;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.model.staffs.StaffStates;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class StaffFacade extends BaseDictFacade<Staff, Department, StaffLog, StaffStates> {
    protected static final Logger LOG = Logger.getLogger(StaffFacade.class.getName());
    
    @EJB
    private UserFacade userFacade;

    public StaffFacade() {
        super(Staff.class, StaffLog.class, StaffStates.class);
    }
    
    @Override
    public String getFRM_NAME() {
        return DictObjectName.STAFF.toLowerCase();
    }
    
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
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_STAFFS;
    }   
        
    /* Создание новой штатной единицы  */
    public Staff createStaff(Department department, Post post, User user){
        if (department == null || post == null || user == null){
            return null;
        }
        String name = post.getName() + " " + user.getName();
        Staff staff = createItem(userFacade.getAdmin());
        staff.setOwner(department);
        staff.setPost(post);
        staff.setEmployee(user);
        staff.setName(name);
        create(staff);
        LOG.log(Level.INFO, "Create staff = {0}", name);
        return staff; 
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
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.isNotNull(c.get("employee"));
        Predicate crit2 = builder.isNotNull(c.get("post"));
        Predicate crit3 = builder.equal(c.get("actual"), true);
        Predicate crit4 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2, crit3, crit4));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Поиск штатных единиц по должности */
    public List<Staff> findStaffByPost(Post post){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("post"), post);
        cq.select(c).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Поиск штатных единиц по пользователю */
    public List<Staff> findStaffsByUser(User user){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("employee"), user);
        cq.select(c).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Поиск штатных единиц, принадлежащих компании и входящих в указанное подразделение */
    public List<Staff> findStaffByCompany(Company company, Department department){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("company"), company);
        Predicate crit2;
        if (department == null){
            crit2 = builder.isNull(c.get("owner"));   
        } else {
            crit2 = builder.equal(c.get("owner"), department);
        }
        Predicate crit3 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Поиск штатных единиц входящих в указанное подразделение */
    public List<Staff> findStaffByDepartment(Department department){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("owner"), department);        
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /* Процедура замены штатной единицы в связанных с ней объектах  */
    @Override
    public void replaceItem(Staff oldItem, Staff newItem) {
        Map<String, Integer> rezultMap = new HashMap<>();
        rezultMap.put("Documents", replaceStaffInDocs(oldItem, newItem));
        rezultMap.put("Users", replaceStaffInUsers(oldItem, newItem));        
    }

    /* Замена штатной единицы в документах */
    private int replaceStaffInDocs(Staff oldItem, Staff newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<Doc> update = builder.createCriteriaUpdate(Doc.class);    
        Root root = update.from(Doc.class);  
        update.set("manager", newItem);
        Predicate predicate = builder.equal(root.get("manager"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
    
    /* Замена штатной единицы в пользователях */
    private int replaceStaffInUsers(Staff oldItem, Staff newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder(); 
        CriteriaUpdate<User> update = builder.createCriteriaUpdate(User.class);    
        Root root = update.from(User.class);  
        update.set("staff", newItem);
        Predicate predicate = builder.equal(root.get("staff"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
    
}
