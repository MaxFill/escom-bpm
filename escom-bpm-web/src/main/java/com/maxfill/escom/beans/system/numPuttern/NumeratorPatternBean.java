
package com.maxfill.escom.beans.system.numPuttern;

import com.maxfill.dictionary.DictNumerator;
import com.maxfill.facade.NumeratorPatternFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import java.util.ArrayList;
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
import javax.faces.model.SelectItem;

/* Сервисный бин "НУМЕРАТОР" */
@SessionScoped
@Named
public class NumeratorPatternBean extends BaseExplBean<NumeratorPattern, NumeratorPattern>{
    private static final long serialVersionUID = 7793437880614397796L;

    @EJB
    private NumeratorPatternFacade numeratorFacade;    
    
    private final List<SelectItem> numPatternTypes = new ArrayList<>();

    @Override
    public void onInitBean() {
        super.onInitBean(); 
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_AUTO, EscomBeanUtils.getBandleLabel("Auto")));
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_MANUAL, EscomBeanUtils.getBandleLabel("ManualInput")));
    }
            
    public String getLabel(String typeCode){
        for (SelectItem item : numPatternTypes){
            if (item.getValue().equals(typeCode)){
                return item.getLabel();
            }
        }
        return "";
    }     
        
    @Override
    public NumeratorPatternFacade getItemFacade() {
        return numeratorFacade;
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
    public List<NumeratorPattern> getGroups(NumeratorPattern item) {
        return null;
    }

    @Override
    public Class<NumeratorPattern> getOwnerClass() {
        return null;
    }
            
    @FacesConverter("numPatternConvertor")
    public static class numPatternConvertor implements Converter {
    
        @Override
        public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
         if(value != null && value.trim().length() > 0) {
             try {          
                 NumeratorPatternBean bean = EscomBeanUtils.findBean("numeratorPatternBean", fc);
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
                Integer id = ((NumeratorPattern)object).getId();
                return String.valueOf(id);
            }
            else {
                return "";
            }
        }      
    }
}