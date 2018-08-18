package com.maxfill.escom.beans.posts;

import com.maxfill.model.posts.PostFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.escom.beans.core.BaseCardBean;

import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/**
 * Контролер карточки "Должность"
 */
@Named
@ViewScoped
public class PostCardBean extends BaseCardBean<Post>{
    private static final long serialVersionUID = -6399475562664755663L;
    
    @EJB
    private PostFacade itemsFacade;
        
    @Override
    public PostFacade getFacade() {        
        return itemsFacade;
    }

}
