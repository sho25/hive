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
name|udf
operator|.
name|generic
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
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
name|codec
operator|.
name|digest
operator|.
name|DigestUtils
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
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|objectinspector
operator|.
name|ObjectInspector
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"mask_hash"
argument_list|,
name|value
operator|=
literal|"returns hash of the given value"
argument_list|,
name|extended
operator|=
literal|"Examples:\n "
operator|+
literal|"  mask_hash(value)\n "
operator|+
literal|"Arguments:\n "
operator|+
literal|"  value - value to mask. Supported types: STRING, VARCHAR, CHAR"
argument_list|)
specifier|public
class|class
name|GenericUDFMaskHash
extends|extends
name|BaseMaskUDF
block|{
specifier|public
specifier|static
specifier|final
name|String
name|UDF_NAME
init|=
literal|"mask_hash"
decl_stmt|;
specifier|public
name|GenericUDFMaskHash
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|MaskHashTransformer
argument_list|()
argument_list|,
name|UDF_NAME
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_class
class|class
name|MaskHashTransformer
extends|extends
name|AbstractTransformer
block|{
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|startIdx
parameter_list|)
block|{   }
annotation|@
name|Override
name|String
name|transform
parameter_list|(
specifier|final
name|String
name|value
parameter_list|)
block|{
return|return
name|DigestUtils
operator|.
name|md5Hex
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
name|Byte
name|transform
parameter_list|(
specifier|final
name|Byte
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
name|Short
name|transform
parameter_list|(
specifier|final
name|Short
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
name|Integer
name|transform
parameter_list|(
specifier|final
name|Integer
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
name|Long
name|transform
parameter_list|(
specifier|final
name|Long
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
name|Date
name|transform
parameter_list|(
specifier|final
name|Date
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

