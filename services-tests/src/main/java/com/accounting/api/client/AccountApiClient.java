package com.accounting.api.client;

import com.accounting.api.model.AccountDTO;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Account API Client
 * Handles all account-related API calls
 */
@Slf4j
public class AccountApiClient extends BaseApiClient {

    private static final String ACCOUNTS_PATH = "/api/accounts";
    private static final String ACCOUNTS_WEB_PATH = "/accounts";

    // API Endpoints (JSON)
    public Response getAllAccounts() {
        return get(ACCOUNTS_PATH);
    }

    public Response getAccountById(Long id) {
        return get(ACCOUNTS_PATH + "/" + id);
    }

    public Response getAccountByCode(String code) {
        return get(ACCOUNTS_PATH + "/code/" + code);
    }

    public Response getAccountsByType(String type) {
        return get(ACCOUNTS_PATH + "/type/" + type);
    }

    public Response createAccount(AccountDTO account) {
        return post(ACCOUNTS_PATH, account);
    }

    public Response updateAccount(Long id, AccountDTO account) {
        return put(ACCOUNTS_PATH + "/" + id, account);
    }

    public Response deleteAccount(Long id) {
        return delete(ACCOUNTS_PATH + "/" + id);
    }

    public Response activateAccount(Long id) {
        return post(ACCOUNTS_PATH + "/" + id + "/activate", null);
    }

    public Response deactivateAccount(Long id) {
        return post(ACCOUNTS_PATH + "/" + id + "/deactivate", null);
    }

    public Response getAccountBalance(Long id) {
        return get(ACCOUNTS_PATH + "/" + id + "/balance");
    }

    // Web Endpoints (Form submission)
    public Response getAccountsListPage() {
        return get(ACCOUNTS_WEB_PATH);
    }

    public Response getAllAccountsPage() {
        return get(ACCOUNTS_WEB_PATH + "/all");
    }

    public Response getNewAccountForm() {
        return get(ACCOUNTS_WEB_PATH + "/new");
    }

    public Response saveAccountForm(AccountDTO account) {
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("code", account.getCode());
        formParams.put("name", account.getName());
        formParams.put("accountType", account.getAccountType());
        if (account.getDescription() != null) {
            formParams.put("description", account.getDescription());
        }
        if (account.getParentId() != null) {
            formParams.put("parent.id", account.getParentId());
        }
        if (account.getCurrencyId() != null) {
            formParams.put("currency.id", account.getCurrencyId());
        }
        return postForm(ACCOUNTS_WEB_PATH + "/save", formParams);
    }

    public Response getEditAccountForm(Long id) {
        return get(ACCOUNTS_WEB_PATH + "/edit/" + id);
    }

    public Response viewAccount(Long id) {
        return get(ACCOUNTS_WEB_PATH + "/view/" + id);
    }

    public Response deactivateAccountForm(Long id) {
        return postForm(ACCOUNTS_WEB_PATH + "/deactivate/" + id, new HashMap<>());
    }

    public Response activateAccountForm(Long id) {
        return postForm(ACCOUNTS_WEB_PATH + "/activate/" + id, new HashMap<>());
    }

    public Response getAccountsByTypeFilter(String type) {
        return get(ACCOUNTS_WEB_PATH + "/by-type/" + type);
    }

    // Helper methods
    public List<AccountDTO> getAllAccountsAsList() {
        return getAllAccounts()
                .then()
                .extract()
                .jsonPath()
                .getList(".", AccountDTO.class);
    }

    public AccountDTO getAccountByIdAsDTO(Long id) {
        return getAccountById(id)
                .then()
                .extract()
                .as(AccountDTO.class);
    }
}