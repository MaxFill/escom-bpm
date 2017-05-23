package com.maxfill.services.print;

import com.maxfill.model.BaseDict;

public interface PrintService {
    void doPrint(BaseDict item, String reportName);
}
