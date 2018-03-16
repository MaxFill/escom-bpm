package com.maxfill.escom.beans.system.logging;

import com.maxfill.dictionary.DictDlgFrmName;
import com.maxfill.escom.beans.BaseDialogBean;
import com.maxfill.escom.utils.EscomMsgUtils;
import com.maxfill.facade.AuthLogFacade;
import com.maxfill.model.authlog.Authlog;
import com.maxfill.utils.DateUtils;

import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/* Контролер формы журнала аутентификации пользователей */

@ViewScoped
@Named
public class AuthLogBean extends BaseDialogBean{
    private static final long serialVersionUID = -2035201127652612778L;

    private Authlog selected;
    private List<Authlog> authlogs;
    private Date dateStart;
    private Date dateEnd;

    @EJB
    private AuthLogFacade authLogFacade;

    @Override
    protected void initBean(){
        dateEnd = DateUtils.clearDate(DateUtils.addDays(new Date(), 1));
        dateStart = DateUtils.addDays(dateEnd, -3);
    }

    @Override
    public String onCloseCard() {
        return super.onFinalCloseCard(null);
    }

    @Override
    public String getFormName(){
        return DictDlgFrmName.FRM_AUTH_LOG;
    }

    public String getBundleName(String keyBundle){
        if (keyBundle == null) return null;
        return EscomMsgUtils.getBandleLabel(keyBundle);
    }

    public void refreshData(){
        authlogs = null;
    }

    public void clearData(){
        Integer countDelete = authLogFacade.clearEvents(dateStart, dateEnd);
        authlogs = null;
        EscomMsgUtils.succesFormatMsg("RemovedEntries", new Object[]{countDelete});
    }

    public List <Authlog> getAuthlogs() {
        if (authlogs == null){
            authlogs = authLogFacade.findEventsByPeriod(dateStart, dateEnd);
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

    public Date getDateStart() {
        return dateStart;
    }
    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
}
