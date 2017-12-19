package com.maxfill.facade;

import com.maxfill.model.numPuttern.NumeratorPatternLog;
import com.maxfill.model.numPuttern.NumeratorPattern;
import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.model.numPuttern.NumeratorPatternStates;
import javax.ejb.Stateless;

/**
 * Шаблоны нумераторв
 * @author mfilatov
 */
@Stateless
public class NumeratorPatternFacade extends BaseDictFacade<NumeratorPattern, NumeratorPattern, NumeratorPatternLog, NumeratorPatternStates> {

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

    @Override
    public void replaceItem(NumeratorPattern oldItem, NumeratorPattern newItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
