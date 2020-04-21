package com.fanap.midhco.ui.component.validator;

import com.fanap.midhco.appstore.applicationUtils.SimpleNumberComparer;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Created by admin123 on 6/22/2016.
 */
public class NumberValidator {

    public static IValidator minimum(final Number threshold)  {
        return new IValidator() {
            @Override
            public void validate(IValidatable iValidatable) {
                Object fieldValueAsObject = iValidatable.getValue();
                if(!(fieldValueAsObject instanceof Number)) {
                    ValidationError validationError = new ValidationError();
                    validationError.addKey("IConverter");
                    iValidatable.error(validationError);
                } else {
                    Number fieldValue = (Number)fieldValueAsObject;
                    if(SimpleNumberComparer.isLessThan(fieldValue, threshold)) {
                        ValidationError validationError = new ValidationError();
                        validationError.addKey("NumberValidator.minimum");
                        validationError.setVariable("minimum", threshold);
                    }
                }
            }
        };
    }

    public static IValidator maximum(final Number threshold)  {
        return new IValidator() {
            @Override
            public void validate(IValidatable iValidatable) {
                Object fieldValueAsObject = iValidatable.getValue();
                if(!(fieldValueAsObject instanceof Number)) {
                    ValidationError validationError = new ValidationError();
                    validationError.addKey("IConverter");
                    iValidatable.error(validationError);
                } else {
                    Number fieldValue = (Number)fieldValueAsObject;
                    if(SimpleNumberComparer.isBiggerThan(fieldValue, threshold)) {
                        ValidationError validationError = new ValidationError();
                        validationError.addKey("NumberValidator.maximum");
                        validationError.setVariable("minimum", threshold);
                    }
                }
            }
        };
    }

    public static IValidator range(final Number minimum, final Number maximum)  {
        return new IValidator() {
            @Override
            public void validate(IValidatable iValidatable) {
                Object fieldValueAsObject = iValidatable.getValue();
                if(!(fieldValueAsObject instanceof Number)) {
                    ValidationError validationError = new ValidationError();
                    validationError.addKey("IConverter");
                    iValidatable.error(validationError);
                } else {
                    Number fieldValue = (Number)fieldValueAsObject;
                    if(SimpleNumberComparer.isBiggerThanEqual(fieldValue, minimum) &&
                            SimpleNumberComparer.isLessThanEqual(fieldValue, maximum)) {
                        ValidationError validationError = new ValidationError();
                        validationError.addKey("StringValidator.range");
                        validationError.setVariable("minimum", minimum);
                        validationError.setVariable("maximum", maximum);
                    }
                }
            }
        };
    }
}
