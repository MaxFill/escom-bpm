package com.maxfill.escom.beans.partners;

import com.maxfill.dictionary.DictPrintTempl;
import com.maxfill.dictionary.SysParams;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.partner.PartnersFacade;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.basedict.doc.Doc;
import com.maxfill.model.basedict.partnerGroups.PartnerGroups;
import com.maxfill.utils.Tuple;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import org.omnifaces.cdi.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.primefaces.PrimeFaces;

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
    protected void checkItemBeforeSave(Partner partner, FacesContext context, Set<String> errors) {       
        String code = partner.getCode();
        Integer partnerId = partner.getId();
        List<Partner> existPartner = getFacade().findByCodeExclId(code, partnerId);
        if (!existPartner.isEmpty()) {
            String partnerName = existPartner.get(0).getName();
            UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:code");
            input.setValid(false);
            Object[] params = new Object[]{partnerName, code};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("PartnerCodeIsExsist"), params);
            errors.add(error);
        }
        
        if (StringUtils.isBlank(partner.getInn()) && StringUtils.isBlank(partner.getKpp())){ //то проверка по имени
            existPartner = getFacade().findByNameAndTypeExclId(partner.getName(), partner.getType(), partnerId);
            if (!existPartner.isEmpty()) {
                UIInput input = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:nameItem");
                input.setValid(false);
                Object[] params = new Object[]{getTitleName()};
                String error = MessageFormat.format(MsgUtils.getMessageLabel("PartnerIsExsist"), params);
                errors.add(error);
            }
        } else {        
            Tuple result = getFacade().findDublicateExcludeItem(partner);   //проверка по ИНН КПП
            if (result.b != null) {
                UIInput inn = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:inn");
                UIInput kpp = (UIInput) context.getViewRoot().findComponent("mainFRM:mainTabView:kpp");
                inn.setValid(false);
                kpp.setValid(false);
                String error = MessageFormat.format(MsgUtils.getMessageLabel("PartnerIsExsist"), new Object[]{partner.getInn(), partner.getKpp()});
                errors.add(error);
            }
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
            partnerName.append(partner.getType().getName()).append(" \"");
        }
        partnerName.append(partner.getName());
        partnerName.append("\"");

        getEditedItem().setFullName(partnerName.toString());
        onItemChange();
    }
    
    /* Обновление после закрытия формы документа, открытой из карточки контрагента */
    public void onUpdateAfterCloseDocForm(SelectEvent event){        
        Tuple<String, String> tuple = (Tuple) event.getObject();
        if (SysParams.EXIT_NEED_UPDATE.equals(tuple.a)){
            PrimeFaces.current().ajax().update("mainFRM:mainTabView:tblDocs");            
        }
    }

    public void onUpdateProcesses(){
        
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