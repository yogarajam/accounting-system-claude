package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.Currency;
import com.accounting.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }

    public Optional<Currency> findById(Long id) {
        return currencyRepository.findById(id);
    }

    public Optional<Currency> findByCode(String code) {
        return currencyRepository.findByCode(code);
    }

    public Optional<Currency> findBaseCurrency() {
        return currencyRepository.findBaseCurrency();
    }

    @Transactional
    public Currency save(Currency currency) {
        if (currency.getId() == null && currencyRepository.existsByCode(currency.getCode())) {
            throw new AccountingException("Currency code already exists: " + currency.getCode());
        }

        if (Boolean.TRUE.equals(currency.getIsBase())) {
            currencyRepository.findBaseCurrency().ifPresent(existing -> {
                if (!existing.getId().equals(currency.getId())) {
                    existing.setIsBase(false);
                    currencyRepository.save(existing);
                }
            });
            currency.setExchangeRate(BigDecimal.ONE);
        }

        return currencyRepository.save(currency);
    }

    @Transactional
    public void updateExchangeRate(Long id, BigDecimal exchangeRate) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> new AccountingException("Currency not found: " + id));

        if (Boolean.TRUE.equals(currency.getIsBase())) {
            throw new AccountingException("Cannot change exchange rate of base currency");
        }

        currency.setExchangeRate(exchangeRate);
        currencyRepository.save(currency);
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from.getId().equals(to.getId())) {
            return amount;
        }

        BigDecimal amountInBase = amount.divide(from.getExchangeRate(), 6, RoundingMode.HALF_UP);
        return amountInBase.multiply(to.getExchangeRate()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal convertToBase(BigDecimal amount, Currency currency) {
        if (Boolean.TRUE.equals(currency.getIsBase())) {
            return amount;
        }
        return amount.divide(currency.getExchangeRate(), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Currency createCurrencyIfNotExists(String code, String name, String symbol, boolean isBase) {
        return currencyRepository.findByCode(code)
                .orElseGet(() -> {
                    Currency currency = new Currency();
                    currency.setCode(code);
                    currency.setName(name);
                    currency.setSymbol(symbol);
                    currency.setIsBase(isBase);
                    currency.setExchangeRate(BigDecimal.ONE);
                    return currencyRepository.save(currency);
                });
    }
}