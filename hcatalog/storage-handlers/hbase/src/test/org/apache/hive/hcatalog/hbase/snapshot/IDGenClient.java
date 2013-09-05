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
operator|.
name|snapshot
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_class
specifier|public
class|class
name|IDGenClient
extends|extends
name|Thread
block|{
name|String
name|connectionStr
decl_stmt|;
name|String
name|base_dir
decl_stmt|;
name|ZKUtil
name|zkutil
decl_stmt|;
name|Random
name|sleepTime
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|int
name|runtime
decl_stmt|;
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|idMap
decl_stmt|;
name|String
name|tableName
decl_stmt|;
name|IDGenClient
parameter_list|(
name|String
name|connectionStr
parameter_list|,
name|String
name|base_dir
parameter_list|,
name|int
name|time
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|connectionStr
operator|=
name|connectionStr
expr_stmt|;
name|this
operator|.
name|base_dir
operator|=
name|base_dir
expr_stmt|;
name|this
operator|.
name|zkutil
operator|=
operator|new
name|ZKUtil
argument_list|(
name|connectionStr
argument_list|,
name|base_dir
argument_list|)
expr_stmt|;
name|this
operator|.
name|runtime
operator|=
name|time
expr_stmt|;
name|idMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
comment|/*      * @see java.lang.Runnable#run()      */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|int
name|timeElapsed
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|timeElapsed
operator|<=
name|runtime
condition|)
block|{
try|try
block|{
name|long
name|id
init|=
name|zkutil
operator|.
name|nextId
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|idMap
operator|.
name|put
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|int
name|sTime
init|=
name|sleepTime
operator|.
name|nextInt
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sTime
operator|*
literal|100
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|timeElapsed
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
operator|(
name|double
operator|)
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|>
name|getIdMap
parameter_list|()
block|{
return|return
name|idMap
return|;
block|}
block|}
end_class

end_unit

