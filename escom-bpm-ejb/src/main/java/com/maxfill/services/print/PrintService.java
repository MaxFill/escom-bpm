package com.maxfill.services.print;

import java.util.ArrayList;
import java.util.Map;

public interface PrintService {
    void doPrint(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName);
}
