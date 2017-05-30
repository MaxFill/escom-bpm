package com.maxfill.services.favorites;

import com.maxfill.model.BaseDict;
import com.maxfill.model.metadates.Metadates;
import com.maxfill.model.users.User;

public interface FavoriteService {
    public boolean addInFavorites(BaseDict item, Metadates metadatesObj, User user);
    public void delFromFavorites(BaseDict item, Metadates metadatesObj, User user);
}
