package com.eipbench.camel;

import com.eipbench.content.BenchmarkContent;
import com.eipbench.content.Constants.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.ExpressionDefinition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class DatalogCamelContent implements BenchmarkContent {
    private static final String GREAT_BRITAIN = "22";
    private static final String DEFAULT_ROOT = "root(id,objecttype,orderid,customerid,OORDERSTATUS,OTOTALPRICE,OORDERDATE,priority,OCLERK,OSHIPPRIORITY,OCOMMENT)";
    private static final String DEFAULT_SUB_ROOT = "sub-root(ccustkey,cname,CADDRESS,cnationkey,cphone,cacctbal,cmksegment,ccomment)";
    private static final String RULE_EIP_CBR_A = "cbr(id,OTOTALPRICE):-" + DEFAULT_ROOT + ", " + "<(OTOTALPRICE,100000.00)" + ".";
    private static final String RULE_EIP_CBR_B = "cbr(id):-" + DEFAULT_ROOT + ", " + "<(OTOTALPRICE,100000.00)"
            + ", =(orderpriority,\"3-MEDIUM\"), " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"P\")" + ".";
    private static final String RULE_EIP_CBR_C_b1 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"O\")" + ".";
    private static final String RULE_EIP_CBR_C_b2 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"P\")" + ".";
    private static final String RULE_EIP_CBR_C_b3 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + ">=(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"O\")" + ".";
    private static final String RULE_EIP_CBR_C_b4 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + ">=(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"P\")" + ".";
    private static final String RULE_EIP_CBR_C_b5 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"2-HIGH\")" + ", " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"O\")" + ".";
    private static final String RULE_EIP_CBR_D = "cbr(cid):-"
            + "root-10(cid,ctype,ccustkey,cname,caddress,cnationkey,cphone,cacctbal,cmktsegment,ccomment), " + "<(cacctbal,100.00)" + ","
            + "root-6(nid,ntype,cnationkey,nname,nregionkey,ncomment), " + "=(nnationkey," + GREAT_BRITAIN + ").";
    
    // TODO adapt datalog rules
    private static final String RULE_EIP_CBR_SCALE_A = "cbr(id,OTOTALPRICE):-" + DEFAULT_ROOT + ", " + "<(OTOTALPRICE,100000.00)" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    private static final String RULE_EIP_CBR_SCALE_B = "cbr(id):-" + DEFAULT_ROOT + ", " + "<(OTOTALPRICE,100000.00)"
            + ", =(orderpriority,\"3-MEDIUM\"), " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"P\")" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    private static final String RULE_EIP_CBR_SCALE_C_b1 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"O\")" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    private static final String RULE_EIP_CBR_SCALE_C_b2 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"P\")" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    private static final String RULE_EIP_CBR_SCALE_C_b3 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + ">=(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"O\")" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    private static final String RULE_EIP_CBR_SCALE_C_b4 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"1-URGENT\")" + ", " + ">=(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"P\")" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    private static final String RULE_EIP_CBR_SCALE_C_b5 = "cbr(id):-" + DEFAULT_ROOT + ", " + ">=(OTOTALPRICE,100000.00)" + ", "
            + "=(priority,\"2-HIGH\")" + ", " + "<(OORDERDATE,01.01.1970)" + ", " + "=(ORDERSTATUS,\"O\")" + ", " + DEFAULT_SUB_ROOT + ", " + "=(CADDRESS, \"does-not-match\")" + ".";
    
    private static final String RULE_EIP_RL_A = buildProjectionRule("priority");
    private static final String RULE_EIP_RS_A = buildProjectionRule("OTOTALPRICE");
    private static final String RULE_EIP_ID_A = buildProjectionRule("id");
    private static final String RULE_EIP_SP_A = "sp(id):-" + DEFAULT_ROOT + "." + "sp(OTOTALPRICE):-" + DEFAULT_ROOT + "."
            + "sp(OORDERDATE):-" + DEFAULT_ROOT + ".";
    private static final String RULE_EIP_SP_B = buildProjectionRule("id") + buildProjectionRule("objecttype")
            + buildProjectionRule("orderid") + buildProjectionRule("customerid") + buildProjectionRule("OORDERSTATUS")
            + buildProjectionRule("OTOTALPRICE") + buildProjectionRule("OORDERDATE") + buildProjectionRule("priority")
            + buildProjectionRule("OCLERK") + buildProjectionRule("OSHIPPRIORITY") + buildProjectionRule("OCOMMENT");
    private static final String RULE_EIP_SP_C = buildProjectionRule("id,objecttype,orderid,customerid,OORDERSTATUS,OCOMMENT")
            + buildProjectionRule("id,objecttype,orderid,customerid,OTOTALPRICE,OCOMMENT")
            + buildProjectionRule("id,objecttype,orderid,customerid,OORDERDATE,OCOMMENT")
            + buildProjectionRule("id,objecttype,orderid,customerid,priority,OCOMMENT")
            + buildProjectionRule("id,objecttype,orderid,customerid,OCLERK,OCOMMENT")
            + buildProjectionRule("id,objecttype,orderid,customerid,OSHIPPRIORITY,OCOMMENT");

    private static String buildProjectionRule(String content) {
        return "p(" + content + "):-" + DEFAULT_ROOT + ".";
    }

    private HashMap<String, String> tpchOrderContentFilterMapping = new LinkedHashMap<String, String>();

    private final ExpressionDefinition cbr_A_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_A));
    private final ExpressionDefinition cbr_A_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_A));
    private final ExpressionDefinition cbr_B_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_B));
    private final ExpressionDefinition cbr_B_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_B));
    private final ExpressionDefinition cbr_C_b1_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_C_b1));
    private final ExpressionDefinition cbr_C_b2_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_C_b2));
    private final ExpressionDefinition cbr_C_b3_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_C_b3));
    private final ExpressionDefinition cbr_C_b4_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_C_b4));
    private final ExpressionDefinition cbr_C_b5_Predicate1 = new ExpressionDefinition(new DatalogRoutingExpression(RULE_EIP_CBR_C_b5));
    private final ExpressionDefinition cbr_C_b1_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_C_b1));
    private final ExpressionDefinition cbr_C_b2_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_C_b2));
    private final ExpressionDefinition cbr_C_b3_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_C_b3));
    private final ExpressionDefinition cbr_C_b4_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_C_b4));
    private final ExpressionDefinition cbr_C_b5_Predicate2 = new ExpressionDefinition(new DatalogRoutingNotExpression(RULE_EIP_CBR_C_b5));

    public DatalogCamelContent() {
        tpchOrderContentFilterMapping.put("name", "id");
        tpchOrderContentFilterMapping.put("type", "objecttype");
        tpchOrderContentFilterMapping.put("O_ORDERKEY", "orderid");
        tpchOrderContentFilterMapping.put("O_CUSTKEY", "customerid");
        tpchOrderContentFilterMapping.put("OORDERSTATUS", "OORDERSTATUS");
        tpchOrderContentFilterMapping.put("OTOTALPRICE", "OTOTALPRICE");
        tpchOrderContentFilterMapping.put("OORDERDATE", "OORDERDATE");
        tpchOrderContentFilterMapping.put("O_ORDERPRIORITY", "priority");
        tpchOrderContentFilterMapping.put("OCLERK", "OCLERK");
        tpchOrderContentFilterMapping.put("OSHIPPRIORITY", "OSHIPPRIORITY");
        tpchOrderContentFilterMapping.put("OCOMMENT", "OCOMMENT");
    }

    final DatalogRule routingCondition = new DatalogRule("symbol", "symbol", "name(name,counter),=c(name,\"IBM\")");
    final DatalogRule routingConditionJson = new DatalogRule("buyStock", "symbol,a,b,volume,d,e",
            "order-subObj(symbol,a,b,volume,d,e),=(symbol,\"IBM\"),<(volume,3000)");

    final ComplexDatalogExpression routingFunction = new ComplexDatalogExpression(routingCondition);
    final ComplexDatalogExpression routingFunctionJson = new ComplexDatalogExpression(routingConditionJson);

    public final static Processor VOID = new Processor() {
        @Override
        public void process(final Exchange exchange) throws Exception {
        }

        @Override
        public String toString() {
            return "void-processor";
        }
    };

    @Override
    public RouteBuilder getRouteContent() throws Exception {
        final StringBuilder ruleBuilder = new StringBuilder();
        ruleBuilder.append("root" + "(");

        final Iterator<Entry<String, String>> iterator = tpchOrderContentFilterMapping.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<String, String> next = iterator.next();
            ruleBuilder.append(next.getValue());
            if (iterator.hasNext()) {
                ruleBuilder.append(",");
            }
        }
        ruleBuilder.append(").");

        // final DatalogRule rule = new DatalogRule("order-mapped",
        // "id,objecttype,orderid,customerid,priority", ruleBuilder.toString());
        // final ComplexDatalogExpression expression = new
        // ComplexDatalogExpression(rule);

        return (RouteBuilder) (new RouteBuilderHelper() {
            @Override
            public void configure() throws Exception { // CAMEL-DATALOG
                // Datalog CSV
                from("", TYPE.DATALOG, FORMAT.CSV).process(new SerializeCsvToDatalogProgram()).choice().when().expression(routingFunction)
                        .process(VOID);
                // Datalog JSON
                from("", TYPE.DATALOG, FORMAT.JSON).process(new JacksonJsonProcessor()).process(new SerializeJsonToDatalogProgram())
                        .choice().when().expression(routingFunctionJson).process(VOID);

                // Datalog JSON
                from("", TYPE.DATALOG_CONVERSION, FORMAT.JSON).process(new JacksonJsonProcessor())
                        .process(new SerializeJsonToDatalogProgram()).process(VOID);
                // Datalog JSON
                from("", TYPE.DATALOG_BASELINE, FORMAT.JSON).choice().when().expression(routingFunctionJson).process(VOID);

                // TPC-H, Orders, 2 operations, branching 1:2
                from("", TYPE.TPC_H_EIP_CD_CBR_A, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_A))
                        .process(VOID).id(buildId("order<100000")).otherwise().process(VOID).id(buildId("order>100000"));

                // TPC-H, Orders, 5 operations, branching 1:2
                from("", TYPE.TPC_H_EIP_CD_CBR_B, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_B))
                        .process(VOID).id(buildId("order<100000AndMore")).otherwise().process(VOID).id(buildId("order>100000AndMore"));

                // TPC-H, Orders, 5 operations, branching 1:6
                from("", TYPE.TPC_H_EIP_CD_CBR_C, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_C_b1))
                        .process(VOID).id(buildId("order-open-urgent-before")).when()
                        .expression(new DatalogRoutingExpression(RULE_EIP_CBR_C_b2)).process(VOID)
                        .id(buildId("order-inprogress-urgent-before")).when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_C_b3))
                        .process(VOID).id(buildId("order-open-urgent-after")).when()
                        .expression(new DatalogRoutingExpression(RULE_EIP_CBR_C_b4)).process(VOID)
                        .id(buildId("order-inprogress-urgent-after")).when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_C_b5))
                        .process(VOID).id(buildId("order-open-high-before")).otherwise().process(VOID).id(buildId("order-otherwise"));

                // TPC-H, Orders, 2 operations + join, branching 1:2
                from("", TYPE.TPC_H_EIP_CD_CBR_D, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_D))
                        .process(VOID).id(buildId("customer<100000")).otherwise().process(VOID).id(buildId("customer>100000"));

                // scale
                from("", TYPE.TPC_H_EIP_CD_CBR_SCALE_A, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_A))
                        .process(VOID).id(buildId("order_scale<100000")).otherwise().process(VOID).id(buildId("order_scale>100000"));

                from("", TYPE.TPC_H_EIP_CD_CBR_SCALE_B, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_B))
                        .process(VOID).id(buildId("order_scale<100000AndMore")).otherwise().process(VOID).id(buildId("order_scale>100000AndMore"));

                from("", TYPE.TPC_H_EIP_CD_CBR_SCALE_C, FORMAT.JSON).choice().when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_C_b1))
                        .process(VOID).id(buildId("order-scale-open-urgent-before")).when()
                        .expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_C_b2)).process(VOID)
                        .id(buildId("order-scale-inprogress-urgent-before")).when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_C_b3))
                        .process(VOID).id(buildId("order-scale-open-urgent-after")).when()
                        .expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_C_b4)).process(VOID)
                        .id(buildId("order-scale-inprogress-urgent-after")).when().expression(new DatalogRoutingExpression(RULE_EIP_CBR_SCALE_C_b5))
                        .process(VOID).id(buildId("order-scale-open-high-before")).otherwise().process(VOID).id(buildId("order-scale-otherwise"));
            }
        });
    }
}
