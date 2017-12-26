package com.maxfill.escom.system.validator;

import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.facade.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

@FacesValidator(UserFolderValidator.VALIDATOR_ID)
public class UserFolderValidator extends AbstractValidator{
    public static final String VALIDATOR_ID = "userFolderValidator";

    @EJB
    private FoldersFacade folderFacade;

    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if(StringUtils.isBlank((String) value)) {
            return;
        }

        Folder folder = (Folder) value;

        UserBean userBean = EscomBeanUtils.findBean("userBean", context);
        User currentUser = userBean.getCurrentUser();
        if(!folderFacade.checkRightAddDetail(folder, currentUser)) {
            String errMsg = getMessageFromBundle(context, "SelectedFolderCantNotAddDocs");
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, errMsg, ""));
        }
    }
}
