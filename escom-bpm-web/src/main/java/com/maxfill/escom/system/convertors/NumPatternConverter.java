package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import javax.faces.convert.FacesConverter;

@FacesConverter("numPatternConvertor")
public class NumPatternConverter extends BaseBeanConvertor<NumeratorPattern>{
    @Override
    protected String getBeanName() {
        return "numeratorPatternBean";
    }
}
