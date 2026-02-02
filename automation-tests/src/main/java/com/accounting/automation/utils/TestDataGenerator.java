package com.accounting.automation.utils;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Test Data Generator using Faker
 */
@Slf4j
public class TestDataGenerator {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private TestDataGenerator() {
    }

    // Account data
    public static String generateAccountCode() {
        return String.valueOf(1000 + random.nextInt(9000));
    }

    public static String generateAccountName() {
        return faker.commerce().department() + " Account";
    }

    // Customer data
    public static String generateCustomerName() {
        return faker.company().name();
    }

    public static String generateCustomerEmail() {
        return faker.internet().emailAddress();
    }

    public static String generateCustomerPhone() {
        return faker.phoneNumber().phoneNumber();
    }

    public static String generateAddress() {
        return faker.address().fullAddress();
    }

    // Invoice data
    public static String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    public static String generateItemDescription() {
        return faker.commerce().productName();
    }

    public static String generateQuantity() {
        return String.valueOf(1 + random.nextInt(10));
    }

    public static String generatePrice() {
        return String.format("%.2f", 10 + random.nextDouble() * 990);
    }

    public static String generateAmount() {
        return String.format("%.2f", 100 + random.nextDouble() * 9900);
    }

    // Date helpers
    public static String getTodayDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getDateDaysFromNow(int days) {
        return LocalDate.now().plusDays(days).format(DATE_FORMAT);
    }

    public static String getDateDaysAgo(int days) {
        return LocalDate.now().minusDays(days).format(DATE_FORMAT);
    }

    public static String getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1).format(DATE_FORMAT);
    }

    public static String getLastDayOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).format(DATE_FORMAT);
    }

    public static String getFirstDayOfYear() {
        return LocalDate.now().withDayOfYear(1).format(DATE_FORMAT);
    }

    // Journal entry data
    public static String generateReference() {
        return "JE-" + System.currentTimeMillis();
    }

    public static String generateDescription() {
        return faker.lorem().sentence(5);
    }

    // Bank data
    public static String generateBankAccountNumber() {
        return faker.finance().iban();
    }

    public static String generateBankName() {
        return faker.company().name() + " Bank";
    }
}