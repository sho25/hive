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
name|parse
package|;
end_package

begin_comment
comment|/**  * Enumeration that encapsulates the various event types that are seen  * while processing the parse tree (in an implementation of the ParseTreeProcessor).  * These event types are used to register the different event processors with  * the parse tree processor.  *  */
end_comment

begin_enum
specifier|public
enum|enum
name|ASTEvent
block|{
comment|/** 	 * Query event 	 */
name|QUERY
argument_list|(
literal|"QUERY"
argument_list|)
block|,
comment|/** 	 * Union 	 */
name|UNION
argument_list|(
literal|"UNION"
argument_list|)
block|,
comment|/** 	 * Source Table (table in the from clause) 	 */
name|SRC_TABLE
argument_list|(
literal|"SRC_TABLE"
argument_list|)
block|,
comment|/** 	 * Any type of Destination (this fires for hdfs directory, local directory and table) 	 */
name|DESTINATION
argument_list|(
literal|"DESTINATION"
argument_list|)
block|,
comment|/** 	 * Select clause 	 */
name|SELECT_CLAUSE
argument_list|(
literal|"SELECT_CLAUSE"
argument_list|)
block|,
comment|/** 	 * Join clause 	 */
name|JOIN_CLAUSE
argument_list|(
literal|"JOIN_CLAUSE"
argument_list|)
block|,
comment|/** 	 * Where clause 	 */
name|WHERE_CLAUSE
argument_list|(
literal|"WHERE_CLAUSE"
argument_list|)
block|,
comment|/** 	 * CLusterby clause 	 */
name|CLUSTERBY_CLAUSE
argument_list|(
literal|"CLUSTERBY_CLAUSE"
argument_list|)
block|,
comment|/** 	 * Group by clause 	 */
name|GROUPBY_CLAUSE
argument_list|(
literal|"GROUPBY_CLAUSE"
argument_list|)
block|,
comment|/** 	 * Limit clause 	 */
name|LIMIT_CLAUSE
argument_list|(
literal|"LIMIT_CLAUSE"
argument_list|)
block|,
comment|/** 	 * Subquery 	 */
name|SUBQUERY
argument_list|(
literal|"SUBQUERY"
argument_list|)
block|;
comment|/** 	 * The name of the event (string representation of the event) 	 */
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
comment|/** 	 * Constructs the event 	 *  	 * @param name The name(String representation of the event) 	 */
name|ASTEvent
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/** 	 * String representation of the event 	 *  	 * @return String 	 */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
end_enum

end_unit

