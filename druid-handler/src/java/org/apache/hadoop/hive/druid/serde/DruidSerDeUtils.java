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
name|druid
operator|.
name|serde
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
name|serde
operator|.
name|serdeConstants
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
name|serde2
operator|.
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Utils class for Druid SerDe.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|DruidSerDeUtils
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|DruidSerDeUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|FLOAT_TYPE
init|=
literal|"FLOAT"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|LONG_TYPE
init|=
literal|"LONG"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|STRING_TYPE
init|=
literal|"STRING"
decl_stmt|;
comment|/* This method converts from the String representation of Druid type    * to the corresponding Hive type */
specifier|public
specifier|static
name|PrimitiveTypeInfo
name|convertDruidToHiveType
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
name|typeName
operator|=
name|typeName
operator|.
name|toUpperCase
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|typeName
condition|)
block|{
case|case
name|FLOAT_TYPE
case|:
return|return
name|TypeInfoFactory
operator|.
name|floatTypeInfo
return|;
case|case
name|LONG_TYPE
case|:
return|return
name|TypeInfoFactory
operator|.
name|longTypeInfo
return|;
case|case
name|STRING_TYPE
case|:
return|return
name|TypeInfoFactory
operator|.
name|stringTypeInfo
return|;
default|default:
comment|// This is a guard for special Druid types e.g. hyperUnique
comment|// (http://druid.io/docs/0.9.1.1/querying/aggregations.html#hyperunique-aggregator).
comment|// Currently, we do not support doing anything special with them in Hive.
comment|// However, those columns are there, and they can be actually read as normal
comment|// dimensions e.g. with a select query. Thus, we print the warning and just read them
comment|// as String.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Transformation to STRING for unknown type "
operator|+
name|typeName
argument_list|)
expr_stmt|;
return|return
name|TypeInfoFactory
operator|.
name|stringTypeInfo
return|;
block|}
block|}
comment|/* This method converts from the String representation of Druid type    * to the String representation of the corresponding Hive type */
specifier|public
specifier|static
name|String
name|convertDruidToHiveTypeString
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
name|typeName
operator|=
name|typeName
operator|.
name|toUpperCase
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|typeName
condition|)
block|{
case|case
name|FLOAT_TYPE
case|:
return|return
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
return|;
case|case
name|LONG_TYPE
case|:
return|return
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
return|;
case|case
name|STRING_TYPE
case|:
return|return
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
return|;
default|default:
comment|// This is a guard for special Druid types e.g. hyperUnique
comment|// (http://druid.io/docs/0.9.1.1/querying/aggregations.html#hyperunique-aggregator).
comment|// Currently, we do not support doing anything special with them in Hive.
comment|// However, those columns are there, and they can be actually read as normal
comment|// dimensions e.g. with a select query. Thus, we print the warning and just read them
comment|// as String.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Transformation to STRING for unknown type "
operator|+
name|typeName
argument_list|)
expr_stmt|;
return|return
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
return|;
block|}
block|}
block|}
end_class

end_unit

