package com.maxfill.model.core.forms;

import com.maxfill.facade.BaseFacade;
import javax.ejb.Stateless;

/**
 * Фасад для сущности "Настройки форм"
 * @author maksim
 */
@Stateless
public class FormsSettingsFacade extends BaseFacade<FormsSettings>{
    
    public FormsSettingsFacade() {
        super(FormsSettings.class);
    }
    
}
