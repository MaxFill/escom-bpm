package com.maxfill.escom.system.validator;

import com.maxfill.escom.beans.users.UserBean;
import com.maxfill.escom.utils.EscomBeanUtils;
import com.maxfill.escom.utils.MsgUtils;
import com.maxfill.facade.UserFacade;
import org.apache.commons.lang3.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;
import java.text.MessageFormat;

@FacesValidator(UserEmailValidator.VALIDATOR_ID)
public class UserEmailValidator extends AbstractValidator{
    public static final String VALIDATOR_ID = "escom.userEmailValidator";

    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if(StringUtils.isBlank((String) value)) {
            return;
        }

        String email = (String) value;
        UserBean userBean = EscomBeanUtils.findBean("userBean", context);
        UserFacade userFacade = userBean.getFacade();

        Integer userId = (Integer)component.getAttributes().get("userId");
        if (userFacade.checkEmailDuplicate(userId, email)) {
            String bundleKey = MsgUtils.getValidateLabel("DUBLICATE_EMAIL");
            String msgError = MessageFormat.format(bundleKey, new Object[]{email});
            String checkError = MsgUtils.getValidateLabel("CHECK_ERROR");
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msgError, checkError));
        }
    }

}