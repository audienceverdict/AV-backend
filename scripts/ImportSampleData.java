import java.nio.file.*;
import java.sql.*;

/** Import generated single-line SQL statements using environment-provided JDBC credentials. */
public class ImportSampleData {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("SEED_DB_URL"),
                user = System.getenv("SEED_DB_USER"),
                password = System.getenv("SEED_DB_PASSWORD");
        if (url == null || user == null || password == null)
            throw new IllegalArgumentException("Set SEED_DB_URL, SEED_DB_USER, SEED_DB_PASSWORD");
        try (Connection c = DriverManager.getConnection(url, user, password)) {
            c.setAutoCommit(false);
            try (Statement s = c.createStatement()) {
                for (String line :
                        Files.readAllLines(
                                Path.of(args.length == 0 ? "sample-data/seed.sql" : args[0]))) {
                    if (line.isBlank()
                            || line.startsWith("--")
                            || line.equals("START TRANSACTION;")
                            || line.equals("COMMIT;")) continue;
                    boolean result = s.execute(line);
                    if (result)
                        try (ResultSet r = s.getResultSet()) {
                            while (r.next()) {
                                var md = r.getMetaData();
                                for (int i = 1; i <= md.getColumnCount(); i++)
                                    System.out.println(md.getColumnLabel(i) + ": " + r.getLong(i));
                            }
                        }
                }
                c.commit();
                System.out.println("Sample data committed.");
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }
}
