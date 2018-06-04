package com.maxfill.escom.beans.system.states;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.core.BaseViewBean;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Контролер формы "Карточка состояния"
 */
@Named
@ViewScoped
public class StateCardBean extends BaseViewBean{    
    private static final long serialVersionUID = -5286296381383874923L;

    @Override
    public String getFormName() {
        return DictDlgFrmName.FRM_STATE;
    }
    
}
