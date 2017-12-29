package com.maxfill.escom.system.validator;

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
        System.out.println();
    }
}
