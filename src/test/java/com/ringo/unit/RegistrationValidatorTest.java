package com.ringo.unit;

import com.ringo.mock.model.RegistrationFormMock;
import com.ringo.mock.model.RegistrationSubmissionMock;
import com.ringo.model.form.MultipleChoiceQuestion;
import com.ringo.model.form.RegistrationForm;
import com.ringo.model.form.RegistrationSubmission;
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
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void invalidFormThrowsException() {
        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        MultipleChoiceQuestion multipleChoiceQuestion = (MultipleChoiceQuestion) registrationForm.getQuestions().get(1);
        multipleChoiceQuestion.setOptions(null);

        try {
            registrationValidator.throwIfFormInvalid(registrationForm);
            assert false;
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Choices are not specified for question [id: 1]");
        }
    }

    @Test
    void validSubmissionSuccess() {
        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();

        try {
            registrationValidator.throwIfSubmissionInvalid(registrationForm, submission);
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void invalidSubmissionThrowsExceptionQuestionDoesntExist() {
        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();
        submission.getAnswers().get(2).setQuestionId(3);

        try {
            registrationValidator.throwIfSubmissionInvalid(registrationForm, submission);
            assert false;
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Answer for question [id: 3] is invalid");
        }
    }

    @Test
    void invalidSubmissionThrowsExceptionRequiredQuestionNotAnswered() {
        RegistrationForm registrationForm = RegistrationFormMock.getRegistrationFormMock();
        RegistrationSubmission submission = RegistrationSubmissionMock.getRegistrationSubmissionMock();
        submission.getAnswers().remove(0);

        try {
            registrationValidator.throwIfSubmissionInvalid(registrationForm, submission);
            assert false;
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Not all required questions are answered");
        }
    }
}
