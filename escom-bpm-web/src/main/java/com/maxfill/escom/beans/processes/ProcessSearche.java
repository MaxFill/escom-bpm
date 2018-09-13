package com.maxfill.escom.beans.processes;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.BaseDict;
import com.maxfill.model.users.User;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProcessSearche extends SearcheModel{
    private static final long serialVersionUID = 3953544990596152030L;

    private User curator;

    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){
        if (curator != null){
           paramEQ.put("curator", curator);
        }        
    }        
       
    /* GETS & SETS */

    public User getCurator() {
        return curator;
    }
    public void setCurator(User curator) {
        this.curator = curator;
    }
}
