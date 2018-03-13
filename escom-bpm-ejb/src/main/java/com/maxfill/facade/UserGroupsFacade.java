package com.maxfill.facade;

import com.maxfill.model.BaseDict;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import com.maxfill.model.users.User_;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictRights;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.model.users.groups.UserGroupsLog;
import com.maxfill.model.users.groups.UserGroupsStates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class UserGroupsFacade extends BaseDictFacade<UserGroups, UserGroups, UserGroupsLog, UserGroupsStates> {
    
    @EJB
    private UserFacade userFacade;

    public UserGroupsFacade() {
        super(UserGroups.class, UserGroupsLog.class, UserGroupsStates.class);
    }

    @Override
    public Class<UserGroups> getItemClass() {
        return UserGroups.class;
    }

    /* Получение прав доступа для иерархического справочника */
    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getParent() != null) {
            return getRightItem(item.getParent(), user); //получаем права от родительской группы
        }

        return getDefaultRights(item);
    }

    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((UserGroups) item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return userFacade.getDefaultRights();
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
  
    /* Получение списка групп root уровня нужного типа, например, групп, являющихся ролями */
    @Override
    public List<UserGroups> findActualChilds(UserGroups parent){
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

    protected void addPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, Map<String, Object> addParams) {
        predicates.add(builder.equal(root.get("typeActualize"), DictRights.ACTUALISE_IN_GROUP)); 
        predicates.add(builder.notEqual(root.get("id"), 0));
    }    
    
    @Override
    public String getFRM_NAME() {
        return UserGroups.class.getSimpleName().toLowerCase();
    }
    
    @Override
    public void create(UserGroups usersGroups) {
        super.create(usersGroups);        
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
        Map<String, Object> params = new HashMap<>();
        params.put("name", groupName);
        UserGroups group = createItem(userFacade.getAdmin(), null, params);               
        create(group);
        return group;
    }

    @Override
    public void replaceItem(UserGroups oldItem, UserGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }        
}
