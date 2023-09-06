package com.ringo.service.company;

import com.ringo.exception.UserException;
import com.ringo.model.form.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegistrationValidator {
    public void throwIfSubmissionInvalid(RegistrationForm form, RegistrationSubmission submission) {
        if(form != null && submission == null)
            throw new UserException("Event requires registration form");
        if(form == null && submission != null)
            throw new UserException("Event does not require registration form");
        if(submission == null)
            return;


        try {
            if(!form.getQuestions().stream()
                    .filter(Question::isRequired)
                    .allMatch(question ->
                            submission.getAnswers().stream()
                                    .map(Answer::getQuestionId).toList()
                                    .contains(form.getQuestions().indexOf(question)))
            )
                throw new UserException("Not all required questions are answered");

            for(Answer answer : submission.getAnswers()) {
                if(answer.getQuestionId() == null)
                    throw new UserException("Answer is invalid");

                Question question;
                try {
                    question = form.getQuestions().get(answer.getQuestionId());
                } catch (IndexOutOfBoundsException e) {
                    throw new UserException("Answer for question [id: %d] is invalid".formatted(answer.getQuestionId()));
                }

                if(question instanceof MultipleChoiceQuestion multipleChoiceQuestion) {
                    if(answer.getOptionIds() == null ||
                            answer.getOptionIds().size() != 1 ||
                            answer.getOptionIds().get(0) == null ||
                            answer.getOptionIds().get(0) < 0 ||
                            answer.getOptionIds().get(0) >= multipleChoiceQuestion.getOptions().size() ||
                            answer.getContent() != null
                    )
                        throw new UserException("Answer for question [id: %d] is invalid".formatted(answer.getQuestionId()));
                }
                else if (question instanceof CheckboxQuestion checkboxQuestion) {
                    if(answer.getOptionIds() == null ||
                            answer.getOptionIds().isEmpty() ||
                            answer.getOptionIds().stream().anyMatch(id -> id == null || id < 0 || id >= checkboxQuestion.getOptions().size()) ||
                            answer.getContent() != null
                    )
                        throw new UserException("Answer for question [id: %d] is invalid".formatted(answer.getQuestionId()));
                }
                else if (question instanceof InputFieldQuestion) {
                    if(answer.getOptionIds() != null ||
                            answer.getContent() == null)
                        throw new UserException("Answer for question [id: %d] is invalid".formatted(answer.getQuestionId()));
                    if(((InputFieldQuestion) question).getMaxCharacters() < answer.getContent().length())
                        throw new UserException("Answer for question [id: %d] is invalid".formatted(answer.getQuestionId()));
                }
                else
                    throw new UserException("Question [id: %d] is invalid".formatted(answer.getQuestionId()));
            }
        } catch (UserException e) {
            throw e;
        } catch (Exception e) {
            throw new UserException("Submission is invalid");
        }
    }

    public void throwIfFormInvalid(RegistrationForm registrationForm) {
        if(registrationForm != null) {
            if(registrationForm.getQuestions() == null)
                throw new UserException("Questions are not specified");
            for(int i = 0; i < registrationForm.getQuestions().size(); i++) {
                Question question = registrationForm.getQuestions().get(i);
                question.setId((long) i);
                List<Option> options = null;
                if(question instanceof MultipleChoiceQuestion multipleChoiceQuestion) {
                    if(multipleChoiceQuestion.getOptions() == null || multipleChoiceQuestion.getOptions().isEmpty())
                        throw new UserException("Choices are not specified for question [id: %d]".formatted(i));
                    options = multipleChoiceQuestion.getOptions();
                }
                if(question instanceof CheckboxQuestion checkboxQuestion) {
                    if(checkboxQuestion.getOptions() == null || checkboxQuestion.getOptions().isEmpty())
                        throw new UserException("Choices are not specified for question [id: %d]".formatted(i));
                    options = checkboxQuestion.getOptions();
                }
                if(options != null) {
                    for(int j = 0; j < options.size(); j++) {
                        Option option = options.get(j);
                        option.setId((long) j);
                    }
                }
            }
        }
    }
}
