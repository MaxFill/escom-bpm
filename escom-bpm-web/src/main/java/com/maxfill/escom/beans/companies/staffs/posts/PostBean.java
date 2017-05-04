package com.maxfill.escom.beans.companies.staffs.posts;

import com.maxfill.facade.PostFacade;
import com.maxfill.model.posts.Post;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Должности
 * @author Maxim
 */
@Named
@ViewScoped
public class PostBean extends BaseExplBean<Post, Post>{
    private static final long serialVersionUID = -257932838724865134L;
    private static final String BEAN_NAME = "postBean";
    
    public PostBean() {    
    }          
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME; 
    }
        
    @Override
    public PostFacade getItemFacade() {
        return sessionBean.getPostFacade();
    }
    
    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }

    /**
     * Формирует число ссылок на объект в связанных объектах 
     * @param item
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(Post item,  Map<String, Integer> rezult){
        rezult.put("Staffs", sessionBean.getStaffFacade().findStaffByPost(item).size());
    }
    
    /**
     * Проверка возможности удаления Должности
     * @param item
     */
    @Override
    protected void checkAllowedDeleteItem(Post item, Set<String> errors){
        super.checkAllowedDeleteItem(item, errors);
        if (!sessionBean.getStaffFacade().findStaffByPost(item).isEmpty()){
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
