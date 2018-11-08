package com.maxfill.services.numerators;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import javax.ejb.Stateless;

/**
 * Стандартная реализация нумератора
 * @author maksim
 */

@Stateless
public class NumeratorServiceImpl extends NumeratorBase implements  NumeratorService{

    @Override
    protected String doGetCounterName(BaseDict item, NumeratorPattern numPattern) {
        return item.getClass().getSimpleName();
    }

    @Override
    protected NumeratorPattern getNumeratorPattern(BaseDict item) {
        return null;
    }
    
}
