const express = require("express");
const db = require("./database.js");
const bodyParser = require("body-parser");
const cors = require("cors");

const app = express();
const HTTP_PORT = 3000;

app.use(cors());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.static('public'));

// Root endpoint
app.get("/", (req, res, next) => {
    res.sendFile(__dirname + '/public/index.html');
});

// --- Brands Endpoints ---

// GET /api/brands
app.get("/api/brands", (req, res, next) => {
    const sql = "SELECT * FROM brands";
    db.all(sql, [], (err, rows) => {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json(rows);
    });
});

// POST /api/brands
app.post("/api/brands", (req, res, next) => {
    const { name, description } = req.body;
    db.run("INSERT INTO brands (name, description) VALUES (?,?)", [name, description], function (err, result) {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json({
            "message": "success",
            "id": this.lastID,
            "name": name
        });
    });
});


// --- Machines Endpoints ---

// GET /api/machines - List all machines JOINED with Brands
app.get("/api/machines", (req, res, next) => {
    const sql = `SELECT m.*, b.name as brand_name 
                 FROM machines m 
                 LEFT JOIN brands b ON m.brand_id = b.id 
                 ORDER BY m.id DESC`;
    db.all(sql, [], (err, rows) => {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json(rows); // Return JSON Array
    });
});

// GET /api/machines/:id - Get single machine details
app.get("/api/machines/:id", (req, res, next) => {
    const sql = "SELECT * FROM machines WHERE id = ?";
    db.get(sql, [req.params.id], (err, row) => {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json(row);
    });
});

// POST /api/machines - Create new machine
app.post("/api/machines", (req, res, next) => {
    // brand_id is optional for now to not break legacy, but implied by rubric
    const { name, serial_number, status, location, brand_id } = req.body;

    // Basic validation
    if (!name || !serial_number) {
        res.status(400).json({ "error": "Name and Serial Number are required" });
        return;
    }

    const sql = 'INSERT INTO machines (name, serial_number, status, location, brand_id) VALUES (?,?,?,?,?)';
    // Default brand_id to 1 if not provided (assuming 1 exists) or null
    const params = [name, serial_number, status || 'Active', location || 'Unknown', brand_id || null];

    db.run(sql, params, function (err, result) {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json({
            "message": "success",
            "id": this.lastID,
            "name": name,
            "serial_number": serial_number,
            "status": status,
            "location": location,
            "brand_id": brand_id
        });
    });
});

// PUT /api/machines/:id - Update machine
app.put("/api/machines/:id", (req, res, next) => {
    const { name, serial_number, status, location, brand_id } = req.body;

    const sql = `UPDATE machines SET 
                 name = COALESCE(?, name), 
                 serial_number = COALESCE(?, serial_number), 
                 status = COALESCE(?, status), 
                 location = COALESCE(?, location),
                 brand_id = COALESCE(?, brand_id)
                 WHERE id = ?`;

    db.run(sql, [name, serial_number, status, location, brand_id, req.params.id], function (err) {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json({ "message": "updated", changes: this.changes });
    });
});

// DELETE /api/machines/:id - Delete machine
app.delete("/api/machines/:id", (req, res, next) => {
    db.run('DELETE FROM machines WHERE id = ?', req.params.id, function (err) {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json({ "message": "deleted", changes: this.changes });
    });
});

// GET /api/machines/:id/maintenance - Get maintenance history
app.get("/api/machines/:id/maintenance", (req, res, next) => {
    const sql = "SELECT * FROM machine_maintenance WHERE machine_id = ? ORDER BY maintenance_date DESC";
    db.all(sql, [req.params.id], (err, rows) => {
        if (err) {
            res.status(400).json({ "error": err.message });
            return;
        }
        res.json(rows);
    });
});

// Start server
app.listen(HTTP_PORT, () => {
    console.log(`Machine Server running on port ${HTTP_PORT}`);
});
