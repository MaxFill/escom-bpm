package com.maxfill.escom.system.convertors;

import com.maxfill.model.basedict.procTempl.ProcTempl;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author maksim
 */
@FacesConverter("procTemplConverter")
public class ProcTemplConverter extends BaseBeanConvertor<ProcTempl>{

    @Override
    protected String getBeanName() {
        return "procTemplBean";
    }
    
}
