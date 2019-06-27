package com.eipbench.camel;

import com.eipbench.BenchmarkContent;
import com.eipbench.Constants.*;
import com.eipbench.generator.OrderMessageSet;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.ExpressionDefinition;
import com.eipbench.generator.OrderMessageSet.OrderNames;
import com.eipbench.camel.CustomerNationRegionEmbeddedMessageSet.*;
import com.eipbench.camel.CustomerNationRegionMultiformatMessageSet.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class JavaCamelContent implements BenchmarkContent {
    private static final String GREAT_BRITAIN = "22";
    private static final String MIDDLE_EAST = "MIDDLE EAST";
    OperatorEquals operatorEqualsIBM = new OperatorEquals("symbol", "IBM");
    OperatorEquals operatorEqualsMSFT = new OperatorEquals("symbol", "MSFT");
    OperatorLowerThan operatorLowerThan = new OperatorLowerThan("volume", 3000);

    private final JsonTreeValueMatchExpression expression = new JsonTreeValueMatchExpression("/m:buyStocks/order", operatorEqualsIBM,
            operatorLowerThan);
    private final JsonTreeValueMatchExpression expression2 = new JsonTreeValueMatchExpression("/m:buyStocks/order", operatorEqualsMSFT);

    private final OperatorEquals typeOrder = new OperatorEquals("type", "ORDERS");

    private final OperatorLowerThan totalPrice = new OperatorLowerThan(OrderMessageSet.OrderNames.O_TOTALPRICE.toString(), 100000);
    private final OperatorBiggerEqualsThan highPrice = new OperatorBiggerEqualsThan(OrderMessageSet.OrderNames.O_TOTALPRICE.toString(), 100000);

    private final OperatorEquals priority = new OperatorEquals(OrderMessageSet.OrderNames.O_ORDERPRIORITY.toString(), "3-MEDIUM");
    private final OperatorEquals priorityUrgent = new OperatorEquals(OrderNames.O_ORDERPRIORITY.toString(), "1-URGENT");
    private final OperatorEquals priorityHigh = new OperatorEquals(OrderNames.O_ORDERPRIORITY.toString(), "2-HIGH");

    private final OperatorSubNode cnre_nationgb = new OperatorSubNode("NATION",
            new OperatorArrayList(0, new OperatorEquals(CustomerNationRegionMultiformatMessageSet.NationNames.N_NATIONKEY.toString(), GREAT_BRITAIN)));
    private final OperatorSubNode cnre_regionme = new OperatorSubNode("NATION", new OperatorArrayList(0,
            new OperatorSubNode("REGION", new OperatorArrayList(0, new OperatorEquals(CustomerNationRegionMultiformatMessageSet.RegionNames.R_NAME.toString(), MIDDLE_EAST)))));
    private final OperatorLowerThan cnre_customeracctbal = new OperatorLowerThan(CustomerNames.C_ACCTBAL.toString(), 100);
    private final OperatorArrayList cnrm_customeracctbal = new OperatorArrayList(0,
            new OperatorLowerThan(CustomerNames.C_ACCTBAL.toString(), 100));
    private final OperatorArrayList cnrm_nationgb = new OperatorArrayList(0,
            new OperatorEquals(CustomerNames.C_NATIONKEY.toString(), GREAT_BRITAIN));
    private final OperatorArrayList cnrm_regionme = new OperatorArrayList(29,
            new OperatorEquals(RegionNames.R_NAME.toString(), MIDDLE_EAST));

    private final OperatorDateLowerThan beforeDate;
    private final OperatorDateLowerThan afterDate;

    private final OperatorEquals statusInProgress = new OperatorEquals(OrderNames.O_ORDERSTATUS.toString(), "P");
    private final OperatorEquals statusOpen = new OperatorEquals(OrderNames.O_ORDERSTATUS.toString(), "O");
    private final OperatorEquals statusFinished = new OperatorEquals(OrderNames.O_ORDERSTATUS.toString(), "O");

    private final Expression cbr_A = new TpcRouterQueryOptimized("/", typeOrder, totalPrice);
    private final Expression cbr_scale_A = new TpcRouterQueryScaleOptimized("/", typeOrder, totalPrice);
    private final Expression cbr_no_A = new TpcRouterQuery("/", typeOrder, totalPrice);
    private final Expression cbr_scale_no_A = new TpcRouterQueryScale("/", typeOrder, totalPrice);
    private final Expression cbr_B;
    private final Expression cbr_no_B;
    private final Expression cbr_scale_B;
    private final Expression cbr_scale_no_B;

    private final Expression cbr_C_b1;
    private final Expression cbr_C_b2;
    private final Expression cbr_C_b3;
    private final Expression cbr_C_b4;
    private final Expression cbr_C_b5;

    private final Expression cbr_scale_C_b1;
    private final Expression cbr_scale_C_b2;
    private final Expression cbr_scale_C_b3;
    private final Expression cbr_scale_C_b4;
    private final Expression cbr_scale_C_b5;

    private final Expression cbr_no_C_b1;
    private final Expression cbr_no_C_b2;
    private final Expression cbr_no_C_b3;
    private final Expression cbr_no_C_b4;
    private final Expression cbr_no_C_b5;

    private final Expression cbr_scale_no_C_b1;
    private final Expression cbr_scale_no_C_b2;
    private final Expression cbr_scale_no_C_b3;
    private final Expression cbr_scale_no_C_b4;
    private final Expression cbr_scale_no_C_b5;

    private final ExpressionDefinition cbr_A_Predicate1 = new ExpressionDefinition(cbr_A);
    private final ExpressionDefinition cbr_A_Predicate2 = new ExpressionDefinition(
            new TpcRouterNotQueryOptimized("/", typeOrder, totalPrice));
    private final ExpressionDefinition cbr_B_Predicate1;
    private final ExpressionDefinition cbr_B_Predicate2;

    private final ExpressionDefinition cbr_C_b1_Predicate1;
    private final ExpressionDefinition cbr_C_b2_Predicate1;
    private final ExpressionDefinition cbr_C_b3_Predicate1;
    private final ExpressionDefinition cbr_C_b4_Predicate1;
    private final ExpressionDefinition cbr_C_b5_Predicate1;
    private final ExpressionDefinition cbr_C_b1_Predicate2;
    private final ExpressionDefinition cbr_C_b2_Predicate2;
    private final ExpressionDefinition cbr_C_b3_Predicate2;
    private final ExpressionDefinition cbr_C_b4_Predicate2;
    private final ExpressionDefinition cbr_C_b5_Predicate2;

    private final Expression cbr_D = new TpcRouterQueryOptimized("/", cnrm_customeracctbal, cnrm_nationgb, cnrm_regionme);
    private final Expression cbr_E = new TpcRouterQueryOptimized("/", cnre_customeracctbal, cnre_nationgb, cnre_regionme);

    private HashMap<String, String> tpchContentFilterMapping = new HashMap<String, String>();

    public JavaCamelContent() {
        try {
            final Date date = new SimpleDateFormat("yyyy-MM-dd").parse("1995-01-01");
            beforeDate = new OperatorDateLowerThan(OrderNames.O_ORDERDATE.toString(), date);
            afterDate = new OperatorDateLowerThan(OrderNames.O_ORDERDATE.toString(), date);

            cbr_B = new TpcRouterQueryOptimized("/", typeOrder, totalPrice, priority, beforeDate, statusInProgress);
            cbr_no_B = new TpcRouterQuery("/", typeOrder, totalPrice, priority, beforeDate, statusInProgress);
            cbr_scale_B = new TpcRouterQueryScaleOptimized("/", typeOrder, totalPrice, priority, beforeDate, statusInProgress);
            cbr_scale_no_B = new TpcRouterQueryScale("/", typeOrder, totalPrice, priority, beforeDate, statusInProgress);
            cbr_B_Predicate1 = new ExpressionDefinition(cbr_B);
            cbr_B_Predicate2 = new ExpressionDefinition(
                    new TpcRouterNotQueryOptimized("/", typeOrder, totalPrice, priority, beforeDate, statusInProgress));

            cbr_C_b5 = new TpcRouterQueryOptimized("/", typeOrder, highPrice, priorityHigh, beforeDate, statusOpen);
            cbr_C_b4 = new TpcRouterQueryOptimized("/", typeOrder, highPrice, priorityUrgent, afterDate, statusInProgress);
            cbr_C_b3 = new TpcRouterQueryOptimized("/", typeOrder, highPrice, priorityUrgent, afterDate, statusOpen);
            cbr_C_b2 = new TpcRouterQueryOptimized("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusInProgress);
            cbr_C_b1 = new TpcRouterQueryOptimized("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusOpen);

            cbr_scale_C_b5 = new TpcRouterQueryScaleOptimized("/", typeOrder, highPrice, priorityHigh, beforeDate, statusOpen);
            cbr_scale_C_b4 = new TpcRouterQueryScaleOptimized("/", typeOrder, highPrice, priorityUrgent, afterDate, statusInProgress);
            cbr_scale_C_b3 = new TpcRouterQueryScaleOptimized("/", typeOrder, highPrice, priorityUrgent, afterDate, statusOpen);
            cbr_scale_C_b2 = new TpcRouterQueryScaleOptimized("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusInProgress);
            cbr_scale_C_b1 = new TpcRouterQueryScaleOptimized("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusOpen);

            cbr_no_C_b5 = new TpcRouterQuery("/", typeOrder, highPrice, priorityHigh, beforeDate, statusOpen);
            cbr_no_C_b4 = new TpcRouterQuery("/", typeOrder, highPrice, priorityUrgent, afterDate, statusInProgress);
            cbr_no_C_b3 = new TpcRouterQuery("/", typeOrder, highPrice, priorityUrgent, afterDate, statusOpen);
            cbr_no_C_b2 = new TpcRouterQuery("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusInProgress);
            cbr_no_C_b1 = new TpcRouterQuery("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusOpen);

            cbr_scale_no_C_b5 = new TpcRouterQueryScale("/", typeOrder, highPrice, priorityHigh, beforeDate, statusOpen);
            cbr_scale_no_C_b4 = new TpcRouterQueryScale("/", typeOrder, highPrice, priorityUrgent, afterDate, statusInProgress);
            cbr_scale_no_C_b3 = new TpcRouterQueryScale("/", typeOrder, highPrice, priorityUrgent, afterDate, statusOpen);
            cbr_scale_no_C_b2 = new TpcRouterQueryScale("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusInProgress);
            cbr_scale_no_C_b1 = new TpcRouterQueryScale("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusOpen);

            cbr_C_b5_Predicate1 = new ExpressionDefinition(cbr_C_b5);
            cbr_C_b4_Predicate1 = new ExpressionDefinition(cbr_C_b4);
            cbr_C_b3_Predicate1 = new ExpressionDefinition(cbr_C_b3);
            cbr_C_b2_Predicate1 = new ExpressionDefinition(cbr_C_b2);
            cbr_C_b1_Predicate1 = new ExpressionDefinition(cbr_C_b1);
            cbr_C_b5_Predicate2 = new ExpressionDefinition(
                    new TpcRouterNotQueryOptimized("/", typeOrder, highPrice, priorityHigh, beforeDate, statusOpen));
            cbr_C_b4_Predicate2 = new ExpressionDefinition(
                    new TpcRouterNotQueryOptimized("/", typeOrder, highPrice, priorityUrgent, afterDate, statusInProgress));
            cbr_C_b3_Predicate2 = new ExpressionDefinition(
                    new TpcRouterNotQueryOptimized("/", typeOrder, highPrice, priorityUrgent, afterDate, statusOpen));
            cbr_C_b2_Predicate2 = new ExpressionDefinition(
                    new TpcRouterNotQueryOptimized("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusInProgress));
            cbr_C_b1_Predicate2 = new ExpressionDefinition(
                    new TpcRouterNotQueryOptimized("/", typeOrder, highPrice, priorityUrgent, beforeDate, statusOpen));
        } catch (final ParseException e) {
            throw new IllegalStateException("Failed to parse Date.", e);
        }

        tpchContentFilterMapping.put(OrderNames.NAME.toString(), "id");
        tpchContentFilterMapping.put(OrderNames.TYPE.toString(), "tpch_type");
        tpchContentFilterMapping.put(OrderNames.O_ORDERKEY.toString(), "order_id");
        tpchContentFilterMapping.put(OrderNames.O_CUSTKEY.toString(), "customer_id");
        tpchContentFilterMapping.put(OrderNames.O_ORDERPRIORITY.toString(), "priority");
    }

    @Override
    public RouteBuilder getRouteContent() throws Exception {
        return (RouteBuilder) (new RouteBuilderHelper() {
            @Override
            public void configure() throws Exception { // CAMEL-DATALOG
                // CAMEL JSON
                from("", TYPE.CAMEL).process(new JacksonJsonProcessor()).choice().when().expression(expression).process(VOID).when()
                        .expression(expression2).process(VOID);
                // CAMEL JSON
                from("", TYPE.CAMEL_CONVERSION).process(new JacksonJsonProcessor());
                // CAMEL JSON
                from("", TYPE.CAMEL_BASELINE).choice().when().expression(expression).process(VOID).when().expression(expression2)
                        .process(VOID);

                // 2 operations, branching 1:2, Orders
                from("", TYPE.TPC_H_EIP_CJ_CBR_A).choice().when().expression(cbr_A).process(VOID).id(buildId("order<100000")).otherwise()
                        .process(VOID).id(buildId("order>100000"));
                from("", TYPE.TPC_H_EIP_CJ_CBR_NO_A).choice().when().expression(cbr_no_A).process(VOID).id(buildId("order<100000"))
                        .otherwise().process(VOID).id(buildId("order>100000"));

                // 5 operations, branching 1:2, Orders
                from("", TYPE.TPC_H_EIP_CJ_CBR_B).choice().when().expression(cbr_B).process(VOID).id(buildId("order<100000AndMore"))
                        .otherwise().process(VOID).id(buildId("order>100000AndMore"));
                from("", TYPE.TPC_H_EIP_CJ_CBR_NO_B).choice().when().expression(cbr_no_B).process(VOID).id(buildId("order<100000AndMore"))
                        .otherwise().process(VOID).id(buildId("order>100000AndMore"));

                // 5 operations, branching 1:6, Orders
                from("", TYPE.TPC_H_EIP_CJ_CBR_C).choice().when().expression(cbr_C_b1).process(VOID).id(buildId("order-open-urgent-before"))
                        .when().expression(cbr_C_b2).process(VOID).id(buildId("order-inprogress-urgent-before")).when().expression(cbr_C_b3)
                        .process(VOID).id(buildId("order-open-urgent-after")).when().expression(cbr_C_b4).process(VOID)
                        .id(buildId("order-inprogress-urgent-after")).when().expression(cbr_C_b5).process(VOID)
                        .id(buildId("order-open-high-before")).otherwise().process(VOID).id("order-otherwise");
                from("", TYPE.TPC_H_EIP_CJ_CBR_NO_C).choice().when().expression(cbr_no_C_b1).process(VOID)
                        .id(buildId("order-open-urgent-before")).when().expression(cbr_no_C_b2).process(VOID)
                        .id(buildId("order-inprogress-urgent-before")).when().expression(cbr_no_C_b3).process(VOID)
                        .id(buildId("order-open-urgent-after")).when().expression(cbr_no_C_b4).process(VOID)
                        .id(buildId("order-inprogress-urgent-after")).when().expression(cbr_no_C_b5).process(VOID)
                        .id(buildId("order-open-high-before")).otherwise().process(VOID).id("order-c-otherwise");

                from("", TYPE.TPC_H_EIP_CJ_CBR_D).choice().when().expression(cbr_D).process(VOID).id(buildId("multigb")).otherwise()
                        .process(VOID).id(buildId("multinotgb"));
                from("", TYPE.TPC_H_EIP_CJ_CBR_FM_D).process(new CustomerJoinProcessor()).choice().when().expression(cbr_E).process(VOID)
                        .id(buildId("multiToNestedgb")).otherwise().process(VOID).id(buildId("multiToNestednotgb"));
                from("", TYPE.TPC_H_EIP_CJ_CBR_E).choice().when().expression(cbr_E).process(VOID).id(buildId("nestedgb")).otherwise()
                        .process(VOID).id(buildId("nestednotgb"));

                // Scale
                from("", TYPE.TPC_H_EIP_CJ_CBR_SCALE_A).choice().when().expression(cbr_scale_A).process(VOID)
                        .id(buildId("order_scale<100000")).otherwise().process(VOID).id(buildId("order_scale>100000"));
                from("", TYPE.TPC_H_EIP_CJ_CBR_SCALE_NO_A).choice().when().expression(cbr_scale_no_A).process(VOID)
                        .id(buildId("order_scale_no<100000")).otherwise().process(VOID).id(buildId("order_scale_no>100000"));

                from("", TYPE.TPC_H_EIP_CJ_CBR_SCALE_B).choice().when().expression(cbr_scale_B).process(VOID)
                        .id(buildId("order_scale<100000AndMore")).otherwise().process(VOID).id(buildId("order_scale>100000AndMore"));
                from("", TYPE.TPC_H_EIP_CJ_CBR_SCALE_NO_B).choice().when().expression(cbr_scale_no_B).process(VOID)
                        .id(buildId("order_scale<100000AndMore")).otherwise().process(VOID).id(buildId("order_scale>100000AndMore"));

                from("", TYPE.TPC_H_EIP_CJ_CBR_SCALE_C).choice().when().expression(cbr_scale_C_b1).process(VOID)
                        .id(buildId("order-scale-open-urgent-before")).when().expression(cbr_scale_C_b2).process(VOID)
                        .id(buildId("order-scale-inprogress-urgent-before")).when().expression(cbr_scale_C_b3).process(VOID)
                        .id(buildId("order-scale-open-urgent-after")).when().expression(cbr_scale_C_b4).process(VOID)
                        .id(buildId("order-scale-inprogress-urgent-after")).when().expression(cbr_scale_C_b5).process(VOID)
                        .id(buildId("order-scale-open-high-before")).otherwise().process(VOID).id("order-scale-otherwise");
                from("", TYPE.TPC_H_EIP_CJ_CBR_SCALE_NO_C).choice().when().expression(cbr_scale_no_C_b1).process(VOID)
                        .id(buildId("order-scale-open-urgent-before")).when().expression(cbr_scale_no_C_b2).process(VOID)
                        .id(buildId("order-scale-inprogress-urgent-before")).when().expression(cbr_scale_no_C_b3).process(VOID)
                        .id(buildId("order-scale-open-urgent-after")).when().expression(cbr_scale_no_C_b4).process(VOID)
                        .id(buildId("order-scale-inprogress-urgent-after")).when().expression(cbr_scale_no_C_b5).process(VOID)
                        .id(buildId("order-scale-open-high-before")).otherwise().process(VOID).id("order-scale-c-otherwise");
            }
        });
    }
}
