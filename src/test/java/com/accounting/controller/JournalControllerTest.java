package com.accounting.controller;

import com.accounting.dto.JournalEntryDTO;
import com.accounting.model.*;
import com.accounting.service.AccountService;
import com.accounting.service.JournalService;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JournalController.class)
@DisplayName("JournalController Integration Tests")
class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JournalService journalService;

    @MockBean
    private AccountService accountService;

    private Account cashAccount;
    private Account revenueAccount;
    private JournalEntry draftEntry;
    private JournalEntry postedEntry;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        revenueAccount = TestDataBuilder.createSalesRevenue();
        draftEntry = TestDataBuilder.createBalancedJournalEntry(cashAccount, revenueAccount, BigDecimal.valueOf(1000));
        postedEntry = TestDataBuilder.createPostedEntry();
    }

    @Nested
    @DisplayName("List Journal Entries")
    class ListJournalEntries {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display journal entries list")
        void listJournalEntries_ReturnsJournalListView() throws Exception {
            when(journalService.findAllWithLines(any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(Arrays.asList(draftEntry)));

            mockMvc.perform(get("/journal"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("journal/list"))
                    .andExpect(model().attributeExists("entries"));
        }

        @Test
        @DisplayName("Should return unauthorized when not authenticated")
        void listJournalEntries_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/journal"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("View Journal Entry")
    class ViewJournalEntry {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display journal entry details")
        void viewJournalEntry_WhenExists_ReturnsDetailView() throws Exception {
            // Use a simple entry without bidirectional references to avoid StackOverflow
            JournalEntry simpleEntry = TestDataBuilder.createDraftEntry();
            simpleEntry.setLines(new ArrayList<>());
            when(journalService.findByIdWithLines(1L)).thenReturn(Optional.of(simpleEntry));

            mockMvc.perform(get("/journal/view/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("journal/view"))
                    .andExpect(model().attributeExists("entry"));
        }
    }

    @Nested
    @DisplayName("Create Journal Entry")
    class CreateJournalEntry {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display create form")
        void showCreateForm_ReturnsCreateView() throws Exception {
            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount, revenueAccount));

            mockMvc.perform(get("/journal/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("journal/form"))
                    .andExpect(model().attributeExists("journalEntry"))
                    .andExpect(model().attributeExists("accounts"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should create entry and redirect on success")
        void createJournalEntry_ValidInput_RedirectsToList() throws Exception {
            when(journalService.createEntry(any(JournalEntryDTO.class), any()))
                    .thenReturn(draftEntry);

            mockMvc.perform(post("/journal/save")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("entryDate", "2026-01-23")
                            .param("description", "Test Entry")
                            .param("lines[0].accountId", "1")
                            .param("lines[0].debitAmount", "1000")
                            .param("lines[0].creditAmount", "0")
                            .param("lines[1].accountId", "4")
                            .param("lines[1].debitAmount", "0")
                            .param("lines[1].creditAmount", "1000"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/journal"));

            verify(journalService).createEntry(any(JournalEntryDTO.class), any());
        }
    }

    @Nested
    @DisplayName("Edit Journal Entry")
    class EditJournalEntry {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should display edit form for draft entry")
        void showEditForm_DraftEntry_ReturnsEditView() throws Exception {
            when(journalService.findByIdWithLines(1L)).thenReturn(Optional.of(draftEntry));
            when(accountService.findAllActive()).thenReturn(Arrays.asList(cashAccount, revenueAccount));

            mockMvc.perform(get("/journal/edit/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("journal/form"))
                    .andExpect(model().attributeExists("journalEntry"));
        }
    }

    @Nested
    @DisplayName("Post Journal Entry")
    class PostJournalEntry {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should post draft entry and redirect")
        void postEntry_DraftEntry_RedirectsToList() throws Exception {
            when(journalService.postEntry(1L)).thenReturn(postedEntry);

            mockMvc.perform(post("/journal/post/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/journal"));

            verify(journalService).postEntry(1L);
        }
    }

    @Nested
    @DisplayName("Void Journal Entry")
    class VoidJournalEntry {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should void posted entry and redirect")
        void voidEntry_PostedEntry_RedirectsToList() throws Exception {
            JournalEntry voidedEntry = TestDataBuilder.createJournalEntry(2L, "JE-202601-0002", EntryStatus.VOID);
            when(journalService.voidEntry(2L)).thenReturn(voidedEntry);

            mockMvc.perform(post("/journal/void/2")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/journal"));

            verify(journalService).voidEntry(2L);
        }
    }

    @Nested
    @DisplayName("Delete Journal Entry")
    class DeleteJournalEntry {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("Should delete draft entry and redirect to list")
        void deleteEntry_DraftEntry_RedirectsToList() throws Exception {
            doNothing().when(journalService).deleteEntry(1L);

            mockMvc.perform(post("/journal/delete/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/journal"));

            verify(journalService).deleteEntry(1L);
        }
    }
}