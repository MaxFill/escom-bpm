
package com.maxfill.escom.beans.docs;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.BaseDict;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/* Расширение поиска Документов  */
public class DocsSearche extends SearcheModel{
    private static final long serialVersionUID = 3953544990596152030L;
    
    private String numberSearche;  
        
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){
        if (StringUtils.isNotBlank(numberSearche)){
            paramLIKE.put("numberSearche", numberSearche);
        }
    }    
    
    public String getNumberSearche() {
        return numberSearche;
    }
    public void setNumberSearche(String numberSearche) {
        this.numberSearche = numberSearche;
    }
    
}
