
package com.maxfill.escom.beans.system.statuses;

import com.maxfill.facade.StatusesDocFacade;
import com.maxfill.model.statuses.StatusesDoc;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.util.List;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Статусы документов" */
@SessionScoped
@Named
public class StatusesDocBean extends BaseExplBean<StatusesDoc, StatusesDoc>{
    private static final long serialVersionUID = 7864211951329104261L;
    
    @EJB
    private StatusesDocFacade itemFacade;     
    
    @Override
    public StatusesDocFacade getItemFacade() {
        return itemFacade;
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
    public List<StatusesDoc> getGroups(StatusesDoc item) {
        return null;
    }

    @Override
    public Class<StatusesDoc> getItemClass() {
        return StatusesDoc.class;
    }

    @Override
    public Class<StatusesDoc> getOwnerClass() {
        return null;
    }

    @FacesConverter("statusesDocConvertor")
    public static class statusesDocConvertor implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {          
                 StatusesDocBean bean = EscomBeanUtils.findBean("statusesDocBean", fc);
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
                Integer id = ((StatusesDoc)object).getId();
                return String.valueOf(id);
            }
            else {
                return "";
            }
        }      
    }
     
}