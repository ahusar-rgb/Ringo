package com.ringo.service.common;

import com.ringo.exception.NotFoundException;
import com.ringo.model.company.Currency;
import com.ringo.model.company.ExchangeRate;
import com.ringo.model.company.ExchangeRateId;
import com.ringo.repository.CurrencyRepository;
import com.ringo.repository.ExchangeRateRepository;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class CurrencyExchanger {

    private final String BASE_PATH = "https://api.api-ninjas.com/v1/convertcurrency";
    private final CurrencyRepository currencyRepository;

    private final ExchangeRateRepository exchangeRateRepository;

    @PostConstruct
    void init() {
        updateCurrencies();
    }

    public float exchange(Currency from, Currency to, float amount) {
        return amount * exchangeRateRepository.findByFromAndTo(from, to)
                .orElseThrow(() -> new NotFoundException("Exchange rate not found"))
                .getRate();
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void updateCurrencies() {

        List<Currency> currencies = currencyRepository.findAll();

        for (Currency from : currencies) {
            for (Currency to: currencies) {
                if(from == to)
                    exchangeRateRepository.save(
                            new ExchangeRate(
                                    new ExchangeRateId(from, to),
                                    1.0F
                            )
                    );
                else
                    exchangeRateRepository.save(
                            new ExchangeRate(
                                    new ExchangeRateId(from, to),
                                    fetchExchangeRate(from.getName(), to.getName())
                            )
                    );
            }
            log.info("Updated exchange rates for currency: {}", from.getName());
        }
    }

    private Float fetchExchangeRate(String from, String to) {
        RequestSpecification request = RestAssured.given();
        request.param("have", from);
        request.param("want", to);
        request.param("amount", "1");

        return request.get(BASE_PATH)
                .then()
                .extract()
                .body()
                .jsonPath()
                .getFloat("new_amount");
    }
}
