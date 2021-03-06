package com.maxfill.escom.beans.docs.docsTypes;

import com.maxfill.model.basedict.docType.DocTypeFacade;
import com.maxfill.model.basedict.docType.DocType;
import com.maxfill.escom.beans.core.BaseCardBean;
import com.maxfill.utils.EscomUtils;
import org.apache.commons.lang.StringUtils;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы "Вид документа"
 * @author mfilatov
 */
@Named
@ViewScoped
public class DocTypeCardBean extends BaseCardBean<DocType>{
    private static final long serialVersionUID = -359963604904783055L;    
            
    @EJB 
    private DocTypeFacade itemsFacade;       

    @Override
    public DocTypeFacade getFacade() {
        return itemsFacade;
    }   

    @Override
    protected void onBeforeSaveItem(DocType item){ 
        if (StringUtils.isEmpty(item.getGuide())){
            String guid = EscomUtils.generateGUID();
            item.setGuide(guid);
        }
        super.onBeforeSaveItem(item);
    }
}