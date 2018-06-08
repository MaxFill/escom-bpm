package com.maxfill.model.process.schemes.templates;

import com.maxfill.model.states.BaseStateItem;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author maksim
 */
@Entity
@Table(name = "schemeTemplatesStates")
@DiscriminatorColumn(name="REF_TYPE")
public class SchemeTemplatesStates extends BaseStateItem{
    private static final long serialVersionUID = 3301975048816630970L;
    
    /* *** *** */

   
    /* *** *** */
    
}
