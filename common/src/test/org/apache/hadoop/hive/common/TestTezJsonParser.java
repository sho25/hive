begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|jsonexplain
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|jsonexplain
operator|.
name|tez
operator|.
name|TezJsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_class
specifier|public
class|class
name|TestTezJsonParser
block|{
name|JsonParser
name|jsonParser
init|=
literal|null
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|jsonParser
operator|=
operator|new
name|TezJsonParser
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQuery27
parameter_list|()
throws|throws
name|Exception
block|{
name|JSONObject
name|jsonPlan
init|=
operator|new
name|JSONObject
argument_list|(
literal|"{\"STAGE DEPENDENCIES\":{\"Stage-1\":{\"ROOT STAGE\":\"TRUE\"},\"Stage-0\":{\"DEPENDENT STAGES\":\"Stage-1\"}},\"STAGE PLANS\":{\"Stage-1\":{\"Tez\":{\"Edges:\":{\"Map 1\":{\"parent\":\"Map 5\",\"type\":\"BROADCAST_EDGE\"},\"Map 6\":{\"parent\":\"Map 8\",\"type\":\"BROADCAST_EDGE\"},\"Map 9\":{\"parent\":\"Map 10\",\"type\":\"BROADCAST_EDGE\"},\"Reducer 2\":[{\"parent\":\"Map 1\",\"type\":\"SIMPLE_EDGE\"},{\"parent\":\"Reducer 7\",\"type\":\"SIMPLE_EDGE\"}],\"Reducer 3\":{\"parent\":\"Reducer 2\",\"type\":\"SIMPLE_EDGE\"},\"Reducer 4\":{\"parent\":\"Reducer 3\",\"type\":\"SIMPLE_EDGE\"},\"Reducer 7\":[{\"parent\":\"Map 11\",\"type\":\"BROADCAST_EDGE\"},{\"parent\":\"Map 12\",\"type\":\"BROADCAST_EDGE\"},{\"parent\":\"Map 6\",\"type\":\"SIMPLE_EDGE\"},{\"parent\":\"Map 9\",\"type\":\"SIMPLE_EDGE\"}]},\"Vertices:\":{\"Map 1\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"cs_sold_date_sk\",\"cs_bill_customer_sk\",\"cs_item_sk\",\"cs_net_profit\"],\"\":\"default@catalog_sales,catalog_sales,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=1441548 width=204\",\"OperatorId:\":\"TS_0\",\"children\":{\"Filter Operator\":{\"predicate:\":\"(cs_bill_customer_sk is not null and cs_item_sk is not null and cs_sold_date_sk is not null)\",\"Statistics:\":\"rows=1441548 width=204\",\"OperatorId:\":\"FIL_85\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\",\"_col1\",\"_col2\",\"_col3\"],\"Statistics:\":\"rows=1441548 width=204\",\"OperatorId:\":\"SEL_2\",\"children\":{\"Map Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"input vertices:\":{\"1\":\"Map 5\"},\"keys:\":{\"0\":\"_col0\",\"1\":\"_col0\"},\"Output:\":[\"_col1\",\"_col2\",\"_col3\"],\"Statistics:\":\"rows=1585702 width=204\",\"HybridGraceHashJoin:\":\"true\",\"OperatorId:\":\"MAPJOIN_93\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 2\",\"PartitionCols:\":\"_col1, _col2\",\"Statistics:\":\"rows=1585702 width=204\",\"OperatorId:\":\"RS_44\"}}}}}}}}}}],\"tag:\":\"0\"},\"Map 10\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"d_date_sk\",\"d_year\",\"d_moy\"],\"\":\"default@date_dim,d2,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=73049 width=140\",\"OperatorId:\":\"TS_15\",\"children\":{\"Filter Operator\":{\"predicate:\":\"(d_moy BETWEEN 4 AND 10 and (d_year = 1998) and d_date_sk is not null)\",\"Statistics:\":\"rows=4058 width=140\",\"OperatorId:\":\"FIL_90\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\"],\"Statistics:\":\"rows=4058 width=140\",\"OperatorId:\":\"SEL_17\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Map 9\",\"PartitionCols:\":\"_col0\",\"Statistics:\":\"rows=4058 width=140\",\"OperatorId:\":\"RS_19\"}}}}}}}}],\"tag:\":\"0\"},\"Map 11\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"i_item_sk\",\"i_item_id\",\"i_item_desc\"],\"\":\"default@item,item,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=18000 width=279\",\"OperatorId:\":\"TS_22\",\"children\":{\"Filter Operator\":{\"predicate:\":\"i_item_sk is not null\",\"Statistics:\":\"rows=18000 width=279\",\"OperatorId:\":\"FIL_91\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\",\"_col1\",\"_col2\"],\"Statistics:\":\"rows=18000 width=279\",\"OperatorId:\":\"SEL_24\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 7\",\"PartitionCols:\":\"_col0\",\"Statistics:\":\"rows=18000 width=279\",\"OperatorId:\":\"RS_35\"}}}}}}}}],\"tag:\":\"0\"},\"Map 12\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"s_store_sk\",\"s_store_id\",\"s_store_name\"],\"\":\"default@store,store,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=12 width=261\",\"OperatorId:\":\"TS_25\",\"children\":{\"Filter Operator\":{\"predicate:\":\"s_store_sk is not null\",\"Statistics:\":\"rows=12 width=261\",\"OperatorId:\":\"FIL_92\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\",\"_col1\",\"_col2\"],\"Statistics:\":\"rows=12 width=261\",\"OperatorId:\":\"SEL_27\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 7\",\"PartitionCols:\":\"_col0\",\"Statistics:\":\"rows=12 width=261\",\"OperatorId:\":\"RS_38\"}}}}}}}}],\"tag:\":\"0\"},\"Map 5\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"d_date_sk\",\"d_year\",\"d_moy\"],\"\":\"default@date_dim,d3,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=73049 width=140\",\"OperatorId:\":\"TS_3\",\"children\":{\"Filter Operator\":{\"predicate:\":\"(d_moy BETWEEN 4 AND 10 and (d_year = 1998) and d_date_sk is not null)\",\"Statistics:\":\"rows=4058 width=140\",\"OperatorId:\":\"FIL_86\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\"],\"Statistics:\":\"rows=4058 width=140\",\"OperatorId:\":\"SEL_5\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Map 1\",\"PartitionCols:\":\"_col0\",\"Statistics:\":\"rows=4058 width=140\",\"OperatorId:\":\"RS_42\"}}}}}}}}],\"tag:\":\"0\"},\"Map 6\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"ss_sold_date_sk\",\"ss_item_sk\",\"ss_customer_sk\",\"ss_store_sk\",\"ss_ticket_number\",\"ss_net_profit\"],\"\":\"default@store_sales,store_sales,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=2880404 width=133\",\"OperatorId:\":\"TS_6\",\"children\":{\"Filter Operator\":{\"predicate:\":\"(ss_item_sk is not null and ss_customer_sk is not null and ss_ticket_number is not null and ss_sold_date_sk is not null and ss_store_sk is not null)\",\"Statistics:\":\"rows=2880404 width=133\",\"OperatorId:\":\"FIL_87\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\"],\"Statistics:\":\"rows=2880404 width=133\",\"OperatorId:\":\"SEL_8\",\"children\":{\"Map Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"input vertices:\":{\"1\":\"Map 8\"},\"keys:\":{\"0\":\"_col0\",\"1\":\"_col0\"},\"Output:\":[\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\"],\"Statistics:\":\"rows=3168444 width=133\",\"HybridGraceHashJoin:\":\"true\",\"OperatorId:\":\"MAPJOIN_94\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 7\",\"PartitionCols:\":\"_col1, _col2, _col4\",\"Statistics:\":\"rows=3168444 width=133\",\"OperatorId:\":\"RS_31\"}}}}}}}}}}],\"tag:\":\"0\"},\"Map 8\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"d_date_sk\",\"d_year\",\"d_moy\"],\"\":\"default@date_dim,d1,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=73049 width=140\",\"OperatorId:\":\"TS_9\",\"children\":{\"Filter Operator\":{\"predicate:\":\"((d_moy = 4) and (d_year = 1998) and d_date_sk is not null)\",\"Statistics:\":\"rows=18262 width=140\",\"OperatorId:\":\"FIL_88\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\"],\"Statistics:\":\"rows=18262 width=140\",\"OperatorId:\":\"SEL_11\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Map 6\",\"PartitionCols:\":\"_col0\",\"Statistics:\":\"rows=18262 width=140\",\"OperatorId:\":\"RS_29\"}}}}}}}}],\"tag:\":\"0\"},\"Map 9\":{\"Map Operator Tree:\":[{\"TableScan\":{\"Output:\":[\"sr_returned_date_sk\",\"sr_item_sk\",\"sr_customer_sk\",\"sr_ticket_number\",\"sr_net_loss\"],\"\":\"default@store_returns,store_returns,Tbl:COMPLETE,Col:NONE\",\"Statistics:\":\"rows=287514 width=112\",\"OperatorId:\":\"TS_12\",\"children\":{\"Filter Operator\":{\"predicate:\":\"(sr_item_sk is not null and sr_customer_sk is not null and sr_ticket_number is not null and sr_returned_date_sk is not null)\",\"Statistics:\":\"rows=287514 width=112\",\"OperatorId:\":\"FIL_89\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\"],\"Statistics:\":\"rows=287514 width=112\",\"OperatorId:\":\"SEL_14\",\"children\":{\"Map Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"input vertices:\":{\"1\":\"Map 10\"},\"keys:\":{\"0\":\"_col0\",\"1\":\"_col0\"},\"Output:\":[\"_col1\",\"_col2\",\"_col3\",\"_col4\"],\"Statistics:\":\"rows=316265 width=112\",\"HybridGraceHashJoin:\":\"true\",\"OperatorId:\":\"MAPJOIN_95\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 7\",\"PartitionCols:\":\"_col1, _col2, _col3\",\"Statistics:\":\"rows=316265 width=112\",\"OperatorId:\":\"RS_32\"}}}}}}}}}}],\"tag:\":\"0\"},\"Reducer 2\":{\"Reduce Operator Tree:\":{\"Merge Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"keys:\":{\"0\":\"_col1, _col2\",\"1\":\"_col17, _col16\"},\"Output:\":[\"_col3\",\"_col8\",\"_col9\",\"_col11\",\"_col12\",\"_col18\",\"_col26\"],\"Statistics:\":\"rows=4638916 width=133\",\"OperatorId:\":\"MERGEJOIN_99\",\"children\":{\"Group By Operator\":{\"aggregations:\":[\"sum(_col18)\",\"sum(_col26)\",\"sum(_col3)\"],\"keys:\":\"_col11, _col12, _col8, _col9\",\"Output:\":[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\",\"_col6\"],\"Statistics:\":\"rows=4638916 width=133\",\"OperatorId:\":\"GBY_48\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 3\",\"PartitionCols:\":\"_col0, _col1, _col2, _col3\",\"Statistics:\":\"rows=4638916 width=133\",\"OperatorId:\":\"RS_49\"}}}}}},\"tag:\":\"0\",\"tagToInput:\":{\"0\":\"Map 1\",\"1\":\"Reducer 7\"}},\"Reducer 3\":{\"Reduce Operator Tree:\":{\"Group By Operator\":{\"aggregations:\":[\"sum(VALUE._col0)\",\"sum(VALUE._col1)\",\"sum(VALUE._col2)\"],\"keys:\":\"KEY._col0, KEY._col1, KEY._col2, KEY._col3\",\"Output:\":[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\",\"_col6\"],\"Statistics:\":\"rows=2319458 width=133\",\"OperatorId:\":\"GBY_50\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 4\",\"Statistics:\":\"rows=2319458 width=133\",\"OperatorId:\":\"RS_52\"}}}},\"tag:\":\"0\",\"tagToInput:\":{\"0\":\"Reducer 2\"}},\"Reducer 4\":{\"Reduce Operator Tree:\":{\"Select Operator\":{\"Output:\":[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\",\"_col6\"],\"Statistics:\":\"rows=2319458 width=133\",\"OperatorId:\":\"SEL_53\",\"children\":{\"Limit\":{\"Number of rows:\":\"100\",\"Statistics:\":\"rows=100 width=133\",\"OperatorId:\":\"LIM_54\",\"children\":{\"File Output Operator\":{\"Statistics:\":\"rows=100 width=133\",\"OperatorId:\":\"FS_55\"}}}}}},\"tag:\":\"0\",\"tagToInput:\":{\"0\":\"Reducer 3\"}},\"Reducer 7\":{\"Reduce Operator Tree:\":{\"Merge Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"keys:\":{\"0\":\"_col1, _col2, _col4\",\"1\":\"_col1, _col2, _col3\"},\"Output:\":[\"_col1\",\"_col3\",\"_col5\",\"_col10\",\"_col11\",\"_col13\"],\"Statistics:\":\"rows=3485288 width=133\",\"OperatorId:\":\"MERGEJOIN_96\",\"children\":{\"Map Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"input vertices:\":{\"1\":\"Map 11\"},\"keys:\":{\"0\":\"_col1\",\"1\":\"_col0\"},\"Output:\":[\"_col3\",\"_col5\",\"_col10\",\"_col11\",\"_col13\",\"_col18\",\"_col19\"],\"Statistics:\":\"rows=3833816 width=133\",\"HybridGraceHashJoin:\":\"true\",\"OperatorId:\":\"MAPJOIN_97\",\"children\":{\"Map Join Operator\":{\"condition map:\":[{\"\":\"{\\\"type\\\":\\\"Inner\\\",\\\"left\\\":0,\\\"right\\\":1}\"}],\"input vertices:\":{\"1\":\"Map 12\"},\"keys:\":{\"0\":\"_col3\",\"1\":\"_col0\"},\"Output:\":[\"_col5\",\"_col10\",\"_col11\",\"_col13\",\"_col18\",\"_col19\",\"_col21\",\"_col22\"],\"Statistics:\":\"rows=4217197 width=133\",\"HybridGraceHashJoin:\":\"true\",\"OperatorId:\":\"MAPJOIN_98\",\"children\":{\"Select Operator\":{\"Output:\":[\"_col1\",\"_col2\",\"_col4\",\"_col5\",\"_col11\",\"_col16\",\"_col17\",\"_col19\"],\"Statistics:\":\"rows=4217197 width=133\",\"OperatorId:\":\"SEL_40\",\"children\":{\"Reduce Output Operator\":{\"outputname:\":\"Reducer 2\",\"PartitionCols:\":\"_col17, _col16\",\"Statistics:\":\"rows=4217197 width=133\",\"OperatorId:\":\"RS_45\"}}}}}}}}}},\"tag:\":\"0\",\"tagToInput:\":{\"0\":\"Map 6\",\"1\":\"Map 9\"}}}}},\"Stage-0\":{\"Fetch Operator\":{\"limit:\":\"100\"}}},\"cboInfo\":\"Plan optimized by CBO.\"}"
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|ps
init|=
operator|new
name|PrintStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|jsonParser
operator|.
name|print
argument_list|(
name|jsonPlan
argument_list|,
name|ps
argument_list|)
expr_stmt|;
name|String
name|content
init|=
operator|new
name|String
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Plan optimized by CBO.\n"
operator|+
literal|"\n"
operator|+
literal|"Vertex dependency in root stage\n"
operator|+
literal|"Map 1<- Map 5 (BROADCAST_EDGE)\n"
operator|+
literal|"Map 6<- Map 8 (BROADCAST_EDGE)\n"
operator|+
literal|"Map 9<- Map 10 (BROADCAST_EDGE)\n"
operator|+
literal|"Reducer 2<- Map 1 (SIMPLE_EDGE), Reducer 7 (SIMPLE_EDGE)\n"
operator|+
literal|"Reducer 3<- Reducer 2 (SIMPLE_EDGE)\n"
operator|+
literal|"Reducer 4<- Reducer 3 (SIMPLE_EDGE)\n"
operator|+
literal|"Reducer 7<- Map 11 (BROADCAST_EDGE), Map 12 (BROADCAST_EDGE), Map 6 (SIMPLE_EDGE), Map 9 (SIMPLE_EDGE)\n"
operator|+
literal|"\n"
operator|+
literal|"Stage-0\n"
operator|+
literal|"  Fetch Operator\n"
operator|+
literal|"    limit:100\n"
operator|+
literal|"    Stage-1\n"
operator|+
literal|"      Reducer 4\n"
operator|+
literal|"      File Output Operator [FS_55]\n"
operator|+
literal|"        Limit [LIM_54] (rows=100 width=133)\n"
operator|+
literal|"          Number of rows:100\n"
operator|+
literal|"          Select Operator [SEL_53] (rows=2319458 width=133)\n"
operator|+
literal|"            Output:[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\",\"_col6\"]\n"
operator|+
literal|"<-Reducer 3 [SIMPLE_EDGE]\n"
operator|+
literal|"            SHUFFLE [RS_52]\n"
operator|+
literal|"              Group By Operator [GBY_50] (rows=2319458 width=133)\n"
operator|+
literal|"                Output:[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\",\"_col6\"],aggregations:[\"sum(VALUE._col0)\",\"sum(VALUE._col1)\",\"sum(VALUE._col2)\"],keys:KEY._col0, KEY._col1, KEY._col2, KEY._col3\n"
operator|+
literal|"<-Reducer 2 [SIMPLE_EDGE]\n"
operator|+
literal|"                SHUFFLE [RS_49]\n"
operator|+
literal|"                  PartitionCols:_col0, _col1, _col2, _col3\n"
operator|+
literal|"                  Group By Operator [GBY_48] (rows=4638916 width=133)\n"
operator|+
literal|"                    Output:[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\",\"_col6\"],aggregations:[\"sum(_col18)\",\"sum(_col26)\",\"sum(_col3)\"],keys:_col11, _col12, _col8, _col9\n"
operator|+
literal|"                    Merge Join Operator [MERGEJOIN_99] (rows=4638916 width=133)\n"
operator|+
literal|"                      Conds:RS_44._col1, _col2=RS_45._col17, _col16(Inner),Output:[\"_col3\",\"_col8\",\"_col9\",\"_col11\",\"_col12\",\"_col18\",\"_col26\"]\n"
operator|+
literal|"<-Map 1 [SIMPLE_EDGE]\n"
operator|+
literal|"                      SHUFFLE [RS_44]\n"
operator|+
literal|"                        PartitionCols:_col1, _col2\n"
operator|+
literal|"                        Map Join Operator [MAPJOIN_93] (rows=1585702 width=204)\n"
operator|+
literal|"                          Conds:SEL_2._col0=RS_42._col0(Inner),HybridGraceHashJoin:true,Output:[\"_col1\",\"_col2\",\"_col3\"]\n"
operator|+
literal|"<-Map 5 [BROADCAST_EDGE]\n"
operator|+
literal|"                          BROADCAST [RS_42]\n"
operator|+
literal|"                            PartitionCols:_col0\n"
operator|+
literal|"                            Select Operator [SEL_5] (rows=4058 width=140)\n"
operator|+
literal|"                              Output:[\"_col0\"]\n"
operator|+
literal|"                              Filter Operator [FIL_86] (rows=4058 width=140)\n"
operator|+
literal|"                                predicate:(d_moy BETWEEN 4 AND 10 and (d_year = 1998) and d_date_sk is not null)\n"
operator|+
literal|"                                TableScan [TS_3] (rows=73049 width=140)\n"
operator|+
literal|"                                  default@date_dim,d3,Tbl:COMPLETE,Col:NONE,Output:[\"d_date_sk\",\"d_year\",\"d_moy\"]\n"
operator|+
literal|"<-Select Operator [SEL_2] (rows=1441548 width=204)\n"
operator|+
literal|"                            Output:[\"_col0\",\"_col1\",\"_col2\",\"_col3\"]\n"
operator|+
literal|"                            Filter Operator [FIL_85] (rows=1441548 width=204)\n"
operator|+
literal|"                              predicate:(cs_bill_customer_sk is not null and cs_item_sk is not null and cs_sold_date_sk is not null)\n"
operator|+
literal|"                              TableScan [TS_0] (rows=1441548 width=204)\n"
operator|+
literal|"                                default@catalog_sales,catalog_sales,Tbl:COMPLETE,Col:NONE,Output:[\"cs_sold_date_sk\",\"cs_bill_customer_sk\",\"cs_item_sk\",\"cs_net_profit\"]\n"
operator|+
literal|"<-Reducer 7 [SIMPLE_EDGE]\n"
operator|+
literal|"                      SHUFFLE [RS_45]\n"
operator|+
literal|"                        PartitionCols:_col17, _col16\n"
operator|+
literal|"                        Select Operator [SEL_40] (rows=4217197 width=133)\n"
operator|+
literal|"                          Output:[\"_col1\",\"_col2\",\"_col4\",\"_col5\",\"_col11\",\"_col16\",\"_col17\",\"_col19\"]\n"
operator|+
literal|"                          Map Join Operator [MAPJOIN_98] (rows=4217197 width=133)\n"
operator|+
literal|"                            Conds:MAPJOIN_97._col3=RS_38._col0(Inner),HybridGraceHashJoin:true,Output:[\"_col5\",\"_col10\",\"_col11\",\"_col13\",\"_col18\",\"_col19\",\"_col21\",\"_col22\"]\n"
operator|+
literal|"<-Map 12 [BROADCAST_EDGE]\n"
operator|+
literal|"                            BROADCAST [RS_38]\n"
operator|+
literal|"                              PartitionCols:_col0\n"
operator|+
literal|"                              Select Operator [SEL_27] (rows=12 width=261)\n"
operator|+
literal|"                                Output:[\"_col0\",\"_col1\",\"_col2\"]\n"
operator|+
literal|"                                Filter Operator [FIL_92] (rows=12 width=261)\n"
operator|+
literal|"                                  predicate:s_store_sk is not null\n"
operator|+
literal|"                                  TableScan [TS_25] (rows=12 width=261)\n"
operator|+
literal|"                                    default@store,store,Tbl:COMPLETE,Col:NONE,Output:[\"s_store_sk\",\"s_store_id\",\"s_store_name\"]\n"
operator|+
literal|"<-Map Join Operator [MAPJOIN_97] (rows=3833816 width=133)\n"
operator|+
literal|"                              Conds:MERGEJOIN_96._col1=RS_35._col0(Inner),HybridGraceHashJoin:true,Output:[\"_col3\",\"_col5\",\"_col10\",\"_col11\",\"_col13\",\"_col18\",\"_col19\"]\n"
operator|+
literal|"<-Map 11 [BROADCAST_EDGE]\n"
operator|+
literal|"                              BROADCAST [RS_35]\n"
operator|+
literal|"                                PartitionCols:_col0\n"
operator|+
literal|"                                Select Operator [SEL_24] (rows=18000 width=279)\n"
operator|+
literal|"                                  Output:[\"_col0\",\"_col1\",\"_col2\"]\n"
operator|+
literal|"                                  Filter Operator [FIL_91] (rows=18000 width=279)\n"
operator|+
literal|"                                    predicate:i_item_sk is not null\n"
operator|+
literal|"                                    TableScan [TS_22] (rows=18000 width=279)\n"
operator|+
literal|"                                      default@item,item,Tbl:COMPLETE,Col:NONE,Output:[\"i_item_sk\",\"i_item_id\",\"i_item_desc\"]\n"
operator|+
literal|"<-Merge Join Operator [MERGEJOIN_96] (rows=3485288 width=133)\n"
operator|+
literal|"                                Conds:RS_31._col1, _col2, _col4=RS_32._col1, _col2, _col3(Inner),Output:[\"_col1\",\"_col3\",\"_col5\",\"_col10\",\"_col11\",\"_col13\"]\n"
operator|+
literal|"<-Map 6 [SIMPLE_EDGE]\n"
operator|+
literal|"                                SHUFFLE [RS_31]\n"
operator|+
literal|"                                  PartitionCols:_col1, _col2, _col4\n"
operator|+
literal|"                                  Map Join Operator [MAPJOIN_94] (rows=3168444 width=133)\n"
operator|+
literal|"                                    Conds:SEL_8._col0=RS_29._col0(Inner),HybridGraceHashJoin:true,Output:[\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\"]\n"
operator|+
literal|"<-Map 8 [BROADCAST_EDGE]\n"
operator|+
literal|"                                    BROADCAST [RS_29]\n"
operator|+
literal|"                                      PartitionCols:_col0\n"
operator|+
literal|"                                      Select Operator [SEL_11] (rows=18262 width=140)\n"
operator|+
literal|"                                        Output:[\"_col0\"]\n"
operator|+
literal|"                                        Filter Operator [FIL_88] (rows=18262 width=140)\n"
operator|+
literal|"                                          predicate:((d_moy = 4) and (d_year = 1998) and d_date_sk is not null)\n"
operator|+
literal|"                                          TableScan [TS_9] (rows=73049 width=140)\n"
operator|+
literal|"                                            default@date_dim,d1,Tbl:COMPLETE,Col:NONE,Output:[\"d_date_sk\",\"d_year\",\"d_moy\"]\n"
operator|+
literal|"<-Select Operator [SEL_8] (rows=2880404 width=133)\n"
operator|+
literal|"                                      Output:[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\",\"_col5\"]\n"
operator|+
literal|"                                      Filter Operator [FIL_87] (rows=2880404 width=133)\n"
operator|+
literal|"                                        predicate:(ss_item_sk is not null and ss_customer_sk is not null and ss_ticket_number is not null and ss_sold_date_sk is not null and ss_store_sk is not null)\n"
operator|+
literal|"                                        TableScan [TS_6] (rows=2880404 width=133)\n"
operator|+
literal|"                                          default@store_sales,store_sales,Tbl:COMPLETE,Col:NONE,Output:[\"ss_sold_date_sk\",\"ss_item_sk\",\"ss_customer_sk\",\"ss_store_sk\",\"ss_ticket_number\",\"ss_net_profit\"]\n"
operator|+
literal|"<-Map 9 [SIMPLE_EDGE]\n"
operator|+
literal|"                                SHUFFLE [RS_32]\n"
operator|+
literal|"                                  PartitionCols:_col1, _col2, _col3\n"
operator|+
literal|"                                  Map Join Operator [MAPJOIN_95] (rows=316265 width=112)\n"
operator|+
literal|"                                    Conds:SEL_14._col0=RS_19._col0(Inner),HybridGraceHashJoin:true,Output:[\"_col1\",\"_col2\",\"_col3\",\"_col4\"]\n"
operator|+
literal|"<-Map 10 [BROADCAST_EDGE]\n"
operator|+
literal|"                                    BROADCAST [RS_19]\n"
operator|+
literal|"                                      PartitionCols:_col0\n"
operator|+
literal|"                                      Select Operator [SEL_17] (rows=4058 width=140)\n"
operator|+
literal|"                                        Output:[\"_col0\"]\n"
operator|+
literal|"                                        Filter Operator [FIL_90] (rows=4058 width=140)\n"
operator|+
literal|"                                          predicate:(d_moy BETWEEN 4 AND 10 and (d_year = 1998) and d_date_sk is not null)\n"
operator|+
literal|"                                          TableScan [TS_15] (rows=73049 width=140)\n"
operator|+
literal|"                                            default@date_dim,d2,Tbl:COMPLETE,Col:NONE,Output:[\"d_date_sk\",\"d_year\",\"d_moy\"]\n"
operator|+
literal|"<-Select Operator [SEL_14] (rows=287514 width=112)\n"
operator|+
literal|"                                      Output:[\"_col0\",\"_col1\",\"_col2\",\"_col3\",\"_col4\"]\n"
operator|+
literal|"                                      Filter Operator [FIL_89] (rows=287514 width=112)\n"
operator|+
literal|"                                        predicate:(sr_item_sk is not null and sr_customer_sk is not null and sr_ticket_number is not null and sr_returned_date_sk is not null)\n"
operator|+
literal|"                                        TableScan [TS_12] (rows=287514 width=112)\n"
operator|+
literal|"                                          default@store_returns,store_returns,Tbl:COMPLETE,Col:NONE,Output:[\"sr_returned_date_sk\",\"sr_item_sk\",\"sr_customer_sk\",\"sr_ticket_number\",\"sr_net_loss\"]\n"
operator|+
literal|"\n"
operator|+
literal|""
argument_list|,
name|content
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

