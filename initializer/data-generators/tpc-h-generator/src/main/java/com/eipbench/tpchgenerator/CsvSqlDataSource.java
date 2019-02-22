package com.eipbench.tpchgenerator;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class CsvSqlDataSource implements DataSource {
    private static final String JDBC_RELIQUE_CSV = "jdbc:relique:csv:";

    private String path;

    public CsvSqlDataSource() {
        try {
            Class.forName("org.relique.jdbc.csv.CsvDriver");
        } catch (final ClassNotFoundException e) {
            throw new IllegalArgumentException("Driver " + "org.relique.jdbc.csv.CsvDriver" + " could not be loaded.");
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_RELIQUE_CSV + path);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public void setUrl(final String path) {
        this.path = path;
    }

    public Connection getConnection(final Properties properties) throws SQLException {
        return DriverManager.getConnection(JDBC_RELIQUE_CSV + path, properties);
    }
}
