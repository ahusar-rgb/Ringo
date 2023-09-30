package com.ringo.service.company;

import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.company.TicketType;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.model.payment.JoiningIntentStatus;
import com.ringo.repository.JoiningIntentRepository;
import com.ringo.service.payment.PaymentData;
import com.ringo.service.payment.PaymentService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JoiningIntentService {
    private final JoiningIntentRepository joiningIntentRepository;
    private final PaymentService paymentService;

    public JoiningIntent create(Participant participant, Event event, TicketType ticketType, RegistrationSubmission submission) {
        PaymentIntent paymentIntent = paymentService.initPayment(
                new PaymentData(
                        event.getHost().getStripeAccountId(),
                        ticketType.getPrice(),
                        ticketType.getCurrency(),
                        participant.getId().toString()  + "_" +
                                event.getId().toString() + "_" +
                                ticketType.getId().toString()
                )
        );

        if(joiningIntentRepository.findByPaymentIntentId(paymentIntent.getId()).isPresent())
            throw new RuntimeException("Payment already exists");

        JoiningIntent joiningIntent = JoiningIntent.builder()
                .participant(participant)
                .event(event)
                .paymentIntentId(paymentIntent.getId())
                .status(JoiningIntentStatus.CREATED)
                .paymentIntentClientSecret(paymentIntent.getClientSecret())
                .ticketType(ticketType)
                .registrationSubmission(submission)
                .createdAt(LocalDateTime.now())
                .build();

        return joiningIntentRepository.save(joiningIntent);
    }


    public JoiningIntent createNoPayment(Participant participant, Event event, TicketType ticketType, RegistrationSubmission submission) {
        JoiningIntent joiningIntent = JoiningIntent.builder()
                .participant(participant)
                .event(event)
                .status(JoiningIntentStatus.NO_PAYMENT)
                .ticketType(ticketType)
                .registrationSubmission(submission)
                .createdAt(LocalDateTime.now())
                .build();

        return joiningIntentRepository.save(joiningIntent);
    }

    public JoiningIntent changeStatus(String paymentIntentId, JoiningIntentStatus status) {
        JoiningIntent joiningIntent = joiningIntentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        joiningIntent.setStatus(status);
        return joiningIntentRepository.save(joiningIntent);
    }
}
