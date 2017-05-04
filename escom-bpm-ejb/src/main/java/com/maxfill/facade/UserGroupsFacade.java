package com.maxfill.facade;

import com.maxfill.model.BaseDataModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.User;
import com.maxfill.model.users.User_;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.model.users.groups.UserGroupsLog;
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
    
    /**
     * Получение списка пользователей в группах
     * @param group
     * @return 
     */ 
    public List<User> findDetail(UserGroups group) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);   
        cq.select(root).distinct(true).where(root.get(User_.usersGroupsList).in(group)).orderBy(cb.asc(root.get(User_.secondName)));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList();  
    }

    
    @Override
    public String getFRM_NAME() {
        return UserGroups.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void pasteItem(UserGroups pasteItem, BaseDict target, Set<String> errors){
        pasteItem.setParent((UserGroups)target);
        doPaste(pasteItem, errors);        
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
    protected void addJoinPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, BaseDataModel model) {
        
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_GROUPS_USERS;
    }
 
    /**
     * Ищет группу с указанным названием и если не найдена то создаёт новую
     * @param groupName
     * @return 
     */
    public UserGroups onGetGroupByName(String groupName){
        if (StringUtils.isBlank(groupName)){
            return null;
        }
        for (UserGroups group : findAll()){
            if (Objects.equals(group.getName(), groupName)){
                return group;
            }
        }
        UserGroups group = createItem(null, userFacade.getAdmin());        
        group.setName(groupName);                
        create(group);        
        LOG.log(Level.INFO, "Create userGroups = {0}", groupName);
        return group;
    }

    @Override
    public Map<String, Integer> replaceItem(UserGroups oldItem, UserGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
