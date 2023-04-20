package com.ringo.service.company;

import com.ringo.dto.company.CurrencyDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.CurrencyMapper;
import com.ringo.model.company.Currency;
import com.ringo.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

        private final CurrencyRepository repository;
        private final CurrencyMapper mapper;

        public CurrencyDto findCurrencyById(Long id) {
            log.info("findCurrencyById: {}", id);
            return mapper.toDto(
                    repository.findById(id).orElseThrow(
                            () -> new NotFoundException("Currency [id: %d] not found".formatted(id)))
            );
        }

        public List<CurrencyDto> findAll() {
            log.info("findAll currencies");
            return mapper.toDtos(repository.findAll());
        }

        public CurrencyDto saveCurrency(CurrencyDto currencyDto) {
            log.info("saveCurrency: {}", currencyDto);
            if(repository.findByName(currencyDto.getName()).isPresent())
                throw new UserException("Currency [name: %s] already exists".formatted(currencyDto.getName()));
            if(repository.findBySymbol(currencyDto.getSymbol()).isPresent())
                throw new UserException("Currency [symbol: %s] already exists".formatted(currencyDto.getSymbol()));

            Currency currency = mapper.toEntity(currencyDto);
            return mapper.toDto(repository.save(currency));
        }

        public CurrencyDto updateCurrency(CurrencyDto currencyDto) {
            log.info("updateCurrency: {}", currencyDto);
            if(repository.findById(currencyDto.getId()).isEmpty())
                throw new NotFoundException("Currency [id: %d] not found".formatted(currencyDto.getId()));
            if(repository.findByName(currencyDto.getName()).isPresent())
                throw new UserException("Currency [name: %s] already exists".formatted(currencyDto.getName()));
            if(repository.findBySymbol(currencyDto.getSymbol()).isPresent())
                throw new UserException("Currency [symbol: %s] already exists".formatted(currencyDto.getSymbol()));

            Currency currency = mapper.toEntity(currencyDto);
            return mapper.toDto(repository.save(currency));
        }

        public void deleteCurrency(Long id) {
            log.info("deleteCurrency: {}", id);
            if(repository.findById(id).isEmpty())
                throw new NotFoundException("Currency [id: %d] not found".formatted(id));
            repository.deleteById(id);
        }
}
