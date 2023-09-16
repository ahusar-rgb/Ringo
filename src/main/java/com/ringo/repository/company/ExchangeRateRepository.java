package com.ringo.repository.company;

import com.ringo.model.company.Currency;
import com.ringo.model.company.ExchangeRate;
import com.ringo.model.company.ExchangeRateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, ExchangeRateId> {

    @Query("SELECT er FROM ExchangeRate er WHERE er.id.from = :from AND er.id.to = :to")
    Optional<ExchangeRate> findByFromAndTo(Currency from, Currency to);
}
