package com.maxfill.facade;

import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsLog;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroups;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.docs.docsTypes.docTypeGroups.DocTypeGroupsStates;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/* Фасад для сущности "Группы видов документов" */
@Stateless
public class DocTypeGroupsFacade extends BaseDictFacade<DocTypeGroups, DocTypeGroups, DocTypeGroupsLog, DocTypeGroupsStates> {

    @EJB
    private DocTypeFacade docTypeFacade;

    public DocTypeGroupsFacade() {
        super(DocTypeGroups.class, DocTypeGroupsLog.class, DocTypeGroupsStates.class);
    }

    @Override
    public Class<DocTypeGroups> getItemClass() {
        return DocTypeGroups.class;
    }

    @Override
    public String getFRM_NAME() {
        return DocTypeGroups.class.getSimpleName().toLowerCase();
    }

    @Override
    public Rights getRightItem(BaseDict item, User user) {
        if (item == null) return null;

        if (!item.isInherits()) {
            return getActualRightItem(item, user); //получаем свои права
        }

        if (item.getParent() != null) {
            return getRightItem(item.getParent(), user); //получаем права от родительской группы
        }
        return getDefaultRights(item);
    }

    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((DocTypeGroups) item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return docTypeFacade.getDefaultRights();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_DOC_TYPE_GROUP;
    }

    @Override
    public void replaceItem(DocTypeGroups oldItem, DocTypeGroups newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
