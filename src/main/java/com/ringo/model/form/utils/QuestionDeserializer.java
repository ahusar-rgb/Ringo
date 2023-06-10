package com.ringo.model.form.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ringo.exception.UserException;
import com.ringo.model.form.*;

import java.io.IOException;
import java.util.Arrays;

public class QuestionDeserializer extends StdDeserializer<Question> {

    public QuestionDeserializer(Class<?> vc) {
        super(vc);
    }

    public QuestionDeserializer() {
        this(null);
    }

    @Override
    public Question deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        String type = node.get("type").asText();
        try {
            return switch (type) {
                case "INPUT_FIELD" -> deserializeInputFieldQuestion(node);
                case "MULTIPLE_CHOICE" -> deserializeMultipleChoiceQuestion(node);
                case "CHECKBOX" -> deserializeCheckboxQuestion(node);
                default -> throw new UserException("Invalid question type: " + type);
            };
        }
        catch (IllegalArgumentException e) {
            throw new UserException("Invalid question structure: " + type);
        }
    }

    private MultipleChoiceQuestion deserializeMultipleChoiceQuestion(JsonNode node) {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion();

        if(node.get("content") != null)
            question.setContent(node.get("content").asText());
        if(node.get("required") != null)
            question.setRequired(node.get("required").asBoolean());
        if(node.get("id") != null)
            question.setId(node.get("id").asLong());

        JsonNode options = node.get("options");
        question.setOptions(Arrays.stream(new ObjectMapper().convertValue(options, Option[].class)).toList());

        return question;
    }

    private CheckboxQuestion deserializeCheckboxQuestion(JsonNode node) {
        CheckboxQuestion question = new CheckboxQuestion();

        if(node.get("content") != null)
            question.setContent(node.get("content").asText());
        if(node.get("required") != null)
            question.setRequired(node.get("required").asBoolean());
        if(node.get("id") != null)
            question.setId(node.get("id").asLong());

        JsonNode options = node.get("options");
        question.setOptions(Arrays.stream(new ObjectMapper().convertValue(options, Option[].class)).toList());
        return question;
    }

    private InputFieldQuestion deserializeInputFieldQuestion(JsonNode node) {
        InputFieldQuestion question = new InputFieldQuestion();

        if(node.get("content") != null)
            question.setContent(node.get("content").asText());
        if(node.get("required") != null)
            question.setRequired(node.get("required").asBoolean());
        if(node.get("maxCharacters") != null)
            question.setMaxCharacters(node.get("maxCharacters").asInt());
        if(node.get("id") != null)
            question.setId(node.get("id").asLong());

        return question;
    }
}
