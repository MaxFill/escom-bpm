package com.maxfill.escom.system.validator;

import com.maxfill.escom.utils.MsgUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

@FacesValidator(EmptyValidator.VALIDATOR_ID)
public class EmptyValidator extends AbstractValidator{
    public static final String VALIDATOR_ID = "emptyValidator";

    @Override
    public String getValidatorId() {
        return VALIDATOR_ID;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            String checkError = MsgUtils.getValidateLabel("CHECK_ERROR");
            String errMsg = MsgUtils.getBandleLabel("MustBeFilled");
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, errMsg, checkError));
        }
    }
}
