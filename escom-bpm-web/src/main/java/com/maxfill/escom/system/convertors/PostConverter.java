package com.maxfill.escom.system.convertors;

import com.maxfill.model.posts.Post;

import javax.faces.convert.FacesConverter;

@FacesConverter("postConverter")
public class PostConverter extends BaseBeanConvertor<Post>{
    @Override
    protected String getBeanName() {
        return "postBean";
    }
}
