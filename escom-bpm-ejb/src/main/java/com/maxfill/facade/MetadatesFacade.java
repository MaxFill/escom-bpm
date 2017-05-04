
package com.maxfill.facade;

import com.maxfill.model.metadates.Metadates;
import javax.ejb.Stateless;

/**
 *
 * @author mfilatov
 */
@Stateless
public class MetadatesFacade extends BaseFacade<Metadates> {
    
    public MetadatesFacade() {
        super(Metadates.class);
    }

}
