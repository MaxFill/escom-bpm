package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.result.Result;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author maksim
 */
@FacesConverter("resultConverter")
public class ResultConverter extends BaseBeanConvertor<Result>{

    @Override
    protected String getBeanName() {
        return "resultBean";
    }
    
}
