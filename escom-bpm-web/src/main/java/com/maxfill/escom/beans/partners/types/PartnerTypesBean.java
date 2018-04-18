
package com.maxfill.escom.beans.partners.types;

import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.facade.PartnersFacade;

import javax.ejb.EJB;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Виды контрагентов" */
@SessionScoped
@Named
public class PartnerTypesBean extends BaseTableBean<PartnerTypes, PartnerTypes>{
    private static final long serialVersionUID = -3103245251075095183L;

    @EJB
    private PartnerTypesFacade partnerTypesFacade;
    @EJB
    private PartnersFacade partnersFacade;

    @Override
    public PartnerTypesFacade getFacade() {
        return partnerTypesFacade;
    }
    
    @Override
    public BaseTableBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseTableBean getDetailBean() {
         return null;
    }

    @Override
    public List<PartnerTypes> getGroups(PartnerTypes item) {
        return null;
    }

    @Override
    public void doGetCountUsesItem(PartnerTypes partnerTypes,  Map<String, Integer> rezult){
        rezult.put("Partners", partnersFacade.findByType(partnerTypes).size());
    }    
    
    @Override
    protected void checkAllowedDeleteItem(PartnerTypes partnerTypes, Set<String> errors){
        super.checkAllowedDeleteItem(partnerTypes, errors);
        if (!partnersFacade.findByType(partnerTypes).isEmpty()){
            Object[] messageParameters = new Object[]{partnerTypes.getName()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("PartnerTypesUsed"), messageParameters);
            errors.add(error);
        }        
    }
    
    @Override
    public Class<PartnerTypes> getOwnerClass() {
        return null;
    }

}