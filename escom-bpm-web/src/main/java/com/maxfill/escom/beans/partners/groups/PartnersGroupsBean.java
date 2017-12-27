package com.maxfill.escom.beans.partners.groups;

import com.maxfill.dictionary.DictExplForm;
import com.maxfill.escom.beans.BaseExplBean;
import com.maxfill.facade.PartnersGroupsFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.escom.beans.BaseTreeBean;
import com.maxfill.escom.beans.partners.PartnersBean;
import com.maxfill.model.BaseDict;
import com.maxfill.model.partners.Partner;
import com.maxfill.facade.PartnersFacade;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.model.rights.Rights;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/* Сервисный бин "Группы контрагентов" */
@Named
@SessionScoped
public class PartnersGroupsBean extends BaseTreeBean<PartnerGroups, PartnerGroups> {
    private static final long serialVersionUID = 6220113121230925868L;
    
    @Inject
    private PartnersBean partnerBean;
    
    @EJB
    private PartnersGroupsFacade itemsFacade;
    @EJB
    private PartnersFacade partnersFacade;
    
    /* Формирование контента группы контрагента */     
    @Override
    public List<BaseDict> makeGroupContent(BaseDict partnerGroup, Integer viewMode) {
        List<BaseDict> cnt = new ArrayList();
        //загружаем в контент группы контрагента
        List<PartnerGroups> groups = itemsFacade.findActualChilds((PartnerGroups)partnerGroup);
        groups.stream().forEach(group -> addChildItemInContent(group, cnt));
        if (Objects.equals(viewMode, DictExplForm.EXPLORER_MODE)){
            //загружаем в контент контрагентов 
            List<Partner> partners = partnersFacade.findActualDetailItems((PartnerGroups)partnerGroup);
            partners.stream().forEach(partner -> addDetailItemInContent(partner, cnt));
        }
        return cnt;
    }

    @Override
    public PartnersGroupsFacade getItemFacade() {
        return itemsFacade;
    }

    @Override
    public List<PartnerGroups> getGroups(PartnerGroups item) {
        return null;
    }
                
    @Override
    public void preparePasteItem(PartnerGroups pasteItem, PartnerGroups sourceItem, BaseDict target){ 
        super.preparePasteItem(pasteItem, sourceItem, target);
        pasteItem.setParent((PartnerGroups)target);
    }
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(PartnerGroups group){
        List<List<?>> dependency = new ArrayList<>();
        dependency.add(group.getChildItems());
        return dependency;
    }
    
    /* Формирует число ссылок на partnerGroups в связанных объектах   */
    @Override
    public void doGetCountUsesItem(PartnerGroups partnerGroups,  Map<String, Integer> rezult){
        rezult.put("Partners", partnerGroups.getDetailItems().size());
        rezult.put("PartnersGroups", partnerGroups.getChildItems().size());
    }    
    
    /* Проверка возможности удаления partnerGroups */
    @Override
    protected void checkAllowedDeleteItem(PartnerGroups partnerGroups, Set<String> errors){
        // Группу контрагентов можно удалить без ограничений, т.к. контрагенты не удаляются, а только ссылки на них
    }

    /* Обработка события перемещения подчинённых объектов при перемещение группы контрагента в корзину */
    @Override
    protected void moveDetailItemsToTrash(PartnerGroups item, Set<String> errors) {          
        // При перемещение группы контрагента в корзину ничего с контрагентами делать не нужно 
    }
    
    /* Обработка события удаление подчинённых объектов при удалении группы контрагента*/
    @Override
    protected void deleteDetails(PartnerGroups partnerGroups) {
        // При удалении группы удалять контрагентов не нужно!
    }

    @Override
    public Class<PartnerGroups> getOwnerClass() {
        return null;
    }

    @Override
    public BaseExplBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseExplBean getDetailBean() {
        return partnerBean;
    }

}