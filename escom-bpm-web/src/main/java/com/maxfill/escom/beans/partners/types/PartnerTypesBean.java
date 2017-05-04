
package com.maxfill.escom.beans.partners.types;

import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.model.BaseDict;
import com.maxfill.facade.PartnersFacade;
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
 * Бин для видов контрагентов
 * @author mfilatov
 */
@ViewScoped
@Named
public class PartnerTypesBean extends BaseExplBean<PartnerTypes, PartnerTypes>{
    private static final long serialVersionUID = -3103245251075095183L;
    private static final String BEAN_NAME = "partnerTypesBean";

    @EJB
    private PartnerTypesFacade partnerTypesFacade;
    @EJB
    private PartnersFacade partnersFacade;
    
    @Override
    protected String getBeanName() {
        return BEAN_NAME;
    }       
        
    @Override
    public PartnerTypesFacade getItemFacade() {
        return partnerTypesFacade;
    }
    
    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseExplBean getDetailBean() {
         return null;
    }

    @Override
    public List<PartnerTypes> getGroups(PartnerTypes item) {
        return null;
    }

    /**
     * Формирует число ссылок на PartnerTypes в связанных объектах 
     * @param partnerTypes
     * @param rezult 
     */
    @Override
    public void doGetCountUsesItem(PartnerTypes partnerTypes,  Map<String, Integer> rezult){
        rezult.put("Partners", partnersFacade.findByType(partnerTypes).size());
    }    
    
    /**
     * Проверка возможности удаления partnerTypes
     * @param partnerTypes
     */
    @Override
    protected void checkAllowedDeleteItem(PartnerTypes partnerTypes, Set<String> errors){
        super.checkAllowedDeleteItem(partnerTypes, errors);
        if (!partnersFacade.findByType(partnerTypes).isEmpty()){
            Object[] messageParameters = new Object[]{partnerTypes.getName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("PartnerTypesUsed"), messageParameters);
            errors.add(error);
        }        
    } 

    @Override
    public Class<PartnerTypes> getItemClass() {
        return PartnerTypes.class;
    }
    
    @Override
    public Class<PartnerTypes> getOwnerClass() {
        return null;
    }
    
    @FacesConverter("partnerTypesConvertor")
    public static class PartnerTypesConvertor implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {          
                 PartnerTypesBean bean = EscomBeanUtils.findBean("partnerTypesBean", fc);
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
                Integer id = ((PartnerTypes)object).getId();
                return String.valueOf(id);
            }
            else {
                return "";
            }
        }      
    }
     
}