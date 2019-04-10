package com.maxfill.escom.beans.task;

import com.maxfill.escom.beans.explorer.SearcheModel;
import java.util.Date;
import java.util.Map;

public class TaskSearche extends SearcheModel{    
    private static final long serialVersionUID = -361672852399586088L;
    
    private Integer tasksStatus; //0 ExeInTime, 1 ExeOverdue, 2 FinishInTime, 3 FinishOverdue    
        
    @Override
    public void addSearcheParams(Map<String, Object> paramEQ, Map<String, Object> paramLIKE, Map<String, Object> paramIN, Map<String, Date[]> paramDATE, Map<String, Object> addParams){    
    }
        
}
