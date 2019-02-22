package com.eipbench.tpchgenerator.graph;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.eipbench.tpchgenerator.CsvSqlDataSource;
import com.eipbench.tpchgenerator.TpchMetaTable;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.DIRECTION;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.EDGE_TYPES;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.TABLE_NAMES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

public class TpchSqlToJsonStreamCompiler {
    private static final transient Logger LOG = LoggerFactory.getLogger(TpchSqlToGraphCompiler.class);

    private TpchGraphModel tpcGraphModel = new TpchGraphModel();

    private final JsonFactory jsonFactory = new JsonFactory();

    private static final int MAGIC_SCALE_FACTOR = 3;

    /**
     * 
     * @param os
     * @throws SQLException
     * @throws IOException
     */
    public void compileEmbeddings(final OutputStream os) throws SQLException, IOException {
        final CsvSqlDataSource csvds = new CsvSqlDataSource();
        csvds.setUrl(this.getTempDirectoryUrl().getPath());
        this.compileEmbeddings(os, "stream" + "-" + UUID.randomUUID(), csvds, DIRECTION.forward, TABLE_NAMES.values());
    }

    /**
     * 
     * @param os
     * @param tableNames
     * @throws SQLException
     * @throws IOException
     */
    public void compileEmbeddings(final OutputStream os, final TABLE_NAMES... tableNames) throws SQLException, IOException {
        final CsvSqlDataSource csvds = new CsvSqlDataSource();
        csvds.setUrl(this.getTempDirectoryUrl().getPath());
        this.compileEmbeddings(os, "stream" + "-" + UUID.randomUUID(), csvds, DIRECTION.forward, tableNames);
    }

    /**
     * 
     * @param os
     * @param rootName
     * @param csvds
     * @param direction
     * @param tableNames
     * @throws SQLException
     */
    public void compileEmbeddings(final OutputStream os, final String rootName, final CsvSqlDataSource csvds, final DIRECTION direction,
            final TABLE_NAMES... tableNames) throws SQLException {
        try {
            try (JsonGenerator generator = jsonFactory.createGenerator(os)) {
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
                generator.writeStartArray();

                TABLE_NAMES startTable = null;
                if (direction == DIRECTION.backward) {
                    startTable = tableNames[tableNames.length - 1];
                } else {
                    startTable = tableNames[0];
                }
                try (ResultSet resultSet = readTable(csvds, startTable)) {
                    toJson(csvds, startTable, generator, resultSet, direction, Arrays.asList(tableNames));
                    resultSet.close();
                }
                generator.writeEndArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param os
     * @param name
     * @param csvds
     * @param direction
     * @param startTable
     * @param cursor
     */
    public void compileEmbeddings(final FileOutputStream os, final String name, final CsvSqlDataSource csvds, final DIRECTION direction,
            final TABLE_NAMES startTable, final TABLE_NAMES secondaryTable, final List<Integer> scaleLevels, final int cursor)
            throws SQLException {
        try {
            try (final JsonGenerator generator = jsonFactory.createGenerator(os)) {
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
                generator.writeStartArray();

                try (final ResultSet resultSet = readFirstNEntries(csvds, startTable, 1)) {
                    while (resultSet.next()) {
                        final ResultSet embeddedResultSet = readFirstNEntries(csvds, secondaryTable, scaleLevels.get(cursor)
                                * MAGIC_SCALE_FACTOR);
                        generator.writeStartObject();
                        convertResultSetToJsonFields(startTable, secondaryTable, generator, resultSet, embeddedResultSet,
                                scaleLevels.get(cursor));
                        embeddedResultSet.close();
                        generator.writeEndObject();
                        generator.flush();
                    }

                    // toJson(csvds, startTable, generator, resultSet,
                    // direction, Arrays.asList(tableNames));
                    resultSet.close();
                }
                generator.writeEndArray();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param os
     * @throws SQLException
     * @throws IOException
     */
    public void compileMultiFormat(final OutputStream os) throws SQLException, IOException {
        final CsvSqlDataSource csvds = new CsvSqlDataSource();
        csvds.setUrl(this.getTempDirectoryUrl().getPath());
        this.compileMultiFormat(os, "stream" + "-" + UUID.randomUUID(), csvds, TABLE_NAMES.values());
    }

    /**
     * 
     * @param os
     * @param tableNames
     * @throws SQLException
     * @throws IOException
     */
    public void compileMultiFormat(final OutputStream os, final TABLE_NAMES... tableNames) throws SQLException, IOException {
        final CsvSqlDataSource csvds = new CsvSqlDataSource();
        csvds.setUrl(this.getTempDirectoryUrl().getPath());
        this.compileMultiFormat(os, "stream" + "-" + UUID.randomUUID(), csvds, tableNames);
    }

    /**
     * 
     * @param os
     * @param graphName
     * @param csvds
     * @param tableNames
     * @throws SQLException
     * @throws IOException
     */
    public void compileMultiFormat(final OutputStream os, final String graphName, final CsvSqlDataSource csvds,
            final TABLE_NAMES... tableNames) throws SQLException, IOException {
        final JsonGenerator jsonGenerator = jsonFactory.createGenerator(os);
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

        jsonGenerator.writeStartArray();

        final List<String> tableNamesAsString = new ArrayList<String>();

        for (final TABLE_NAMES tableName : tableNames) {
            LOG.debug("Starting to compile table " + tableName);

            Connection connRegion = null;
            try {
                final String tableTag = tableName.name();
                tableNamesAsString.add(tableTag);
                final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(tableTag);
                connRegion = csvds.getConnection(tpcGraphModel.getTableProperties(tableMetaData));

                // FIXME: not thread safe.
                LOG.debug("\tStarted query data...");
                final long queryStart = System.currentTimeMillis();
                final Statement stmt = connRegion.createStatement();
                final ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableTag);
                LOG.debug("\tQueried data (ms)" + (System.currentTimeMillis() - queryStart));

                LOG.debug("\tStarted to add nodes...");
                final long nodeCreateStart = System.currentTimeMillis();
                this.createNodes(tableTag, tableMetaData, jsonGenerator, rs);
                LOG.debug("\tCreated nodes (ms) " + (System.currentTimeMillis() - nodeCreateStart));
            } finally {
                if (null != connRegion) {
                    connRegion.close();
                }
            }
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.close();
    }

    /**
     * 
     * @param os
     * @param nonDuplicateTable
     * @param csvds
     * @param bulkSize
     * @param thinning
     * @param smallDuplicateTables
     * @throws SQLException
     * @throws IOException
     */
    public void compileMultiFormatForJoin(final OutputStream os, final TABLE_NAMES nonDuplicateTable, final CsvSqlDataSource csvds,
            final int bulkSize, final int thinning, final TABLE_NAMES... smallDuplicateTables) throws SQLException, IOException {
        final JsonGenerator jsonGenerator = jsonFactory.createGenerator(os);
        jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());

        final String nonDuplicateTableName = nonDuplicateTable.name();
        final TpchMetaTable nonDuplicateTableMetaData = tpcGraphModel.getTableMetaData().get(nonDuplicateTableName);
        final Connection initialConnection = csvds.getConnection(tpcGraphModel.getTableProperties(nonDuplicateTableMetaData));
        LOG.debug("\tStarted query data...");
        final long query = System.currentTimeMillis();
        final Statement stmt = initialConnection.createStatement();
        final ResultSet primaryResultSet = stmt.executeQuery("SELECT * FROM " + nonDuplicateTableName);
        LOG.debug("\tQueried data (ms)" + (System.currentTimeMillis() - query));

        jsonGenerator.writeStartArray();

        while (primaryResultSet.next()) {
            takeOnlyEveryFourthRecord(primaryResultSet, thinning);

            jsonGenerator.writeStartArray();

            this.createBulkNode(nonDuplicateTable, nonDuplicateTableMetaData, jsonGenerator, primaryResultSet, bulkSize);

            final List<String> smallDuplicateTableListToGo = new ArrayList<String>();

            for (final TABLE_NAMES smallDuplicateTable : smallDuplicateTables) {
                LOG.debug("Starting to compile table " + smallDuplicateTable);

                Connection connection = null;
                try {
                    final String tableTag = smallDuplicateTable.name();
                    smallDuplicateTableListToGo.add(tableTag);
                    final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(tableTag);
                    connection = csvds.getConnection(tpcGraphModel.getTableProperties(tableMetaData));

                    // FIXME: not thread safe.
                    LOG.debug("\tStarted query data...");
                    final long queryStart = System.currentTimeMillis();
                    final Statement statement = connection.createStatement();
                    final ResultSet rs = statement.executeQuery("SELECT * FROM " + tableTag);
                    LOG.debug("\tQueried data (ms)" + (System.currentTimeMillis() - queryStart));

                    LOG.debug("\tStarted to add nodes...");
                    final long nodeCreateStart = System.currentTimeMillis();
                    this.createNodes(tableTag, tableMetaData, jsonGenerator, rs);
                    LOG.debug("\tCreated nodes (ms) " + (System.currentTimeMillis() - nodeCreateStart));
                } finally {
                    if (null != connection) {
                        connection.close();
                    }
                }
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.close();
    }

    private ResultSet readTable(CsvSqlDataSource csvds, TABLE_NAMES tableName) throws SQLException {
        final String tableTag = tableName.name();
        final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(tableTag);
        final Connection connection = csvds.getConnection(tpcGraphModel.getTableProperties(tableMetaData));

        final Statement stmt = connection.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableTag);
        return rs;
    }

    private ResultSet readFirstNEntries(CsvSqlDataSource csvds, TABLE_NAMES tableName, final int limit) throws SQLException {
        final String tableTag = tableName.name();
        final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(tableTag);
        final Connection connection = csvds.getConnection(tpcGraphModel.getTableProperties(tableMetaData));

        final Statement stmt = connection.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableTag + " LIMIT " + limit);
        return rs;
    }

    private void toJson(final CsvSqlDataSource csvds, TABLE_NAMES startTable, JsonGenerator generator, ResultSet resultSet,
            final DIRECTION direction, List<TABLE_NAMES> table_names) throws SQLException, IOException {
        while (resultSet.next()) {
            generator.writeStartObject();

            convertResultSetToJsonFields(startTable, generator, resultSet);

            for (EDGE_TYPES edge_types : EDGE_TYPES.edgesForDirection(startTable, direction)) {
                final TABLE_NAMES from = edge_types.from(direction);

                if (table_names.contains(from)) {
                    try (final ResultSet rs = join(csvds, resultSet, edge_types, direction)) {
                        if (rs.getType() == ResultSet.TYPE_FORWARD_ONLY) {
                            generator.writeArrayFieldStart(from.name());
                            toJson(csvds, from, generator, rs, direction, table_names);
                            generator.writeEndArray();
                        }
                    }
                }
            }
            generator.writeEndObject();
            generator.flush();
        }
    }

    private ResultSet join(CsvSqlDataSource csvds, ResultSet resultSet, EDGE_TYPES edge, DIRECTION direction) throws SQLException {
        TABLE_NAMES joinTable = null;

        joinTable = edge.from(direction);

        final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(joinTable.name());
        final Connection connection = csvds.getConnection(tpcGraphModel.getTableProperties(tableMetaData));

        final Statement stmt = connection.createStatement();
        final String sql = "SELECT * FROM " + joinTable.name() + " WHERE " + edge.getFromJoinColumn(direction) + "="
                + resultSet.getString(edge.getToJoinColumn(direction));
        // LOG.debug("Query: " + sql);
        final ResultSet rs = stmt.executeQuery(sql);

        return rs;
    }

    private void convertResultSetToJsonFields(final TABLE_NAMES startTable, final TABLE_NAMES secondaryTable,
            final JsonGenerator generator, final ResultSet resultSet, final ResultSet embeddedResultSet, final int msgScaleLevel)
            throws IOException, SQLException {
        final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(startTable.name());

        //FIXME: added name and type
        generator.writeStringField(TpchGraphModel.FIELD_NAME, startTable.name() + "-" + UUID.randomUUID());
        generator.writeStringField(TpchGraphModel.FIELD_TYPE, startTable.name());
        
        for (Entry<String, String> next : tableMetaData.getFields().entrySet()) {
            // TODO: determine foreign key dynamically
            if (next.getKey().equals("O_CUSTKEY") && msgScaleLevel > 1) {
                generator.writeArrayFieldStart("CUSTOMERS");
                while (embeddedResultSet.next()) {
                    generator.writeStartObject();
                    convertResultSetToJsonFields(secondaryTable, generator, embeddedResultSet);
                    generator.writeEndObject();
                    generator.flush();
                }
                generator.writeEndArray();
                generator.flush();
            } else {
                attributeToJsonFieldWithTypeConversion(generator, resultSet, next);
            }
        }
    }

    private void convertResultSetToJsonFields(TABLE_NAMES tableName, JsonGenerator generator, ResultSet resultSet) throws IOException,
            SQLException {
        final TpchMetaTable tableMetaData = tpcGraphModel.getTableMetaData().get(tableName.name());

        for (Entry<String, String> next : tableMetaData.getFields().entrySet()) {
            attributeToJsonFieldWithTypeConversion(generator, resultSet, next);
        }
    }

    private void attributeToJsonFieldWithTypeConversion(JsonGenerator generator, ResultSet resultSet, Entry<String, String> next)
            throws IOException, SQLException {
        if (next.getValue().equals("java.sql.Types.INTEGER")) {
            generator.writeNumberField(next.getKey(), resultSet.getInt(next.getKey()));
        } else if (next.getValue().equals("java.sql.Types.VARCHAR")) {
            generator.writeStringField(next.getKey(), resultSet.getString(next.getKey()));
        } else if (next.getValue().equals("java.sql.Types.DECIMAL")) {
            generator.writeNumberField(next.getKey(), resultSet.getDouble(next.getKey()));
        } else if (next.getValue().equals("java.sql.Types.DATE")) {
            // FIXME: date not string
            generator.writeStringField(next.getKey(), resultSet.getDate(next.getKey()).toString());
        } else {
            throw new IllegalArgumentException("Missing handled type: " + next.getValue());
        }
    }

    private void takeOnlyEveryFourthRecord(final ResultSet primaryResultSet, int thinning) throws SQLException {
        for (int i = 1; i < thinning; i++) {
            primaryResultSet.next();
        }
    }

    private URL getTempDirectoryUrl() {
        try {
            return new File(System.getProperty("java.io.tmpdir") + "tpchdata").toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not read tmp directory.", e);
        }
    }

    private void createNodes(final String tableName, final TpchMetaTable tableMetaData, final JsonGenerator jsonGenerator,
            final ResultSet rs) throws SQLException, IOException {
        while (rs.next()) {
            createJsonObject(tableName, tableMetaData, jsonGenerator, rs);
        }
    }

    private void createBulkNode(final TABLE_NAMES tableName, final TpchMetaTable tableMetaData, final JsonGenerator jsonGenerator,
            final ResultSet rs, int bulkSize) throws IOException, SQLException {
        for (int i = 0; i < bulkSize; i++) {
            createJsonObject(tableName.name(), tableMetaData, jsonGenerator, rs);
            if (!rs.next()) {
                System.out.println("iterator out of items.");
                break;
            }
        }
    }

    private void createJsonObject(final String tableName, final TpchMetaTable tableMetaData, final JsonGenerator jsonGenerator,
            final ResultSet rs) throws IOException, SQLException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField(TpchGraphModel.FIELD_NAME, tableName + "-" + UUID.randomUUID());
        jsonGenerator.writeStringField(TpchGraphModel.FIELD_TYPE, tableName);

        final Iterator<Entry<String, String>> iterator = tableMetaData.getFields().entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, String> next = iterator.next();

            attributeToJsonFieldWithTypeConversion(jsonGenerator, rs, next);
        }
        jsonGenerator.writeEndObject();
    }
}
