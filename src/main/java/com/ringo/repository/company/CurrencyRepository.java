package com.ringo.repository.company;

import com.ringo.model.company.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByName(String name);
    Optional<Currency> findBySymbol(Character symbol);
}
