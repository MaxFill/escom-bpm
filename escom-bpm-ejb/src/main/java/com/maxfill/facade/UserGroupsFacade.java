package com.maxfill.facade;

import com.maxfill.model.users.User;
import com.maxfill.model.users.User_;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.model.users.groups.UserGroupsLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Maxim
 */
@Stateless
public class UserGroupsFacade extends BaseDictFacade<UserGroups, UserGroups, UserGroupsLog> {
    protected static final Logger LOG = Logger.getLogger(UserGroups.class.getName());
    
    @EJB
    private UserFacade userFacade;

    public UserGroupsFacade() {
        super(UserGroups.class, UserGroupsLog.class);
    }
    
    /* Получение списка пользователей в группах */ 
    public List<User> findDetail(UserGroups group) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);   
        cq.select(root).distinct(true).where(root.get(User_.usersGroupsList).in(group)).orderBy(cb.asc(root.get(User_.secondName)));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();  
    }
  
    /* Получение списка групп root уровня без групп, являющихся ролями */
    @Override
    public List<UserGroups> findChilds(UserGroups parent){
        getEntityManager().getEntityManagerFactory().getCache().evict(UserGroups.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserGroups> cq = builder.createQuery(UserGroups.class);
        Root<UserGroups> c = cq.from(UserGroups.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.equal(c.get("parent"), parent));
        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("actual"), true));
        criteries.add(builder.equal(c.get("typeActualize"), 0));        

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    public List<UserGroups> findGroupsByType(Integer typeActualize){
        getEntityManager().getEntityManagerFactory().getCache().evict(UserGroups.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserGroups> cq = builder.createQuery(UserGroups.class);
        Root<UserGroups> c = cq.from(UserGroups.class);   
        List<Predicate> criteries = new ArrayList<>();

        criteries.add(builder.equal(c.get("deleted"), false));
        criteries.add(builder.equal(c.get("actual"), true));
        criteries.add(builder.equal(c.get("typeActualize"), typeActualize));        

        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);

        cq.select(c).where(builder.and(predicates));               
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();
    }
    
    @Override
    public String getFRM_NAME() {
        return UserGroups.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void create(UserGroups usersGroups) {
        getEntityManager().persist(usersGroups);
        List<User> usersListNew = usersGroups.getUsersList();
        for (User usersListNewUsers : usersListNew) {
            usersListNewUsers.getUsersGroupsList().add(usersGroups);
            usersListNewUsers = getEntityManager().merge(usersListNewUsers);
        }
    }
    
    @Override
    public void edit(UserGroups usersGroups) { 
        UserGroups persistentUsersGroups = getEntityManager().find(UserGroups.class, usersGroups.getId());
        List<User> usersListOld = persistentUsersGroups.getUsersList();
        List<User> usersListNew = usersGroups.getUsersList();
        List<User> attachedUsersListNew = new ArrayList<>();
        for (User usersListNewUsersToAttach : usersListNew) {
            usersListNewUsersToAttach = getEntityManager().getReference(usersListNewUsersToAttach.getClass(), usersListNewUsersToAttach.getId());
            attachedUsersListNew.add(usersListNewUsersToAttach);
        }
        usersListNew = attachedUsersListNew;
        usersGroups.setUsersList(usersListNew);
        usersGroups = getEntityManager().merge(usersGroups);
        for (User usersListOldUsers : usersListOld) {
            if (!usersListNew.contains(usersListOldUsers)) {
                usersListOldUsers.getUsersGroupsList().remove(usersGroups);
                usersListOldUsers = getEntityManager().merge(usersListOldUsers);
            }
        }
        for (User usersListNewUsers : usersListNew) {
            if (!usersListOld.contains(usersListNewUsers)) {
                usersListNewUsers.getUsersGroupsList().add(usersGroups);
                usersListNewUsers = getEntityManager().merge(usersListNewUsers);
            }
        }
    }  

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_GROUPS_USERS;
    }
 
    /* Ищет группу с указанным названием и если не найдена то создаёт новую  */
    public UserGroups onGetGroupByName(String groupName){
        if (StringUtils.isBlank(groupName)){
            return null;
        }
        for (UserGroups group : findAll()){
            if (Objects.equals(group.getName(), groupName)){
                return group;
            }
        }
        UserGroups group = createItem(userFacade.getAdmin());        
        group.setName(groupName);                
        create(group);        
        LOG.log(Level.INFO, "Create userGroups = {0}", groupName);
        return group;
    }

    @Override
    public void replaceItem(UserGroups oldItem, UserGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }        
}
