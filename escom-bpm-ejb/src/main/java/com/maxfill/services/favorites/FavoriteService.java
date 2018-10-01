package com.maxfill.services.favorites;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.core.metadates.Metadates;
import com.maxfill.model.basedict.user.User;

public interface FavoriteService {
    public boolean addInFavorites(BaseDict item, Metadates metadatesObj, User user);
    public void delFromFavorites(BaseDict item, Metadates metadatesObj, User user);
}
