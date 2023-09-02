package com.ringo.service.company;

import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.model.payment.JoiningIntentStatus;
import com.ringo.repository.StripePaymentRepository;
import com.ringo.service.payment.PaymentData;
import com.ringo.service.payment.PaymentService;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JoiningIntentService {
    private final StripePaymentRepository stripePaymentRepository;
    private final PaymentService paymentService;

    public JoiningIntent create(Participant participant, Event event) {

        PaymentIntent paymentIntent = paymentService.initPayment(
                new PaymentData(
                        event.getHost().getStripeAccountId(),
                        event.getPrice(),
                        event.getCurrency(),
                        participant.getId().toString()
                )
        );

        if(stripePaymentRepository.findByPaymentIntentId(paymentIntent.getId()).isPresent())
            throw new RuntimeException("Payment already exists");

        JoiningIntent joiningIntent = JoiningIntent.builder()
            .participant(participant)
            .event(event)
            .paymentIntentId(paymentIntent.getId())
            .status(JoiningIntentStatus.CREATED)
            .createdAt(LocalDateTime.now())
            .build();

        return stripePaymentRepository.save(joiningIntent);
    }

    public JoiningIntent changeStatus(String paymentIntentId, JoiningIntentStatus status) {
        JoiningIntent joiningIntent = stripePaymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        joiningIntent.setStatus(status);
        return stripePaymentRepository.save(joiningIntent);
    }
}
