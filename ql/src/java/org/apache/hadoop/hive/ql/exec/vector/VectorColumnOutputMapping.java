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
name|ql
operator|.
name|exec
operator|.
name|vector
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|VectorColumnOrderedMap
operator|.
name|Mapping
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
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * This class collects column information for copying a row from one VectorizedRowBatch to  * same/another batch.  *  * In this variation, column information is ordered by the output column number.  */
end_comment

begin_class
specifier|public
class|class
name|VectorColumnOutputMapping
extends|extends
name|VectorColumnMapping
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|VectorColumnOutputMapping
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|int
name|sourceColumn
parameter_list|,
name|int
name|outputColumn
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
comment|// Order on outputColumn.
name|vectorColumnMapping
operator|.
name|add
argument_list|(
name|outputColumn
argument_list|,
name|sourceColumn
argument_list|,
name|typeInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|containsOutputColumn
parameter_list|(
name|int
name|outputColumn
parameter_list|)
block|{
return|return
name|vectorColumnMapping
operator|.
name|orderedColumnsContain
argument_list|(
name|outputColumn
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finalize
parameter_list|()
block|{
if|if
condition|(
name|vectorColumnMapping
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|Mapping
name|mapping
init|=
name|vectorColumnMapping
operator|.
name|getMapping
argument_list|()
decl_stmt|;
comment|// Ordered columns are the output columns.
name|sourceColumns
operator|=
name|mapping
operator|.
name|getValueColumns
argument_list|()
expr_stmt|;
name|outputColumns
operator|=
name|mapping
operator|.
name|getOrderedColumns
argument_list|()
expr_stmt|;
name|typeInfos
operator|=
name|mapping
operator|.
name|getTypeInfos
argument_list|()
expr_stmt|;
comment|// Not needed anymore.
name|vectorColumnMapping
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

