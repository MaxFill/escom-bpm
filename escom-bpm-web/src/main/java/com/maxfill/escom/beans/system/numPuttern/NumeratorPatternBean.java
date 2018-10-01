
package com.maxfill.escom.beans.system.numPuttern;

import com.maxfill.dictionary.DictNumerator;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPatternFacade;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.escom.beans.core.BaseDetailsBean;

import java.util.ArrayList;
import javax.ejb.EJB;
import javax.inject.Named;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;

/* Сервисный бин "НУМЕРАТОР" */
@SessionScoped
@Named
public class NumeratorPatternBean extends BaseTableBean<NumeratorPattern>{
    private static final long serialVersionUID = 7793437880614397796L;

    @EJB
    private NumeratorPatternFacade numeratorFacade;    
    
    private final List<SelectItem> numPatternTypes = new ArrayList<>();

    @Override
    public void initBean() {
        super.initBean();
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_AUTO, MsgUtils.getBandleLabel("Auto")));
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_MANUAL, MsgUtils.getBandleLabel("ManualInput")));
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
    public NumeratorPatternFacade getLazyFacade() {
        return numeratorFacade;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
         return null;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }
    
}