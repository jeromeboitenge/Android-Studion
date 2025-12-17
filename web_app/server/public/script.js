const API_URL = '/api/machines';
const BRANDS_API_URL = '/api/brands';
let isEditing = false;
let currentMachineId = null;

document.addEventListener('DOMContentLoaded', () => {
    loadBrands(); // Load brands first
    fetchMachines();

    document.getElementById('machineForm').addEventListener('submit', async (e) => {
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
    } else {
        const res = await response.json();
        alert('Error: ' + (res.error || 'Unknown error'));
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
        list.innerHTML = '<p>No machines found.</p>';
        return;
    }

    machines.forEach(m => {
        const card = document.createElement('div');
        card.className = 'computer-card';
        card.innerHTML = `
            <h3>${m.name}</h3>
            <div style="font-size: 0.9em; color: #555; font-weight: bold;">${m.brand_name || 'Unknown Brand'}</div> 
            <div style="font-size: 0.9em; color: #666; margin-bottom: 5px;">SN: ${m.serial_number}</div>
            <div style="font-size: 0.9em; margin-bottom: 5px;">Location: ${m.location}</div>
            <div class="${m.status === 'Active' ? 'status-active' : 'status-inactive'}">${m.status}</div>
            
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

    // NOTE: This assumes we have a POST endpoint or we are just mocking for UI demo of "Linked Tables". 
    // Since I must strictly follow "view details... from 2 linked tables", showing is enough.
    // But to make it interactive, I'll alert the user.
    alert("Maintenance Log feature is view-only for this proof of concept. To fully implement adding logs, a new API endpoint is required on the server.");
}
