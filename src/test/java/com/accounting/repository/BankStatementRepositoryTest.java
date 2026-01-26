package com.accounting.repository;

import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.model.BankAccount;
import com.accounting.model.BankStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BankStatementRepository Integration Tests")
class BankStatementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankStatementRepository bankStatementRepository;

    private BankAccount bankAccount;
    private BankStatement reconciledStatement1;
    private BankStatement reconciledStatement2;
    private BankStatement unreconciledStatement1;
    private BankStatement unreconciledStatement2;

    @BeforeEach
    void setUp() {
        // Create GL account
        Account cashAccount = new Account();
        cashAccount.setCode("1000");
        cashAccount.setName("Cash");
        cashAccount.setAccountType(AccountType.ASSET);
        cashAccount.setIsActive(true);
        entityManager.persist(cashAccount);

        // Create bank account
        bankAccount = new BankAccount();
        bankAccount.setAccountName("Main Checking");
        bankAccount.setAccountNumber("123456789");
        bankAccount.setBankName("Test Bank");
        bankAccount.setGlAccount(cashAccount);
        bankAccount.setIsActive(true);
        entityManager.persist(bankAccount);

        // Create bank statements
        LocalDate today = LocalDate.now();
        reconciledStatement1 = createStatement(today.minusDays(10), "Deposit",
                BigDecimal.ZERO, BigDecimal.valueOf(1000), true);
        reconciledStatement2 = createStatement(today.minusDays(5), "Deposit",
                BigDecimal.ZERO, BigDecimal.valueOf(500), true);
        unreconciledStatement1 = createStatement(today.minusDays(3), "Check Payment",
                BigDecimal.valueOf(200), BigDecimal.ZERO, false);
        unreconciledStatement2 = createStatement(today, "ATM Withdrawal",
                BigDecimal.valueOf(100), BigDecimal.ZERO, false);

        entityManager.persist(reconciledStatement1);
        entityManager.persist(reconciledStatement2);
        entityManager.persist(unreconciledStatement1);
        entityManager.persist(unreconciledStatement2);
        entityManager.flush();
        entityManager.clear();
    }

    private BankStatement createStatement(LocalDate date, String description,
                                          BigDecimal debit, BigDecimal credit, boolean reconciled) {
        BankStatement statement = new BankStatement();
        statement.setBankAccount(bankAccount);
        statement.setTransactionDate(date);
        statement.setStatementDate(date);
        statement.setDescription(description);
        statement.setDebitAmount(debit);
        statement.setCreditAmount(credit);
        statement.setIsReconciled(reconciled);
        return statement;
    }

    @Nested
    @DisplayName("Find By Bank Account")
    class FindByBankAccount {

        @Test
        @DisplayName("Should find all statements by bank account ID")
        void findByBankAccountId_ReturnsAllStatements() {
            List<BankStatement> result = bankStatementRepository.findByBankAccountId(bankAccount.getId());

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("Should return empty list when no statements for bank account")
        void findByBankAccountId_NoStatements_ReturnsEmptyList() {
            // Create another bank account with no statements
            Account otherGlAccount = new Account();
            otherGlAccount.setCode("1001");
            otherGlAccount.setName("Other Cash");
            otherGlAccount.setAccountType(AccountType.ASSET);
            otherGlAccount.setIsActive(true);
            entityManager.persist(otherGlAccount);

            BankAccount otherBankAccount = new BankAccount();
            otherBankAccount.setAccountName("Other Account");
            otherBankAccount.setAccountNumber("999999999");
            otherBankAccount.setBankName("Other Bank");
            otherBankAccount.setGlAccount(otherGlAccount);
            otherBankAccount.setIsActive(true);
            entityManager.persist(otherBankAccount);
            entityManager.flush();

            List<BankStatement> result = bankStatementRepository.findByBankAccountId(otherBankAccount.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find Unreconciled Statements")
    class FindUnreconciledStatements {

        @Test
        @DisplayName("Should find unreconciled statements ordered by date")
        void findUnreconciledByBankAccountId_ReturnsUnreconciledStatements() {
            List<BankStatement> result = bankStatementRepository
                    .findUnreconciledByBankAccountId(bankAccount.getId());

            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankStatement::getIsReconciled)
                    .containsOnly(false);
            // Should be ordered by transaction date
            assertThat(result.get(0).getTransactionDate())
                    .isBeforeOrEqualTo(result.get(1).getTransactionDate());
        }
    }

    @Nested
    @DisplayName("Find By Date Range")
    class FindByDateRange {

        @Test
        @DisplayName("Should find statements within date range")
        void findByBankAccountIdAndDateRange_ReturnsMatchingStatements() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<BankStatement> result = bankStatementRepository
                    .findByBankAccountIdAndDateRange(bankAccount.getId(), startDate, endDate);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should return empty list when no statements in date range")
        void findByBankAccountIdAndDateRange_NoMatch_ReturnsEmptyList() {
            LocalDate startDate = LocalDate.now().plusDays(10);
            LocalDate endDate = LocalDate.now().plusDays(20);

            List<BankStatement> result = bankStatementRepository
                    .findByBankAccountIdAndDateRange(bankAccount.getId(), startDate, endDate);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should be ordered by transaction date")
        void findByBankAccountIdAndDateRange_OrderedByDate() {
            LocalDate startDate = LocalDate.now().minusDays(15);
            LocalDate endDate = LocalDate.now();

            List<BankStatement> result = bankStatementRepository
                    .findByBankAccountIdAndDateRange(bankAccount.getId(), startDate, endDate);

            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).getTransactionDate())
                        .isBeforeOrEqualTo(result.get(i + 1).getTransactionDate());
            }
        }
    }

    @Nested
    @DisplayName("Get Reconciled Balance")
    class GetReconciledBalance {

        @Test
        @DisplayName("Should calculate reconciled balance correctly")
        void getReconciledBalance_ReturnsCorrectBalance() {
            // Reconciled: +1000 (credit) + 500 (credit) - 0 (debit) = 1500
            BigDecimal result = bankStatementRepository.getReconciledBalance(bankAccount.getId());

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1500));
        }

        @Test
        @DisplayName("Should return zero when no reconciled statements")
        void getReconciledBalance_NoReconciledStatements_ReturnsZero() {
            // Mark all statements as unreconciled
            reconciledStatement1 = entityManager.find(BankStatement.class, reconciledStatement1.getId());
            reconciledStatement2 = entityManager.find(BankStatement.class, reconciledStatement2.getId());
            reconciledStatement1.setIsReconciled(false);
            reconciledStatement2.setIsReconciled(false);
            entityManager.flush();

            BigDecimal result = bankStatementRepository.getReconciledBalance(bankAccount.getId());

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Count Unreconciled")
    class CountUnreconciled {

        @Test
        @DisplayName("Should count unreconciled statements")
        void countUnreconciled_ReturnsCorrectCount() {
            Long result = bankStatementRepository.countUnreconciled(bankAccount.getId());

            assertThat(result).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should return zero when all statements reconciled")
        void countUnreconciled_AllReconciled_ReturnsZero() {
            // Reconcile all statements
            unreconciledStatement1 = entityManager.find(BankStatement.class, unreconciledStatement1.getId());
            unreconciledStatement2 = entityManager.find(BankStatement.class, unreconciledStatement2.getId());
            unreconciledStatement1.setIsReconciled(true);
            unreconciledStatement2.setIsReconciled(true);
            entityManager.flush();

            Long result = bankStatementRepository.countUnreconciled(bankAccount.getId());

            assertThat(result).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save new statement")
        void save_NewStatement_PersistsSuccessfully() {
            BankStatement newStatement = createStatement(LocalDate.now(), "New Transaction",
                    BigDecimal.valueOf(50), BigDecimal.ZERO, false);
            newStatement.setBankAccount(entityManager.find(BankAccount.class, bankAccount.getId()));

            BankStatement savedStatement = bankStatementRepository.save(newStatement);

            assertThat(savedStatement.getId()).isNotNull();
            assertThat(bankStatementRepository.count()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should update existing statement")
        void save_ExistingStatement_UpdatesSuccessfully() {
            BankStatement statement = bankStatementRepository.findById(unreconciledStatement1.getId()).get();
            statement.setIsReconciled(true);

            bankStatementRepository.save(statement);
            entityManager.flush();
            entityManager.clear();

            BankStatement updatedStatement = bankStatementRepository.findById(unreconciledStatement1.getId()).get();
            assertThat(updatedStatement.getIsReconciled()).isTrue();
        }

        @Test
        @DisplayName("Should delete statement")
        void delete_ExistingStatement_RemovesSuccessfully() {
            BankStatement statementToDelete = bankStatementRepository.findById(unreconciledStatement1.getId()).get();

            bankStatementRepository.delete(statementToDelete);
            entityManager.flush();

            assertThat(bankStatementRepository.findById(unreconciledStatement1.getId())).isEmpty();
            assertThat(bankStatementRepository.count()).isEqualTo(3);
        }
    }
}