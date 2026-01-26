package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.Currency;
import com.accounting.repository.CurrencyRepository;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyService Unit Tests")
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency baseCurrency;
    private Currency foreignCurrency;

    @BeforeEach
    void setUp() {
        baseCurrency = TestDataBuilder.createBaseCurrency();
        foreignCurrency = TestDataBuilder.createForeignCurrency();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find all currencies")
        void findAll_ReturnsAllCurrencies() {
            when(currencyRepository.findAll()).thenReturn(Arrays.asList(baseCurrency, foreignCurrency));

            List<Currency> result = currencyService.findAll();

            assertThat(result).hasSize(2);
            verify(currencyRepository).findAll();
        }

        @Test
        @DisplayName("Should find currency by ID")
        void findById_WhenExists_ReturnsCurrency() {
            when(currencyRepository.findById(1L)).thenReturn(Optional.of(baseCurrency));

            Optional<Currency> result = currencyService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should find currency by code")
        void findByCode_WhenExists_ReturnsCurrency() {
            when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(foreignCurrency));

            Optional<Currency> result = currencyService.findByCode("EUR");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Euro");
        }

        @Test
        @DisplayName("Should find base currency")
        void findBaseCurrency_ReturnsBaseCurrency() {
            when(currencyRepository.findBaseCurrency()).thenReturn(Optional.of(baseCurrency));

            Optional<Currency> result = currencyService.findBaseCurrency();

            assertThat(result).isPresent();
            assertThat(result.get().getIsBase()).isTrue();
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("Should save new currency successfully")
        void save_NewCurrency_SavesSuccessfully() {
            Currency newCurrency = TestDataBuilder.createCurrency(null, "GBP", "British Pound", false);

            when(currencyRepository.existsByCode("GBP")).thenReturn(false);
            when(currencyRepository.save(any(Currency.class))).thenAnswer(invocation -> {
                Currency c = invocation.getArgument(0);
                c.setId(3L);
                return c;
            });

            Currency result = currencyService.save(newCurrency);

            assertThat(result.getId()).isEqualTo(3L);
            verify(currencyRepository).save(newCurrency);
        }

        @Test
        @DisplayName("Should throw exception when saving currency with duplicate code")
        void save_DuplicateCode_ThrowsException() {
            Currency newCurrency = TestDataBuilder.createCurrency(null, "USD", "Dollar", false);
            when(currencyRepository.existsByCode("USD")).thenReturn(true);

            assertThatThrownBy(() -> currencyService.save(newCurrency))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Currency code already exists");
        }

        @Test
        @DisplayName("Should update existing base currency without changing other")
        void save_UpdateBaseCurrency_DoesNotAffectOthers() {
            baseCurrency.setName("Updated US Dollar");
            when(currencyRepository.findBaseCurrency()).thenReturn(Optional.of(baseCurrency));
            when(currencyRepository.save(any(Currency.class))).thenAnswer(i -> i.getArgument(0));

            Currency result = currencyService.save(baseCurrency);

            assertThat(result.getName()).isEqualTo("Updated US Dollar");
            assertThat(result.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("Should unset previous base currency when setting new base")
        void save_NewBaseCurrency_UnsetsOldBase() {
            Currency newBase = TestDataBuilder.createCurrency(null, "GBP", "British Pound", true);

            when(currencyRepository.existsByCode("GBP")).thenReturn(false);
            when(currencyRepository.findBaseCurrency()).thenReturn(Optional.of(baseCurrency));
            when(currencyRepository.save(any(Currency.class))).thenAnswer(i -> i.getArgument(0));

            currencyService.save(newBase);

            assertThat(baseCurrency.getIsBase()).isFalse();
            verify(currencyRepository, times(2)).save(any(Currency.class));
        }

        @Test
        @DisplayName("Should set exchange rate to 1 when saving as base currency")
        void save_BaseCurrency_SetsExchangeRateToOne() {
            Currency newBase = TestDataBuilder.createCurrency(null, "GBP", "British Pound", true);
            newBase.setExchangeRate(BigDecimal.valueOf(1.5));

            when(currencyRepository.existsByCode("GBP")).thenReturn(false);
            when(currencyRepository.findBaseCurrency()).thenReturn(Optional.empty());
            when(currencyRepository.save(any(Currency.class))).thenAnswer(i -> i.getArgument(0));

            Currency result = currencyService.save(newBase);

            assertThat(result.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
        }
    }

    @Nested
    @DisplayName("Exchange Rate Operations")
    class ExchangeRateOperations {

        @Test
        @DisplayName("Should update exchange rate for non-base currency")
        void updateExchangeRate_NonBaseCurrency_UpdatesSuccessfully() {
            when(currencyRepository.findById(2L)).thenReturn(Optional.of(foreignCurrency));
            when(currencyRepository.save(any(Currency.class))).thenAnswer(i -> i.getArgument(0));

            currencyService.updateExchangeRate(2L, BigDecimal.valueOf(1.25));

            assertThat(foreignCurrency.getExchangeRate()).isEqualByComparingTo(BigDecimal.valueOf(1.25));
        }

        @Test
        @DisplayName("Should throw exception when updating base currency exchange rate")
        void updateExchangeRate_BaseCurrency_ThrowsException() {
            when(currencyRepository.findById(1L)).thenReturn(Optional.of(baseCurrency));

            assertThatThrownBy(() -> currencyService.updateExchangeRate(1L, BigDecimal.valueOf(0.5)))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Cannot change exchange rate of base currency");
        }

        @Test
        @DisplayName("Should throw exception when currency not found")
        void updateExchangeRate_CurrencyNotFound_ThrowsException() {
            when(currencyRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> currencyService.updateExchangeRate(99L, BigDecimal.valueOf(1.5)))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Currency not found");
        }
    }

    @Nested
    @DisplayName("Currency Conversion")
    class CurrencyConversion {

        @Test
        @DisplayName("Should return same amount when converting same currency")
        void convert_SameCurrency_ReturnsSameAmount() {
            BigDecimal amount = BigDecimal.valueOf(100);

            BigDecimal result = currencyService.convert(amount, baseCurrency, baseCurrency);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("Should convert from foreign to base currency")
        void convert_ForeignToBase_ConvertsCorrectly() {
            // EUR to USD: 100 EUR / 1.10 (EUR rate) * 1.00 (USD rate) = 90.91
            BigDecimal amount = BigDecimal.valueOf(100);

            BigDecimal result = currencyService.convert(amount, foreignCurrency, baseCurrency);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(90.91));
        }

        @Test
        @DisplayName("Should convert from base to foreign currency")
        void convert_BaseToForeign_ConvertsCorrectly() {
            // USD to EUR: 100 USD / 1.00 (USD rate) * 1.10 (EUR rate) = 110.00
            BigDecimal amount = BigDecimal.valueOf(100);

            BigDecimal result = currencyService.convert(amount, baseCurrency, foreignCurrency);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(110.00));
        }

        @Test
        @DisplayName("Should convert between two foreign currencies")
        void convert_ForeignToForeign_ConvertsCorrectly() {
            Currency gbp = TestDataBuilder.createCurrency(3L, "GBP", "British Pound", false);
            gbp.setExchangeRate(BigDecimal.valueOf(0.85));

            // EUR to GBP: 100 EUR / 1.10 (EUR rate) * 0.85 (GBP rate) = 77.27
            BigDecimal amount = BigDecimal.valueOf(100);

            BigDecimal result = currencyService.convert(amount, foreignCurrency, gbp);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(77.27));
        }

        @Test
        @DisplayName("Should convert to base currency for base currency input")
        void convertToBase_BaseCurrency_ReturnsSameAmount() {
            BigDecimal amount = BigDecimal.valueOf(100);

            BigDecimal result = currencyService.convertToBase(amount, baseCurrency);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("Should convert foreign currency to base")
        void convertToBase_ForeignCurrency_ConvertsCorrectly() {
            // 110 EUR to USD: 110 / 1.10 = 100
            BigDecimal amount = BigDecimal.valueOf(110);

            BigDecimal result = currencyService.convertToBase(amount, foreignCurrency);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
        }
    }

    @Nested
    @DisplayName("Create Currency If Not Exists")
    class CreateCurrencyIfNotExists {

        @Test
        @DisplayName("Should return existing currency when code exists")
        void createCurrencyIfNotExists_CodeExists_ReturnsExisting() {
            when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(baseCurrency));

            Currency result = currencyService.createCurrencyIfNotExists("USD", "US Dollar", "$", true);

            assertThat(result).isEqualTo(baseCurrency);
            verify(currencyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new currency when code does not exist")
        void createCurrencyIfNotExists_CodeNotExists_CreatesNew() {
            when(currencyRepository.findByCode("JPY")).thenReturn(Optional.empty());
            when(currencyRepository.save(any(Currency.class))).thenAnswer(invocation -> {
                Currency c = invocation.getArgument(0);
                c.setId(4L);
                return c;
            });

            Currency result = currencyService.createCurrencyIfNotExists("JPY", "Japanese Yen", "¥", false);

            assertThat(result.getCode()).isEqualTo("JPY");
            assertThat(result.getExchangeRate()).isEqualByComparingTo(BigDecimal.ONE);
            verify(currencyRepository).save(any(Currency.class));
        }
    }
}