package com.maxfill.services.favorites;

import com.maxfill.facade.FavoriteObjFacade;
import com.maxfill.model.favorites.FavoriteObj;
import com.maxfill.model.BaseDict;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class FavoriteServiceImpl implements FavoriteService{
    @EJB
    private FavoriteObjFacade favoriteFacade;
    
    @Override
    public boolean addInFavorites(BaseDict item, Metadates metadatesObj, User user){
        if (favoriteFacade.findFavoriteObj(item.getId(), metadatesObj, user).isEmpty()){
            FavoriteObj favorite = new FavoriteObj();
            favorite.setObjId(item.getId());
            favorite.setUserId(user);
            favorite.setMetadateObj(metadatesObj);
            favoriteFacade.create(favorite);
            user.getFavoriteObjList().add(favorite);            
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void delFromFavorites(BaseDict item, Metadates metadatesObj, User user){      
        FavoriteObj favorite = favoriteFacade.findFavoriteObj(item.getId(), metadatesObj, user).get(0);
        favoriteFacade.remove(favorite);
        user.getFavoriteObjList().remove(favorite);
    }
}
