begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|Stack
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
name|conf
operator|.
name|HiveConf
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
name|ErrorMsg
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
name|MapJoinOperator
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|parse
operator|.
name|ParseContext
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
name|parse
operator|.
name|QBJoinTree
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
name|parse
operator|.
name|SemanticException
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

begin_class
specifier|public
class|class
name|BucketMapjoinProc
extends|extends
name|AbstractBucketJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|BucketMapjoinProc
parameter_list|(
name|ParseContext
name|pGraphContext
parameter_list|)
block|{
name|super
argument_list|(
name|pGraphContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|BucketJoinProcCtx
name|context
init|=
operator|(
name|BucketJoinProcCtx
operator|)
name|procCtx
decl_stmt|;
name|MapJoinOperator
name|mapJoinOperator
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
comment|// can the mapjoin present be converted to a bucketed mapjoin
name|boolean
name|convert
init|=
name|canConvertMapJoinToBucketMapJoin
argument_list|(
name|mapJoinOperator
argument_list|,
name|context
argument_list|)
decl_stmt|;
name|HiveConf
name|conf
init|=
name|context
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// Throw an error if the user asked for bucketed mapjoin to be enforced and
comment|// bucketed mapjoin cannot be performed
if|if
condition|(
operator|!
name|convert
operator|&&
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEENFORCEBUCKETMAPJOIN
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|BUCKET_MAPJOIN_NOT_POSSIBLE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|convert
condition|)
block|{
comment|// convert the mapjoin to a bucketized mapjoin
name|convertMapJoinToBucketMapJoin
argument_list|(
name|mapJoinOperator
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Check if a mapjoin can be converted to a bucket mapjoin,    * and do the version if possible.    */
specifier|public
specifier|static
name|void
name|checkAndConvertBucketMapJoin
parameter_list|(
name|ParseContext
name|pGraphContext
parameter_list|,
name|MapJoinOperator
name|mapJoinOp
parameter_list|,
name|String
name|baseBigAlias
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|joinAliases
parameter_list|)
throws|throws
name|SemanticException
block|{
name|BucketJoinProcCtx
name|ctx
init|=
operator|new
name|BucketJoinProcCtx
argument_list|(
name|pGraphContext
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|BucketMapjoinProc
name|proc
init|=
operator|new
name|BucketMapjoinProc
argument_list|(
name|pGraphContext
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keysMap
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
decl_stmt|;
if|if
condition|(
name|proc
operator|.
name|checkConvertBucketMapJoin
argument_list|(
name|ctx
argument_list|,
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasToOpInfo
argument_list|()
argument_list|,
name|keysMap
argument_list|,
name|baseBigAlias
argument_list|,
name|joinAliases
argument_list|)
condition|)
block|{
name|proc
operator|.
name|convertMapJoinToBucketMapJoin
argument_list|(
name|mapJoinOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

