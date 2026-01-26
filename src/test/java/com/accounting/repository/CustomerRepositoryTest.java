package com.accounting.repository;

import com.accounting.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CustomerRepository Integration Tests")
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    @BeforeEach
    void setUp() {
        customer1 = createCustomer("CUST001", "Acme Corporation", "acme@example.com");
        customer2 = createCustomer("CUST002", "Beta Industries", "beta@example.com");
        customer3 = createCustomer("ACME001", "Acme Small Business", "small@acme.com");

        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.persist(customer3);
        entityManager.flush();
        entityManager.clear();
    }

    private Customer createCustomer(String code, String name, String email) {
        Customer customer = new Customer();
        customer.setCode(code);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone("123-456-7890");
        return customer;
    }

    @Nested
    @DisplayName("Find By Code")
    class FindByCode {

        @Test
        @DisplayName("Should find customer by code")
        void findByCode_WhenExists_ReturnsCustomer() {
            Optional<Customer> result = customerRepository.findByCode("CUST001");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Acme Corporation");
        }

        @Test
        @DisplayName("Should return empty when code does not exist")
        void findByCode_WhenNotExists_ReturnsEmpty() {
            Optional<Customer> result = customerRepository.findByCode("INVALID");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if code exists")
        void existsByCode_ReturnsCorrectResult() {
            assertThat(customerRepository.existsByCode("CUST001")).isTrue();
            assertThat(customerRepository.existsByCode("INVALID")).isFalse();
        }
    }

    @Nested
    @DisplayName("Find By Name Containing")
    class FindByNameContaining {

        @Test
        @DisplayName("Should find customers by partial name match (case insensitive)")
        void findByNameContainingIgnoreCase_ReturnsMatchingCustomers() {
            List<Customer> result = customerRepository.findByNameContainingIgnoreCase("acme");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Customer::getName)
                    .containsExactlyInAnyOrder("Acme Corporation", "Acme Small Business");
        }

        @Test
        @DisplayName("Should return empty list when no name matches")
        void findByNameContainingIgnoreCase_NoMatch_ReturnsEmptyList() {
            List<Customer> result = customerRepository.findByNameContainingIgnoreCase("xyz");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle uppercase search term")
        void findByNameContainingIgnoreCase_UppercaseSearch_ReturnsResults() {
            List<Customer> result = customerRepository.findByNameContainingIgnoreCase("INDUSTRIES");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Beta Industries");
        }
    }

    @Nested
    @DisplayName("Search By Name Or Code")
    class SearchByNameOrCode {

        @Test
        @DisplayName("Should search by name")
        void searchByNameOrCode_ByName_ReturnsMatchingCustomers() {
            List<Customer> result = customerRepository.searchByNameOrCode("Corporation");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Acme Corporation");
        }

        @Test
        @DisplayName("Should search by code")
        void searchByNameOrCode_ByCode_ReturnsMatchingCustomers() {
            List<Customer> result = customerRepository.searchByNameOrCode("CUST002");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Beta Industries");
        }

        @Test
        @DisplayName("Should search both name and code simultaneously")
        void searchByNameOrCode_MatchesNameAndCode_ReturnsAllMatches() {
            // "ACME" matches code ACME001 and names containing "Acme" (2 customers)
            List<Customer> result = customerRepository.searchByNameOrCode("acme");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Customer::getName)
                    .containsExactlyInAnyOrder("Acme Corporation", "Acme Small Business");
        }

        @Test
        @DisplayName("Should be case insensitive")
        void searchByNameOrCode_CaseInsensitive_ReturnsResults() {
            List<Customer> result = customerRepository.searchByNameOrCode("BETA");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Beta Industries");
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void searchByNameOrCode_NoMatch_ReturnsEmptyList() {
            List<Customer> result = customerRepository.searchByNameOrCode("xyz123");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save new customer")
        void save_NewCustomer_PersistsSuccessfully() {
            Customer newCustomer = createCustomer("CUST004", "New Customer", "new@example.com");

            Customer savedCustomer = customerRepository.save(newCustomer);

            assertThat(savedCustomer.getId()).isNotNull();
            assertThat(customerRepository.count()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should update existing customer")
        void save_ExistingCustomer_UpdatesSuccessfully() {
            Customer existingCustomer = customerRepository.findByCode("CUST001").get();
            existingCustomer.setName("Updated Name");

            customerRepository.save(existingCustomer);
            entityManager.flush();
            entityManager.clear();

            Customer updatedCustomer = customerRepository.findByCode("CUST001").get();
            assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should delete customer")
        void delete_ExistingCustomer_RemovesSuccessfully() {
            Customer customerToDelete = customerRepository.findByCode("CUST001").get();

            customerRepository.delete(customerToDelete);
            entityManager.flush();

            assertThat(customerRepository.findByCode("CUST001")).isEmpty();
            assertThat(customerRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find all customers")
        void findAll_ReturnsAllCustomers() {
            List<Customer> result = customerRepository.findAll();

            assertThat(result).hasSize(3);
        }
    }
}