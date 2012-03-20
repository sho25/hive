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
name|ql
operator|.
name|stats
operator|.
name|jdbc
package|;
end_package

begin_class
specifier|public
specifier|final
class|class
name|JDBCStatsSetupConstants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|PART_STAT_ID_COLUMN_NAME
init|=
literal|"ID"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PART_STAT_TIMESTAMP_COLUMN_NAME
init|=
literal|"TS"
decl_stmt|;
comment|// NOTE:
comment|// For all table names past and future, Hive will not drop old versions of this table, it is up
comment|// to the administrator
specifier|public
specifier|static
specifier|final
name|String
name|PART_STAT_TABLE_NAME
init|=
literal|"PARTITION_STATS_V2"
decl_stmt|;
comment|// supported statistics - column names
specifier|public
specifier|static
specifier|final
name|String
name|PART_STAT_ROW_COUNT_COLUMN_NAME
init|=
literal|"ROW_COUNT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PART_STAT_RAW_DATA_SIZE_COLUMN_NAME
init|=
literal|"RAW_DATA_SIZE"
decl_stmt|;
block|}
end_class

end_unit

