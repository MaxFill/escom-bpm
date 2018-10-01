/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.beans.partners;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.model.basedict.partnerGroups.PartnerGroups;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/* Расширение поиска Контрагентов */
 
public class PartnersSearche extends SearcheModel{
    private static final long serialVersionUID = -8486117031439802318L;
    private String codeSearche;
    
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){        
        if (StringUtils.isNotBlank(codeSearche)){
            paramLIKE.put("codeSearche", codeSearche); 
        }        
    } 
    
    public String getCodeSearche() {
        return codeSearche;
    }
    public void setCodeSearche(String codeSearche) {
        this.codeSearche = codeSearche;
    }
}
