package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.BankAccountRepository;
import com.accounting.repository.BankStatementRepository;
import com.accounting.repository.JournalEntryLineRepository;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankReconciliationService Unit Tests")
class BankReconciliationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private BankStatementRepository bankStatementRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @InjectMocks
    private BankReconciliationService bankReconciliationService;

    private Account cashAccount;
    private BankAccount bankAccount;
    private BankStatement bankStatement;
    private JournalEntryLine journalLine;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        bankAccount = TestDataBuilder.createBankAccount(1L, "Main Checking", cashAccount);
        bankStatement = TestDataBuilder.createBankStatement(1L, bankAccount,
                BigDecimal.valueOf(500), false); // Credit (deposit)
        journalLine = TestDataBuilder.createJournalEntryLine(1L, cashAccount,
                BigDecimal.valueOf(500), BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("Bank Account Operations")
    class BankAccountOperations {

        @Test
        @DisplayName("Should find all bank accounts")
        void findAllBankAccounts_ReturnsAllAccounts() {
            when(bankAccountRepository.findAllWithRelations()).thenReturn(Arrays.asList(bankAccount));

            List<BankAccount> result = bankReconciliationService.findAllBankAccounts();

            assertThat(result).hasSize(1);
            verify(bankAccountRepository).findAllWithRelations();
        }

        @Test
        @DisplayName("Should find active bank accounts")
        void findActiveBankAccounts_ReturnsActiveAccounts() {
            when(bankAccountRepository.findAllActive()).thenReturn(Arrays.asList(bankAccount));

            List<BankAccount> result = bankReconciliationService.findActiveBankAccounts();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should find bank account by ID")
        void findBankAccountById_WhenExists_ReturnsAccount() {
            when(bankAccountRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(bankAccount));

            Optional<BankAccount> result = bankReconciliationService.findBankAccountById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getAccountName()).isEqualTo("Main Checking");
        }

        @Test
        @DisplayName("Should save bank account")
        void saveBankAccount_SavesSuccessfully() {
            when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);

            BankAccount result = bankReconciliationService.saveBankAccount(bankAccount);

            assertThat(result).isNotNull();
            verify(bankAccountRepository).save(bankAccount);
        }
    }

    @Nested
    @DisplayName("Bank Statement Operations")
    class BankStatementOperations {

        @Test
        @DisplayName("Should find statements by bank account")
        void findStatementsByBankAccount_ReturnsStatements() {
            when(bankStatementRepository.findByBankAccountId(1L))
                    .thenReturn(Arrays.asList(bankStatement));

            List<BankStatement> result = bankReconciliationService.findStatementsByBankAccount(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should find unreconciled statements")
        void findUnreconciledStatements_ReturnsUnreconciledOnly() {
            when(bankStatementRepository.findUnreconciledByBankAccountId(1L))
                    .thenReturn(Arrays.asList(bankStatement));

            List<BankStatement> result = bankReconciliationService.findUnreconciledStatements(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsReconciled()).isFalse();
        }

        @Test
        @DisplayName("Should find statements by date range")
        void findStatementsByDateRange_ReturnsMatchingStatements() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(bankStatementRepository.findByBankAccountIdAndDateRange(1L, startDate, endDate))
                    .thenReturn(Arrays.asList(bankStatement));

            List<BankStatement> result = bankReconciliationService.findStatementsByDateRange(
                    1L, startDate, endDate);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should import single statement")
        void importStatement_SavesStatement() {
            when(bankStatementRepository.save(bankStatement)).thenReturn(bankStatement);

            BankStatement result = bankReconciliationService.importStatement(bankStatement);

            assertThat(result).isNotNull();
            verify(bankStatementRepository).save(bankStatement);
        }

        @Test
        @DisplayName("Should import multiple statements")
        void importStatements_SavesAllStatements() {
            BankStatement statement2 = TestDataBuilder.createBankStatement(2L, bankAccount,
                    BigDecimal.valueOf(300), true);
            List<BankStatement> statements = Arrays.asList(bankStatement, statement2);

            when(bankStatementRepository.saveAll(statements)).thenReturn(statements);

            List<BankStatement> result = bankReconciliationService.importStatements(statements);

            assertThat(result).hasSize(2);
            verify(bankStatementRepository).saveAll(statements);
        }
    }

    @Nested
    @DisplayName("Reconciliation Operations")
    class ReconciliationOperations {

        @Test
        @DisplayName("Should reconcile statement with matching journal line")
        void reconcileStatement_MatchingAmounts_ReconcilesSuccessfully() {
            // Statement is a credit (deposit) of 500
            bankStatement.setCreditAmount(BigDecimal.valueOf(500));
            bankStatement.setDebitAmount(BigDecimal.ZERO);

            // Journal line is a debit of 500 (which should show as negative for reconciliation)
            journalLine.setDebitAmount(BigDecimal.valueOf(500));
            journalLine.setCreditAmount(BigDecimal.ZERO);

            when(bankStatementRepository.findById(1L)).thenReturn(Optional.of(bankStatement));
            when(journalEntryLineRepository.findById(1L)).thenReturn(Optional.of(journalLine));
            when(bankStatementRepository.save(any(BankStatement.class))).thenAnswer(i -> i.getArgument(0));
            when(bankStatementRepository.getReconciledBalance(1L)).thenReturn(BigDecimal.valueOf(500));
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArgument(0));

            bankReconciliationService.reconcileStatement(1L, 1L);

            assertThat(bankStatement.getIsReconciled()).isTrue();
            assertThat(bankStatement.getMatchedJournalLine()).isEqualTo(journalLine);
            verify(bankStatementRepository).save(bankStatement);
        }

        @Test
        @DisplayName("Should throw exception when amounts don't match")
        void reconcileStatement_MismatchedAmounts_ThrowsException() {
            bankStatement.setCreditAmount(BigDecimal.valueOf(500));
            bankStatement.setDebitAmount(BigDecimal.ZERO);

            JournalEntryLine wrongAmountLine = TestDataBuilder.createJournalEntryLine(2L, cashAccount,
                    BigDecimal.valueOf(300), BigDecimal.ZERO);

            when(bankStatementRepository.findById(1L)).thenReturn(Optional.of(bankStatement));
            when(journalEntryLineRepository.findById(2L)).thenReturn(Optional.of(wrongAmountLine));

            assertThatThrownBy(() -> bankReconciliationService.reconcileStatement(1L, 2L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("does not match");
        }

        @Test
        @DisplayName("Should throw exception when statement not found")
        void reconcileStatement_StatementNotFound_ThrowsException() {
            when(bankStatementRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankReconciliationService.reconcileStatement(99L, 1L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Bank statement not found");
        }

        @Test
        @DisplayName("Should throw exception when journal line not found")
        void reconcileStatement_JournalLineNotFound_ThrowsException() {
            when(bankStatementRepository.findById(1L)).thenReturn(Optional.of(bankStatement));
            when(journalEntryLineRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankReconciliationService.reconcileStatement(1L, 99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Journal entry line not found");
        }

        @Test
        @DisplayName("Should unreconcile statement")
        void unreconcileStatement_UnreconcilesSuccessfully() {
            bankStatement.setIsReconciled(true);
            bankStatement.setMatchedJournalLine(journalLine);

            when(bankStatementRepository.findById(1L)).thenReturn(Optional.of(bankStatement));
            when(bankStatementRepository.save(any(BankStatement.class))).thenAnswer(i -> i.getArgument(0));
            when(bankStatementRepository.getReconciledBalance(1L)).thenReturn(BigDecimal.ZERO);
            when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArgument(0));

            bankReconciliationService.unreconcileStatement(1L);

            assertThat(bankStatement.getIsReconciled()).isFalse();
            assertThat(bankStatement.getMatchedJournalLine()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when unreconciling non-existent statement")
        void unreconcileStatement_StatementNotFound_ThrowsException() {
            when(bankStatementRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankReconciliationService.unreconcileStatement(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Bank statement not found");
        }
    }

    @Nested
    @DisplayName("Balance Operations")
    class BalanceOperations {

        @Test
        @DisplayName("Should get reconciled balance")
        void getReconciledBalance_ReturnsCorrectBalance() {
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
            when(bankStatementRepository.getReconciledBalance(1L)).thenReturn(BigDecimal.valueOf(1500));

            BigDecimal result = bankReconciliationService.getReconciledBalance(1L);

            // Opening balance (10000) + reconciled (1500) = 11500
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(11500));
        }

        @Test
        @DisplayName("Should throw exception when bank account not found for balance")
        void getReconciledBalance_AccountNotFound_ThrowsException() {
            when(bankAccountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bankReconciliationService.getReconciledBalance(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Bank account not found");
        }

        @Test
        @DisplayName("Should get unreconciled difference")
        void getUnreconciledDifference_ReturnsCorrectDifference() {
            when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));
            when(journalEntryLineRepository.findByAccountIdAndPosted(1L))
                    .thenReturn(Arrays.asList(journalLine));
            when(bankStatementRepository.getReconciledBalance(1L)).thenReturn(BigDecimal.valueOf(200));

            BigDecimal result = bankReconciliationService.getUnreconciledDifference(1L);

            // GL balance (500 debit) - Reconciled (10000 + 200) = -9700
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(-9700));
        }

        @Test
        @DisplayName("Should throw exception when GL account not linked")
        void getUnreconciledDifference_NoGLAccount_ThrowsException() {
            BankAccount noGlAccount = TestDataBuilder.createBankAccount(2L, "No GL", null);
            when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(noGlAccount));

            assertThatThrownBy(() -> bankReconciliationService.getUnreconciledDifference(2L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("not linked to a GL account");
        }

        @Test
        @DisplayName("Should count unreconciled statements")
        void countUnreconciled_ReturnsCorrectCount() {
            when(bankStatementRepository.countUnreconciled(1L)).thenReturn(5L);

            Long count = bankReconciliationService.countUnreconciled(1L);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Potential Matches Operations")
    class PotentialMatchesOperations {

        @Test
        @DisplayName("Should find potential matches for statement")
        void findPotentialMatches_ReturnsMatchingJournalLines() {
            when(journalEntryLineRepository.findByAccountIdAndPostedBetweenDates(
                    eq(1L), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Arrays.asList(journalLine));

            List<JournalEntryLine> result = bankReconciliationService.findPotentialMatches(bankStatement);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when GL account not linked for potential matches")
        void findPotentialMatches_NoGLAccount_ThrowsException() {
            BankAccount noGlAccount = TestDataBuilder.createBankAccount(2L, "No GL", null);
            BankStatement statementNoGl = TestDataBuilder.createBankStatement(2L, noGlAccount,
                    BigDecimal.valueOf(100), false);

            assertThatThrownBy(() -> bankReconciliationService.findPotentialMatches(statementNoGl))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("not linked to a GL account");
        }
    }
}