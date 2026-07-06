package billing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// SRP: database work for special pricing stays here.
public class SpecialPricingRepository {
    private final Connection conn;

    public SpecialPricingRepository(Connection conn) {
        this.conn = conn;
    }

    public List<SpecialPricingRecord> loadAll() throws SQLException {
        List<SpecialPricingRecord> records = new ArrayList<>();

        String sql =
                "SELECT p.pricing_id, r.role_id, r.role_name, p.pricing_name, " +
                "p.pricing_type, p.operator, p.price_adjustment " +
                "FROM rolepricing p " +
                "JOIN roleadjustments a ON p.pricing_id = a.pricing_id " +
                "JOIN role r ON a.role = r.role_id " +
                "ORDER BY p.pricing_id";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                records.add(new SpecialPricingRecord(
                        result.getInt("pricing_id"),
                        result.getInt("role_id"),
                        result.getString("role_name"),
                        result.getString("pricing_name"),
                        result.getString("pricing_type"),
                        result.getString("operator"),
                        result.getBigDecimal("price_adjustment")
                ));
            }
        }

        return records;
    }

    public List<RoleItem> loadRoles() throws SQLException {
        List<RoleItem> roles = new ArrayList<>();
        String sql = "SELECT role_id, role_name FROM role ORDER BY role_id";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                roles.add(new RoleItem(
                        result.getInt("role_id"),
                        result.getString("role_name")
                ));
            }
        }

        return roles;
    }

    public void add(int roleId, String name, String type, String operator, BigDecimal value)
            throws SQLException {
        boolean oldAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            String pricingSql =
                    "INSERT INTO rolepricing(pricing_name, pricing_type, operator, price_adjustment) " +
                    "VALUES (?, ?, ?, ?) RETURNING pricing_id";

            int pricingId;

            try (PreparedStatement statement = conn.prepareStatement(pricingSql)) {
                statement.setString(1, name);
                statement.setString(2, type);
                statement.setString(3, operator);
                statement.setBigDecimal(4, value);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException("Failed to create pricing rule.");
                    }
                    pricingId = result.getInt("pricing_id");
                }
            }

            String roleSql = "INSERT INTO roleadjustments(role, pricing_id) VALUES (?, ?)";

            try (PreparedStatement statement = conn.prepareStatement(roleSql)) {
                statement.setInt(1, roleId);
                statement.setInt(2, pricingId);
                statement.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    public void update(int pricingId, int roleId, String name, String type,
                       String operator, BigDecimal value) throws SQLException {
        boolean oldAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            String pricingSql =
                    "UPDATE rolepricing SET pricing_name = ?, pricing_type = ?, " +
                    "operator = ?, price_adjustment = ? WHERE pricing_id = ?";

            try (PreparedStatement statement = conn.prepareStatement(pricingSql)) {
                statement.setString(1, name);
                statement.setString(2, type);
                statement.setString(3, operator);
                statement.setBigDecimal(4, value);
                statement.setInt(5, pricingId);
                statement.executeUpdate();
            }

            String roleSql = "UPDATE roleadjustments SET role = ? WHERE pricing_id = ?";

            try (PreparedStatement statement = conn.prepareStatement(roleSql)) {
                statement.setInt(1, roleId);
                statement.setInt(2, pricingId);
                statement.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    public void delete(int pricingId) throws SQLException {
        boolean oldAutoCommit = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM roleadjustments WHERE pricing_id = ?")) {
                statement.setInt(1, pricingId);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = conn.prepareStatement(
                    "DELETE FROM rolepricing WHERE pricing_id = ?")) {
                statement.setInt(1, pricingId);
                statement.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    public static class SpecialPricingRecord {
        private final int pricingId;
        private final int roleId;
        private final String roleName;
        private final String name;
        private final String type;
        private final String operator;
        private final BigDecimal value;

        public SpecialPricingRecord(int pricingId, int roleId, String roleName, String name,
                                    String type, String operator, BigDecimal value) {
            this.pricingId = pricingId;
            this.roleId = roleId;
            this.roleName = roleName;
            this.name = name;
            this.type = type;
            this.operator = operator;
            this.value = value;
        }

        public int getPricingId() {
            return pricingId;
        }

        public int getRoleId() {
            return roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getOperator() {
            return operator;
        }

        public BigDecimal getValue() {
            return value;
        }
    }

    public static class RoleItem {
        private final int id;
        private final String name;

        public RoleItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
