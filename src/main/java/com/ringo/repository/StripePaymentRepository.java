package com.ringo.repository;

import com.ringo.model.payment.JoiningIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripePaymentRepository extends JpaRepository<JoiningIntent, Long> {

    @Query("SELECT s FROM JoiningIntent s WHERE s.paymentIntentId = :paymentIntentId")
    Optional<JoiningIntent> findByPaymentIntentId(String paymentIntentId);
}
