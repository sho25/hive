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
name|optimizer
operator|.
name|calcite
operator|.
name|stats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|ReflectiveRelMetadataProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMdSize
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|metadata
operator|.
name|RelMetadataProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|type
operator|.
name|RelDataTypeField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|BuiltInMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_class
specifier|public
class|class
name|HiveRelMdSize
extends|extends
name|RelMdSize
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveRelMdSize
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HiveRelMdSize
name|INSTANCE
init|=
operator|new
name|HiveRelMdSize
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|RelMetadataProvider
name|SOURCE
init|=
name|ReflectiveRelMetadataProvider
operator|.
name|reflectiveSource
argument_list|(
name|INSTANCE
argument_list|,
name|BuiltInMethod
operator|.
name|AVERAGE_COLUMN_SIZES
operator|.
name|method
argument_list|,
name|BuiltInMethod
operator|.
name|AVERAGE_ROW_SIZE
operator|.
name|method
argument_list|)
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
specifier|private
name|HiveRelMdSize
parameter_list|()
block|{}
comment|//~ Methods ----------------------------------------------------------------
specifier|public
name|Double
name|averageTypeValueSize
parameter_list|(
name|RelDataType
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
operator|.
name|getSqlTypeName
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|TINYINT
case|:
return|return
literal|1d
return|;
case|case
name|SMALLINT
case|:
return|return
literal|2d
return|;
case|case
name|INTEGER
case|:
case|case
name|FLOAT
case|:
case|case
name|REAL
case|:
case|case
name|DATE
case|:
case|case
name|TIME
case|:
return|return
literal|4d
return|;
case|case
name|BIGINT
case|:
case|case
name|DOUBLE
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|INTERVAL_DAY_TIME
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
return|return
literal|8d
return|;
case|case
name|BINARY
case|:
return|return
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
return|;
case|case
name|VARBINARY
case|:
return|return
name|Math
operator|.
name|min
argument_list|(
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
argument_list|,
literal|100d
argument_list|)
return|;
case|case
name|CHAR
case|:
return|return
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
operator|*
name|BYTES_PER_CHARACTER
return|;
case|case
name|VARCHAR
case|:
comment|// Even in large (say VARCHAR(2000)) columns most strings are small
return|return
name|Math
operator|.
name|min
argument_list|(
operator|(
name|double
operator|)
name|type
operator|.
name|getPrecision
argument_list|()
operator|*
name|BYTES_PER_CHARACTER
argument_list|,
literal|100d
argument_list|)
return|;
case|case
name|ROW
case|:
name|Double
name|average
init|=
literal|0.0
decl_stmt|;
for|for
control|(
name|RelDataTypeField
name|field
range|:
name|type
operator|.
name|getFieldList
argument_list|()
control|)
block|{
name|average
operator|+=
name|averageTypeValueSize
argument_list|(
name|field
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|average
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

