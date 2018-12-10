package com.maxfill.model.basedict.doc.numerator;

import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.model.basedict.docType.DocTypeFacade;
import com.maxfill.model.basedict.numeratorPattern.NumeratorPattern;
import com.maxfill.model.basedict.numeratorPattern.counter.Counter;
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

/* Нумератор для документов */
@Stateless
public class DocNumeratorImpl extends NumeratorBase implements DocNumerator{    
    @EJB
    private DocTypeFacade docTypeFacade;
    @EJB
    private DocFacade docFacade;
    
    @Override
    protected Counter doGetCounter(BaseDict item, NumeratorPattern numPattern) {
        Doc doc = (Doc) item;
        String counterName = doGetCounterName(item, numPattern);
        List<Counter> counters = getCounterFacade().findCounterByName(counterName);
        if (counters.isEmpty()) {
            Counter counter = new Counter();
            counter.setName(counterName);
            counter.setNumber(0);
            counter.setCompany(doc.getCompany());
            counter.setCompanyName(doc.getCompany().getName());
            counter.setTypeName(doc.getDocType().getName());
            counter.setYear(Integer.valueOf(EscomUtils.getYearYY(doc.getItemDate())));
            getCounterFacade().create(counter);
            return counter;
        } else {
            return counters.get(0);
        }
    }
     
    @Override
    protected String doGetCounterName(BaseDict item, NumeratorPattern numPattern) {
        Doc doc = (Doc) item;
        DocType docType = doc.getDocType();
        
        StringBuilder sb = new StringBuilder();
        sb.append("DOC_");
        if (doc.getCompany() != null){
            sb.append("Company_").append(doc.getCompany().getId()).append("_");
        }                
        
        if (numPattern.isSerialNumber()){
            sb.append("SerialNumber");
        } else {
            String counterName = docType.getGuide();
            if (StringUtils.isEmpty(counterName)){
                counterName = EscomUtils.generateGUID();
                docType.setGuide(counterName);            
                docTypeFacade.edit(docType);
            }
            sb.append("_").append(counterName);
        }        
        if (doc.getDocType().getNumerator().getResetNewYear()){                
            sb.append("_").append(EscomUtils.getYearYY(doc.getItemDate()));
        }
        
        return sb.toString();
    }
    
    /* Регистрация документа  */
    @Override
    public void registratedDoc(Doc doc, Set<Tuple> errors){
        Date dateReg = doc.getItemDate();
        if (dateReg == null){
            dateReg = new Date();
            doc.setItemDate(dateReg);
        }
        if (doc.getDocType() == null){
            errors.add(new Tuple("DOCTYPE_NO_SET", new Object[]{}));
        }
        if (!errors.isEmpty()){
            return;
        }
        NumeratorPattern numPattern = getNumeratorPattern(doc);
        if (numPattern == null){
            errors.add(new Tuple("NUMERATOR_NO_SET", new Object[]{}));
            return;
        }
        Map<String, Object> params = new HashMap<>();
        String typeCode = doc.getDocType().getCode();
        if (StringUtils.isNoneBlank(typeCode)){
            params.put("T", typeCode);
        }
        params.put("O", doc.getCompany().getCode());
        String regNumber = doRegistrNumber(doc, params, dateReg);
        while(docFacade.checkRegNumber(regNumber, doc) == false){
            regNumber = doRegistrNumber(doc, params, dateReg);
        }        
        doc.setRegNumber(regNumber);        
    }   

    @Override
    protected NumeratorPattern getNumeratorPattern(BaseDict item) {
        return ((Doc)item).getDocType().getNumerator();
    }
    
}