package billing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Reads billing data from PostgreSQL
public class BillingRepository {
    private final Connection conn;

    public BillingRepository(Connection conn) {
        this.conn = conn;
    }

    public RentalBillingData findCurrentRental(int userId) throws SQLException {
        String sql =
                "SELECT r.rental_id, r.time_start, r.time_end, " +
                "e.equipment_name, e.daily_rental, " +
                "c.category_id, c.category_name, c.max_rent_duration, " +
                "ro.role_name " +
                "FROM sysuser u " +
                "JOIN rental r ON u.currrental = r.rental_id " +
                "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "JOIN category c ON e.category_id = c.category_id " +
                "JOIN role ro ON u.user_role = ro.role_id " +
                "WHERE u.user_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                Date endDate = result.getDate("time_end");

                return new RentalBillingData(
                        result.getInt("rental_id"),
                        result.getInt("category_id"),
                        result.getString("equipment_name"),
                        result.getString("category_name"),
                        result.getString("role_name"),
                        result.getBigDecimal("daily_rental"),
                        result.getInt("max_rent_duration"),
                        result.getDate("time_start").toLocalDate(),
                        endDate == null ? null : endDate.toLocalDate()
                );
            }
        }
    }

    public List<BillAdjustment> loadRoleAdjustments(String role) throws SQLException {
        List<BillAdjustment> adjustments = new ArrayList<>();

        String sql =
                "SELECT p.pricing_name, p.operator, p.price_adjustment " +
                "FROM rolepricing p " +
                "JOIN roleadjustments a ON p.pricing_id = a.pricing_id " +
                "JOIN role r ON a.role = r.role_id " +
                "WHERE r.role_name = ? " +
                "ORDER BY p.pricing_id";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, role);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    adjustments.add(mapDiscount(
                            result.getString("pricing_name"),
                            result.getString("operator"),
                            result.getBigDecimal("price_adjustment")
                    ));
                }
            }
        }

        return adjustments;
    }

    public BillAdjustment loadLatePenalty(int categoryId) throws SQLException {
        String sql =
                "SELECT p.penalty_name, p.operator, p.price_adjustment " +
                "FROM penaltypricing p " +
                "JOIN categoryadjustments a ON p.penalty_id = a.penalty_id " +
                "WHERE a.category_id = ? AND LOWER(p.penalty_name) = 'late'";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return mapPenalty(
                        result.getString("penalty_name"),
                        result.getString("operator"),
                        result.getBigDecimal("price_adjustment")
                );
            }
        }
    }

    public BillAdjustment loadPenalty(int categoryId, int penaltyId) throws SQLException {
        String sql =
                "SELECT p.penalty_name, p.operator, p.price_adjustment " +
                "FROM penaltypricing p " +
                "JOIN categoryadjustments a ON p.penalty_id = a.penalty_id " +
                "WHERE a.category_id = ? AND p.penalty_id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.setInt(2, penaltyId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return mapPenalty(
                        result.getString("penalty_name"),
                        result.getString("operator"),
                        result.getBigDecimal("price_adjustment")
                );
            }
        }
    }

    public List<PenaltyOption> loadConditionPenalties(int categoryId) throws SQLException {
        List<PenaltyOption> penalties = new ArrayList<>();

        String sql =
                "SELECT p.penalty_id, p.penalty_name " +
                "FROM penaltypricing p " +
                "JOIN categoryadjustments a ON p.penalty_id = a.penalty_id " +
                "WHERE a.category_id = ? AND LOWER(p.penalty_name) <> 'late' " +
                "ORDER BY p.penalty_name";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    penalties.add(new PenaltyOption(
                            result.getInt("penalty_id"),
                            result.getString("penalty_name")
                    ));
                }
            }
        }

        return penalties;
    }

    public List<PricingDisplayItem> loadPricingForRole(String role) throws SQLException {
        List<PricingDisplayItem> items = new ArrayList<>();

        String sql =
                "SELECT p.pricing_name, p.pricing_type, p.operator, p.price_adjustment " +
                "FROM rolepricing p " +
                "JOIN roleadjustments a ON p.pricing_id = a.pricing_id " +
                "JOIN role r ON a.role = r.role_id " +
                "WHERE r.role_name = ? " +
                "ORDER BY p.pricing_id";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, role);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new PricingDisplayItem(
                            result.getString("pricing_name"),
                            result.getString("pricing_type"),
                            result.getString("operator"),
                            result.getBigDecimal("price_adjustment")
                    ));
                }
            }
        }

        return items;
    }

    private BillAdjustment mapDiscount(String name, String operator, BigDecimal value)
            throws SQLException {
        if ("MUL".equals(operator)) {
            return new MultiplierDiscount(name, value);
        }

        if ("SUB".equals(operator)) {
            return new FixedDiscount(name, value);
        }

        throw new SQLException("Unsupported pricing operator: " + operator);
    }

    private BillAdjustment mapPenalty(String name, String operator, BigDecimal value)
            throws SQLException {
        if ("ADD".equals(operator)) {
            return new FixedPenalty(name, value);
        }

        if ("MUL".equals(operator)) {
            return new PercentagePenalty(name, value);
        }

        throw new SQLException("Unsupported penalty operator: " + operator);
    }

    public static class PenaltyOption {
        private final int id;
        private final String name;

        public PenaltyOption(int id, String name) {
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

    public static class PricingDisplayItem {
        private final String name;
        private final String type;
        private final String operator;
        private final BigDecimal value;

        public PricingDisplayItem(String name, String type, String operator, BigDecimal value) {
            this.name = name;
            this.type = type;
            this.operator = operator;
            this.value = value;
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
}
