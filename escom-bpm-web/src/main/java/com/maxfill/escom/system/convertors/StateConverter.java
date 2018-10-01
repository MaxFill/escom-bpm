package com.maxfill.escom.system.convertors;

import com.maxfill.escom.beans.system.states.StateBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.core.states.State;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("stateConvertor")
public class StateConverter implements Converter{
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
