package com.ringo.service.company;

import com.ringo.auth.AuthenticationService;
import com.ringo.dto.company.CurrencyDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.CurrencyMapper;
import com.ringo.model.company.Currency;
import com.ringo.model.security.Role;
import com.ringo.repository.company.CurrencyRepository;
import com.ringo.repository.company.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CurrencyService {
    private final CurrencyRepository repository;
    private final EventRepository eventRepository;
    private final CurrencyMapper mapper;
    private final AuthenticationService authenticationService;

    @Transactional(readOnly = true)
    public CurrencyDto findCurrencyById(Long id) {
        log.info("findCurrencyById: {}", id);
        Currency currency = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Currency [id: %d] not found".formatted(id))
        );

        return mapper.toDto(currency);
    }

    @Transactional(readOnly = true)
    public List<CurrencyDto> findAll() {
        log.info("findAll currencies");
        return mapper.toDtoList(repository.findAll());
    }

    public CurrencyDto saveCurrency(CurrencyDto currencyDto) {
        log.info("saveCurrency: {}", currencyDto);
        throwIfNotAdmin();

        Currency currency = mapper.toEntity(currencyDto);
        throwIfUniqueConstraintsViolated(currency);

        return mapper.toDto(repository.save(currency));
    }

    public CurrencyDto updateCurrency(Long id, CurrencyDto currencyDto) {
        log.info("updateCurrency: {}", currencyDto);
        throwIfNotAdmin();

        Currency currency = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Currency [id: %d] not found".formatted(id))
        );

        mapper.partialUpdate(currency, currencyDto);
        throwIfUniqueConstraintsViolated(currency);

        return mapper.toDto(repository.save(currency));
    }

    public void deleteCurrency(Long id) {
        log.info("deleteCurrency: {}", id);
        throwIfNotAdmin();
        if(repository.findById(id).isEmpty())
            throw new NotFoundException("Currency [id: %d] not found".formatted(id));
        if(eventRepository.existsByCurrencyId(id))
            throw new UserException("Currency [id: %d] is used in events".formatted(id));

        repository.deleteById(id);
    }

    private void throwIfUniqueConstraintsViolated(Currency currency) {
        if(currency.getName() != null) {
            Currency found = repository.findByName(currency.getName()).orElse(null);
            if(found != null && !found.getId().equals(currency.getId()))
                throw new UserException("Currency [name: %s] already exists".formatted(currency.getName()));
        }
        if(currency.getSymbol() != null) {
            Currency found = repository.findBySymbol(currency.getSymbol()).orElse(null);
            if(found != null && !found.getId().equals(currency.getId()))
                throw new UserException("Currency [symbol: %s] already exists".formatted(currency.getSymbol()));
        }
    }

    private void throwIfNotAdmin() {
        if(authenticationService.getCurrentUser().getRole() != Role.ROLE_ADMIN)
            throw new UserException("Only admin can perform this action");
    }
}
