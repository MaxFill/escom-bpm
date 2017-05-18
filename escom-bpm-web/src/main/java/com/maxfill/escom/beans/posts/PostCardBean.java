package com.maxfill.escom.beans.posts;

import com.maxfill.facade.PostFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictObjectName;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;


/**
 * Должности
 * @author Maxim
 */
@Named
@ViewScoped
public class PostCardBean extends BaseCardBean<Post>{
    private static final long serialVersionUID = -6399475562664755663L;
    
    @EJB
    private PostFacade itemsFacade;
        
    @Override
    public PostFacade getItemFacade() {
        return itemsFacade;
    }        

    @Override
    protected void afterCreateItem(Post item) {        
    }

    @Override
    public Class<Post> getItemClass() {
        return Post.class;
    }
}
