begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|spark
package|;
end_package

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
name|ArrayList
import|;
end_import

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
name|Set
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
name|fs
operator|.
name|Path
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
name|Context
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
name|Utilities
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
name|mr
operator|.
name|ExecMapper
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
name|io
operator|.
name|HiveInputFormat
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
name|BaseWork
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
name|MapWork
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
name|ReduceWork
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
name|SparkEdgeProperty
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
name|SparkWork
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
name|io
operator|.
name|BytesWritable
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
name|io
operator|.
name|Writable
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
name|io
operator|.
name|WritableComparable
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
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
import|;
end_import

begin_class
specifier|public
class|class
name|SparkPlanGenerator
block|{
specifier|private
name|JavaSparkContext
name|sc
decl_stmt|;
specifier|private
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
name|Context
name|context
decl_stmt|;
specifier|private
name|Path
name|scratchDir
decl_stmt|;
specifier|public
name|SparkPlanGenerator
parameter_list|(
name|JavaSparkContext
name|sc
parameter_list|,
name|Context
name|context
parameter_list|,
name|JobConf
name|jobConf
parameter_list|,
name|Path
name|scratchDir
parameter_list|)
block|{
name|this
operator|.
name|sc
operator|=
name|sc
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
name|this
operator|.
name|scratchDir
operator|=
name|scratchDir
expr_stmt|;
block|}
specifier|public
name|SparkPlan
name|generate
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
name|SparkPlan
name|plan
init|=
operator|new
name|SparkPlan
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|SparkTran
argument_list|>
name|trans
init|=
operator|new
name|ArrayList
argument_list|<
name|SparkTran
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|roots
init|=
name|sparkWork
operator|.
name|getRoots
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|roots
operator|!=
literal|null
operator|&&
name|roots
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
assert|;
name|BaseWork
name|w
init|=
name|roots
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|MapWork
name|mapWork
init|=
operator|(
name|MapWork
operator|)
name|w
decl_stmt|;
name|trans
operator|.
name|add
argument_list|(
name|generate
argument_list|(
name|w
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|BaseWork
name|child
init|=
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|w
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SparkEdgeProperty
name|edge
init|=
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|w
argument_list|,
name|child
argument_list|)
decl_stmt|;
name|trans
operator|.
name|add
argument_list|(
name|generate
argument_list|(
name|edge
argument_list|)
argument_list|)
expr_stmt|;
name|trans
operator|.
name|add
argument_list|(
name|generate
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|=
name|child
expr_stmt|;
block|}
name|ChainedTran
name|chainedTran
init|=
operator|new
name|ChainedTran
argument_list|(
name|trans
argument_list|)
decl_stmt|;
name|plan
operator|.
name|setTran
argument_list|(
name|chainedTran
argument_list|)
expr_stmt|;
name|JavaPairRDD
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
name|input
init|=
name|generateRDD
argument_list|(
name|mapWork
argument_list|)
decl_stmt|;
name|plan
operator|.
name|setInput
argument_list|(
name|input
argument_list|)
expr_stmt|;
return|return
name|plan
return|;
block|}
specifier|private
name|JavaPairRDD
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
name|generateRDD
parameter_list|(
name|MapWork
name|mapWork
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|inputPaths
init|=
name|Utilities
operator|.
name|getInputPaths
argument_list|(
name|jobConf
argument_list|,
name|mapWork
argument_list|,
name|scratchDir
argument_list|,
name|context
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|setInputPaths
argument_list|(
name|jobConf
argument_list|,
name|inputPaths
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|jobConf
argument_list|,
name|mapWork
argument_list|,
name|scratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Class
name|ifClass
init|=
name|HiveInputFormat
operator|.
name|class
decl_stmt|;
comment|// The mapper class is expected by the HiveInputFormat.
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.mapper.class"
argument_list|,
name|ExecMapper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sc
operator|.
name|hadoopRDD
argument_list|(
name|jobConf
argument_list|,
name|ifClass
argument_list|,
name|WritableComparable
operator|.
name|class
argument_list|,
name|Writable
operator|.
name|class
argument_list|)
return|;
block|}
specifier|private
name|SparkTran
name|generate
parameter_list|(
name|BaseWork
name|bw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bw
operator|instanceof
name|MapWork
condition|)
block|{
return|return
name|generate
argument_list|(
operator|(
name|MapWork
operator|)
name|bw
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|bw
operator|instanceof
name|ReduceWork
condition|)
block|{
return|return
name|generate
argument_list|(
operator|(
name|ReduceWork
operator|)
name|bw
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only MapWork and ReduceWork are expected"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|MapTran
name|generate
parameter_list|(
name|MapWork
name|mw
parameter_list|)
throws|throws
name|IOException
block|{
name|MapTran
name|result
init|=
operator|new
name|MapTran
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|setMapWork
argument_list|(
name|jobConf
argument_list|,
name|mw
argument_list|,
name|scratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|createTmpDirs
argument_list|(
name|jobConf
argument_list|,
name|mw
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.mapper.class"
argument_list|,
name|ExecMapper
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|confBytes
init|=
name|KryoSerializer
operator|.
name|serializeJobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|HiveMapFunction
name|mapFunc
init|=
operator|new
name|HiveMapFunction
argument_list|(
name|confBytes
argument_list|)
decl_stmt|;
name|result
operator|.
name|setMapFunction
argument_list|(
name|mapFunc
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|ShuffleTran
name|generate
parameter_list|(
name|SparkEdgeProperty
name|edge
parameter_list|)
block|{
comment|// TODO: based on edge type, create groupBy or sortBy transformations.
return|return
operator|new
name|ShuffleTran
argument_list|()
return|;
block|}
specifier|private
name|ReduceTran
name|generate
parameter_list|(
name|ReduceWork
name|rw
parameter_list|)
throws|throws
name|IOException
block|{
name|ReduceTran
name|result
init|=
operator|new
name|ReduceTran
argument_list|()
decl_stmt|;
name|Utilities
operator|.
name|setReduceWork
argument_list|(
name|jobConf
argument_list|,
name|rw
argument_list|,
name|scratchDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|createTmpDirs
argument_list|(
name|jobConf
argument_list|,
name|rw
argument_list|)
expr_stmt|;
name|byte
index|[]
name|confBytes
init|=
name|KryoSerializer
operator|.
name|serializeJobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|HiveReduceFunction
name|mapFunc
init|=
operator|new
name|HiveReduceFunction
argument_list|(
name|confBytes
argument_list|)
decl_stmt|;
name|result
operator|.
name|setReduceFunction
argument_list|(
name|mapFunc
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

