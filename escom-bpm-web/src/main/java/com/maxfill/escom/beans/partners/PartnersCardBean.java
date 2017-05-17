package com.maxfill.escom.beans.partners;

import com.maxfill.facade.PartnersFacade;
import com.maxfill.model.partners.Partner;
import com.maxfill.escom.beans.BaseCardBeanGroups;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.model.partners.types.PartnerTypes;
import com.maxfill.facade.PartnerTypesFacade;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.utils.Tuple;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  Контрагенты
 * @author mfilatov
 */
@Named
@ViewScoped
public class PartnersCardBean extends BaseCardBeanGroups<Partner, PartnerGroups>{
    private static final long serialVersionUID = -972953149732759437L;
    
    @EJB 
    private PartnersFacade itemsFacade;
    @EJB
    private PartnerTypesFacade partnerTypesFacade;

    private List<PartnerTypes> partnerTypes;

    @Override
    public PartnersFacade getItemFacade() {
        return itemsFacade;
    }         
    
    /**
     * Проверка корректности Контрагента перед сохранением карточки
     *
     * @param partner
     * @param errors
     */
    @Override
    protected void checkItemBeforeSave(Partner partner, Set<String> errors) {       
        String code = partner.getCode();
        Integer partnerId = partner.getId();
        List<Partner> existPartner = getItemFacade().findByCodeExclId(code, partnerId);
        if (!existPartner.isEmpty()) {
            Object[] params = new Object[]{code};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("PartnerCodeIsExsist"), params);
            errors.add(error);
        }
        
        existPartner = getItemFacade().findByNameAndTypeExclId(partner.getName(), partner.getType(), partnerId);
        if (!existPartner.isEmpty()) {
            Object[] params = new Object[]{partner.getTitleName()};
            String error = MessageFormat.format(EscomBeanUtils.getMessageLabel("PartnerIsExsist"), params);
            errors.add(error);
        }
    }
    
    /**
     * Формирование полного наименования контрагента
     */
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
    
    /**
     * Обновление после закрытия формы документа, открытой из карточки контрагента
     * @param event 
     */
    public void onUpdateAfterCloseDocForm(SelectEvent event){
        Tuple<Boolean, Doc> tuple = (Tuple) event.getObject();
        Boolean isNeedUdate = tuple.a;
        if (isNeedUdate){
            RequestContext context = RequestContext.getCurrentInstance();
            context.update("itemCard:mainTabView:tblDocs");
            Doc item = tuple.b;
            EscomBeanUtils.SuccesFormatMessage("Successfully", "DataIsSaved", new Object[]{item.getName()});
        }
    }

    public List<PartnerTypes> getPartnerTypes() {
        if (partnerTypes == null){
            partnerTypes = partnerTypesFacade.findAll().stream()
                    .filter(item -> preloadCheckRightView(item))
                    .collect(Collectors.toList());
        }
        return partnerTypes;
    }

    @Override
    public List<PartnerGroups> getGroups(Partner partner) {
        return partner.getPartnersGroupsList();
    }

    @Override
    protected void afterCreateItem(Partner item) {        
    }

    @Override
    public Class<Partner> getItemClass() {
        return Partner.class;
    }
}