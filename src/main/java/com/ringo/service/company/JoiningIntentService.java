package com.ringo.service.company;

import com.ringo.config.ApplicationProperties;
import com.ringo.model.company.Event;
import com.ringo.model.company.Participant;
import com.ringo.model.company.TicketType;
import com.ringo.model.form.RegistrationSubmission;
import com.ringo.model.payment.JoiningIntent;
import com.ringo.model.payment.JoiningIntentStatus;
import com.ringo.repository.JoiningIntentRepository;
import com.ringo.repository.company.EventRepository;
import com.ringo.repository.company.TicketTypeRepository;
import com.ringo.service.payment.PaymentData;
import com.ringo.service.payment.PaymentService;
import com.ringo.service.time.Time;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Service
@RequiredArgsConstructor
public class JoiningIntentService {
    private final JoiningIntentRepository joiningIntentRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentService paymentService;
    private final ApplicationProperties config;
    private final Timer timer = new Timer();

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

        JoiningIntent found = joiningIntentRepository.findActiveByPaymentIntentId(paymentIntent.getId()).orElse(null);
        if(found != null)
            return found;

        JoiningIntent joiningIntent = JoiningIntent.builder()
                .participant(participant)
                .event(event)
                .paymentIntentId(paymentIntent.getId())
                .status(JoiningIntentStatus.CREATED)
                .paymentIntentClientSecret(paymentIntent.getClientSecret())
                .ticketType(ticketType)
                .registrationSubmission(submission)
                .createdAt(Time.getLocalUTC())
                .expiresAt(Time.getLocalUTC().plusMinutes(Integer.parseInt(config.getPaymentTimeoutInMinutes())))
                .build();

        scheduleExpiration(joiningIntent);
        JoiningIntent intent = joiningIntentRepository.save(joiningIntent);
        occupyTicket(intent);
        return intent;
    }


    public JoiningIntent createNoPayment(Participant participant, Event event, TicketType ticketType, RegistrationSubmission submission) {
        JoiningIntent joiningIntent = JoiningIntent.builder()
                .participant(participant)
                .event(event)
                .status(JoiningIntentStatus.NO_PAYMENT)
                .ticketType(ticketType)
                .registrationSubmission(submission)
                .createdAt(Time.getLocalUTC())
                .expiresAt(Time.getLocalUTC().plusMinutes(Integer.parseInt(config.getPaymentTimeoutInMinutes())))
                .build();

        JoiningIntent intent = joiningIntentRepository.save(joiningIntent);
        occupyTicket(intent);
        return intent;
    }

    public JoiningIntent changeStatus(String paymentIntentId, JoiningIntentStatus status) {
        JoiningIntent joiningIntent = joiningIntentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        joiningIntent.setStatus(status);
        return joiningIntentRepository.save(joiningIntent);
    }


    private void scheduleExpiration(JoiningIntent joiningIntent) {
        if(joiningIntent.getStatus() == JoiningIntentStatus.NO_PAYMENT)
            return;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                paymentService.cancelPayment(joiningIntent.getPaymentIntentId());

                JoiningIntent found = joiningIntentRepository.findByPaymentIntentId(joiningIntent.getPaymentIntentId())
                        .orElse(null);
                if(found == null || found.getStatus() != JoiningIntentStatus.CREATED)
                    return;

                retractIntent(found);
            }
        }, Date.from(joiningIntent.getExpiresAt().atZone(ZoneOffset.UTC).toInstant()));
    }

    private void retractIntent(JoiningIntent intent) {
        intent.setStatus(JoiningIntentStatus.EXPIRED);
        joiningIntentRepository.save(intent);

        Event event = intent.getEvent();
        event.setPeopleCount(event.getPeopleCount() - 1);
        eventRepository.save(event);

        TicketType ticketType = intent.getTicketType();
        if(ticketType != null) {
            ticketType.setPeopleCount(ticketType.getPeopleCount() - 1);
            ticketTypeRepository.save(ticketType);
        }
    }

    private void occupyTicket(JoiningIntent intent) {
        Event event = intent.getEvent();
        event.setPeopleCount(event.getPeopleCount() + 1);
        eventRepository.save(event);

        TicketType ticketType = intent.getTicketType();
        if(ticketType != null) {
            ticketType.setPeopleCount(ticketType.getPeopleCount() + 1);
            ticketTypeRepository.save(ticketType);
        }
    }
}
