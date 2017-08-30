package com.maxfill.services.numerators;

import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.BaseDict;
import java.util.Date;
import java.util.Map;

public interface NumeratorService {
    String doRegistrNumber(BaseDict item, NumeratorPattern numPattern, Map<String, Object> params, Date dateReg);
    void doRollBackRegistred(BaseDict item);
}