const API_URL = 'http://localhost:3000/api/machines';

// DOM Elements
const machineGrid = document.getElementById('machine-grid');
const addMachineBtn = document.getElementById('add-machine-btn');
const modal = document.getElementById('modal');
const closeBtn = document.querySelector('.close');
const addMachineForm = document.getElementById('add-machine-form');

// State
let machines = [];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchMachines();
    setupEventListeners();
});

function setupEventListeners() {
    // Modal controls
    addMachineBtn.addEventListener('click', () => {
        modal.classList.add('show');
    });

    closeBtn.addEventListener('click', () => {
        closeModal();
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });

    // Form Submit
    addMachineForm.addEventListener('submit', handleAddMachine);
}

function closeModal() {
    modal.classList.remove('show');
    addMachineForm.reset();
}

// Fetch Machines
async function fetchMachines() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error('Failed to fetch machines');

        machines = await response.json();
        renderMachines();
    } catch (error) {
        console.error('Error:', error);
        // showToast('Failed to load machines', 'error');
    }
}

// Render Machines
function renderMachines() {
    machineGrid.innerHTML = '';

    if (machines.length === 0) {
        machineGrid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: var(--text-sub);">No machines found. Add one to get started.</p>';
        return;
    }

    machines.forEach(machine => {
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
            <h3>${escapeHtml(machine.name)}</h3>
            <span class="sn">${escapeHtml(machine.serial_number)}</span>
            
            <div style="margin-top: 12px;">
                <span class="badge ${getStatusClass(machine.status)}">${escapeHtml(machine.status)}</span>
            </div>

            <div class="meta">
                <span style="color: var(--text-sub); font-size: 0.9rem;">
                    📍 ${escapeHtml(machine.location)}
                </span>
                <button class="btn-delete" onclick="deleteMachine(${machine.id})">Delete</button>
            </div>
        `;
        machineGrid.appendChild(card);
    });
}

function getStatusClass(status) {
    if (!status) return 'active';
    const s = status.toLowerCase();
    if (s.includes('active')) return 'active';
    if (s.includes('maintenance')) return 'maintenance';
    return 'inactive';
}

// Add Machine
async function handleAddMachine(e) {
    e.preventDefault();

    const formData = new FormData(addMachineForm);
    const data = {
        name: formData.get('name'),
        serial_number: formData.get('serial_number'),
        status: formData.get('status'),
        location: formData.get('location')
    };

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error('Failed to add machine');

        await fetchMachines(); // Reload list
        closeModal();
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to add machine');
    }
}

// Delete Machine
window.deleteMachine = async (id) => {
    if (!confirm('Are you sure you want to delete this machine?')) return;

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete machine');

        await fetchMachines();
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to delete machine');
    }
};

// Utils
function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
