package com.maxfill.facade;

import com.maxfill.dictionary.DictRights;
import com.maxfill.model.rights.Right;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.states.State;
import com.maxfill.model.users.User;
import com.maxfill.model.users.groups.UserGroups;
import com.maxfill.utils.ItemUtils;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Stateless
public class RightFacade extends BaseFacade<Right> {           
    public RightFacade() {
        super(Right.class);
    }
    
    @Override
    public void remove(Right entity){
        entity = getEntityManager().getReference(Right.class, entity.getId());
        getEntityManager().remove(entity);
    }
    
    /* Отбирает из базы дефолтные права объекта */
    private List<Right> findDefaultRight(Metadates objLink){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Right> cq = builder.createQuery(Right.class);
        Root<Right> root = cq.from(Right.class);        
        Predicate crit1 = builder.equal(root.get("objLink"), objLink);
        cq.select(root).where(builder.and(crit1));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }    
    
    /* Отбирает из базы дефолтные права объекта для определённого состояния */
    public List<Right> findDefaultRightState(Metadates objLink, State state ){
        getEntityManager().getEntityManagerFactory().getCache().evict(Right.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Right> cq = builder.createQuery(Right.class);
        Root<Right> c = cq.from(Right.class);        
        Predicate crit1 = builder.equal(c.get("objLink"), objLink);
        Predicate crit2 = builder.equal(c.get("state"), state);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    } 
    
    /* Получение дефольных прав объекта */
    public Rights getObjectDefaultRights(Metadates objLink){
        List<Right> right = findDefaultRight(objLink);
        return new Rights(right);
    }
    
    /* Подготовка прав для визуализации на форме */
    public void prepareRightsForView(List<Right> sourceRights){
        String accessorName;
        for (Right rg: sourceRights){
            switch (rg.getObjType()){
                case (DictRights.TYPE_GROUP):{
                    rg.setIcon("folder_open20");
                    break;
                }
                case (DictRights.TYPE_ROLE):{
                    rg.setIcon("roles20");
                    break;
                }
                case (DictRights.TYPE_USER):{
                    rg.setIcon("user20");
                    break;
                }
            }
            //хардкодные проверки
            if (rg.getObjId() == 0 && rg.getObjType() == DictRights.TYPE_GROUP){
               accessorName = ItemUtils.getBandleLabel("All"); //ToDo в bundle!
            } else
                if (rg.getObjId() == 2 && rg.getObjType() == DictRights.TYPE_GROUP){
                  accessorName = ItemUtils.getBandleLabel("Administrators"); 
                } else
                    if (rg.getObjId() == 1 && rg.getObjType() == DictRights.TYPE_USER){
                        accessorName = ItemUtils.getBandleLabel("Administrator"); 
                      } else{ //тогда ищем названия в базе
                            accessorName = getAccessName(rg);
                        }
            rg.setName(accessorName);
        }
    }
    
    /* Получение прав по id группы пользователей */
    public List<Rights> findRightsByGroupId(Integer groupId){
        getEntityManager().getEntityManagerFactory().getCache().evict(Right.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Right> cq = builder.createQuery(Right.class);
        Root<Right> c = cq.from(Right.class);        
        Predicate crit1 = builder.equal(c.get("objType"), 0);
        Predicate crit2 = builder.equal(c.get("objId"), groupId);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Получение прав по id пользователя */
    public List<Rights> findRightsByUserId(Integer userId){
        getEntityManager().getEntityManagerFactory().getCache().evict(Right.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Right> cq = builder.createQuery(Right.class);
        Root<Right> c = cq.from(Right.class);        
        Predicate crit1 = builder.equal(c.get("objType"), 1);
        Predicate crit2 = builder.equal(c.get("objId"), userId);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
    
    /* Возвращает название пользователя или группы для которого назначаются права */ 
    private String getAccessName(Right rg) {
        String accessorName = "";
        if (rg.getObjType() == 1 ){
            accessorName = findUserById(rg.getObjId()).getShortFIO();          
        } else {
            accessorName = findGroupUserById(rg.getObjId()).getName();
        }
        return accessorName;
    } 
    
    /* Получение пользователя по ID для прав доступа */
    public User findUserById(Integer userId){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<User> cq = builder.createQuery(User.class);
        Root<User> c = cq.from(User.class);
        Predicate crit1 = builder.equal(c.get("id"), userId);        
        cq.select(c).where(builder.and(crit1));        
        Query q = getEntityManager().createQuery(cq);       
        return (User)q.getSingleResult();
    }
    
    /* Получение группы пользователей по ID для прав доступа */
    public UserGroups findGroupUserById(Integer groupId){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<UserGroups> cq = builder.createQuery(UserGroups.class);
        Root<UserGroups> root = cq.from(UserGroups.class);
        Predicate crit1 = builder.equal(root.get("id"), groupId);        
        cq.select(root).where(builder.and(crit1));        
        Query q = getEntityManager().createQuery(cq);       
        return (UserGroups)q.getSingleResult();
    }
}
