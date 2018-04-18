package com.maxfill.escom.system.convertors;

import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.BaseDict;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public abstract class BaseBeanConvertor<T extends BaseDict> implements Converter{

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value != null && value.trim().length() > 0) {
            try {
                BaseTableBean bean = EscomBeanUtils.findBean(getBeanName(), fc);
                Object searcheObj = bean.getFacade().find(Integer.parseInt(value));
                return searcheObj;
            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not valid"));
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            return String.valueOf(((T) object).getId());
        } else {
            return "";
        }
    }

    protected abstract String getBeanName();
}
