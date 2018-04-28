package com.maxfill.facade;

import com.maxfill.facade.base.BaseDictFacade;
import com.maxfill.model.docs.docsTypes.DocType;
import com.maxfill.model.docs.docsTypes.DocType_;
import com.maxfill.model.numPuttern.NumeratorPatternLog;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.numPuttern.NumeratorPatternStates;

import javax.ejb.Stateless;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Шаблоны нумераторв
 * @author mfilatov
 */
@Stateless
public class NumeratorPatternFacade extends BaseDictFacade<NumeratorPattern, NumeratorPattern, NumeratorPatternLog, NumeratorPatternStates>{

    public NumeratorPatternFacade() {
        super(NumeratorPattern.class, NumeratorPatternLog.class, NumeratorPatternStates.class);
    }

    @Override
    public Class<NumeratorPattern> getItemClass() {
        return NumeratorPattern.class;
    }

    @Override
    public String getFRM_NAME() {
        return NumeratorPattern.class.getSimpleName().toLowerCase();
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_NUMERATOR;
    }

    /**
     * Замена шаблона нумератора в связанных объектах
     * @param oldItem
     * @param newItem
     * @return
     */
    @Override
    public int replaceItem(NumeratorPattern oldItem, NumeratorPattern newItem) {
        int count = replaceNumeratorPatternInDocTypes(oldItem, newItem);
        return count;
    }

    /**
     * Замена шаблона нумератора в видах документов
     * @param oldItem
     * @param newItem
     * @return
     */
    private int replaceNumeratorPatternInDocTypes(NumeratorPattern oldItem, NumeratorPattern newItem) {
        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
        CriteriaUpdate<DocType> update = builder.createCriteriaUpdate(DocType.class);
        Root root = update.from(DocType.class);
        update.set(DocType_.numerator, newItem);
        Predicate predicate = builder.equal(root.get(DocType_.numerator), oldItem);
        update.where(predicate);
        Query query = getEntityManager().createQuery(update);
        return query.executeUpdate();
    }

}
