begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|repl
package|;
end_package

begin_comment
comment|/**  * A class that defines the constant strings used by the replication implementation.  */
end_comment

begin_class
specifier|public
class|class
name|ReplConst
block|{
comment|/**    * The constant that denotes the table data location is changed to different path. This indicates    * Metastore to update corresponding path in Partitions and also need to delete old path.    */
specifier|public
specifier|static
specifier|final
name|String
name|REPL_DATA_LOCATION_CHANGED
init|=
literal|"REPL_DATA_LOCATION_CHANGED"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TRUE
init|=
literal|"true"
decl_stmt|;
comment|/**    * The constant string literal added as a property of database being replicated into. We choose    * this property over other properties is because this property is added right when the    * database is created as part of repl load and survives the incremental cycles.    */
specifier|public
specifier|static
specifier|final
name|String
name|REPL_TARGET_DB_PROPERTY
init|=
literal|"hive.repl.ckpt.key"
decl_stmt|;
comment|/**    * A table which is target of replication will have this property set. The property serves two    * purposes, 1. identifies the tables being replicated into and 2. records the event id of the    * last event affecting this table.    */
specifier|public
specifier|static
specifier|final
name|String
name|REPL_TARGET_TABLE_PROPERTY
init|=
literal|"repl.last.id"
decl_stmt|;
block|}
end_class

end_unit

