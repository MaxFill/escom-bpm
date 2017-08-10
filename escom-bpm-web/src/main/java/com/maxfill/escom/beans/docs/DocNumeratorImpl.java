
package com.maxfill.escom.beans.docs;

import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.BaseDict;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.services.numerator.DocNumerator;
import com.maxfill.services.numerator.NumeratorServiceAbst;
import com.maxfill.utils.EscomUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;

/* Нумератор для документов */
@Stateless
public class DocNumeratorImpl extends NumeratorServiceAbst implements DocNumerator{    
    
    /* Отмена регистрации документа  */
    @Override
    public void doRollBackRegistred(BaseDict item, String counterName){        
        counterName = onGetCounterName(item);        
        super.doRollBackRegistred(item, counterName);
    }
    
    @Override
    public String doRegistrNumber(BaseDict item, String counterName, NumeratorPattern numPattern, Map<String, Object> params, Date dateReg) {
        counterName = onGetCounterName(item);
        return super.doRegistrNumber(item, counterName, numPattern, params, dateReg);
    }
    
    /*Регистрация документа  */
    @Override
    public void doRegistDoc(Doc doc){
        Date dateReg = doc.getDateDoc();
        if (dateReg != null){
            if (doc.getDocType() != null){
                NumeratorPattern numPattern = doc.getDocType().getNumerator();
                if (numPattern != null){
                    Map<String, Object> params = new HashMap<>();
                    String typeCode = doc.getDocType().getCode();
                    if (StringUtils.isNoneBlank(typeCode)){
                        params.put("T", typeCode);
                    }
                    params.put("O", doc.getCompany().getCode());
                    String number = doRegistrNumber(doc, "", numPattern, params, dateReg);
                    doc.setRegNumber(number);
                    EscomBeanUtils.SuccesMsgAdd("Successfully", "DocIsRegistred");
                } else{
                    EscomBeanUtils.ErrorMsgAdd("RegistrationError", "NUMERATOR_NO_SET", "");
                }
            }else {
                EscomBeanUtils.ErrorMsgAdd("RegistrationError", "DOCTYPE_NO_SET", "");
            }
        }else{
            EscomBeanUtils.ErrorMsgAdd("RegistrationError", "DOCDATE_NO_SET", "");
        }
    }
    
    /* Возвращает имя счётчика нумератора для документа  */
    private String onGetCounterName(BaseDict item){        
        Doc doc = (Doc) item;
        String counterName = doc.getDocType().getGuide();
        if (doc.getDocType().getNumerator().getResetNewYear()){
            counterName = counterName + "_" + doc.getCompany().getId() + "_" + EscomUtils.getYearYY(doc.getDateDoc());
        }
       return counterName; 
    }
}