package com.maxfill.services.favorites;

import com.maxfill.model.core.favorites.FavoriteObj;
import com.maxfill.facade.BaseFacade;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.basedict.user.User;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 */
@Stateless
public class FavoriteObjFacade extends BaseFacade<FavoriteObj>{

    public FavoriteObjFacade() {
        super(FavoriteObj.class);
    }    
    
    public List<FavoriteObj> findFavoriteObj(Integer itemId, Metadates metadatesObj, User user){
        em.getEntityManagerFactory().getCache().evict(FavoriteObj.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<FavoriteObj> cq = builder.createQuery(FavoriteObj.class);
        Root<FavoriteObj> c = cq.from(FavoriteObj.class);        
        Predicate crit1 = builder.equal(c.get("objId"), itemId);
        Predicate crit2 = builder.equal(c.get("metadateObj"), metadatesObj);
        Predicate crit3 = builder.equal(c.get("userId"), user);
        cq.select(c).where(builder.and(crit1, crit2, crit3));
        Query q = em.createQuery(cq);       
        return q.getResultList(); 
    }
}
