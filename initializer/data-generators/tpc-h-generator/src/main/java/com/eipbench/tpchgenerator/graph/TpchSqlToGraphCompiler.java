package com.eipbench.tpchgenerator.graph;

import com.eipbench.tpchgenerator.CsvSqlDataSource;
import com.eipbench.tpchgenerator.TpchMetaTable;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.EDGE_TYPES;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.TABLE_NAMES;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;


public class TpchSqlToGraphCompiler {
    private static final transient Logger LOG = LoggerFactory.getLogger(TpchSqlToGraphCompiler.class);

    private TpchGraphModel tpcGraphModel = new TpchGraphModel();

    public Graph compile() throws SQLException {
        final CsvSqlDataSource csvds = new CsvSqlDataSource();
        csvds.setUrl(this.getTempDirectoryUrl().getPath());
        return this.compile("graph" + "-" + UUID.randomUUID(), csvds, TABLE_NAMES.values());
    }

    public Graph compile(final String graphName, final CsvSqlDataSource csvds, final TABLE_NAMES... tableNames) throws SQLException {
        final Graph graph = this.create(graphName);
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
                this.createNodes(tableTag, tableMetaData, graph, rs);
                LOG.debug("\tCreated nodes (ms) " + (System.currentTimeMillis() - nodeCreateStart));

                // CsvDriver.writeToCsv(rsRegion, System.out, true);
            } finally {
                if (null != connRegion) {
                    connRegion.close();
                }
            }
        }
        LOG.debug("\tStarted to add references...");
        final long referenceCreateStart = System.currentTimeMillis();
        this.addNodeReferences(graph, tableNamesAsString);
        LOG.debug("\tCreated references (ms)" + (System.currentTimeMillis() - referenceCreateStart));

        return graph;
    }

    private Graph create(final String graphName) {
        final Graph graph = new MultiGraph(graphName);
        graph.addAttribute("ui.stylesheet", new Object[]{"graph { fill-color: white; } node { shape: box; fill-color: black; } node.stateful { shape: box; size: 13px, 13px; fill-color: green; stroke-mode: plain; }edge { shape: line; fill-color: black; }"});
        return graph;
    }

    private void addNodeReferences(final Graph graph, final List<String> tableNames) {
        final List<EDGE_TYPES> rets = this.determineRequiredEdgeTypes(tableNames);

        for (final EDGE_TYPES edgeType : rets) {
            // FIXME: only one key supported; no n:m.
            final List<String> joinKeys = tpcGraphModel.getJoinKeyMap().get(edgeType.name());

            for (Node source : graph.getNodeSet()) {
                for (Node target : graph.getNodeSet()) {
                    final Object sourceValue = source.getAttribute(joinKeys.get(0));
                    final Object targetValue = target.getAttribute(joinKeys.get(1));

                    if (sourceValue != null && targetValue != null) {
                        if (sourceValue.equals(targetValue)) {
                            registerEdge(graph, source, target, edgeType.name());
                        }
                    }
                }
            }
        }
    }

    private void registerEdge(final Graph graph, final Node source, final Node target, final String edgeType) {
        final Edge edge = graph.addEdge(source.getId() + target.getId(), source.getId(), target.getId(), true);
        edge.addAttribute("ui.label", new Object[]{edge.getId()});
        edge.addAttribute("type", new Object[]{edgeType});
    }

    private List<EDGE_TYPES> determineRequiredEdgeTypes(final List<String> tableNames) {
        final List<EDGE_TYPES> requiredEdgeTypes = new ArrayList<EDGE_TYPES>();
        for (final EDGE_TYPES edgeType : EDGE_TYPES.values()) {
            final String[] sourceAndTargetTable = edgeType.name().split("_TO_");
            if (tableNames.contains(sourceAndTargetTable[0]) && tableNames.contains(sourceAndTargetTable[1])) {
                requiredEdgeTypes.add(edgeType);
            }
        }
        return requiredEdgeTypes;
    }

    public URL getTempDirectoryUrl() {
        try {
            return new File(System.getProperty("java.io.tmpdir") + "tpchdata").toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Could not read tmp directory.", e);
        }
    }

    private void createNodes(final String tableName, final TpchMetaTable tableMetaData, final Graph graph, final ResultSet rs)
            throws SQLException {
        while (rs.next()) {
            final Node node = registerNode(graph, tableName + "-" + UUID.randomUUID(), tableName);

            final Iterator<Entry<String, String>> iterator = tableMetaData.getFields().entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<String, String> next = iterator.next();

                if (next.getValue().equals("java.sql.Types.INTEGER")) {
                    node.addAttribute(next.getKey(), rs.getInt(next.getKey()));
                } else if (next.getValue().equals("java.sql.Types.VARCHAR")) {
                    node.addAttribute(next.getKey(), rs.getString(next.getKey()));
                } else if (next.getValue().equals("java.sql.Types.DECIMAL")) {
                    node.addAttribute(next.getKey(), rs.getDouble(next.getKey()));
                } else if (next.getValue().equals("java.sql.Types.DATE")) {
                    node.addAttribute(next.getKey(), rs.getDate(next.getKey()));
                } else {
                    throw new IllegalArgumentException("Missing handled type: " + next.getValue());
                }
            }
        }
    }

    private Node registerNode(final Graph graph, final String name, final String nodeType) {
        final Node node = graph.addNode(name);
        node.addAttribute("ui.label", new Object[]{node.getId()});
        node.addAttribute("type", new Object[]{nodeType});

        return node;
    }
}
