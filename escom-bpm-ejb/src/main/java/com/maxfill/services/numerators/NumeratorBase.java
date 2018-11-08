package com.maxfill.services.numerators;

import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
import com.maxfill.model.basedict.numeratorPattern.counter.CounterFacade;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.utils.EscomUtils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.commons.lang.StringUtils;

/**
 * Базовая функциональность нумератора
 * @author maksim
 */
public abstract class NumeratorBase implements NumeratorService{

    @EJB
    private CounterFacade counterFacade;
    
    /* Откат регистрационного номера */    
    @Override
    public void doRollBackRegistred(BaseDict item) {
        NumeratorPattern numPattern = getNumeratorPattern(item);
        Counter counter = doGetCounter(item, numPattern);
        Integer templNumber = item.getNumber();
        if (templNumber != null) {
            Integer number = counter.getNumber();
            if (number.equals(templNumber)) {
                number--;
                counter.setNumber(number);
                getCounterFacade().edit(counter);
            }
            item.setNumber(null);
        }
    }
    
    /* Получение счётчика объекта. Возвращается всегда актуальный
     * счётчик, для учёта изменений сделанных в других сессиях */
    protected Counter doGetCounter(BaseDict item, NumeratorPattern numPattern) {
        String counterName = doGetCounterName(item, numPattern);
        List<Counter> counters = getCounterFacade().findCounterByName(counterName);
        if (counters.isEmpty()) {
            Counter counter = new Counter();
            counter.setName(counterName);
            counter.setNumber(0);
            counter.setCompanyName("---");
            counter.setTypeName("---");
            getCounterFacade().create(counter);
            return counter;
        } else {
            return counters.get(0);
        }
    }
    
    protected abstract String doGetCounterName(BaseDict item, NumeratorPattern numPattern);    
    protected abstract NumeratorPattern getNumeratorPattern(BaseDict item);
  
    public CounterFacade getCounterFacade() {
        return counterFacade;
    }
   
    /* Формирование регистрационного номера объекта по заданной маске  */
    @Override
    public String doRegistrNumber(BaseDict item, Map<String, Object> params, Date dateReg) {
        NumeratorPattern numPattern = getNumeratorPattern(item);
        Counter counter = doGetCounter(item, numPattern);
        Integer number = doGetNextRegNumber(counter);
        item.setNumber(number);        
        int leadingZeros = numPattern.getLeadingZeros();
        String regNumber = Integer.toString(number);
        regNumber = StringUtils.leftPad(regNumber, leadingZeros, '0');
        String pattern = numPattern.getPattern();
        String yy = EscomUtils.getYearYY(dateReg);
        String year = EscomUtils.getYearStr(dateReg);
        regNumber = pattern.replaceAll("Y", yy).replaceAll("N", regNumber).replaceAll("y", year);
        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                regNumber = regNumber.replace(param.getKey(), (String) param.getValue());
            }
        }
        return regNumber;
    }
    
    /* Формирование номера по порядку  */
    private Integer doGetNextRegNumber(Counter numerator) {
        Integer number = numerator.getNumber();
        number++;
        numerator.setNumber(number);
        getCounterFacade().edit(numerator);
        return number;
    }
}
