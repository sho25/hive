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
name|hbase
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
name|hbase
operator|.
name|mapreduce
operator|.
name|TableMapReduceUtil
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
name|plan
operator|.
name|ExprNodeDesc
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
name|plan
operator|.
name|TableDesc
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
name|Deserializer
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
name|SerDeException
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractHBaseKeyFactory
implements|implements
name|HBaseKeyFactory
block|{
specifier|protected
name|HBaseSerDeParameters
name|hbaseParams
decl_stmt|;
specifier|protected
name|ColumnMappings
operator|.
name|ColumnMapping
name|keyMapping
decl_stmt|;
specifier|protected
name|Properties
name|properties
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|HBaseSerDeParameters
name|hbaseParam
parameter_list|,
name|Properties
name|properties
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
operator|.
name|hbaseParams
operator|=
name|hbaseParam
expr_stmt|;
name|this
operator|.
name|keyMapping
operator|=
name|hbaseParam
operator|.
name|getKeyColumnMapping
argument_list|()
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|TableDesc
name|tableDesc
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|IOException
block|{
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|jobConf
argument_list|,
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DecomposedPredicate
name|decomposePredicate
parameter_list|(
name|JobConf
name|jobConf
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
return|return
name|HBaseStorageHandler
operator|.
name|decomposePredicate
argument_list|(
name|jobConf
argument_list|,
operator|(
name|HBaseSerDe
operator|)
name|deserializer
argument_list|,
name|predicate
argument_list|)
return|;
block|}
block|}
end_class

end_unit

