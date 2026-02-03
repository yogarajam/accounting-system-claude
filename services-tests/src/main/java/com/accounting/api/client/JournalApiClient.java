package com.accounting.api.client;

import com.accounting.api.model.JournalEntryDTO;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Journal Entry API Client
 * Handles all journal entry-related API calls
 */
@Slf4j
public class JournalApiClient extends BaseApiClient {

    private static final String JOURNAL_API_PATH = "/api/journal";
    private static final String JOURNAL_WEB_PATH = "/journal";

    // API Endpoints (JSON)
    public Response getAllEntries() {
        return get(JOURNAL_API_PATH);
    }

    public Response getEntryById(Long id) {
        return get(JOURNAL_API_PATH + "/" + id);
    }

    public Response getEntriesByStatus(String status) {
        Map<String, String> params = new HashMap<>();
        params.put("status", status);
        return get(JOURNAL_API_PATH, params);
    }

    public Response createEntry(JournalEntryDTO entry) {
        return post(JOURNAL_API_PATH, entry);
    }

    public Response updateEntry(Long id, JournalEntryDTO entry) {
        return put(JOURNAL_API_PATH + "/" + id, entry);
    }

    public Response deleteEntry(Long id) {
        return delete(JOURNAL_API_PATH + "/" + id);
    }

    public Response postEntry(Long id) {
        return post(JOURNAL_API_PATH + "/" + id + "/post", null);
    }

    public Response voidEntry(Long id) {
        return post(JOURNAL_API_PATH + "/" + id + "/void", null);
    }

    public Response getAccounts() {
        return get(JOURNAL_WEB_PATH + "/api/accounts");
    }

    // Web Endpoints (Form submission)
    public Response getJournalListPage() {
        return get(JOURNAL_WEB_PATH);
    }

    public Response getJournalListPage(int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        return get(JOURNAL_WEB_PATH, params);
    }

    public Response getNewEntryForm() {
        return get(JOURNAL_WEB_PATH + "/new");
    }

    public Response saveEntryForm(JournalEntryDTO entry) {
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("entryDate", entry.getEntryDate().toString());
        formParams.put("description", entry.getDescription());
        if (entry.getReference() != null) {
            formParams.put("reference", entry.getReference());
        }

        // Add lines
        for (int i = 0; i < entry.getLines().size(); i++) {
            JournalEntryDTO.JournalEntryLineDTO line = entry.getLines().get(i);
            formParams.put("lines[" + i + "].accountId", line.getAccountId());
            formParams.put("lines[" + i + "].debitAmount", line.getDebitAmount());
            formParams.put("lines[" + i + "].creditAmount", line.getCreditAmount());
            if (line.getDescription() != null) {
                formParams.put("lines[" + i + "].description", line.getDescription());
            }
        }

        return postForm(JOURNAL_WEB_PATH + "/save", formParams);
    }

    public Response getEditEntryForm(Long id) {
        return get(JOURNAL_WEB_PATH + "/edit/" + id);
    }

    public Response viewEntry(Long id) {
        return get(JOURNAL_WEB_PATH + "/view/" + id);
    }

    public Response postEntryForm(Long id) {
        return postForm(JOURNAL_WEB_PATH + "/post/" + id, new HashMap<>());
    }

    public Response voidEntryForm(Long id) {
        return postForm(JOURNAL_WEB_PATH + "/void/" + id, new HashMap<>());
    }

    public Response deleteEntryForm(Long id) {
        return postForm(JOURNAL_WEB_PATH + "/delete/" + id, new HashMap<>());
    }

    // Helper methods
    public List<JournalEntryDTO> getAllEntriesAsList() {
        return getAllEntries()
                .then()
                .extract()
                .jsonPath()
                .getList("content", JournalEntryDTO.class);
    }

    public JournalEntryDTO getEntryByIdAsDTO(Long id) {
        return getEntryById(id)
                .then()
                .extract()
                .as(JournalEntryDTO.class);
    }
}