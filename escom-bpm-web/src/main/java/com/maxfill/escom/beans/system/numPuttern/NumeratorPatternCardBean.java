package com.maxfill.escom.beans.system.numPuttern;

import com.maxfill.facade.NumeratorPatternFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.escom.beans.BaseCardBean;
import com.maxfill.dictionary.DictNumerator;
import com.maxfill.escom.utils.EscomBeanUtils;

import javax.ejb.EJB;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@ViewScoped
@Named
public class NumeratorPatternCardBean extends BaseCardBean<NumeratorPattern>{
    private static final long serialVersionUID = -7116956120448913137L;
    
    private List<SelectItem> numPatternTypes = new ArrayList<>();
    
    public NumeratorPatternCardBean() {
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_AUTO, EscomBeanUtils.getBandleLabel("Auto")));
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_MANUAL, EscomBeanUtils.getBandleLabel("ManualInput")));
    }    
    
    @EJB
    private NumeratorPatternFacade numeratorFacade;        

    @Override
    public NumeratorPatternFacade getItemFacade() {
        return numeratorFacade;
    }

    @Override
    public Class<NumeratorPattern> getItemClass() {
        return NumeratorPattern.class;
    }
    
    public List<SelectItem> getNumPatternTypes() {
        return numPatternTypes;
    }
    public void setNumPatternTypes(List<SelectItem> numPatternTypes) {
        this.numPatternTypes = numPatternTypes;
    }
}