
package com.maxfill.escom.beans.users.settings;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Maxim
 */
@Named
@ViewScoped
public class UserSettingsBean implements Serializable{
    private static final long serialVersionUID = -3347099990747559193L;

    public UserSettingsBean() {
    }

    /**
     * Сохранение настроек пользователя
     * @param ajax
     */
    public void saveSettings(){
        RequestContext.getCurrentInstance().closeDialog(null);
    }

}
