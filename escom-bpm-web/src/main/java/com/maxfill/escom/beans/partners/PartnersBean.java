package com.maxfill.escom.beans.partners;

import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.basedict.partner.PartnersFacade;
import com.maxfill.model.basedict.partner.Partner;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.BaseExplBeanGroups;
import com.maxfill.escom.beans.explorer.SearcheModel;
import com.maxfill.escom.beans.partners.groups.PartnersGroupsBean;
import com.maxfill.model.basedict.BaseDict;
import com.maxfill.model.basedict.doc.DocFacade;
import com.maxfill.model.basedict.partnerGroups.PartnerGroups;
import org.primefaces.model.TreeNode;
import javax.ejb.EJB;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

/* Сервисный бин "Контрагенты" */
@Named
@SessionScoped
public class PartnersBean extends BaseExplBeanGroups<Partner, PartnerGroups>{
    private static final long serialVersionUID = -6099934518557372507L;
    
    @Inject
    private PartnersGroupsBean groupsBean;
            
    @EJB 
    private PartnersFacade itemsFacade;    
    @EJB 
    private DocFacade docFacade;     
    
    @Override
    public SearcheModel initSearcheModel() {
        return new PartnersSearche();
    }
    
    @Override
    public PartnersFacade getLazyFacade() {
        return itemsFacade;
    }

    @Override
    public BaseDetailsBean getDetailBean(){
        return null;
    }          
    
    /* Контрагента нужно копировать в случае если он вставляется не в группу или если в ту же группу. В других случаях только добавление ссылки */
    @Override
    public boolean isNeedCopyOnPaste(Partner sourceItem, BaseDict recipient){
        if (!(recipient instanceof PartnerGroups)){
            return true;
        }
        if (sourceItem.getPartnersGroupsList().contains((PartnerGroups)recipient)){
            return true;
        }
        return false;
    }
    
    @Override
    public void preparePasteItem(Partner pasteItem, Partner sourceItem, BaseDict recipient){
        super.preparePasteItem(pasteItem, sourceItem, recipient);
        if (!isNeedCopyOnPaste(pasteItem, recipient)){
            addItemToGroup(pasteItem, recipient);
        }
    } 
    
    @Override
    public boolean addItemToGroup(Partner partner, BaseDict targetGroup){
        if (partner == null || targetGroup == null) return false;
        
        PartnerGroups group = (PartnerGroups)targetGroup;
        if (!partner.getPartnersGroupsList().contains(group)){
            partner.getPartnersGroupsList().add(group);          
            group.getPartnersList().add(partner);
            getLazyFacade().edit(partner);
            return true;
        }
        return false;
    }       
    
    @Override
    public List<PartnerGroups> getGroups(Partner partner){
        return partner.getPartnersGroupsList();
    } 
    
    @Override
    public void doGetCountUsesItem(Partner partner,  Map<String, Integer> rezult){
        rezult.put("Documents", docFacade.findDocsByPartner(partner).size());
    }    
    
    @Override
    protected void checkAllowedDeleteItem(Partner partner, Set<String> errors){
        super.checkAllowedDeleteItem(partner, errors);
        if (!docFacade.findDocsByPartner(partner).isEmpty()){
            Object[] messageParameters = new Object[]{partner.getName()};
            String error = MessageFormat.format(MsgUtils.getMessageLabel("PartnerUsedInDocs"), messageParameters);
            errors.add(error);
        }
    }           
    
    @Override
    protected void actualizeRightForDropItem(BaseDict dropItem){
        groupsBean.getLazyFacade().actualizeRightItem(dropItem, getCurrentUser());
    }
    
    @Override
    public void moveItemToGroup(BaseDict targetGroup, Partner partner, TreeNode sourceNode) {        
        if (sourceNode != null){
            PartnerGroups sourceGroup = (PartnerGroups)sourceNode.getData();
            partner.getPartnersGroupsList().remove(sourceGroup);
            sourceGroup.getPartnersList().remove(partner);
        }                             
        partner.getPartnersGroupsList().add((PartnerGroups)targetGroup);
        getLazyFacade().edit(partner);
    }

    @Override
    public Class<PartnerGroups> getOwnerClass() {
        return PartnerGroups.class;
    }    

    @Override
    public BaseDetailsBean getOwnerBean() {
        return null;
    }
    
    @Override
    public BaseDetailsBean getGroupBean() {
        return groupsBean;
    }
    
    public String getTitleName(Partner partner){
        StringBuilder builder = new StringBuilder();        
        if (partner.getType() != null){
           builder.append(partner.getType().getName()).append(" ");
        }
        if (StringUtils.isNotBlank(partner.getName())){
            builder.append(partner);
        }
        if (builder.length() == 0){
            builder.append(MsgUtils.getBandleLabel("NewPartner"));
        }
        return builder.toString();
    }

}