package com.maxfill.model.basedict.process.numerator;

import com.maxfill.model.basedict.process.Process;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
import com.maxfill.model.basedict.processType.ProcessType;
import com.maxfill.model.basedict.processType.ProcessTypesFacade;
import com.maxfill.services.numerators.NumeratorBase;
import com.maxfill.utils.EscomUtils;
import com.maxfill.utils.Tuple;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;

/**
 * Нумератор для процессов
 */ 
@Stateless
public class ProcessNumeratorImpl extends NumeratorBase implements ProcessNumerator{    
    
    @EJB
    private ProcessTypesFacade processTypesFacade;
    
    /**
     * Возвращает счётчик для объекта
     * @param item
     * @param numPattern
     * @return 
     */
    @Override
    protected Counter doGetCounter(BaseDict item, NumeratorPattern numPattern) {
        Process process = (Process) item;
        String counterName = doGetCounterName(process, numPattern);
        List<Counter> counters = getCounterFacade().findCounterByName(counterName);
        if (counters.isEmpty()) {
            Counter counter = new Counter();
            counter.setName(counterName);
            counter.setNumber(0);
            counter.setCompany(process.getCompany());
            counter.setCompanyName(process.getCompanyName());
            counter.setTypeName(process.getOwner().getName());
            counter.setYear(Integer.valueOf(EscomUtils.getYearYY(process.getItemDate())));
            getCounterFacade().create(counter);
            return counter;
        } else {
            return counters.get(0);
        }
    }
    
    /**
     * Формирование имени счётчика
     * @param item
     * @param numPattern
     * @return 
     */
    @Override
    protected String doGetCounterName(BaseDict item, NumeratorPattern numPattern) {
        Process process = (Process) item;
        ProcessType procType = process.getOwner();         
        
        StringBuilder sb = new StringBuilder();
        sb.append("PROCESS_");
        if (process.getCompany() != null){
            sb.append("Company_").append(process.getCompany().getId()).append("_");
        }                
        
        Process parent = process.getParent();
        if (parent != null){
            sb.append("_parent_").append(parent.getId());
        } else {
            if (numPattern.isSerialNumber()){
                sb.append("_SerialNumber");
            } else {
                String counterName = procType.getGuide();
                if (StringUtils.isEmpty(counterName)){
                    counterName = EscomUtils.generateGUID();
                    procType.setGuide(counterName);            
                    processTypesFacade.edit(procType);
                    sb.append("_").append(counterName);
                }
            }
        }
        
        if (processTypesFacade.getProcTypeForOpt(procType).getNumerator().getResetNewYear()){                
            sb.append("_").append(EscomUtils.getYearYY(process.getItemDate()));
        }        
        return sb.toString();
    }
    
    /**
     * Регистрация процесса
     * @param process
     * @param errors 
     */
    @Override
    public void registrate(Process process, Set<Tuple> errors){
        if (!errors.isEmpty()) return;
        
        Date dateReg = process.getItemDate();
        if (dateReg == null){
            dateReg = new Date();
            process.setItemDate(dateReg);
        }                
        NumeratorPattern numPattern = getNumeratorPattern(process);
        if (numPattern == null){
            errors.add(new Tuple("NUMERATOR_NO_SET", new Object[]{}));
            return;
        }
        Map<String, Object> params = new HashMap<>();
        String typeCode = process.getOwner().getCode();
        if (StringUtils.isNoneBlank(typeCode)){
            params.put("T", typeCode);
        }
        if (process.getCompany() != null){
            String companyCode = process.getCompany().getCode();
            if (StringUtils.isNoneBlank(companyCode)){
                params.put("O", companyCode);
            }
        }
        String number = doRegistrNumber(process, params, dateReg);
        process.setRegNumber(number);        
    }    

    @Override
    protected NumeratorPattern getNumeratorPattern(BaseDict item) {
        return processTypesFacade.getProcTypeForOpt((ProcessType)item.getOwner()).getNumerator();
    }
}