package com.maxfill.services.numerators;

import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.BaseDict;
import java.util.Date;
import java.util.Map;

/**
 * Базовый интерфейс нумераторов
 * @author maksim
 */
public interface NumeratorService {
    String doRegistrNumber(BaseDict item, NumeratorPattern numPattern, Map<String, Object> params, Date dateReg);
    void doRollBackRegistred(BaseDict item);
}