package com.accounting.automation.pages;

import com.accounting.automation.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Invoices Page Object
 */
@Slf4j
public class InvoicesPage extends BasePage {

    @FindBy(css = "h2")
    private WebElement pageTitle;

    @FindBy(css = "a[href*='/invoices/new']")
    private WebElement newInvoiceButton;

    @FindBy(css = "table.table tbody tr")
    private List<WebElement> invoiceRows;

    @FindBy(css = ".alert-success")
    private WebElement successMessage;

    // Form fields
    @FindBy(id = "customerId")
    private WebElement customerSelect;

    @FindBy(id = "invoiceDate")
    private WebElement invoiceDateField;

    @FindBy(id = "dueDate")
    private WebElement dueDateField;

    @FindBy(css = "button.add-item, #addItem")
    private WebElement addItemButton;

    @FindBy(css = ".item-description, input[name*='description']")
    private List<WebElement> itemDescriptionFields;

    @FindBy(css = ".item-quantity, input[name*='quantity']")
    private List<WebElement> itemQuantityFields;

    @FindBy(css = ".item-price, input[name*='price']")
    private List<WebElement> itemPriceFields;

    @FindBy(css = "button[type='submit']")
    private WebElement saveButton;

    @FindBy(css = ".total-amount")
    private WebElement totalAmount;

    public InvoicesPage() {
        super();
    }

    public InvoicesPage open() {
        navigateTo(ConfigReader.getBaseUrl() + "/invoices");
        waitForPageLoad();
        log.info("Opened invoices page");
        return this;
    }

    public boolean isInvoicesPageDisplayed() {
        return getCurrentUrl().contains("/invoices");
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public int getInvoiceCount() {
        return invoiceRows.size();
    }

    public InvoicesPage clickNewInvoiceButton() {
        click(newInvoiceButton);
        waitForPageLoad();
        log.info("Clicked New Invoice button");
        return this;
    }

    public InvoicesPage selectCustomer(String customerName) {
        selectByVisibleText(customerSelect, customerName);
        return this;
    }

    public InvoicesPage enterInvoiceDate(String date) {
        type(invoiceDateField, date);
        return this;
    }

    public InvoicesPage enterDueDate(String date) {
        type(dueDateField, date);
        return this;
    }

    public InvoicesPage addItem() {
        click(addItemButton);
        return this;
    }

    public InvoicesPage enterItemDescription(int index, String description) {
        if (index < itemDescriptionFields.size()) {
            type(itemDescriptionFields.get(index), description);
        }
        return this;
    }

    public InvoicesPage enterItemQuantity(int index, String quantity) {
        if (index < itemQuantityFields.size()) {
            type(itemQuantityFields.get(index), quantity);
        }
        return this;
    }

    public InvoicesPage enterItemPrice(int index, String price) {
        if (index < itemPriceFields.size()) {
            type(itemPriceFields.get(index), price);
        }
        return this;
    }

    public InvoicesPage clickSaveButton() {
        click(saveButton);
        waitForPageLoad();
        return this;
    }

    public InvoicesPage createInvoice(String customer, String invoiceDate, String dueDate,
                                       String itemDescription, String quantity, String price) {
        clickNewInvoiceButton();
        selectCustomer(customer);
        enterInvoiceDate(invoiceDate);
        enterDueDate(dueDate);
        enterItemDescription(0, itemDescription);
        enterItemQuantity(0, quantity);
        enterItemPrice(0, price);
        clickSaveButton();
        log.info("Created invoice for customer: {}", customer);
        return this;
    }

    public InvoicesPage viewInvoice(String invoiceNumber) {
        for (WebElement row : invoiceRows) {
            if (row.getText().contains(invoiceNumber)) {
                row.findElement(By.cssSelector("a[href*='/view']")).click();
                break;
            }
        }
        waitForPageLoad();
        return this;
    }

    public InvoicesPage sendInvoice(String invoiceNumber) {
        for (WebElement row : invoiceRows) {
            if (row.getText().contains(invoiceNumber)) {
                row.findElement(By.cssSelector("button[title='Send'], form[action*='send'] button")).click();
                break;
            }
        }
        waitForPageLoad();
        return this;
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successMessage);
    }

    public String getSuccessMessage() {
        return getText(successMessage);
    }

    public boolean isInvoiceDisplayed(String invoiceNumber) {
        for (WebElement row : invoiceRows) {
            if (row.getText().contains(invoiceNumber)) {
                return true;
            }
        }
        return false;
    }

    public String getTotalAmount() {
        return getText(totalAmount);
    }
}