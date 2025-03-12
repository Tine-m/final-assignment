# **📝 Assignment: Implementing Partitioning in the Esports Tournament Database**

Your task is to **implement partitioning** on relevant tables to optimize query performance and scalability.

---

## **📌 Exercise: Implementing Partitioning**

### **1️⃣ Partition `Matches` by Year (RANGE Partitioning)**
The `Matches` table grows **quickly**, slowing down queries. Implement **RANGE partitioning** based on `match_date` to store matches in separate partitions per year.

✅ **Modify the `Matches` table:**
```sql
CREATE TABLE Matches (
    match_id INT NOT NULL,
    tournament_id INT NOT NULL,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    winner_id INT NULL,
    match_date DATETIME NOT NULL,
    PRIMARY KEY (match_id, match_date)
)
PARTITION BY RANGE(YEAR(match_date)) (
    PARTITION p0 VALUES LESS THAN (2020),
    PARTITION p1 VALUES LESS THAN (2022),
    PARTITION p2 VALUES LESS THAN (2024)
);
```

✅ **Test Query Optimization:**
```sql
EXPLAIN ANALYZE SELECT * FROM Matches WHERE match_date >= '2021-01-01';
```
📌 **Compare execution time before and after partitioning!**

---

### **2️⃣ Partition `Tournaments` by Game Type (LIST Partitioning)**
To optimize tournament queries, partition the `Tournaments` table **by game type**.

✅ **Modify the `Tournaments` table:**
```sql
CREATE TABLE Tournaments (
    tournament_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    game VARCHAR(50) NOT NULL,
    max_players INT NOT NULL,
    start_date DATETIME NOT NULL,
    PRIMARY KEY (tournament_id, game)
)
PARTITION BY LIST COLUMNS(game) (
    PARTITION csgo VALUES IN ('CS:GO'),
    PARTITION dota VALUES IN ('Dota 2'),
    PARTITION valorant VALUES IN ('Valorant'),
    PARTITION lol VALUES IN ('League of Legends')
);
```

✅ **Run Optimized Queries:**
```sql
SELECT * FROM Tournaments WHERE game = 'Dota 2';
```
📌 **Check that only the relevant partition is accessed!**

---

### **3️⃣ Partition `Players` Table (HASH Partitioning)**
Distribute players **evenly across multiple partitions** to improve lookup performance.

✅ **Modify the `Players` table:**
```sql
CREATE TABLE Players (
    player_id INT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    ranking INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
PARTITION BY HASH(player_id) PARTITIONS 4;
```

✅ **Verify Even Distribution:**
```sql
SELECT partition_name, table_rows 
FROM information_schema.partitions 
WHERE table_name = 'Players';
```

📌 **Do different queries distribute data evenly?**

---

## **📌 Final Task: Compare Query Performance**

✅ Run queries **before and after partitioning** and compare execution time.
✅ Use **`EXPLAIN ANALYZE`** to observe query execution improvements.
✅ Optimize indexes in combination with partitioning.
