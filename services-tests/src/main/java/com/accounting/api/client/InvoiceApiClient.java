package com.accounting.api.client;

import com.accounting.api.model.InvoiceDTO;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Invoice API Client
 * Handles all invoice-related API calls
 */
@Slf4j
public class InvoiceApiClient extends BaseApiClient {

    private static final String INVOICES_API_PATH = "/api/invoices";
    private static final String INVOICES_WEB_PATH = "/invoices";

    // API Endpoints (JSON)
    public Response getAllInvoices() {
        return get(INVOICES_API_PATH);
    }

    public Response getInvoiceById(Long id) {
        return get(INVOICES_API_PATH + "/" + id);
    }

    public Response getInvoicesByStatus(String status) {
        Map<String, String> params = new HashMap<>();
        params.put("status", status);
        return get(INVOICES_API_PATH, params);
    }

    public Response getInvoicesByCustomer(Long customerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerId", customerId);
        return get(INVOICES_API_PATH, params);
    }

    public Response createInvoice(InvoiceDTO invoice) {
        return post(INVOICES_API_PATH, invoice);
    }

    public Response updateInvoice(Long id, InvoiceDTO invoice) {
        return put(INVOICES_API_PATH + "/" + id, invoice);
    }

    public Response deleteInvoice(Long id) {
        return delete(INVOICES_API_PATH + "/" + id);
    }

    public Response sendInvoice(Long id) {
        return post(INVOICES_API_PATH + "/" + id + "/send", null);
    }

    public Response markAsPaid(Long id, BigDecimal amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        return post(INVOICES_API_PATH + "/" + id + "/pay", body);
    }

    public Response cancelInvoice(Long id) {
        return post(INVOICES_API_PATH + "/" + id + "/cancel", null);
    }

    // Customer endpoints
    public Response getAllCustomers() {
        return get(INVOICES_API_PATH + "/customers");
    }

    public Response getCustomerById(Long id) {
        return get(INVOICES_API_PATH + "/customers/" + id);
    }

    public Response createCustomer(Map<String, Object> customer) {
        return post(INVOICES_API_PATH + "/customers", customer);
    }

    // Web Endpoints
    public Response getInvoicesListPage() {
        return get(INVOICES_WEB_PATH);
    }

    public Response getNewInvoiceForm() {
        return get(INVOICES_WEB_PATH + "/new");
    }

    public Response saveInvoiceForm(InvoiceDTO invoice) {
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("customer.id", invoice.getCustomerId());
        formParams.put("invoiceDate", invoice.getInvoiceDate().toString());
        formParams.put("dueDate", invoice.getDueDate().toString());
        if (invoice.getNotes() != null) {
            formParams.put("notes", invoice.getNotes());
        }

        // Add items
        for (int i = 0; i < invoice.getItems().size(); i++) {
            InvoiceDTO.InvoiceItemDTO item = invoice.getItems().get(i);
            formParams.put("items[" + i + "].description", item.getDescription());
            formParams.put("items[" + i + "].quantity", item.getQuantity());
            formParams.put("items[" + i + "].unitPrice", item.getUnitPrice());
            if (item.getAccountId() != null) {
                formParams.put("items[" + i + "].account.id", item.getAccountId());
            }
        }

        return postForm(INVOICES_WEB_PATH + "/save", formParams);
    }

    public Response getEditInvoiceForm(Long id) {
        return get(INVOICES_WEB_PATH + "/edit/" + id);
    }

    public Response viewInvoice(Long id) {
        return get(INVOICES_WEB_PATH + "/view/" + id);
    }

    public Response sendInvoiceForm(Long id) {
        return postForm(INVOICES_WEB_PATH + "/send/" + id, new HashMap<>());
    }

    public Response markAsPaidForm(Long id, BigDecimal amount) {
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("amount", amount);
        return postForm(INVOICES_WEB_PATH + "/pay/" + id, formParams);
    }

    public Response cancelInvoiceForm(Long id) {
        return postForm(INVOICES_WEB_PATH + "/cancel/" + id, new HashMap<>());
    }

    public Response getCustomersPage() {
        return get(INVOICES_WEB_PATH + "/customers");
    }

    public Response getNewCustomerForm() {
        return get(INVOICES_WEB_PATH + "/customers/new");
    }

    public Response saveCustomerForm(Map<String, Object> customer) {
        return postForm(INVOICES_WEB_PATH + "/customers/save", customer);
    }

    // Helper methods
    public List<InvoiceDTO> getAllInvoicesAsList() {
        return getAllInvoices()
                .then()
                .extract()
                .jsonPath()
                .getList(".", InvoiceDTO.class);
    }

    public InvoiceDTO getInvoiceByIdAsDTO(Long id) {
        return getInvoiceById(id)
                .then()
                .extract()
                .as(InvoiceDTO.class);
    }
}