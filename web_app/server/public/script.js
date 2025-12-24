const API_URL = '/api/machines';
const BRANDS_API_URL = '/api/brands';
let isEditing = false;
let currentMachineId = null;

document.addEventListener('DOMContentLoaded', () => {
    // Only load brands if the select element exists (for the form)
    if (document.getElementById('brand')) {
        loadBrands();
    }

    // Only fetch machines if the list container exists
    if (document.getElementById('machineList')) {
        fetchMachines();
    }

    if (document.getElementById('dashboardStats')) {
        fetchStats();
    }

    const form = document.getElementById('machineForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const id = document.getElementById('editId').value;
            const name = document.getElementById('name').value;
            const serial = document.getElementById('serial').value;
            const location = document.getElementById('location').value;
            const status = document.getElementById('status').value;
            const brandId = document.getElementById('brand').value; // Get selected brand ID

            if (isEditing && id) {
                updateMachine(id, name, serial, location, status, brandId);
            } else {
                createMachine(name, serial, location, status, brandId);
            }
        });
    }
});

async function loadBrands() {
    try {
        const response = await fetch(BRANDS_API_URL);
        const brands = await response.json();
        const select = document.getElementById('brand');

        // Clear existing (except default if needed, but here we reload all)
        select.innerHTML = '<option value="">Select Brand</option>';

        brands.forEach(b => {
            const option = document.createElement('option');
            option.value = b.id;
            option.textContent = b.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading brands:', error);
    }
}

async function createMachine(name, serial, location, status, brandId) {
    const data = { name, serial_number: serial, location, status, brand_id: brandId };

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        handleResponse(response);
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to connect to server.');
    }
}

async function updateMachine(id, name, serial, location, status, brandId) {
    const data = { name, serial_number: serial, location, status, brand_id: brandId };

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        handleResponse(response, true);
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to connect to server.');
    }
}

async function deleteMachine(id) {
    if (!confirm('Are you sure you want to delete this machine?')) return;

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });
        if (response.ok) fetchMachines();
    } catch (error) {
        console.error('Error:', error);
    }
}

function startEdit(id, name, serial, location, status, brandId) {
    isEditing = true;
    document.getElementById('editId').value = id;
    document.getElementById('name').value = name;
    document.getElementById('serial').value = serial;
    document.getElementById('location').value = location;
    document.getElementById('status').value = status;
    document.getElementById('brand').value = brandId || ""; // Set brand selection

    document.querySelector('button[type="submit"]').textContent = "Update Machine";
}

function resetForm() {
    isEditing = false;
    document.getElementById('editId').value = '';
    document.getElementById('machineForm').reset();
    document.querySelector('button[type="submit"]').textContent = "Save Machine";
}

async function handleResponse(response, isUpdate = false) {
    if (response.ok) {
        resetForm();
        fetchMachines();
        Toast.success(isUpdate ? 'Machine updated successfully!' : 'Machine added successfully!');
    } else {
        const res = await response.json();
        Toast.error('Error: ' + (res.error || 'Unknown error'));
    }
}

async function fetchMachines() {
    try {
        const response = await fetch(API_URL);
        const machines = await response.json();
        displayMachines(machines);
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}

function displayMachines(machines) {
    const list = document.getElementById('machineList');
    list.innerHTML = '';

    if (machines.length === 0) {
        list.innerHTML = '<p style="text-align: center; color: #666; padding: 2rem;">No machines found. Add your first machine above!</p>';
        return;
    }

    machines.forEach(m => {
        const card = document.createElement('div');
        card.className = 'computer-card';
        const statusClass = m.status === 'Active' ? 'status-active' : 'status-inactive';

        card.innerHTML = `
            <h3>${m.name}</h3>
            <div class="brand-name">${m.brand_name || 'Unknown Brand'}</div>
            <div class="machine-info"><strong>Serial:</strong> ${m.serial_number}</div>
            <div class="machine-info"><strong>Location:</strong> ${m.location || 'Not specified'}</div>
            <div class="${statusClass}" style="margin: 0.8rem 0; font-weight: 600;">${m.status}</div>
            
            <div class="card-actions">
                <button onclick="viewMaintenance(${m.id}, '${m.name}')" class="action-btn history-btn">History</button>
                <button onclick="startEdit(${m.id}, '${m.name}', '${m.serial_number}', '${m.location}', '${m.status}', '${m.brand_id}')" class="action-btn edit-btn">Edit</button>
                <button onclick="deleteMachine(${m.id})" class="action-btn delete-btn">Delete</button>
            </div>
        `;
        list.appendChild(card);
    });
}

// --- Maintenance Modal Logic ---

function closeModal() {
    document.getElementById('maintenanceModal').style.display = 'none';
    currentMachineId = null;
}

// Close modal when clicking outside
window.onclick = function (event) {
    const modal = document.getElementById('maintenanceModal');
    if (event.target == modal) {
        modal.style.display = 'none';
    }
}

async function viewMaintenance(machineId, machineName) {
    currentMachineId = machineId;
    document.getElementById('modalTitle').textContent = `History: ${machineName}`;
    document.getElementById('maintenanceModal').style.display = 'block';

    // Fetch logs
    const listDiv = document.getElementById('maintenanceList');
    listDiv.innerHTML = '<p>Loading...</p>';

    try {
        const response = await fetch(`${API_URL}/${machineId}/maintenance`);
        const logs = await response.json();

        listDiv.innerHTML = '';
        if (logs.length === 0) {
            listDiv.innerHTML = '<p>No maintenance records found.</p>';
        } else {
            logs.forEach(log => {
                const item = document.createElement('div');
                item.className = 'maintenance-item';
                item.innerHTML = `
                    <strong>${log.maintenance_type || 'General'}</strong> <small>(${log.maintenance_date})</small><br>
                    ${log.description}
                `;
                listDiv.appendChild(item);
            });
        }
    } catch (e) {
        listDiv.innerHTML = '<p>Error loading logs.</p>';
    }
}

// Optional: Add simple log for demo (Server needs standard POST endpoint for this really, ensuring schema is followed)
async function addMaintenanceLog() {
    if (!currentMachineId) return;
    const desc = document.getElementById('logDesc').value;
    const date = document.getElementById('logDate').value || new Date().toISOString().split('T')[0];

    if (!desc) { alert("Enter description"); return; }

    try {
        const response = await fetch(`${API_URL}/${currentMachineId}/maintenance`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ description: desc, maintenance_date: date })
        });

        if (response.ok) {
            Toast.success('Maintenance log added successfully!');
        } else {
            const res = await response.json();
            Toast.error('Error: ' + res.error);
        }
    } catch (e) {
        console.error(e);
        Toast.error('Failed to add maintenance log');
    }
}

async function deleteBrand(id) {
    if (!confirm('Are you sure you want to delete this brand?')) return;

    try {
        const response = await fetch(`${BRANDS_API_URL}/${id}`, {
            method: 'DELETE'
        });
        const res = await response.json();

        if (response.ok) {
            // Refresh the brand list (this function needs to be available or we reload)
            if (typeof fetchBrandsList === 'function') {
                fetchBrandsList();
            } else {
                window.location.reload();
            }
            Toast.success('Brand deleted successfully!');
        } else {
            Toast.error('Error: ' + res.error);
        }
    } catch (error) {
        console.error('Error:', error);
        Toast.error('Failed to delete brand');
    }
}

async function fetchStats() {
    try {
        const response = await fetch('/api/stats');
        const stats = await response.json();

        document.getElementById('statTotal').textContent = stats.total_machines;
        document.getElementById('statActive').textContent = stats.active_machines;
        document.getElementById('statInactive').textContent = stats.inactive_machines;
        document.getElementById('statBrands').textContent = stats.total_brands;
    } catch (error) {
        console.error("Error loading stats:", error);
    }
}
