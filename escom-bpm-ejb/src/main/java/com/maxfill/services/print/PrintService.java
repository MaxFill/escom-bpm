package com.maxfill.services.print;

import com.maxfill.model.BaseDict;
import java.util.ArrayList;
import java.util.Map;

public interface PrintService {
    void doPrint(ArrayList<BaseDict> dataReport, Map<String, Object> parameters, String reportName);
}
