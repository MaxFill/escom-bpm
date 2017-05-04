package com.maxfill.escom.beans.docs.docsTypes.docTypeGroups;

import com.maxfill.facade.DocTypeGroupsFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.utils.EscomBeanUtils;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Бин для Группы видов документов
 * @author mfilatov
 */
@Named
@ViewScoped
public class DocTypeGroupsBean extends BaseTreeBean<DocTypeGroups, DocTypeGroups>{
    private static final long serialVersionUID = -690060212424991825L;
    private static final String BEAN_NAME = "docTypeGroupsBean";    
    
    @EJB
    private DocTypeGroupsFacade itemsFacade;
    @EJB
    private DocTypeFacade docTypeFacade;
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME; 
    }
    
    @Override
    public DocTypeGroupsFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    public List<DocTypeGroups> getGroups(DocTypeGroups item) {
        return null;
    }
    
    /**
     * Формирует число ссылок на docTypeGroups в связанных объектах 
     * @param docTypeGroups
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(DocTypeGroups docTypeGroups,  Map<String, Integer> rezult){
        rezult.put("DocTypes", docTypeFacade.findItemByOwner(docTypeGroups).size());
    }    
    
    /**
     * Проверка возможности удаления docTypeGroups
     * @param docTypeGroups
     */
    @Override
    protected void checkAllowedDeleteItem(DocTypeGroups docTypeGroups, Set<String> errors){
        super.checkAllowedDeleteItem(docTypeGroups, errors);       
    }
    
    @Override
    public Class<DocTypeGroups> getItemClass() {
        return DocTypeGroups.class;
    }

    @Override
    public Class<DocTypeGroups> getOwnerClass() {
        return null;
    }
    
    @FacesConverter("docTypeGroupConvertor")
    public static class departmentConvertor implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {  
                 DocTypeGroupsBean bean = EscomBeanUtils.findBean("DocTypeGroupsBean", fc);
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
                return String.valueOf(((DocTypeGroups)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}
