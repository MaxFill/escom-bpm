package com.maxfill.services.print;

import java.util.List;
import java.util.Map;

public interface PrintService {
    void doPrint(List<Object> dataReport, Map<String, Object> parameters, String reportName);
}
