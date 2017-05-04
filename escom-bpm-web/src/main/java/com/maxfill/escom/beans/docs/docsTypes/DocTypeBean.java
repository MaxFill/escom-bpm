package com.maxfill.escom.beans.docs.docsTypes;

import com.maxfill.facade.DocTypeFacade;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.DocFacade;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.facade.FoldersFacade;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.EscomUtils;

import javax.ejb.EJB;
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
 * Виды документов
 * @author mfilatov
 */
@Named
@ViewScoped
public class DocTypeBean extends BaseExplBeanGroups<DocType, DocTypeGroups>{
    private static final long serialVersionUID = -4625860665708197046L;
    private static final String BEAN_NAME = "docTypeBean";    
            
    @EJB 
    private DocTypeFacade itemsFacade;    
    @EJB 
    private DocFacade docFacade;
    @EJB 
    private FoldersFacade foldersFacade;
        
    public DocTypeBean() {
    }
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME; 
    }
    
    @Override
    public DocTypeFacade getItemFacade() {
        return itemsFacade;
    }   

    @Override
    public BaseExplBean getDetailBean() {
        return null;
    }

    @Override
    public List<DocTypeGroups> getGroups(DocType item) {
        List<DocTypeGroups> groups = null;
        if (item.getOwner() != null){
            groups = item.getOwner().getChildItems();
        }
        return groups;
    }
    
    /**
     * Проверка возможности удаления Вида документа
     * @param docType
     */
    @Override
    protected void checkAllowedDeleteItem(DocType docType, Set<String> errors){
        super.checkAllowedDeleteItem(docType, errors);
        if (!docFacade.findDocsByDocTyper(docType).isEmpty()){
            Object[] messageParameters = new Object[]{docType.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("DocTypeUsedInDocs"), messageParameters);
            errors.add(error);
        }
        if (!foldersFacade.findFoldersByDocTyper(docType).isEmpty()){
            Object[] messageParameters = new Object[]{docType.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("DocTypeUsedInFolders"), messageParameters);
            errors.add(error);
        }
    }
    
    /**
     * Формирует число ссылок на объект в связанных объектах 
     * @param docType
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(DocType docType,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByDocTyper(docType).size());
    }
    
    @Override
    public Class<DocType> getItemClass() {
        return DocType.class;
    }

    @Override
    public Class<DocTypeGroups> getOwnerClass() {
        return DocTypeGroups.class;
    }
    
    @FacesConverter("docsTypesConvertor")
    public static class docsTypesConvertor implements Converter {
   
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {  
                 DocTypeBean bean = EscomBeanUtils.findBean("docTypeBean", fc);
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
                return String.valueOf(((DocType)object).getId());
            }
            else {
                return "";
            }
        }      
    }
}