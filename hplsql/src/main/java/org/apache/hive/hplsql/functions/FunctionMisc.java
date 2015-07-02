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
name|hive
operator|.
name|hplsql
operator|.
name|functions
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
name|hplsql
operator|.
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|FunctionMisc
extends|extends
name|Function
block|{
specifier|public
name|FunctionMisc
parameter_list|(
name|Exec
name|e
parameter_list|)
block|{
name|super
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
comment|/**     * Register functions    */
annotation|@
name|Override
specifier|public
name|void
name|register
parameter_list|(
name|Function
name|f
parameter_list|)
block|{
name|f
operator|.
name|map
operator|.
name|put
argument_list|(
literal|"COALESCE"
argument_list|,
operator|new
name|FuncCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
name|nvl
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|map
operator|.
name|put
argument_list|(
literal|"DECODE"
argument_list|,
operator|new
name|FuncCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
name|decode
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|map
operator|.
name|put
argument_list|(
literal|"NVL"
argument_list|,
operator|new
name|FuncCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
name|nvl
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|map
operator|.
name|put
argument_list|(
literal|"NVL2"
argument_list|,
operator|new
name|FuncCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
name|nvl2
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|specMap
operator|.
name|put
argument_list|(
literal|"ACTIVITY_COUNT"
argument_list|,
operator|new
name|FuncSpecCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|activityCount
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|specMap
operator|.
name|put
argument_list|(
literal|"CAST"
argument_list|,
operator|new
name|FuncSpecCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|cast
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|specMap
operator|.
name|put
argument_list|(
literal|"CURRENT"
argument_list|,
operator|new
name|FuncSpecCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|current
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|specMap
operator|.
name|put
argument_list|(
literal|"CURRENT_USER"
argument_list|,
operator|new
name|FuncSpecCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|currentUser
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|specMap
operator|.
name|put
argument_list|(
literal|"USER"
argument_list|,
operator|new
name|FuncSpecCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|currentUser
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|f
operator|.
name|specSqlMap
operator|.
name|put
argument_list|(
literal|"CURRENT"
argument_list|,
operator|new
name|FuncSpecCommand
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|currentSql
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * ACTIVITY_COUNT function (built-in variable)    */
name|void
name|activityCount
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|evalInt
argument_list|(
operator|new
name|Long
argument_list|(
name|exec
operator|.
name|getRowCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * CAST function    */
name|void
name|cast
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|ctx
operator|.
name|expr
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|evalNull
argument_list|()
expr_stmt|;
return|return;
block|}
name|String
name|type
init|=
name|ctx
operator|.
name|dtype
argument_list|()
operator|.
name|getText
argument_list|()
decl_stmt|;
name|String
name|len
init|=
literal|null
decl_stmt|;
name|String
name|scale
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|dtype_len
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|len
operator|=
name|ctx
operator|.
name|dtype_len
argument_list|()
operator|.
name|L_INT
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
expr_stmt|;
if|if
condition|(
name|ctx
operator|.
name|dtype_len
argument_list|()
operator|.
name|L_INT
argument_list|(
literal|1
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|scale
operator|=
name|ctx
operator|.
name|dtype_len
argument_list|()
operator|.
name|L_INT
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
block|}
name|Var
name|var
init|=
operator|new
name|Var
argument_list|(
literal|null
argument_list|,
name|type
argument_list|,
name|len
argument_list|,
name|scale
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|var
operator|.
name|cast
argument_list|(
name|evalPop
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|evalVar
argument_list|(
name|var
argument_list|)
expr_stmt|;
block|}
comment|/**    * CURRENT<VALUE> function    */
name|void
name|current
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|ctx
operator|.
name|T_DATE
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|evalVar
argument_list|(
name|FunctionDatetime
operator|.
name|currentDate
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ctx
operator|.
name|T_TIMESTAMP
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|int
name|precision
init|=
name|evalPop
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|3
argument_list|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
name|evalVar
argument_list|(
name|FunctionDatetime
operator|.
name|currentTimestamp
argument_list|(
name|precision
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ctx
operator|.
name|T_USER
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|evalVar
argument_list|(
name|FunctionMisc
operator|.
name|currentUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|evalNull
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * CURRENT<VALUE> function in executable SQL statement    */
name|void
name|currentSql
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|ctx
operator|.
name|T_DATE
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|exec
operator|.
name|getConnectionType
argument_list|()
operator|==
name|Conn
operator|.
name|Type
operator|.
name|HIVE
condition|)
block|{
name|evalString
argument_list|(
literal|"TO_DATE(FROM_UNIXTIME(UNIX_TIMESTAMP()))"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|evalString
argument_list|(
literal|"CURRENT_DATE"
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|ctx
operator|.
name|T_TIMESTAMP
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|exec
operator|.
name|getConnectionType
argument_list|()
operator|==
name|Conn
operator|.
name|Type
operator|.
name|HIVE
condition|)
block|{
name|evalString
argument_list|(
literal|"FROM_UNIXTIME(UNIX_TIMESTAMP())"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|evalString
argument_list|(
literal|"CURRENT_TIMESTAMP"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|evalString
argument_list|(
name|exec
operator|.
name|getFormattedText
argument_list|(
name|ctx
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * CURRENT_USER function    */
name|void
name|currentUser
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_spec_funcContext
name|ctx
parameter_list|)
block|{
name|evalVar
argument_list|(
name|currentUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Var
name|currentUser
parameter_list|()
block|{
return|return
operator|new
name|Var
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * DECODE function    */
name|void
name|decode
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
name|int
name|cnt
init|=
name|ctx
operator|.
name|expr
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|cnt
operator|<
literal|3
condition|)
block|{
name|evalNull
argument_list|()
expr_stmt|;
return|return;
block|}
name|Var
name|value
init|=
name|evalPop
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|i
operator|+
literal|1
operator|<
name|cnt
condition|)
block|{
name|Var
name|when
init|=
name|evalPop
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|value
operator|.
name|isNull
argument_list|()
operator|&&
name|when
operator|.
name|isNull
argument_list|()
operator|)
operator|||
name|value
operator|.
name|equals
argument_list|(
name|when
argument_list|)
condition|)
block|{
name|eval
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|i
operator|+=
literal|2
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|<
name|cnt
condition|)
block|{
comment|// ELSE expression
name|eval
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|evalNull
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * NVL function - Return first non-NULL expression    */
name|void
name|nvl
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ctx
operator|.
name|expr
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Var
name|v
init|=
name|evalPop
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|type
operator|!=
name|Var
operator|.
name|Type
operator|.
name|NULL
condition|)
block|{
name|exec
operator|.
name|stackPush
argument_list|(
name|v
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|evalNull
argument_list|()
expr_stmt|;
block|}
comment|/**    * NVL2 function - If expr1 is not NULL return expr2, otherwise expr3    */
name|void
name|nvl2
parameter_list|(
name|HplsqlParser
operator|.
name|Expr_func_paramsContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|ctx
operator|.
name|expr
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|3
condition|)
block|{
if|if
condition|(
operator|!
name|evalPop
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|isNull
argument_list|()
condition|)
block|{
name|eval
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|eval
argument_list|(
name|ctx
operator|.
name|expr
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|evalNull
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

