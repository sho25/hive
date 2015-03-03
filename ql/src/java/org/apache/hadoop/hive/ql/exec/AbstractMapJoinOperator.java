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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|persistence
operator|.
name|RowContainer
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
name|metadata
operator|.
name|HiveException
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
name|MapJoinDesc
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
name|api
operator|.
name|OperatorType
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
specifier|public
specifier|abstract
class|class
name|AbstractMapJoinOperator
parameter_list|<
name|T
extends|extends
name|MapJoinDesc
parameter_list|>
extends|extends
name|CommonJoinOperator
argument_list|<
name|T
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * The expressions for join inputs's join keys.    */
specifier|protected
specifier|transient
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|joinKeys
decl_stmt|;
comment|/**    * The ObjectInspectors for the join inputs's join keys.    */
specifier|protected
specifier|transient
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|joinKeysObjectInspectors
decl_stmt|;
specifier|protected
specifier|transient
name|byte
name|posBigTable
init|=
operator|-
literal|1
decl_stmt|;
comment|// pos of driver alias
specifier|protected
specifier|transient
name|RowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|emptyList
init|=
literal|null
decl_stmt|;
specifier|transient
name|int
name|numMapRowsRead
decl_stmt|;
specifier|public
name|AbstractMapJoinOperator
parameter_list|()
block|{   }
specifier|public
name|AbstractMapJoinOperator
parameter_list|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|mjop
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|CommonJoinOperator
operator|)
name|mjop
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|conf
operator|.
name|getGenJoinKeys
argument_list|()
condition|)
block|{
name|int
name|tagLen
init|=
name|conf
operator|.
name|getTagLength
argument_list|()
decl_stmt|;
name|joinKeys
operator|=
operator|new
name|List
index|[
name|tagLen
index|]
expr_stmt|;
name|JoinUtil
operator|.
name|populateJoinKeyValue
argument_list|(
name|joinKeys
argument_list|,
name|conf
operator|.
name|getKeys
argument_list|()
argument_list|,
name|NOTSKIPBIGTABLE
argument_list|)
expr_stmt|;
name|joinKeysObjectInspectors
operator|=
name|JoinUtil
operator|.
name|getObjectInspectorsFromEvaluators
argument_list|(
name|joinKeys
argument_list|,
name|inputObjInspectors
argument_list|,
name|NOTSKIPBIGTABLE
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|numMapRowsRead
operator|=
literal|0
expr_stmt|;
comment|// all other tables are small, and are cached in the hash table
name|posBigTable
operator|=
operator|(
name|byte
operator|)
name|conf
operator|.
name|getPosBigTable
argument_list|()
expr_stmt|;
name|emptyList
operator|=
operator|new
name|RowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|,
name|hconf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|RowContainer
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|bigPosRC
init|=
name|JoinUtil
operator|.
name|getRowContainer
argument_list|(
name|hconf
argument_list|,
name|rowContainerStandardObjectInspectors
index|[
name|posBigTable
index|]
argument_list|,
name|posBigTable
argument_list|,
name|joinCacheSize
argument_list|,
name|spillTableDesc
argument_list|,
name|conf
argument_list|,
operator|!
name|hasFilter
argument_list|(
name|posBigTable
argument_list|)
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
name|storage
index|[
name|posBigTable
index|]
operator|=
name|bigPosRC
expr_stmt|;
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|getValueObjectInspectors
parameter_list|(
name|byte
name|alias
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|aliasToObjectInspectors
parameter_list|)
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|inspectors
init|=
name|aliasToObjectInspectors
index|[
name|alias
index|]
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|retained
init|=
name|conf
operator|.
name|getRetainList
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|inspectors
operator|.
name|size
argument_list|()
operator|==
name|retained
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|inspectors
return|;
block|}
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|retainedOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|index
range|:
name|retained
control|)
block|{
name|retainedOIs
operator|.
name|add
argument_list|(
name|inspectors
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|retainedOIs
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|MAPJOIN
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|emptyList
operator|=
literal|null
expr_stmt|;
name|joinKeys
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

