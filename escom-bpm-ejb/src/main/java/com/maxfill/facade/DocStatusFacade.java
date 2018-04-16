
package com.maxfill.facade;

import com.maxfill.model.docs.docStatuses.DocStatuses;
import com.maxfill.facade.base.BaseFacade;
import javax.ejb.Stateless;

/**
 * Фасад для статусов документа
 * @author mfilatov
 */
@Stateless
public class DocStatusFacade extends BaseFacade<DocStatuses>{
    
    @Override
    public void remove(DocStatuses entity){
        entity = getEntityManager().getReference(DocStatuses.class, entity.getId());
        getEntityManager().remove(entity);
    }
    
    public DocStatusFacade() {
        super(DocStatuses.class);
    }
 
}
