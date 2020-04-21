package com.fanap.midhco.ui.component.limitedTextField;

import com.fanap.midhco.appstore.wicketApp.AppStorePropertyReader;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by admin123 on 6/25/2016.
 */
public class LimitedTextField extends TextField implements Serializable {
    Integer maxLength;
    Boolean isEnglish;
    Boolean isNumeric;
    boolean textWithNumeric;
    List<String> validatorString;

    public List<String> getValidatorString() {
        return validatorString;
    }


    public LimitedTextField(String id) {
        this(id, null, "");
    }

    public LimitedTextField(String id, String label) {
        this(id, null, "");
    }

    public LimitedTextField(String id, Boolean isEnglish, String label) {
        this(id, isEnglish, false, "");
    }

    public LimitedTextField(String id, Boolean isEnglish, Integer maxLength, String label) {
        this(id, isEnglish, false, true, null, null, maxLength, "");
    }

    public LimitedTextField(String id, Boolean isEnglish, Boolean isNumeric, String label) {
        this(id, isEnglish, isNumeric, true, null, null, 200, "");
    }

    public LimitedTextField(String id, Boolean isEnglish, Boolean isNumeric, Boolean textWithNumeric, Boolean isText,
                            Boolean usePunctuation, Integer maxLength, String label) {
        super(id);
        this.isEnglish = isEnglish;
        this.isNumeric = isNumeric;
        this.maxLength = maxLength;
        this.textWithNumeric = textWithNumeric;
        add(new IValidator() {
            @Override
            public void validate(IValidatable iValidatable) {
                String inputString = getInput();
                validatorString = new ArrayList<>();
                boolean hasCorrectChar = true;
                if (inputString != null && !inputString.trim().equals("")) {

                    if (inputString.length() > maxLength) {
                        validatorString.add(AppStorePropertyReader.getString("invalid.data.size").replace("${maxSize}", maxLength.toString()).replace("${label}", label));
                    }
//                    if (Pattern.matches("(.)*([/`‍‍~\\\\])(.)*", inputString)) {
//                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                        hasCorrectChar = false;
//                    } else if (Pattern.matches("(.)*([ـ{}\"+=&@#%$()/_<>-])(.)*", inputString)) {
//                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                        hasCorrectChar = false;
//                    } else if (isText != null && !isText && (Pattern.matches("(.)*([\"\\.&,@!?#%'$()/\\\\ \\-_<>])(.)*", inputString))) {
//                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                        hasCorrectChar = false;
//                    } else if (usePunctuation != null && !usePunctuation && Pattern.matches("(.)*([\"&,;^*،?؟!@#%$()/_<>])(.)*", inputString)) {
//                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                        hasCorrectChar = false;
//                    }

//                    if (hasCorrectChar) {
//                        boolean correctPattern = true;
//
//                        if (isNumeric != null) {
//                            if (isNumeric) {
//                                if (!(Pattern.matches("[\\p{N}]+", inputString))) {
//                                    validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                    correctPattern = false;
//                                }
//                            } else {
//                                if ((Pattern.matches("[\\p{N}]+", inputString))) {
//                                    validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                    correctPattern = false;
//                                }
//                            }
//                        }
//                        if (correctPattern) {
//                            if (textWithNumeric && isEnglish != null) {
//                                if (isEnglish) {
//                                    if (!Pattern.matches("^[[a-z][A-Z][0-9] .'-]+$", inputString)) {
//                                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                    }
//                                } else {
//                                    if (!Pattern.matches("[\\p{InARABIC} .'-[\\u06f0-\\u06f9]+$]", inputString)) {
//                                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                    }
//                                }
//
//                            } else {
//                                if ((Pattern.matches("[\\p{N}]+", inputString) && Pattern.matches("^[\\p{L} .'-]+$", inputString))) {
//                                    validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                } else {
//                                    boolean secondLevelCorrectPattern = true;
//                                    if (isNumeric != null && isNumeric) {
//                                        if (!Pattern.matches("[\\p{N}]+", inputString)) {
//                                            validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                            secondLevelCorrectPattern = false;
//                                        }
//                                    }
//
//                                    if (secondLevelCorrectPattern) {
//                                        if (isEnglish != null) {
//                                            if (isEnglish) {
//                                                if (isNumeric == null || !isNumeric) {
//                                                    if (Pattern.matches("(.)*(\\d)(.)*", inputString)) {
//                                                        if (textWithNumeric == false) {
//                                                            validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                                        }
//                                                    } else if (!Pattern.matches("^[[a-z][A-Z] .'-]+$", inputString)) {
//                                                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                                    }
//                                                }
//                                            } else {
//                                                if (isNumeric == null || !isNumeric) {
//                                                    if (Pattern.matches("(.)*(\\d)(.)*", inputString)) {
//                                                        if (textWithNumeric == false) {
//                                                            validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                                        }
//                                                    } else if (!Pattern.matches("[\\p{InARABIC} .'-]+$", inputString)) {
//                                                        validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                                    }
//                                                }
//                                            }
//                                        } else if (isNumeric == null || !isNumeric) {
//                                            if (Pattern.matches("(.)*(\\d)(.)*", inputString)) {
//                                                if (textWithNumeric == false) {
//                                                    validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                                }
//                                            }
//                                        } else {
//                                            if (!Pattern.matches("[\\p{N}]+", inputString)) {
//                                                validatorString.add(AppStorePropertyReader.getString("invalid.data.char").replace("${label}", label));
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

                }
            }
        });
    }

    @Override
    public void error(IValidationError error) {
        super.error(error);

        if (!error.toString().contains("Required")) {
            List feedbackMessages = Session.get().getFeedbackMessages().toList();
            boolean hasThisMessage = false;
            if (feedbackMessages != null && !feedbackMessages.isEmpty()) {
                for (Object msg : feedbackMessages) {
                    FeedbackMessage message = (FeedbackMessage) msg;
                    if (message.getMessage().equals(AppStorePropertyReader.getString("IConverter.Long"))) {
                        hasThisMessage = true;
                    }
                    if (!hasThisMessage) {
                        Session.get().getFeedbackMessages().add(this, AppStorePropertyReader.getString("IConverter.Long"), 1);
                    }
                }
            } else {
                //Session.get().getFeedbackMessages().add(this, AppStorePropertyReader.getString("IConverter.Long"), 1);
            }
        }

    }


    @Override
    public void convertInput() {
        super.convertInput();
        if (this.validatorString != null && !this.validatorString.isEmpty()) {
            this.validatorString.clear();
        }
    }

}