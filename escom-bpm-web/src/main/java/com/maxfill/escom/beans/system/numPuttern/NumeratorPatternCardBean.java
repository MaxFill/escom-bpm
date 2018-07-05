package com.maxfill.escom.beans.system.numPuttern;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.numPuttern.NumeratorPatternFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.dictionary.DictNumerator;

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
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_AUTO, MsgUtils.getBandleLabel("Auto")));
        numPatternTypes.add(new SelectItem(DictNumerator.TYPE_MANUAL, MsgUtils.getBandleLabel("ManualInput")));
    }       
    
    @EJB
    private NumeratorPatternFacade numeratorFacade;

    @Override
    public String getRightsForObjectTitle() {
        return MsgUtils.getBandleLabel("RightsForObject");
    }

    @Override
    public NumeratorPatternFacade getFacade() {
        return numeratorFacade;
    }
    
    public List<SelectItem> getNumPatternTypes() {
        return numPatternTypes;
    }
    public void setNumPatternTypes(List<SelectItem> numPatternTypes) {
        this.numPatternTypes = numPatternTypes;
    }

}