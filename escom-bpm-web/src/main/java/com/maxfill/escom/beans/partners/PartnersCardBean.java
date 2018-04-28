package com.maxfill.escom.beans.partners;

import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.PartnersFacade;
import com.maxfill.model.partners.Partner;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.utils.Tuple;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

/* Контрагенты */
@Named
@ViewScoped
public class PartnersCardBean extends BaseCardBeanGroups<Partner, PartnerGroups>{
    private static final long serialVersionUID = -972953149732759437L;
    
    @EJB 
    private PartnersFacade itemsFacade;

    @Inject
    private PartnersBean partnersBean;
       
    private Doc selectedDoc;
    
    @Override
    public PartnersFacade getFacade() {
        return itemsFacade;
    }

    /* Проверка корректности Контрагента перед сохранением карточки */
    @Override
    protected void checkItemBeforeSave(Partner partner, Set<String> errors) {       
        String code = partner.getCode();
        Integer partnerId = partner.getId();
        List<Partner> existPartner = getFacade().findByCodeExclId(code, partnerId);
        if (!existPartner.isEmpty()) {
            String partnerName = existPartner.get(0).getName();
            Object[] params = new Object[]{partnerName, code};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("PartnerCodeIsExsist"), params);
            errors.add(error);
        }
        
        existPartner = getFacade().findByNameAndTypeExclId(partner.getName(), partner.getType(), partnerId);
        if (!existPartner.isEmpty()) {
            Object[] params = new Object[]{getTitleName()};
            String error = MessageFormat.format(EscomMsgUtils.getMessageLabel("PartnerIsExsist"), params);
            errors.add(error);
        }
    }
    
    @Override
    protected void addItemInGroup(Partner item, PartnerGroups group) {
        if (group == null || group.getId() == 0) return;
        super.addItemInGroup(item, group);
    }
    
    public void onRowDblClckOpen(SelectEvent event){
        selectedDoc = (Doc) event.getObject();      
    }
        
    public String getTitleName(){
        return partnersBean.getTitleName(getEditedItem());
    }
        
    /* Формирование полного наименования контрагента */
    public void makeFullName(){
        Partner partner = getEditedItem();
        StringBuilder partnerName = new StringBuilder();
        
        if (partner.getType() != null){
            partnerName.append(partner.getType().getName()).append(" ");
        }
        partnerName.append(partner.getName());

        getEditedItem().setFullName(partnerName.toString());
        onItemChange();
    }
    
    /* Обновление после закрытия формы документа, открытой из карточки контрагента */
    public void onUpdateAfterCloseDocForm(SelectEvent event){
        Tuple<Boolean, Doc> tuple = (Tuple) event.getObject();
        Boolean isNeedUdate = tuple.a;
        if (isNeedUdate){
            RequestContext context = RequestContext.getCurrentInstance();
            context.update("itemCard:mainTabView:tblDocs");
            Doc item = tuple.b;
            EscomMsgUtils.succesFormatMsg("DataIsSaved", new Object[]{item.getName()});
        }
    }

    /* Печать карточки контрагента */
    @Override
    protected void doPreViewItemCard(ArrayList<Object> dataReport, Map<String, Object> parameters, String reportName){
        super.doPreViewItemCard(dataReport, parameters, DictPrintTempl.REPORT_PARTNER_CARD);
    }
    
    @Override
    public List<PartnerGroups> getGroups(Partner partner) {
        return partner.getPartnersGroupsList();
    }

    public Doc getSelectedDoc() {
        return selectedDoc;
    }
    public void setSelectedDoc(Doc selectedDoc) {
        this.selectedDoc = selectedDoc;
    }

}