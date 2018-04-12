package com.maxfill.facade.treelike;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.facade.DocFacade;
import com.maxfill.facade.DocTypeFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.companies.Company;
import com.maxfill.model.departments.Department;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.folders.FolderLog;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.folders.FolderStates;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.staffs.Staff;
import com.maxfill.model.staffs.Staff_;
import com.maxfill.model.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.*;

/* Папки  */
@Stateless
public class FoldersFacade extends BaseDictFacade<Folder, Folder, FolderLog, FolderStates>{
    @EJB
    private DocTypeFacade docTypeFacade;
    @EJB
    private DocFacade docFacade;

    public FoldersFacade() {
        super(Folder.class, FolderLog.class, FolderStates.class);
    }

    @Override
    public Class<Folder> getItemClass() {
        return Folder.class;
    }

    @Override
    public String getFRM_NAME() {
        return Folder.class.getSimpleName().toLowerCase();
    }      
       
    @Override
    public void setSpecAtrForNewItem(Folder folder, Map<String, Object> params) {
        folder.setModerator(folder.getAuthor());
        folder.setDocTypeDefault(docTypeFacade.find(SysParams.DEFAULT_DOC_TYPE_ID));
    }

    /**
     * Отбирает все подчинённые объекты для владельца
     * @param owner
     * @return
     */
    public List<BaseDict> findAllDetailItems(Folder owner){
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);
        Predicate crit = builder.equal(c.get(Doc_.owner), owner);
        cq.select(c).where(builder.and(crit));
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    /**
     * Формирование прав доступа к папке
     */
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

    /* Получение прав для документов в папке */
    @Override
    public Rights getRightForChild(BaseDict item){
        if (item == null) return null;

        if (!item.isInheritsAccessChilds()) { //если не наследует права
            return getActualRightChildItem((Folder)item);
        }

        if (item.getParent() != null) {
            return getRightForChild(item.getParent()); //получаем права от родителя
        }

        return docFacade.getDefaultRights(); //если иного не найдено, то берём дефолтные права справочника
    }

    /* Возвращает все папки */
    public List<Folder> findAllFolders(){ 
        getEntityManager().getEntityManagerFactory().getCache().evict(Folder.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Folder> cq = builder.createQuery(Folder.class);
        Root<Folder> c = cq.from(Folder.class);
        cq.orderBy(builder.asc(c.get("name")));
        Query q = getEntityManager().createQuery(cq);      
        return q.getResultList(); 
    }    

    /* Поиск папок по виду документа, указанного в дефолтном поле  */
    public List<Folder> findFoldersByDocTyper(DocType docType){
        getEntityManager().getEntityManagerFactory().getCache().evict(Folder.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Folder> cq = builder.createQuery(Folder.class);
        Root<Folder> c = cq.from(Folder.class);        
        Predicate crit1 = builder.equal(c.get("docTypeDefault"), docType);
        Predicate crit2 = builder.equal(c.get("deleted"), false);
        cq.select(c).where(builder.and(crit1, crit2));
        Query q = getEntityManager().createQuery(cq);       
        return q.getResultList(); 
    }

    /*
     * Проверка возможности добавлять документы в указанной папке, указанному пользователю
     * @return true - можно, false - нельзя
     */
    public boolean checkRightAddDetail(Folder folder, User user){
        actualizeRightItem(folder, user);
        return isHaveRightAddChild(folder);
    }

    /**
     * Определяет специфичный порядок сортировки папок
     * @param builder
     * @param root
     * @return
     */
    @Override
    protected List<Order> orderBuilder(CriteriaBuilder builder, Root root){
        List<Order> orderList = new ArrayList();
        orderList.add(builder.asc(root.get("folderNumber")));
        orderList.add(builder.asc(root.get("name")));
        return orderList;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_FOLDERS;
    }

    @Override
    public void replaceItem(Folder oldItem, Folder newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
