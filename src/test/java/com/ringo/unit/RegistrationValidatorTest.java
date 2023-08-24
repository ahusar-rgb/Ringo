package com.ringo.unit;

import com.ringo.mock.dto.RegistrationFormMock;
import com.ringo.model.form.MultipleChoiceQuestion;
import com.ringo.model.form.RegistrationForm;
import com.ringo.service.company.RegistrationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class RegistrationValidatorTest {

    @InjectMocks
    private RegistrationValidator registrationValidator;

    @Test
    void validFormSuccess() {
        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();

        try {
            registrationValidator.throwIfFormInvalid(registrationForm);
        } catch (Exception e) {
            assert false;
        }

        assert true;
    }

    @Test
    void invalidFormThrowsException() {
        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        MultipleChoiceQuestion multipleChoiceQuestion = (MultipleChoiceQuestion) registrationForm.getQuestions().get(1);
        multipleChoiceQuestion.setOptions(null);

        try {
            registrationValidator.throwIfFormInvalid(registrationForm);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Choices are not specified for question [id: 1]");
        }
    }

    @Test
    void validSubmissionSuccess() {
        assert false;
    }

    @Test
    void invalidSubmissionThrowsException() {
        assert false;
    }
}
