/**
 * Accounting System - Main JavaScript
 */

$(document).ready(function() {
    // Initialize sidebar toggle
    initSidebarToggle();

    // Initialize tooltips
    initTooltips();

    // Initialize form validations
    initFormValidations();

    // Initialize number formatting
    initNumberFormatting();

    // Auto-dismiss alerts after 5 seconds
    initAutoAlertDismiss();
});

/**
 * Sidebar toggle functionality
 */
function initSidebarToggle() {
    $('#menu-toggle').on('click', function(e) {
        e.preventDefault();
        $('#wrapper').toggleClass('toggled');

        // Save preference to localStorage
        localStorage.setItem('sidebarToggled', $('#wrapper').hasClass('toggled'));
    });

    // Restore sidebar state from localStorage
    if (localStorage.getItem('sidebarToggled') === 'true') {
        $('#wrapper').addClass('toggled');
    }
}

/**
 * Initialize Bootstrap tooltips
 */
function initTooltips() {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * Form validation enhancements
 */
function initFormValidations() {
    // Prevent double form submission
    $('form').on('submit', function(e) {
        var $form = $(this);
        var $submitBtn = $form.find('button[type="submit"]');

        if ($form.data('submitted') === true) {
            e.preventDefault();
            return false;
        }

        $form.data('submitted', true);
        $submitBtn.prop('disabled', true);
        $submitBtn.html('<span class="spinner-border spinner-border-sm me-1" role="status"></span>Processing...');
    });

    // Confirm delete actions
    $('form[data-confirm]').on('submit', function(e) {
        var message = $(this).data('confirm') || 'Are you sure?';
        if (!confirm(message)) {
            e.preventDefault();
            return false;
        }
    });
}

/**
 * Number formatting for accounting displays
 */
function initNumberFormatting() {
    // Format currency display
    $('.currency-format').each(function() {
        var value = parseFloat($(this).text());
        if (!isNaN(value)) {
            $(this).text(formatCurrency(value));
        }
    });
}

/**
 * Format a number as currency
 */
function formatCurrency(amount, decimals = 2) {
    return new Intl.NumberFormat('en-US', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals
    }).format(amount);
}

/**
 * Auto-dismiss alerts after a delay
 */
function initAutoAlertDismiss() {
    setTimeout(function() {
        $('.alert-dismissible').fadeOut('slow', function() {
            $(this).remove();
        });
    }, 5000);
}

/**
 * Calculate journal entry totals
 */
function calculateJournalTotals() {
    var totalDebit = 0;
    var totalCredit = 0;

    $('.debit-amount').each(function() {
        totalDebit += parseFloat($(this).val()) || 0;
    });

    $('.credit-amount').each(function() {
        totalCredit += parseFloat($(this).val()) || 0;
    });

    $('#totalDebit').text(formatCurrency(totalDebit));
    $('#totalCredit').text(formatCurrency(totalCredit));

    var difference = Math.abs(totalDebit - totalCredit);
    $('#difference').text(formatCurrency(difference));

    // Highlight imbalance
    if (difference > 0.001) {
        $('#balanceStatus').removeClass('text-success').addClass('text-danger');
        $('#balanceStatus').html('<i class="bi bi-exclamation-triangle me-1"></i>Out of Balance');
    } else {
        $('#balanceStatus').removeClass('text-danger').addClass('text-success');
        $('#balanceStatus').html('<i class="bi bi-check-circle me-1"></i>Balanced');
    }
}

/**
 * Add a new line to journal entry form
 */
function addJournalLine() {
    var $template = $('#lineTemplate').html();
    var index = $('.journal-line').length;

    $template = $template.replace(/\{index\}/g, index);
    $('#journalLines').append($template);
}

/**
 * Remove a journal entry line
 */
function removeJournalLine(button) {
    if ($('.journal-line').length > 2) {
        $(button).closest('.journal-line').remove();
        calculateJournalTotals();
    } else {
        alert('A journal entry must have at least 2 lines.');
    }
}

/**
 * Date picker initialization
 */
function initDatePickers() {
    $('input[type="date"]').each(function() {
        if (!$(this).val()) {
            $(this).val(new Date().toISOString().split('T')[0]);
        }
    });
}

/**
 * Search/filter table rows
 */
function filterTable(inputId, tableId) {
    var filter = $('#' + inputId).val().toUpperCase();
    var $table = $('#' + tableId);
    var $rows = $table.find('tbody tr');

    $rows.each(function() {
        var text = $(this).text().toUpperCase();
        $(this).toggle(text.indexOf(filter) > -1);
    });
}

/**
 * Export table to CSV
 */
function exportTableToCSV(tableId, filename) {
    var csv = [];
    var $table = $('#' + tableId);
    var $rows = $table.find('tr');

    $rows.each(function() {
        var row = [];
        $(this).find('th, td').each(function() {
            var text = $(this).text().trim().replace(/"/g, '""');
            row.push('"' + text + '"');
        });
        csv.push(row.join(','));
    });

    var csvContent = csv.join('\n');
    var blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    var link = document.createElement('a');

    if (navigator.msSaveBlob) {
        navigator.msSaveBlob(blob, filename);
    } else {
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
    }
}

/**
 * Print specific element
 */
function printElement(elementId) {
    var $element = $('#' + elementId);
    var printContents = $element.html();
    var originalContents = document.body.innerHTML;

    document.body.innerHTML = printContents;
    window.print();
    document.body.innerHTML = originalContents;
    location.reload();
}

/**
 * Confirm action with custom message
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * Show loading spinner
 */
function showLoading() {
    $('body').append('<div class="spinner-overlay"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>');
}

/**
 * Hide loading spinner
 */
function hideLoading() {
    $('.spinner-overlay').remove();
}

/**
 * Format date for display
 */
function formatDate(dateString) {
    var date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

/**
 * Validate that debits equal credits before form submission
 */
function validateJournalEntry() {
    var totalDebit = 0;
    var totalCredit = 0;

    $('.debit-amount').each(function() {
        totalDebit += parseFloat($(this).val()) || 0;
    });

    $('.credit-amount').each(function() {
        totalCredit += parseFloat($(this).val()) || 0;
    });

    if (Math.abs(totalDebit - totalCredit) > 0.001) {
        alert('Journal entry is not balanced. Total debits must equal total credits.');
        return false;
    }

    if (totalDebit === 0) {
        alert('Journal entry cannot be empty. Please add at least one debit and credit entry.');
        return false;
    }

    return true;
}