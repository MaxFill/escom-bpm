package com.maxfill.model;

import com.maxfill.model.basedict.result.Result;
import java.util.List;

/**
 *
 * @author maksim
 */
public interface Results {
    String getAvaibleResultsJSON();
    void setAvaibleResultsJSON(String avaibleResultsJSON);
    
    void setResults(List<Result> taskResults);
    
    String getResult();
    void setResult(String result);
    
    String getResultIcon();
    void setResultIcon(String resultIcon);
}
