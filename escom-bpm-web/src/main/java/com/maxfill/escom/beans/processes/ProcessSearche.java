package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.user.User;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class ProcessSearche extends SearcheModel{
    private static final long serialVersionUID = 3953544990596152030L;

    private User curator;
    private String numberSearche;
    
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){
        if (curator != null){
           paramEQ.put("curator", curator);
        }
        if(StringUtils.isNotBlank(numberSearche)){
            paramLIKE.put("regNumber", numberSearche);
        }
    }        
       
    /* GETS & SETS */

    public User getCurator() {
        return curator;
    }
    public void setCurator(User curator) {
        this.curator = curator;
    }

    public String getNumberSearche() {
        return numberSearche;
    }
    public void setNumberSearche(String numberSearche) {
        this.numberSearche = numberSearche;
    }

    
}
