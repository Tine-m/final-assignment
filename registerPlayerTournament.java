public void registerPlayerForTournament(int playerId, int tournamentId) {
    String checkTournament = "SELECT max_players, version FROM Tournaments WHERE tournament_id = ?";
    String updateVersion = "UPDATE Tournaments SET version = version + 1 WHERE tournament_id = ? AND version = ?";
    String insertRegistration = "INSERT INTO Tournament_Registrations (tournament_id, player_id, registered_at) VALUES (?, ?, NOW())";

    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
        conn.setAutoCommit(false); // Start transaction
        boolean success = false;

        while (!success) {
            int maxPlayers, currentVersion, registeredPlayers;
            
            // Step 1: Get tournament details
            try (PreparedStatement stmt1 = conn.prepareStatement(checkTournament)) {
                stmt1.setInt(1, tournamentId);
                ResultSet rs = stmt1.executeQuery();
                if (rs.next()) {
                    maxPlayers = rs.getInt("max_players");
                    currentVersion = rs.getInt("version");
                } else {
                    throw new SQLException("Tournament not found.");
                }
            }

            // Step 2: Count current registrations
            String countQuery = "SELECT COUNT(*) FROM Tournament_Registrations WHERE tournament_id = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(countQuery)) {
                stmt2.setInt(1, tournamentId);
                ResultSet rs = stmt2.executeQuery();
                rs.next();
                registeredPlayers = rs.getInt(1);
            }

            // Step 3: Ensure tournament isn't full
            if (registeredPlayers >= maxPlayers) {
                throw new SQLException("Tournament is full.");
            }

            // Step 4: Try to update the version (OCC check)
            try (PreparedStatement stmt3 = conn.prepareStatement(updateVersion)) {
                stmt3.setInt(1, tournamentId);
                stmt3.setInt(2, currentVersion);
                int rowsUpdated = stmt3.executeUpdate();
                if (rowsUpdated == 0) {
                    // Version mismatch → Retry the transaction
                    continue;
                }
            }

            // Step 5: Insert the registration
            try (PreparedStatement stmt4 = conn.prepareStatement(insertRegistration)) {
                stmt4.setInt(1, tournamentId);
                stmt4.setInt(2, playerId);
                stmt4.executeUpdate();
            }

            conn.commit(); // Commit transaction
            success = true; // Mark success
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
