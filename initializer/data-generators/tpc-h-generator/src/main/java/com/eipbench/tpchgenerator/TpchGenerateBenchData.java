package com.eipbench.tpchgenerator;

import com.eipbench.pdgf.TpchDataGenerator;
import com.eipbench.tpchgenerator.graph.TpchGraphModel;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.DIRECTION;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.TABLE_NAMES;
import com.eipbench.tpchgenerator.graph.TpchGraphModel.TYPE;
import com.eipbench.tpchgenerator.graph.TpchSqlToJsonStreamCompiler;
import pdgf.core.exceptions.NotSupportedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TpchGenerateBenchData {
    private static final String PATH_SEPARATOR = System.getProperty("file.separator");

    private final static List<Integer> scaleLevels = Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384,
            32768, 65536, 131072, 262144, 524288);

    private TpchSqlToJsonStreamCompiler compiler;
    private CsvSqlDataSource csvds;

    public TpchGenerateBenchData() {
        compiler = new TpchSqlToJsonStreamCompiler();
        csvds = new CsvSqlDataSource();
        csvds.setUrl(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath());
    }

    /**
     * @param args
     * @throws SQLException
     * @throws IOException
     */
    public static void main(final String[] args) throws SQLException, IOException {
        final TpchGenerateBenchData gbd = new TpchGenerateBenchData();

        File outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_customer-embedding-150-k.json");
        long startTime = System.currentTimeMillis();

        generateTpchJsonData(gbd, outputFile, DIRECTION.forward, TYPE.embedding, TpchGraphModel.TABLE_NAMES.CUSTOMER,
                TpchGraphModel.TABLE_NAMES.NATION, TpchGraphModel.TABLE_NAMES.REGION);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_nation_region-multiformat-25.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonData(gbd, outputFile, DIRECTION.forward, TYPE.multiformat, TpchGraphModel.TABLE_NAMES.NATION,
                TpchGraphModel.TABLE_NAMES.REGION);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR
                + "tpch_customer_nation_region-multiformat-150k.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonDataMultiFormatForJoin(gbd, outputFile, TpchGraphModel.TABLE_NAMES.CUSTOMER, 1, 8,
                TpchGraphModel.TABLE_NAMES.NATION, TpchGraphModel.TABLE_NAMES.REGION);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR
                + "tpch_customer_nation_region-multiformat-150k-bulk10.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonDataMultiFormatForJoin(gbd, outputFile, TpchGraphModel.TABLE_NAMES.CUSTOMER, 10, 8,
                TpchGraphModel.TABLE_NAMES.NATION, TpchGraphModel.TABLE_NAMES.REGION);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR
                + "tpch_customer_nation_region-multiformat-150k-bulk100.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonDataMultiFormatForJoin(gbd, outputFile, TpchGraphModel.TABLE_NAMES.CUSTOMER, 100, 0,
                TpchGraphModel.TABLE_NAMES.NATION, TpchGraphModel.TABLE_NAMES.REGION);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR
                + "tpch_customer_nation_region-multiformat-150k-bulk1000.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonDataMultiFormatForJoin(gbd, outputFile, TpchGraphModel.TABLE_NAMES.CUSTOMER, 1000, 0,
                TpchGraphModel.TABLE_NAMES.NATION, TpchGraphModel.TABLE_NAMES.REGION);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_customer-150-k.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonData(gbd, outputFile, DIRECTION.forward, TYPE.singleformat, TpchGraphModel.TABLE_NAMES.CUSTOMER);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_supplier-10-k.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonData(gbd, outputFile, DIRECTION.forward, TYPE.singleformat, TpchGraphModel.TABLE_NAMES.SUPPLIER);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_order-1_5-mio.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonData(gbd, outputFile, DIRECTION.forward, TYPE.singleformat, TpchGraphModel.TABLE_NAMES.ORDERS);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_lineitem-6-mio.json");
        startTime = System.currentTimeMillis();
        generateTpchJsonData(gbd, outputFile, DIRECTION.forward, TYPE.singleformat, TpchGraphModel.TABLE_NAMES.LINEITEM);
        System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));

        generateMessageSizeScalingData(gbd);
    }

    private static void generateMessageSizeScalingData(final TpchGenerateBenchData gbd) throws SQLException, IOException {
        File outputFile;
        long startTime;
        for (int i = 0; i < scaleLevels.size(); i++) {
            outputFile = new File(TpchDataGenerator.OUTPUT_DIR.getAbsolutePath() + PATH_SEPARATOR + "tpch_order-customer-sizes-sl-" + i
                    + ".json");
            startTime = System.currentTimeMillis();
            generateTpchJsonDataMsgSize(gbd, outputFile, DIRECTION.forward, TYPE.embedding, scaleLevels, i,
                    TpchGraphModel.TABLE_NAMES.ORDERS, TpchGraphModel.TABLE_NAMES.CUSTOMER);
            System.out.println(outputFile.getAbsolutePath() + " in ms: " + (System.currentTimeMillis() - startTime));
        }
    }

    private static void generateTpchJsonDataMsgSize(final TpchGenerateBenchData gbd, final File outputFile, final DIRECTION direction,
            final TYPE type, final List<Integer> scaleLevels, final int cursor, final TABLE_NAMES... tables) throws SQLException,
            IOException {
        if (type == TYPE.embedding) {
            gbd.getCompiler().compileEmbeddings(new FileOutputStream(outputFile), outputFile.getName(), gbd.getCsvds(), direction,
                    tables[0], tables[1], scaleLevels, cursor);
        } else {
            throw new NotSupportedException("Multi-format message size generation is not supported.");
        }
    }

    private static void generateTpchJsonData(final TpchGenerateBenchData gbd, final File outputFile, final DIRECTION direction,
            final TYPE type, final TABLE_NAMES... tables) throws SQLException, IOException {
        if (type == TYPE.embedding) {
            gbd.getCompiler().compileEmbeddings(new FileOutputStream(outputFile), outputFile.getName(), gbd.getCsvds(), direction, tables);
        } else {
            gbd.getCompiler().compileMultiFormat(new FileOutputStream(outputFile), outputFile.getName(), gbd.getCsvds(), tables);
        }
    }

    private static void generateTpchJsonDataMultiFormatForJoin(final TpchGenerateBenchData gbd, final File outputFile,
            final TABLE_NAMES primaryTable, int bulkSize, int thinning, final TABLE_NAMES... secondaryTables) throws SQLException,
            IOException {
        gbd.getCompiler().compileMultiFormatForJoin(new FileOutputStream(outputFile), primaryTable, gbd.getCsvds(), bulkSize, thinning,
                secondaryTables);
    }

    public TpchSqlToJsonStreamCompiler getCompiler() {
        return compiler;
    }

    public CsvSqlDataSource getCsvds() {
        return csvds;
    }
}
