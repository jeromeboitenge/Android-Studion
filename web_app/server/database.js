const sqlite3 = require('sqlite3').verbose();

// Connect to SQLite database
const db = new sqlite3.Database('./machines.db', (err) => {
    if (err) {
        console.error('Error opening database ' + err.message);
    } else {
        console.log('Connected to the SQLite database.');

        // Create Brands Table (New)
        db.run(`CREATE TABLE IF NOT EXISTS brands (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT UNIQUE NOT NULL,
            description TEXT
        )`, (err) => {
            if (err) {
                console.error("Error creating brands table: " + err.message);
            } else {
                // Seed Brands if empty
                db.get("SELECT count(*) as count FROM brands", (err, row) => {
                    if (row && row.count === 0) {
                        const brands = ['Dell', 'HP', 'Apple', 'Lenovo', 'Asus'];
                        const stmt = db.prepare("INSERT INTO brands (name, description) VALUES (?, ?)");
                        brands.forEach(brand => {
                            stmt.run(brand, `Description for ${brand}`);
                        });
                        stmt.finalize();
                        console.log("Seeded brands table");
                    }
                });
            }
        });

        // Create Machines Table
        db.run(`CREATE TABLE IF NOT EXISTS machines (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            serial_number TEXT UNIQUE NOT NULL,
            status TEXT CHECK(status IN ('Active', 'Inactive')) DEFAULT 'Active',
            location TEXT,
            brand_id INTEGER,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (brand_id) REFERENCES brands(id)
        )`, (err) => {
            if (err) {
                console.error("Error creating machines table: " + err.message);
            } else {
                // Try to add brand_id column if it doesn't exist (Migration)
                db.run(`ALTER TABLE machines ADD COLUMN brand_id INTEGER`, (err) => {
                    // Ignore error if column already exists
                    if (!err) console.log("Added brand_id column to machines");
                });
            }
        });

        // Create Machine Maintenance Table
        db.run(`CREATE TABLE IF NOT EXISTS machine_maintenance (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            machine_id INTEGER,
            maintenance_type TEXT,
            description TEXT,
            maintenance_date DATE,
            FOREIGN KEY (machine_id) REFERENCES machines(id) ON DELETE CASCADE
        )`, (err) => {
            if (err) {
                console.error("Error creating maintenance table: " + err.message);
            }
        });
    }
});

module.exports = db;
