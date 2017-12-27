package com.maxfill.escom.system.convertors;

import com.maxfill.escom.beans.system.login.CountryFlags;
import com.maxfill.escom.beans.system.login.LoginBean;
import com.maxfill.escom.utils.EscomBeanUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("langConverter")
public class LngConverter implements Converter{
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {
                LoginBean bean = EscomBeanUtils.findBean("loginBean", fc);
                Object searcheObj = bean.getLanguages().get(Integer.parseInt(value));
                return searcheObj;
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Некорректное значение."));
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if(object != null) {
            return String.valueOf(((CountryFlags) object).getId());
        }
        else {
            return "";
        }
    }
}
