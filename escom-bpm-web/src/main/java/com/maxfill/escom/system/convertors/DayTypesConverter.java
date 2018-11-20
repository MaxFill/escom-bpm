/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.system.convertors;

import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.services.worktime.DayType;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("DayTypesConverter")
public class DayTypesConverter implements Converter{
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {
                SessionBean bean = EscomBeanUtils.findBean("sessionBean", fc);
                Object searcheObj = bean.getDayTypes().get(Integer.parseInt(value));
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
            return String.valueOf(((DayType) object).getId());
        }
        else {
            return "";
        }
    }
}