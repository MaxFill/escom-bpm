package com.maxfill.model.process.schemes.templates;

import com.maxfill.model.BaseLogItems;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author maksim
 */
@Entity
@Table(name = "schemeTemplatesLog")
@DiscriminatorColumn(name="REF_TYPE")
public class SchemeTemplatesLog extends BaseLogItems<SchemeTemplates> {
    private static final long serialVersionUID = -7301196432966137396L;
    
    /* *** *** */


 
    /* *** *** */
}
