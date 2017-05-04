package com.maxfill.services.numerator;

import com.maxfill.model.BaseDict;
import com.maxfill.model.numPuttern.counter.Counter;
import com.maxfill.model.numPuttern.counter.CounterFacade;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.utils.EscomUtils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Maxim
 */
public abstract class NumeratorServiceAbst {

    @EJB
    private CounterFacade counterFacade;
    
    /**
     * Откат регистрационного номера
     *
     * @param item
     * @param counterName
     */    
    public void doRollBackRegistred(BaseDict item, String counterName) {
        Integer templNumber = item.getNumber();
        if (templNumber != null) {
            Counter counter = doGetCounter(counterName);
            Integer number = counter.getNumber();
            if (number.equals(templNumber)) {
                number--;
                counter.setNumber(number);
                getCounterFacade().edit(counter);
            }
            item.setNumber(null);
        }        
    }
    
    /**
     * Получение счётчика объекта. Возвращается всегда актуальный
     * (fresh) счётчик, для учёта изменений сделанных в других сессиях
     * @param counterName
     * @return 
     */
    protected Counter doGetCounter(String counterName) {
        List<Counter> counters = getCounterFacade().findCounterByName(counterName);
        if (counters.isEmpty()) {
            Counter numerator = new Counter();
            numerator.setName(counterName);
            numerator.setNumber(0);
            getCounterFacade().create(numerator);
            return numerator;
        } else {
            return counters.get(0);
        }
    }
    
    public CounterFacade getCounterFacade() {
        return counterFacade;
    }
   
    /**
     * Формирование регистрационного номера объекта по заданной маске
     *
     * @param item
     * @param counterName
     * @param numPattern
     * @param params
     * @param dateReg
     * @return Строковое значение
     */
    public String doRegistrNumber(BaseDict item, String counterName, NumeratorPattern numPattern, Map<String, Object> params, Date dateReg) {
        Counter counter = doGetCounter(counterName);
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
    
    /**
     * Формирование номера по порядку
     *
     * @param numerator
     * @return
     */
    private Integer doGetNextRegNumber(Counter numerator) {
        Integer number = numerator.getNumber();
        number++;
        numerator.setNumber(number);
        getCounterFacade().edit(numerator);
        return number;
    }
}
