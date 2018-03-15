package com.maxfill.escom.beans.system.logging;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.AuthLogFacade;
import com.maxfill.model.authlog.Authlog;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;

/* Контролер формы журнала аутентификации пользователей */

@ViewScoped
@Named
public class AuthLogBean extends BaseDialogBean{
    private static final long serialVersionUID = -2035201127652612778L;

    private Authlog selected;
    private List<Authlog> authlogs;

    @EJB
    private AuthLogFacade authLogFacade;

    @Override
    protected void initBean(){
    }

    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    protected String getFormName(){
        return DictDlgFrmName.FRM_AUTH_LOG;
    }

    public String getBundleName(String keyBundle){
        if (keyBundle == null) return null;
        return EscomMsgUtils.getBandleLabel(keyBundle);
    }

    public void refreshData(){
        authlogs = null;
    }

    public List <Authlog> getAuthlogs() {
        if (authlogs == null){
            authlogs = authLogFacade.findAll();
        }
        return authlogs;
    }

    /* gets & sets */

    public Authlog getSelected() {
        return selected;
    }
    public void setSelected(Authlog selected) {
        this.selected = selected;
    }
}
