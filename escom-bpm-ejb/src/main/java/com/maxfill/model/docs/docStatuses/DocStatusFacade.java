
package com.maxfill.model.docs.docStatuses;

import com.maxfill.facade.BaseFacade;
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
