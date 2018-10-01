package com.maxfill.escom.system.convertors;

import com.maxfill.escom.beans.processes.elements.ProcedureBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.basedict.process.procedures.Procedure;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("procedureConvertor")
public class ProcedureConvertor implements Converter{
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if(value != null && value.trim().length() > 0) {
            try {
                ProcedureBean bean = EscomBeanUtils.findBean("procedureBean", context);
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
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if(object != null) {
            Integer id = ((Procedure)object).getId();
            return String.valueOf(id);
        }
        else {
            return "";
        }
    }
    
}
