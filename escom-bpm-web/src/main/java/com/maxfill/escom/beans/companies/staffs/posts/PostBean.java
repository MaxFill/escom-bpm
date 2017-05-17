package com.maxfill.escom.beans.companies.staffs.posts;

import com.maxfill.facade.PostFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.StaffFacade;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
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
public class PostBean extends BaseExplBean<Post, Post>{
    private static final long serialVersionUID = -257932838724865134L;
    
    @EJB
    private PostFacade itemFacade;
    @EJB
    private StaffFacade staffFacade;    
        
    @Override
    public PostFacade getItemFacade() {
        return itemFacade;
    }
    
    @Override
    public BaseExplBean getOwnerBean() {
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
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("PostUsedInStaffs"), messageParameters);
            errors.add(error);
        }
    }    

    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

    @Override
    public List<Post> getGroups(Post item) {
        return null;
    }

    @Override
    public Class<Post> getItemClass() {
        return Post.class;
    }

    @Override
    public Class<Post> getOwnerClass() {
        return null;
    }
    
    @FacesConverter("postConverter")
    public static class PostConverter implements Converter {   
        
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {  
                PostBean bean = EscomBeanUtils.findBean("postBean", fc);
                Object searcheObj = bean.getItemFacade().find(Integer.parseInt(value));
                return searcheObj;
             } catch(NumberFormatException e) {
                 throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
             }
         }
         else {
             return null;
         }
        }

        @Override
        public String getAsString(FacesContext fc, UIComponent uic, Object object) {
            if(object != null) {
                return String.valueOf(((Post)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}
