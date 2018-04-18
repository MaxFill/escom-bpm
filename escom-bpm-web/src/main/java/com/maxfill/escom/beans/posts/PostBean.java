package com.maxfill.escom.beans.posts;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.PostFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.facade.StaffFacade;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Должности" */
@Named
@SessionScoped
public class PostBean extends BaseTableBean<Post, Post>{
    private static final long serialVersionUID = -257932838724865134L;
    
    @EJB
    private PostFacade itemFacade;
    @EJB
    private StaffFacade staffFacade;    
        
    @Override
    public PostFacade getFacade() {
        return itemFacade;
    }
    
    @Override
    public BaseTableBean getOwnerBean() {
        return null;
    }

    @Override
    public void doGetCountUsesItem(Post item,  Map<String, Integer> rezult){
        rezult.put("Staffs", staffFacade.findStaffByPost(item).size());
    }
    
    @Override
    protected void checkAllowedDeleteItem(Post item, Set<String> errors){
        super.checkAllowedDeleteItem(item, errors);
        if (!staffFacade.findStaffByPost(item).isEmpty()){
            Object[] messageParameters = new Object[]{item.getName()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("PostUsedInStaffs"), messageParameters);
            errors.add(error);
        }
    }    

    @Override
    public BaseTableBean getDetailBean() {
        return null;
    }

    @Override
    public List<Post> getGroups(Post item) {
        return null;
    }

    @Override
    public Class<Post> getOwnerClass() {
        return null;
    }

}
