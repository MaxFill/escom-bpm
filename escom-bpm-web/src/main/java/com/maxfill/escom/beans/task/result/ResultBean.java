package com.maxfill.escom.beans.task.result;

import com.maxfill.dictionary.DictResults;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.task.result.ResultFacade;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.task.result.Result;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;

/**
 * Сервисный бин "Результаты выполнения задачи" 
 * @author maksim
 */
@Named
@SessionScoped
public class ResultBean extends BaseTableBean<Result> {    
    private static final long serialVersionUID = -3352797318187287815L;

    @EJB
    private ResultFacade resultFacade;
    
    @Override
    public BaseTableBean getDetailBean() {
        return null;
    }

    @Override
    public BaseDictFacade getLazyFacade() {
        return resultFacade;
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }
    
    public String getResultIcon(String status){
        if (StringUtils.isBlank(status)) return "";        
        List<Result> results = resultFacade.findByName(status);
        if (results.isEmpty()) return "";
        Result result = results.get(0);
        return "/resources/icon/" + result.getIconName() + ".png";
    }
}
