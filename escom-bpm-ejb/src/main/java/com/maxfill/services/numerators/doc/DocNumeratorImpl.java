package com.maxfill.services.numerators.doc;

import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.docType.DocTypeFacade;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
import com.maxfill.services.numerators.NumeratorBase;
import com.maxfill.utils.EscomUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;

/* Нумератор для документов */
@Stateless
public class DocNumeratorImpl extends NumeratorBase implements DocNumeratorService{    
    @EJB
    private DocTypeFacade docTypeFacade;
    
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
            counter.setYear(Integer.valueOf(EscomUtils.getYearYY(doc.getItemDate())));
            getCounterFacade().create(counter);
            return counter;
        } else {
            return counters.get(0);
        }
    }
     
    @Override
    protected String doGetCounterName(BaseDict item) {
        Doc doc = (Doc) item;
        DocType docType = doc.getDocType();
        StringBuilder sb = new StringBuilder();
        String counterName = docType.getGuide();
        if (StringUtils.isEmpty(counterName)){
            counterName = EscomUtils.generateGUID();
            docType.setGuide(counterName);            
            docTypeFacade.edit(docType);
        }
        sb.append(counterName);
        if (doc.getCompany() != null){
            sb.append("_").append(doc.getCompany().getId());
        }
        if (doc.getDocType().getNumerator().getResetNewYear()){                
            sb.append("_").append(EscomUtils.getYearYY(doc.getItemDate()));
        }
        
        return sb.toString();
    }
    
    /* Регистрация документа  */
    @Override
    public void registratedDoc(Doc doc, Set<String> errors){
        Date dateReg = doc.getItemDate();
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