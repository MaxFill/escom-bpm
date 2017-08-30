package com.maxfill.services.numerators;

import com.maxfill.model.BaseDict;
import javax.ejb.Stateless;

@Stateless
public class NumeratorServiceImpl extends NumeratorBase implements NumeratorService{                      
     
    @Override
    protected String doGetCounterName(BaseDict item) {
        return item.getClass().getSimpleName();
    }
    
    
}