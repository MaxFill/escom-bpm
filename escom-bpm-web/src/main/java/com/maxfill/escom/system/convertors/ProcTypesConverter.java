package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.procTempl.ProcTempl;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author maksim
 */
@FacesConverter("procTypesConverter")
public class ProcTypesConverter extends BaseBeanConvertor<ProcTempl>{

    @Override
    protected String getBeanName() {
        return "processTypesBean";
    }
    
}
