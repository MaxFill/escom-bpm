
package com.maxfill.model.docs.docStatuses;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 * Фасад для статусов документа
 * @author mfilatov
 */
@Stateless
public class DocStatusFacade extends BaseFacade<DocStatuses>{
    
    public DocStatusFacade() {
        super(DocStatuses.class);
    }
 
}
