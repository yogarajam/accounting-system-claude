package com.accounting.repository;

import com.accounting.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CurrencyRepository Integration Tests")
class CurrencyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency usdCurrency;
    private Currency eurCurrency;
    private Currency gbpCurrency;

    @BeforeEach
    void setUp() {
        usdCurrency = createCurrency("USD", "US Dollar", "$", BigDecimal.ONE, true);
        eurCurrency = createCurrency("EUR", "Euro", "\u20AC", BigDecimal.valueOf(0.92), false);
        gbpCurrency = createCurrency("GBP", "British Pound", "\u00A3", BigDecimal.valueOf(0.79), false);

        entityManager.persist(usdCurrency);
        entityManager.persist(eurCurrency);
        entityManager.persist(gbpCurrency);
        entityManager.flush();
        entityManager.clear();
    }

    private Currency createCurrency(String code, String name, String symbol,
                                    BigDecimal exchangeRate, boolean isBase) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(name);
        currency.setSymbol(symbol);
        currency.setExchangeRate(exchangeRate);
        currency.setIsBase(isBase);
        return currency;
    }

    @Nested
    @DisplayName("Find By Code")
    class FindByCode {

        @Test
        @DisplayName("Should find currency by code")
        void findByCode_WhenExists_ReturnsCurrency() {
            Optional<Currency> result = currencyRepository.findByCode("USD");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("US Dollar");
            assertThat(result.get().getIsBase()).isTrue();
        }

        @Test
        @DisplayName("Should return empty when code does not exist")
        void findByCode_WhenNotExists_ReturnsEmpty() {
            Optional<Currency> result = currencyRepository.findByCode("JPY");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if code exists")
        void existsByCode_ReturnsCorrectResult() {
            assertThat(currencyRepository.existsByCode("USD")).isTrue();
            assertThat(currencyRepository.existsByCode("EUR")).isTrue();
            assertThat(currencyRepository.existsByCode("JPY")).isFalse();
        }
    }

    @Nested
    @DisplayName("Find Base Currency")
    class FindBaseCurrency {

        @Test
        @DisplayName("Should find base currency")
        void findBaseCurrency_ReturnsBaseCurrency() {
            Optional<Currency> result = currencyRepository.findBaseCurrency();

            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("USD");
            assertThat(result.get().getIsBase()).isTrue();
        }

        @Test
        @DisplayName("Should return empty when no base currency set")
        void findBaseCurrency_NoBase_ReturnsEmpty() {
            // Remove base flag from USD
            Currency usd = currencyRepository.findByCode("USD").get();
            usd.setIsBase(false);
            currencyRepository.save(usd);
            entityManager.flush();
            entityManager.clear();

            Optional<Currency> result = currencyRepository.findBaseCurrency();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save new currency")
        void save_NewCurrency_PersistsSuccessfully() {
            Currency jpyCurrency = createCurrency("JPY", "Japanese Yen", "\u00A5",
                    BigDecimal.valueOf(149.50), false);

            Currency savedCurrency = currencyRepository.save(jpyCurrency);

            assertThat(savedCurrency.getId()).isNotNull();
            assertThat(currencyRepository.count()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should update existing currency")
        void save_ExistingCurrency_UpdatesSuccessfully() {
            Currency existingCurrency = currencyRepository.findByCode("EUR").get();
            existingCurrency.setExchangeRate(BigDecimal.valueOf(0.95));

            currencyRepository.save(existingCurrency);
            entityManager.flush();
            entityManager.clear();

            Currency updatedCurrency = currencyRepository.findByCode("EUR").get();
            assertThat(updatedCurrency.getExchangeRate()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
        }

        @Test
        @DisplayName("Should delete currency")
        void delete_ExistingCurrency_RemovesSuccessfully() {
            Currency currencyToDelete = currencyRepository.findByCode("GBP").get();

            currencyRepository.delete(currencyToDelete);
            entityManager.flush();

            assertThat(currencyRepository.findByCode("GBP")).isEmpty();
            assertThat(currencyRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find all currencies")
        void findAll_ReturnsAllCurrencies() {
            List<Currency> result = currencyRepository.findAll();

            assertThat(result).hasSize(3);
            assertThat(result).extracting(Currency::getCode)
                    .containsExactlyInAnyOrder("USD", "EUR", "GBP");
        }
    }

    @Nested
    @DisplayName("Exchange Rate Operations")
    class ExchangeRateOperations {

        @Test
        @DisplayName("Should handle exchange rate precision")
        void save_WithPreciseExchangeRate_PreservesPrecision() {
            // Use 4 decimal places which is typical for exchange rates
            Currency preciseRateCurrency = createCurrency("CHF", "Swiss Franc", "CHF",
                    new BigDecimal("0.8765"), false);

            Currency savedCurrency = currencyRepository.save(preciseRateCurrency);
            entityManager.flush();
            entityManager.clear();

            Currency retrievedCurrency = currencyRepository.findByCode("CHF").get();
            assertThat(retrievedCurrency.getExchangeRate())
                    .isEqualByComparingTo(new BigDecimal("0.8765"));
        }
    }
}