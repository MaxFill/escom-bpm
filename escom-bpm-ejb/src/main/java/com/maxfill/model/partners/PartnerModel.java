
package com.maxfill.model.partners;

import com.maxfill.model.BaseDataModel;

/**
 *
 * @author mfilatov
 */
public class PartnerModel extends BaseDataModel{    
    private static final long serialVersionUID = -1568395726018512158L;
    
    private String codeSearche;
    
    public String getCodeSearche() {
        return codeSearche;
    }

    public void setCodeSearche(String codeSearche) {
        this.codeSearche = codeSearche;
    }
 
}
