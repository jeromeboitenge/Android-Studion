// Professional Utility Functions

// Toast Notification System
const Toast = {
    container: null,

    init() {
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        }
    },

    show(message, type = 'info', duration = 3000) {
        this.init();

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        const icon = this.getIcon(type);
        toast.innerHTML = `
            <span style="font-size: 20px;">${icon}</span>
            <span>${message}</span>
        `;

        this.container.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideIn 0.3s ease reverse';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    },

    getIcon(type) {
        const icons = {
            success: '✓',
            error: '✕',
            warning: '⚠',
            info: 'ℹ'
        };
        return icons[type] || icons.info;
    },

    success(message) { this.show(message, 'success'); },
    error(message) { this.show(message, 'error'); },
    warning(message) { this.show(message, 'warning'); },
    info(message) { this.show(message, 'info'); }
};

// CSV Export Function
function exportToCSV(data, filename = 'export.csv') {
    if (!data || data.length === 0) {
        Toast.warning('No data to export');
        return;
    }

    // Get headers from first object
    const headers = Object.keys(data[0]);

    // Create CSV content
    let csv = headers.join(',') + '\n';

    data.forEach(row => {
        const values = headers.map(header => {
            const value = row[header] || '';
            // Escape quotes and wrap in quotes if contains comma
            return typeof value === 'string' && value.includes(',')
                ? `"${value.replace(/"/g, '""')}"`
                : value;
        });
        csv += values.join(',') + '\n';
    });

    // Create download link
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);

    Toast.success('Data exported successfully!');
}

// Loading Spinner
const Loading = {
    show(container) {
        const spinner = document.createElement('div');
        spinner.className = 'spinner';
        spinner.id = 'loading-spinner';
        container.appendChild(spinner);
    },

    hide() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) spinner.remove();
    }
};

// Chart Creation Helper (requires Chart.js)
function createChart(canvasId, type, data, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    // Destroy existing chart if any
    const existingChart = Chart.getChart(canvasId);
    if (existingChart) existingChart.destroy();

    return new Chart(ctx, {
        type: type,
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            ...options
        }
    });
}

// Status Chart for Dashboard
function createStatusChart(activeCount, inactiveCount) {
    const data = {
        labels: ['Active', 'Inactive'],
        datasets: [{
            data: [activeCount, inactiveCount],
            backgroundColor: ['#4caf50', '#f44336'],
            borderWidth: 0
        }]
    };

    const options = {
        plugins: {
            legend: {
                position: 'bottom'
            }
        }
    };

    return createChart('statusChart', 'doughnut', data, options);
}

// Brand Distribution Chart
function createBrandChart(machines) {
    const brandCounts = {};
    machines.forEach(m => {
        const brand = m.brand_name || 'Unknown';
        brandCounts[brand] = (brandCounts[brand] || 0) + 1;
    });

    const data = {
        labels: Object.keys(brandCounts),
        datasets: [{
            label: 'Machines per Brand',
            data: Object.values(brandCounts),
            backgroundColor: [
                '#009688', '#00796B', '#FFC107', '#FF5722', '#2196F3',
                '#9C27B0', '#4CAF50', '#FF9800', '#795548', '#607D8B'
            ],
            borderWidth: 0
        }]
    };

    const options = {
        plugins: {
            legend: {
                position: 'right'
            }
        }
    };

    return createChart('brandChart', 'pie', data, options);
}

// Advanced Table Sorting
function sortTable(table, column, ascending = true) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.sort((a, b) => {
        const aValue = a.cells[column].textContent.trim();
        const bValue = b.cells[column].textContent.trim();

        // Try to parse as number
        const aNum = parseFloat(aValue);
        const bNum = parseFloat(bValue);

        if (!isNaN(aNum) && !isNaN(bNum)) {
            return ascending ? aNum - bNum : bNum - aNum;
        }

        // String comparison
        return ascending
            ? aValue.localeCompare(bValue)
            : bValue.localeCompare(aValue);
    });

    rows.forEach(row => tbody.appendChild(row));
}

// Date Formatting
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Debounce function for search
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Print Report
function printReport() {
    window.print();
}

// Generate Report Summary
async function generateReportSummary() {
    try {
        const [machines, stats] = await Promise.all([
            fetch('/api/machines').then(r => r.json()),
            fetch('/api/stats').then(r => r.json())
        ]);

        return {
            totalMachines: stats.total_machines,
            activeMachines: stats.active_machines,
            inactiveMachines: stats.inactive_machines,
            totalBrands: stats.total_brands,
            machines: machines,
            generatedAt: new Date().toLocaleString()
        };
    } catch (error) {
        console.error('Error generating report:', error);
        Toast.error('Failed to generate report');
        return null;
    }
}
