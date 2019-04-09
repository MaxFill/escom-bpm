package com.maxfill.escom.beans.partners.types;

import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.partnerTypes.PartnerTypesFacade;
import com.maxfill.model.basedict.partnerTypes.PartnerTypes;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.model.basedict.partner.PartnersFacade;

import javax.ejb.EJB;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;

/* Сервисный бин "Виды контрагентов" */
@SessionScoped
@Named
public class PartnerTypesBean extends BaseTableBean<PartnerTypes>{
    private static final long serialVersionUID = -3103245251075095183L;

    @EJB
    private PartnerTypesFacade partnerTypesFacade;
    @EJB
    private PartnersFacade partnersFacade;

    @Override
    public PartnerTypesFacade getLazyFacade() {
        return partnerTypesFacade;
    }
    
    @Override
    public BaseDetailsBean getDetailBean() {
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
            String error = MessageFormat.format(MsgUtils.getMessageLabel("PartnerTypesUsed"), messageParameters);
            errors.add(error);
        }        
    }

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public SearcheModel initSearcheModel() {
        return new PartnerTypesSearche();
    }
}