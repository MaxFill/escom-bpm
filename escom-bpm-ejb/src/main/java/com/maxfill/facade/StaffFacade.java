
package com.maxfill.facade;

import com.maxfill.model.staffs.StaffModel;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.Staff_;
import com.maxfill.model.staffs.StaffLog;
import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.posts.Post;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.users.User;
import com.maxfill.model.users.User_;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.primefaces.model.TreeNode;

/**
 * 
 * @author mfilatov
 */
@Stateless
public class StaffFacade extends BaseDictFacade<Staff, Department, StaffLog> {
    protected static final Logger LOG = Logger.getLogger(StaffFacade.class.getName());
    
    @EJB
    private UserFacade userFacade;

    public StaffFacade() {
        super(Staff.class, StaffLog.class);
    }    
    
    @Override
    public String getFRM_NAME() {
        return DictObjectName.STAFF.toLowerCase();
    }
    
    @Override
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel baseDataModel){
        StaffModel model = (StaffModel) baseDataModel;
        String postName = model.getPostSearche().trim();
        if (StringUtils.isNotBlank(postName)){
            Join<Staff, Post> postJoin = root.join(Staff_.post);
            predicates.add(builder.like(postJoin.<String>get("name"), postName));            
        }
        String secondName = model.getSecondNameSearche();
        if (StringUtils.isNotBlank(secondName)){
            Join<Staff, User> userJoin = root.join(Staff_.employee);
            predicates.add(builder.like(userJoin.<String>get(User_.secondName), secondName));            
        }
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_STAFFS;
    }
    
    @Override
    public void pasteItem(Staff pasteItem, BaseDict target,  Set<String> errors){
        detectParentOwner(pasteItem, target);
        doPaste(pasteItem, errors);
    }
    
    @Override
    public boolean addItemToGroup(Staff staff, BaseDict group){ 
        //поскольку шт.ед. может быть только в одном подразделении, то выполняем перемещение
        moveItemToGroup(group, staff, null); 
        return true;
    }

    public void moveItemToGroup(BaseDict group, Staff staff, TreeNode sourceNode){
        detectParentOwner(staff, group);
        edit(staff);
    }
        
    @Override
    protected void detectParentOwner(Staff staff, BaseDict target){
        if (target instanceof Company){
            staff.setCompany((Company)target);
            staff.setOwner(null); //теперь нет связи с подразделением
        } else
            if (target instanceof Department){
                staff.setOwner((Department)target);
                staff.setCompany(null);
            }
    }
    
    /**
     * Создание новой штатной единицы
     * @param department
     * @param post
     * @param user
     * @return 
     */
    public Staff createStaff(Department department, Post post, User user){
        if (department == null || post == null || user == null){
            return null;
        }
        String name = post.getName() + " " + user.getName();
        Staff staff = createItem(department, userFacade.getAdmin());
        staff.setPost(post);
        staff.setEmployee(user);
        staff.setName(name);
        create(staff);
        LOG.log(Level.INFO, "Create staff = {0}", name);
        return staff; 
    }
    
    /**
     * Обновление штатной единицы
     * @param staff
     * @param department
     * @param post
     * @param user 
     */
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

    /**
     * Поиск штатных единиц по должности
     * @param post 
     * @return  
     */
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
    
    /**
     * Поиск штатных единиц по пользователю
     * @param user 
     * @return  
     */
    public List<Staff> findStaffByUser(User user){
        getEntityManager().getEntityManagerFactory().getCache().evict(Staff.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Staff> cq = builder.createQuery(Staff.class);
        Root<Staff> c = cq.from(Staff.class);        
        Predicate crit1 = builder.equal(c.get("employee"), user);
        cq.select(c).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /**
     * Поиск штатных единиц, принадлежащих компании и входящих в указанное подразделение
     * @param company
     * @param department
     * @return 
     */
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
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    /**
     * Процедура замены штатной единицы в связанных с ней объектах
     * @param oldItem
     * @param newItem
     * @return 
     */
    @Override
    public Map<String, Integer> replaceItem(Staff oldItem, Staff newItem) {
        Map<String, Integer> rezultMap = new HashMap<>();
        rezultMap.put("Documents", replaceStaffInDocs(oldItem, newItem));
        rezultMap.put("Users", replaceStaffInUsers(oldItem, newItem));
        return rezultMap;
    }

    /**
     * Замена штатной единицы в документах
     * @param oldItem
     * @param newItem
     * @return 
     */
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
    
    /**
     * Замена штатной единицы в пользователях
     * @param oldItem
     * @param newItem
     * @return 
     */
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
