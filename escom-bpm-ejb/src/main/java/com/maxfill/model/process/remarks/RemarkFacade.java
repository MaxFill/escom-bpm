package com.maxfill.model.process.remarks;

import com.maxfill.dictionary.DictMetadatesIds;
import com.maxfill.dictionary.DictObjectName;
import com.maxfill.facade.BaseDictFacade;
import com.maxfill.model.docs.Doc;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author maksim
 */
@Stateless
public class RemarkFacade extends BaseDictFacade<Remark, Doc, RemarkLog, RemarkStates>{

    public RemarkFacade() {
        super(Remark.class, RemarkLog.class, RemarkStates.class);
    }
    
    @Override
    public Class<Remark> getItemClass() {
        return Remark.class;
    }

    @Override
    public int replaceItem(Remark oldItem, Remark newItem) {
        return 0;
    }

    @Override
    protected Integer getMetadatesObjId() {
        return DictMetadatesIds.OBJ_REMARK;
    }

    @Override
    public String getFRM_NAME() {
        return DictObjectName.REMARK.toLowerCase();
    }
    
    @Override
    protected void setSpecAtrForNewItem(Remark item, Map<String, Object> params) {
        item.setDateChange(new Date());
    }
}
