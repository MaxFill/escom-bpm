package com.maxfill.escom.beans.partners;

import com.maxfill.escom.beans.explorer.SearcheModel;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/* Расширение поиска Контрагентов */
 
public class PartnersSearche extends SearcheModel{
    private static final long serialVersionUID = -8486117031439802318L;
    
    private String codeSearche;
    private String inn;
    private String kpp;
        
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams){        
        if (StringUtils.isNotBlank(codeSearche)){
            paramLIKE.put("codeSearche", codeSearche); 
        }  
        if (StringUtils.isNotBlank(inn)){
            paramLIKE.put("inn", inn); 
        }
        if (StringUtils.isNotBlank(kpp)){
            paramLIKE.put("kpp", kpp); 
        }
    } 
    
    /* GETS & SETS */
    
    public String getCodeSearche() {
        return codeSearche;
    }
    public void setCodeSearche(String codeSearche) {
        this.codeSearche = codeSearche;
    }

    public String getInn() {
        return inn;
    }
    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getKpp() {
        return kpp;
    }
    public void setKpp(String kpp) {
        this.kpp = kpp;
    }
        
}
