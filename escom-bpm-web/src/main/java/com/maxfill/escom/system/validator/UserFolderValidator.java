package com.maxfill.escom.system.validator;

import com.maxfill.escom.beans.folders.FoldersBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.model.folders.FoldersFacade;
import com.maxfill.model.folders.Folder;
import com.maxfill.model.users.User;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

@FacesValidator(UserFolderValidator.VALIDATOR_ID)
public class UserFolderValidator extends AbstractValidator{
    public static final String VALIDATOR_ID = "escom.userFolderValidator";

    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Folder folder = (Folder) component.getAttributes().get("item");
        if (folder == null) return;
        FoldersBean foldersBean = EscomBeanUtils.findBean("foldersBean", context);
        User currentUser = foldersBean.getCurrentUser();
        FoldersFacade folderFacade = foldersBean.getFacade();
        if(!folderFacade.checkRightAddDetail(folder, currentUser)) {
            String errMsg = MsgUtils.getMessageLabel("SelectedFolderCantNotAddDocs");
            String checkError = MsgUtils.getValidateLabel("CHECK_ERROR");
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, errMsg, checkError));
        }
    }
}