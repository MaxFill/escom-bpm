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
}
