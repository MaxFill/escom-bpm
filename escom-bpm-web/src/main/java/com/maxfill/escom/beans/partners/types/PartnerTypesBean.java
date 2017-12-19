
package com.maxfill.escom.beans.partners.types;

import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.facade.PartnersFacade;
import com.maxfill.escom.utils.EscomBeanUtils;

import javax.ejb.EJB;
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
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Виды контрагентов" */
@SessionScoped
@Named
public class PartnerTypesBean extends BaseExplBean<PartnerTypes, PartnerTypes>{
    private static final long serialVersionUID = -3103245251075095183L;

    @EJB
    private PartnerTypesFacade partnerTypesFacade;
    @EJB
    private PartnersFacade partnersFacade;

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

    @Override
    public void doGetCountUsesItem(PartnerTypes partnerTypes,  Map<String, Integer> rezult){
        rezult.put("Partners", partnersFacade.findByType(partnerTypes).size());
    }    
    
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