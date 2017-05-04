
package com.maxfill.facade;

import com.maxfill.model.docs.statuses.DocsStatus;
import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 * Фасад для статусов документа
 * @author mfilatov
 */
@Stateless
public class DocStatusFacade extends BaseFacade<DocsStatus>{
    
    @Override
    public void remove(DocsStatus entity){
        entity = getEntityManager().getReference(DocsStatus.class, entity.getId());
        getEntityManager().remove(entity);
    }
    
    public DocStatusFacade() {
        super(DocsStatus.class);
    }
 
}
