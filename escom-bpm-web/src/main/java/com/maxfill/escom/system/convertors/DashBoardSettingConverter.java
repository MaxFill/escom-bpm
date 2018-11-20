package com.maxfill.escom.system.convertors;

import com.maxfill.escom.beans.SessionBean;
import com.maxfill.escom.beans.users.settings.DashBoardSettings;
import com.maxfill.escom.utils.EscomBeanUtils;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("DashBoardSettingConverter")
public class DashBoardSettingConverter implements Converter{
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {
                SessionBean bean = EscomBeanUtils.findBean("sessionBean", fc);
                Object searcheObj = bean.getDbsList().stream()
                        .filter(dbs->value.equals(dbs.getWidget()))
                        .findFirst()
                        .orElse(null);
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
            return ((DashBoardSettings) object).getWidget();
        }
        else {
            return "";
        }
    }
}
