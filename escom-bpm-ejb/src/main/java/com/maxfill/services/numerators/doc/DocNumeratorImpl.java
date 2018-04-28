package com.maxfill.services.numerators.doc;

import com.maxfill.model.docs.Doc;
import com.maxfill.model.BaseDict;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.model.numPuttern.counter.Counter;
import com.maxfill.services.numerators.NumeratorBase;
import com.maxfill.utils.EscomUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;

/* Нумератор для документов */
@Stateless
public class DocNumeratorImpl extends NumeratorBase implements DocNumeratorService{    
              
    @Override
    protected Counter doGetCounter(BaseDict item) {
        Doc doc = (Doc) item;
        String counterName = doGetCounterName(item);
        List<Counter> counters = getCounterFacade().findCounterByName(counterName);
        if (counters.isEmpty()) {
            Counter counter = new Counter();
            counter.setName(counterName);
            counter.setNumber(0);
            counter.setCompany(doc.getCompany());
            counter.setDocType(doc.getDocType());
            counter.setYear(Integer.valueOf(EscomUtils.getYearYY(doc.getDateDoc())));
            getCounterFacade().create(counter);
            return counter;
        } else {
            return counters.get(0);
        }
    }
     
    @Override
    protected String doGetCounterName(BaseDict item) {
        Doc doc = (Doc) item;
        String counterName = doc.getDocType().getGuide();       
        if (doc.getDocType().getNumerator().getResetNewYear()){
            counterName = counterName + "_" + doc.getCompany().getId() + "_" + EscomUtils.getYearYY(doc.getDateDoc());
        }
        return counterName;
    }
    
    /* Регистрация документа  */
    @Override
    public void registratedDoc(Doc doc, Set<String> errors){
        Date dateReg = doc.getDateDoc();
        if (dateReg == null){
            errors.add("DOCDATE_NO_SET");            
        }
        if (doc.getDocType() == null){
            errors.add("DOCTYPE_NO_SET");
        }
        if (!errors.isEmpty()){
            return;
        }
        NumeratorPattern numPattern = doc.getDocType().getNumerator();
        if (numPattern == null){
            errors.add("NUMERATOR_NO_SET");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        String typeCode = doc.getDocType().getCode();
        if (StringUtils.isNoneBlank(typeCode)){
            params.put("T", typeCode);
        }
        params.put("O", doc.getCompany().getCode());
        String number = doRegistrNumber(doc, numPattern, params, dateReg);
        doc.setRegNumber(number);                         
    }   
    
}