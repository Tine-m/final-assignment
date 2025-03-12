# **📝 Performance Test: Optimistic vs. Pessimistic Concurrency Control (OCC vs. PCC)**

## **📌 Objective**
The goal of this test is to measure and compare the performance of **Optimistic Concurrency Control (OCC)** and **Pessimistic Concurrency Control (PCC)** under heavy concurrent load. We will simulate multiple users performing database operations simultaneously and evaluate execution time, transaction failures, and throughput.

---

## **📖 Test Steps**
### **1️⃣ Simulate Concurrent Transactions**
We use **Java's `ExecutorService`** to create multiple threads that execute transactions concurrently.

### **2️⃣ Implement OCC and PCC Approaches**
- **OCC** will rely on versioning and retry logic.
- **PCC** will use `SELECT ... FOR UPDATE` to lock rows during updates.

### **3️⃣ Measure Performance**
- **Execution Time**: How long the transactions take.
- **Transaction Success Rate**: How many succeed vs. fail.
- **Lock Wait Time (for PCC)**: Impact of locks on concurrent execution.

---

## **📌 Database Setup**
### **🔹 Create a Sample Table for Testing**
```sql
CREATE TABLE Players (
    player_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    ranking INT DEFAULT 1000,
    version INT DEFAULT 1
);

INSERT INTO Players (username, ranking) VALUES ('Alice', 1200), ('Bob', 1500), ('Charlie', 1400);
```

---

## **📌 Java Implementation**

### **1️⃣ Implement OCC with Versioning**
```java
import java.sql.*;
import java.util.concurrent.*;

public class OptimisticCC {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/esports";
    private static final String USER = "root";
    private static final String PASS = "password";

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> updateRankingOCC(1, 50));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    public static void updateRankingOCC(int playerId, int points) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            boolean success = false;
            while (!success) {
                conn.setAutoCommit(false);
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT ranking, version FROM Players WHERE player_id = ?");
                stmt.setInt(1, playerId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int ranking = rs.getInt("ranking");
                    int version = rs.getInt("version");
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE Players SET ranking = ?, version = version + 1 WHERE player_id = ? AND version = ?");
                    updateStmt.setInt(1, ranking + points);
                    updateStmt.setInt(2, playerId);
                    updateStmt.setInt(3, version);
                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        conn.commit();
                        success = true;
                    } else {
                        conn.rollback();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

### **2️⃣ Implement PCC with Row Locking**
```java
public class PessimisticCC {
    public static void updateRankingPCC(int playerId, int points) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setAutoCommit(false);
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT ranking FROM Players WHERE player_id = ? FOR UPDATE");
            stmt.setInt(1, playerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int ranking = rs.getInt("ranking");
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE Players SET ranking = ? WHERE player_id = ?");
                updateStmt.setInt(1, ranking + points);
                updateStmt.setInt(2, playerId);
                updateStmt.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

## **📌 Performance Comparison Table**
| **Metric**               | **Optimistic CC (OCC)** | **Pessimistic CC (PCC)** |
|--------------------------|------------------|------------------|
| **Execution Time**       | Fast (Retry on Conflict) | Slower (Locks cause waits) |
| **Transaction Success Rate** | Lower due to retries | Higher due to locking |
| **Lock Contention**      | None | High |
| **Best Use Case**       | Read-heavy (Registrations) | Write-heavy (Updates) |

---

## **📌 Final Discussion**
- **OCC is better for read-heavy workloads** since it avoids locks but may require retries.
- **PCC is better for write-heavy transactions** where consistency is more important.
- **Testing concurrency under different loads** helps determine the best strategy.

📌 **Would you like to include a JUnit performance test framework? 🚀**

