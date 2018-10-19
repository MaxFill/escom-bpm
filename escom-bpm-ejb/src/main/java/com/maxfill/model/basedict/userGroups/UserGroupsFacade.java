package com.maxfill.model.basedict.userGroups;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.core.rights.Right;
import com.maxfill.model.core.rights.Rights;
import com.maxfill.model.basedict.user.User;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictRights;
import com.maxfill.model.basedict.user.User_;
import com.maxfill.model.core.rights.Right_;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Stateless
public class UserGroupsFacade extends BaseDictFacade<UserGroups, UserGroups, UserGroupsLog, UserGroupsStates>{    

    public UserGroupsFacade() {
        super(UserGroups.class, UserGroupsLog.class, UserGroupsStates.class);
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
    public List<User> findDetails(UserGroups group, int first, int pageSize, String sortField, String sortOrder, User currentUser) {
        first = 0;
        pageSize = configuration.getMaxResultCount();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class); 
        
        List<Predicate> criteries = new ArrayList<>();
        criteries.add(cb.equal(root.get("deleted"), false));
        criteries.add(cb.equal(root.get("actual"), true));
        criteries.add(root.get(User_.usersGroupsList).in(group));
        Predicate[] predicates = new Predicate[criteries.size()];
        predicates = criteries.toArray(predicates);
        
        cq.select(root)
                .distinct(true)
                .where(predicates)
                .orderBy(cb.asc(root.get(User_.secondName)));
        
        TypedQuery<User> query = em.createQuery(cq);
        //query.setFirstResult(first);
        //query.setMaxResults(pageSize);
        return query.getResultStream().parallel()
                .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                .collect(Collectors.toList());
    }
  
    /* Получение списка групп root уровня нужного типа, например, групп, являющихся ролями */
    @Override
    public Stream<UserGroups> findActualChilds(UserGroups parent, User currentUser){
        em.getEntityManagerFactory().getCache().evict(UserGroups.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
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
        TypedQuery<UserGroups> query = em.createQuery(cq);       
        return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser));
    }
    
    public List<UserGroups> findGroupsByType(Integer typeActualize, User currentUser){
        em.getEntityManagerFactory().getCache().evict(UserGroups.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
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
        TypedQuery<UserGroups> query = em.createQuery(cq);       
        return query.getResultStream()      
                    .filter(item -> preloadCheckRightView((BaseDict) item, currentUser))
                    .collect(Collectors.toList());
    }

    protected void addPredicatesAndOrders(Root root, List<Predicate> predicates, CriteriaBuilder builder, Map<String, Object> addParams) {
        predicates.add(builder.equal(root.get("typeActualize"), DictRights.ACTUALISE_IN_GROUP)); 
        predicates.add(builder.notEqual(root.get("id"), 0));
    }        
    
    @Override
    public void create(UserGroups usersGroups) {
        super.create(usersGroups);        
        if (CollectionUtils.isNotEmpty(usersGroups.getUsersList())){
            usersGroups.getUsersList().forEach(user->{
                user.getUsersGroupsList().add(usersGroups);
                em.merge(user);
            });            
        }
    }
    
    @Override
    public void edit(UserGroups usersGroups) { 
        UserGroups persistentUsersGroups = em.find(UserGroups.class, usersGroups.getId());
                
        List<User> usersListOld = persistentUsersGroups.getUsersList();
        if (usersListOld == null){
            usersListOld = new ArrayList<>();    
        }
                
        List<User> usersListNew = usersGroups.getUsersList();
        if (usersListNew == null){
            usersListNew = new ArrayList<>();    
        }
        
        List<User> attachedUsersListNew = new ArrayList<>();
        for (User usersListNewUsersToAttach : usersListNew) {
            usersListNewUsersToAttach = em.getReference(usersListNewUsersToAttach.getClass(), usersListNewUsersToAttach.getId());
            attachedUsersListNew.add(usersListNewUsersToAttach);
        }
        usersListNew = attachedUsersListNew;
        usersGroups.setUsersList(usersListNew);
        
        usersGroups = em.merge(usersGroups);
        
        if (CollectionUtils.isNotEmpty(usersListOld) ){
            for (User usersListOldUsers : usersListOld) {
                if (!usersListNew.contains(usersListOldUsers)) {
                    usersListOldUsers.getUsersGroupsList().remove(usersGroups);
                    em.merge(usersListOldUsers);
                }
            }
            for (User usersListNewUsers : usersListNew) {
                if (!usersListOld.contains(usersListNewUsers)) {
                    usersListNewUsers.getUsersGroupsList().add(usersGroups);
                    em.merge(usersListNewUsers);
                }
            }
        }
    }  

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_GROUPS_USERS;
    }
 
    /* Ищет группу пользователей с указанным названием и если не найдена то создаёт новую  */
    public UserGroups onGetGroupByName(String groupName, User currentUser){
        if (StringUtils.isBlank(groupName)){
            return null;
        }
        for (UserGroups group : findAll(currentUser)){
            if (Objects.equals(group.getName(), groupName)){
                return group;
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", groupName);
        UserGroups group = createItem(userFacade.getAdmin(), null, null, params);               
        create(group);
        return group;
    }

    /**
     * Замена группы пользователей на другую в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(UserGroups oldItem, UserGroups newItem) {
        int count = replaceUserGroups(oldItem, newItem);
        count = count + replaceUserGroupInRights(oldItem, newItem);
        return count;
    }

    /**
     * Замена группы пользователей в правах доступа
     * @param oldItem
     * @param newItem
     * @return
     */
    public int replaceUserGroupInRights(UserGroups oldItem, UserGroups newItem){
        em.getEntityManagerFactory().getCache().evict(Right.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Right> update = builder.createCriteriaUpdate(Right.class);
        Root<Right> root = update.from(Right.class);
        update.set(Right_.objId, newItem.getId());
        Predicate crit1 = builder.equal(root.get(Right_.objType), 0);
        Predicate crit2 = builder.equal(root.get(Right_.objId), oldItem.getId());
        update.where(builder.and(crit1, crit2));
        Query query = em.createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Замена группы пользователей в группах
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceUserGroups(UserGroups oldItem, UserGroups newItem) {
        em.getEntityManagerFactory().getCache().evict(UserGroups.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<UserGroups> update = builder.createCriteriaUpdate(UserGroups.class);
        Root root = update.from(UserGroups.class);
        update.set(UserGroups_.parent, newItem);
        Predicate predicate = builder.equal(root.get(UserGroups_.parent), oldItem);
        update.where(predicate);
        Query query = em.createQuery(update);
        return query.executeUpdate();
    }
}
