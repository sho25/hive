begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_comment
comment|/**  * Constants class for constants used in HBase storage handler.  */
end_comment

begin_class
class|class
name|HBaseConstants
block|{
comment|/** key used to store write transaction object */
specifier|public
specifier|static
specifier|final
name|String
name|PROPERTY_WRITE_TXN_KEY
init|=
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
operator|+
literal|".hbase.mapreduce.writeTxn"
decl_stmt|;
comment|/** key used to define the name of the table to write to */
specifier|public
specifier|static
specifier|final
name|String
name|PROPERTY_OUTPUT_TABLE_NAME_KEY
init|=
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
operator|+
literal|".hbase.mapreduce.outputTableName"
decl_stmt|;
comment|/** key used to define whether bulk storage output format will be used or not  */
specifier|public
specifier|static
specifier|final
name|String
name|PROPERTY_BULK_OUTPUT_MODE_KEY
init|=
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
operator|+
literal|".hbase.output.bulkMode"
decl_stmt|;
comment|/** key used to define the hbase table snapshot. */
specifier|public
specifier|static
specifier|final
name|String
name|PROPERTY_TABLE_SNAPSHOT_KEY
init|=
name|HCatConstants
operator|.
name|HCAT_DEFAULT_TOPIC_PREFIX
operator|+
literal|"hbase.table.snapshot"
decl_stmt|;
block|}
end_class

end_unit

