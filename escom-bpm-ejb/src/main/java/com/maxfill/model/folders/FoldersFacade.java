package com.maxfill.model.folders;

import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.docs.DocFacade;
import com.maxfill.model.docs.docsTypes.DocTypeFacade;
import com.maxfill.model.BaseDict;
import com.maxfill.model.docs.Doc;
import com.maxfill.model.docs.Doc_;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.SysParams;
import com.maxfill.model.rights.Rights;
import com.maxfill.model.users.User;
import com.maxfill.model.users.User_;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    protected void detectParentOwner(Folder item, BaseDict parent, BaseDict owner){
        item.setOwner(null);
        item.setParent((Folder)parent);
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
    @Override
    public List<BaseDict> findDetailItems(Folder owner){        
        getEntityManager().getEntityManagerFactory().getCache().evict(Doc.class);
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Doc> cq = builder.createQuery(Doc.class);
        Root<Doc> c = cq.from(Doc.class);
        Predicate crit = builder.equal(c.get(Doc_.owner), owner);
        cq.select(c).where(builder.and(crit));
        cq.orderBy(builder.asc(c.get("name")));
        Query query = getEntityManager().createQuery(cq);        
        return query.getResultList();
    }

    /**
     * Формирование прав доступа к папке
     * @param item
     * @param user
     * @return 
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

    /**
     * Замена папки на другую папку в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(Folder oldItem, Folder newItem) {
        int count = replaceFolders(oldItem, newItem);
        count = count + replaceFoldersInUsers(oldItem, newItem);
        return count;
    }

    /**
     * Выполняет замену папки в папках
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceFolders(Folder oldItem, Folder newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<Folder> update = builder.createCriteriaUpdate(Folder.class);
        Root root = update.from(Folder.class);
        update.set(root.get("parent"), newItem);
        Predicate predicate = builder.equal(root.get("parent"), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }

    /**
     * Выполняет замену папки в пользователях
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceFoldersInUsers(Folder oldItem, Folder newItem){
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<User> update = builder.createCriteriaUpdate(User.class);
        Root root = update.from(User.class);
        update.set(User_.inbox, newItem);
        Predicate predicate = builder.equal(root.get(User_.inbox), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }
}
