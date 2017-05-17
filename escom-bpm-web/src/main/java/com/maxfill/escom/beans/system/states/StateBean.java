package com.maxfill.escom.beans.system.states;

import com.maxfill.facade.StateFacade;
import com.maxfill.model.states.State;
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

/* Сервисный бин "Состояния документа" */
@SessionScoped
@Named
public class StateBean extends BaseExplBean<State, State>{
    private static final long serialVersionUID = -3106225231045015183L;
    
    @EJB
    private StateFacade docsStateFacade;
    
    @Override
    public StateFacade getItemFacade() {
        return docsStateFacade;
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
    public List<State> getGroups(State item) {
        return null;
    }
    
    @Override
    public Class<State> getItemClass() {
        return State.class;
    }

    @Override
    public Class<State> getOwnerClass() {
        return null;
    }

    @FacesConverter("stateConvertor")
    public static class stateConvertor implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {          
                 StateBean bean = EscomBeanUtils.findBean("stateBean", fc);
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
                Integer id = ((State)object).getId();
                return String.valueOf(id);
            }
            else {
                return "";
            }
        }      
    }
     
}