
package com.maxfill.facade;

import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.facade.BaseFacade;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author mfilatov
 */
@Stateless
public class FavoriteObjFacade extends BaseFacade<FavoriteObj>{

    public FavoriteObjFacade() {
        super(FavoriteObj.class);
    }
    
    @Override
    public void remove(FavoriteObj entity){
        entity = getEntityManager().getReference(FavoriteObj.class, entity.getId());
        getEntityManager().remove(entity);
    }
    
    public List<FavoriteObj> findFavoriteObj(Integer itemId, Metadates metadatesObj, User user){
        getEntityManager().getEntityManagerFactory().getCache().evict(FavoriteObj.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<FavoriteObj> cq = builder.createQuery(FavoriteObj.class);
        Root<FavoriteObj> c = cq.from(FavoriteObj.class);        
        Predicate crit1 = builder.equal(c.get("objId"), itemId);
        Predicate crit2 = builder.equal(c.get("metadateObj"), metadatesObj);
        Predicate crit3 = builder.equal(c.get("userId"), user);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }
}
