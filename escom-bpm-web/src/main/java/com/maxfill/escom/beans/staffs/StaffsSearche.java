/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.maxfill.escom.beans.staffs;

import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.basedict.BaseDict;
import java.util.Date;
import java.util.List;
import java.util.Map;

/* Расширение поиска Штатных единиц */
 
public class StaffsSearche extends SearcheModel{
    private static final long serialVersionUID = 4197000011831824656L;
    
    private String postSearche;
    private String secondNameSearche;
    
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, List<BaseDict> searcheGroups, Map<String, Object> addParams){
        if (postSearche != null){
            addParams.put("postName", postSearche);
        }
        if (secondNameSearche != null){
            addParams.put("secondName", secondNameSearche);
        }
    }
    
    public String getPostSearche() {
        return postSearche;
    }
    public void setPostSearche(String postSearche) {
        this.postSearche = postSearche;
    }

    public String getSecondNameSearche() {
        return secondNameSearche;
    }
    public void setSecondNameSearche(String secondNameSearche) {
        this.secondNameSearche = secondNameSearche;
    }
}
