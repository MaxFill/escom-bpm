package com.maxfill.model.basedict.partner.numerator;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.partner.PartnersFacade;
import com.maxfill.services.numerators.NumeratorBase;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/* Нумератор для Подразделений */
@Stateless
public class PartnerNumeratorImpl extends NumeratorBase implements PartnerNumerator{                      
    
    @EJB
    private PartnersFacade facade;
    
    @Override
    protected String doGetCounterName(BaseDict item, NumeratorPattern numPattern) {       
        return "Partner";
    }

    @Override
    protected NumeratorPattern getNumeratorPattern(BaseDict item) {
        return facade.getMetadatesObj().getNumPattern();
    }

}