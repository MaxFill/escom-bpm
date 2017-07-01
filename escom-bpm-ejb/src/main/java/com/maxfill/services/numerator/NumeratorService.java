package com.maxfill.services.numerator;

import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.BaseDict;
import java.util.Date;
import java.util.Map;

public interface NumeratorService {
    String doRegistrNumber(BaseDict item, String counterName, NumeratorPattern numPattern, Map<String, Object> params, Date dateReg);
    void doRollBackRegistred(BaseDict item, String counterName);
}
