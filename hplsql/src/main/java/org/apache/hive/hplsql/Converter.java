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
package|;
end_package

begin_comment
comment|/**  * On-the-fly SQL Converter  */
end_comment

begin_class
specifier|public
class|class
name|Converter
block|{
name|Exec
name|exec
decl_stmt|;
name|boolean
name|trace
init|=
literal|false
decl_stmt|;
name|Converter
parameter_list|(
name|Exec
name|e
parameter_list|)
block|{
name|exec
operator|=
name|e
expr_stmt|;
name|trace
operator|=
name|exec
operator|.
name|getTrace
argument_list|()
expr_stmt|;
block|}
comment|/**    * Convert a data type    */
name|String
name|dataType
parameter_list|(
name|HplsqlParser
operator|.
name|DtypeContext
name|type
parameter_list|,
name|HplsqlParser
operator|.
name|Dtype_lenContext
name|len
parameter_list|)
block|{
name|String
name|t
init|=
name|exec
operator|.
name|getText
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|boolean
name|enclosed
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|t
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'['
condition|)
block|{
name|t
operator|=
name|t
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|t
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|enclosed
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"BIT"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"TINYINT"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INT"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INTEGER"
argument_list|)
condition|)
block|{
comment|// MySQL can use INT(n)
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INT2"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"SMALLINT"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INT4"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"INT"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"INT8"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"BIGINT"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"DATETIME"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"SMALLDATETIME"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"TIMESTAMP"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"VARCHAR"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NVARCHAR"
argument_list|)
operator|)
operator|&&
name|len
operator|.
name|T_MAX
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|t
operator|=
literal|"STRING"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"VARCHAR2"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NCHAR"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NVARCHAR"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"TEXT"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"STRING"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NUMBER"
argument_list|)
operator|||
name|t
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NUMERIC"
argument_list|)
condition|)
block|{
name|t
operator|=
literal|"DECIMAL"
expr_stmt|;
if|if
condition|(
name|len
operator|!=
literal|null
condition|)
block|{
name|t
operator|+=
name|exec
operator|.
name|getText
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|len
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|enclosed
condition|)
block|{
return|return
name|exec
operator|.
name|getText
argument_list|(
name|type
argument_list|,
name|type
operator|.
name|getStart
argument_list|()
argument_list|,
name|len
operator|.
name|getStop
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|t
operator|+
name|exec
operator|.
name|getText
argument_list|(
name|len
argument_list|,
name|len
operator|.
name|getStart
argument_list|()
argument_list|,
name|len
operator|.
name|getStop
argument_list|()
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|enclosed
condition|)
block|{
return|return
name|exec
operator|.
name|getText
argument_list|(
name|type
argument_list|,
name|type
operator|.
name|getStart
argument_list|()
argument_list|,
name|type
operator|.
name|getStop
argument_list|()
argument_list|)
return|;
block|}
return|return
name|t
return|;
block|}
block|}
end_class

end_unit

