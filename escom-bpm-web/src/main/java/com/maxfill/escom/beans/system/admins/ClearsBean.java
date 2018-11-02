package com.maxfill.escom.beans.system.admins;

import com.maxfill.dictionary.DictFrmName;
import com.maxfill.escom.beans.core.BaseView;
import com.maxfill.escom.beans.core.BaseViewBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.services.attaches.AttacheService;
import com.maxfill.utils.Tuple;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.util.Map;
import javax.ejb.EJB;

/* Контролер формы очистки базы */
@Named
@ViewScoped
public class ClearsBean extends BaseViewBean<BaseView>{
    private static final long serialVersionUID = -7160350484665477991L;        

    @EJB
    private AttacheService attacheService;
            
    @Override
    public void doBeforeOpenCard(Map<String, String> params){        
    }
    
    public void onCleanUpFileStorage(){ 
        Tuple result = attacheService.cleanUpFileStorage();
        int countDel = (Integer)result.a;
        int countTotal = (Integer)result.b;
        MsgUtils.succesFormatMsg("CountFilesWereDeleted", new Object[]{countDel, countTotal});
    }
    
    /* GETS & SETS */

    @Override
    public String getFormName() {
        return DictFrmName.FRM_ADMIN_CLEANS;
    }        

    @Override
    public String getFormHeader() {
        return getLabelFromBundle("Administation");
    }
}
