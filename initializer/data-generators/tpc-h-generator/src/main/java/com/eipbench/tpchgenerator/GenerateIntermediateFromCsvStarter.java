package com.eipbench.tpchgenerator;

import com.eipbench.tpchgenerator.graph.TpchGraphModel.TABLE_NAMES;
import com.eipbench.tpchgenerator.graph.TpchSqlToJsonStreamCompiler;

import java.io.IOException;
import java.sql.SQLException;

public class GenerateIntermediateFromCsvStarter {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        try {
            final TpchSqlToJsonStreamCompiler tpchc = new TpchSqlToJsonStreamCompiler();
            tpchc.compileMultiFormat(System.out, TABLE_NAMES.ORDERS, TABLE_NAMES.NATION, TABLE_NAMES.REGION);

            // GraphPrinter.create()
            // .printGraphAsGml(graph, new FileOutputStream(new
            // File(tpchc.getTempDirectoryUrl().getPath() +
            // "tpch_orders_customer.gml")));
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
