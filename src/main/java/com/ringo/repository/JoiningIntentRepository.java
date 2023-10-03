package com.ringo.repository;

import com.ringo.model.payment.JoiningIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoiningIntentRepository extends JpaRepository<JoiningIntent, Long> {

    @Query("SELECT s FROM JoiningIntent s WHERE s.paymentIntentId = :paymentIntentId")
    Optional<JoiningIntent> findByPaymentIntentId(String paymentIntentId);

    @Query("SELECT s FROM JoiningIntent s WHERE s.paymentIntentId = :paymentIntentId AND s.status = 'CREATED'")
    Optional<JoiningIntent> findCreatedByPaymentIntentId(String paymentIntentId);

    @Query("SELECT s FROM JoiningIntent s WHERE s.participant.id = :participantId")
    List<JoiningIntent> findAllByParticipantId(Long participantId);
}
