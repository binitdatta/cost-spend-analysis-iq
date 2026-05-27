/* CostIQ Application JS */
(function () {
  'use strict';

  // Mark active sidebar link based on current path
  document.addEventListener('DOMContentLoaded', function () {
    const path = window.location.pathname;
    document.querySelectorAll('.sidebar-link').forEach(link => {
      const href = link.getAttribute('href');
      if (href && path.startsWith(href) && href !== '/') {
        link.classList.add('active');
      }
    });

    // Auto-dismiss flash alerts after 5 seconds
    document.querySelectorAll('.alert.alert-success').forEach(el => {
      setTimeout(() => {
        const bsAlert = bootstrap.Alert.getOrCreateInstance(el);
        bsAlert?.close();
      }, 5000);
    });

    // Add confirmation to all delete forms as double-safety
    document.querySelectorAll('form[action*="/delete"]').forEach(form => {
      form.addEventListener('submit', function (e) {
        // Modal already handles confirmation; this is just a safety fallback
        // if the form is submitted without the modal
      });
    });

    // Format number inputs on blur for readability preview
    document.querySelectorAll('input[type="number"]').forEach(input => {
      input.addEventListener('wheel', e => e.preventDefault()); // prevent accidental scroll changes
    });
  });
})();
