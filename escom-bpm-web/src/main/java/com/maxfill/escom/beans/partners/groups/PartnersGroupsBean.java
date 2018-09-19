package com.maxfill.escom.beans.partners.groups;

import com.maxfill.dictionary.DictExplForm;
import com.maxfill.escom.beans.core.BaseDetailsBean;
import com.maxfill.escom.beans.core.BaseTableBean;
import com.maxfill.model.partners.groups.PartnersGroupsFacade;
import com.maxfill.model.partners.groups.PartnerGroups;
import com.maxfill.escom.beans.core.BaseTreeBean;
import com.maxfill.escom.beans.partners.PartnersBean;
import com.maxfill.model.BaseDict;
import java.util.*;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
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
    
    /* Формирование контента группы контрагента */     
    @Override
    public List<BaseDict> makeGroupContent(BaseDict partnerGroup, BaseTableBean tableBean, Integer viewMode, int first, int pageSize, String sortField, String sortOrder) {
        List<BaseDict> cnt = new ArrayList<>();
        if (Objects.equals(viewMode, DictExplForm.SELECTOR_MODE)) {
            cnt.addAll(itemsFacade.findActualChilds((PartnerGroups) partnerGroup, getCurrentUser()).collect(Collectors.toList()));
        } else {
            cnt.addAll(itemsFacade.findDetails((PartnerGroups)partnerGroup, first, pageSize, sortField,  sortOrder, getCurrentUser()));
        }
        return cnt;
    }

    @Override
    public PartnersGroupsFacade getFacade() {
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
        pasteItem.setDetailItems(sourceItem.getDetailItems()); //копируем только ссылки!
    }
    
    /* Возвращает списки зависимых объектов, необходимых для копирования */
    @Override
    public List<List<?>> doGetDependency(PartnerGroups group){
        List<List<?>> dependency = new ArrayList<>();
        List<PartnerGroups> partnerGroups = itemsFacade.findActualChilds(group, getCurrentUser()).collect(Collectors.toList());
        if (!partnerGroups.isEmpty()) {
            dependency.add(partnerGroups);
        }
        return dependency;
    }
    
    /* Формирует число ссылок на partnerGroups в связанных объектах   */
    @Override
    public void doGetCountUsesItem(PartnerGroups partnerGroups,  Map<String, Integer> rezult){
        rezult.put("Partners", partnerGroups.getDetailItems().size());
        rezult.put("PartnersGroups", partnerGroups.getChildItems().size());
    }

    /**
     * Проверка возможности удаления Группы контрагентов
     * Группу контрагентов можно удалить без ограничений, т.к. контрагенты не удаляются, а удаляются только ссылки на них
     * @param partnerGroups
     * @param errors
     */
    @Override
    protected void checkAllowedDeleteItem(PartnerGroups partnerGroups, Set<String> errors){
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
    public BaseDetailsBean getOwnerBean() {
        return null;
    }

    @Override
    public BaseDetailsBean getDetailBean() {
        return partnerBean;
    }

}