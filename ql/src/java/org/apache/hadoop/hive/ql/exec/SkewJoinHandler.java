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
name|FileNotFoundException
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
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
name|fs
operator|.
name|FileSystem
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
name|JoinDesc
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
name|OperatorDesc
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
name|SerDe
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|ObjectInspectorFactory
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
name|StructField
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
name|StructObjectInspector
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
name|LongWritable
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * At runtime in Join, we output big keys in one table into one corresponding  * directories, and all same keys in other tables into different dirs(one for  * each table). The directories will look like:  *<ul>  *<li>  * dir-T1-bigkeys(containing big keys in T1), dir-T2-keys(containing keys which  * is big in T1),dir-T3-keys(containing keys which is big in T1), ...  *<li>  * dir-T1-keys(containing keys which is big in T2), dir-T2-bigkeys(containing  * big keys in T2),dir-T3-keys(containing keys which is big in T2), ...  *<li>  * dir-T1-keys(containing keys which is big in T3), dir-T2-keys(containing big  * keys in T3),dir-T3-bigkeys(containing keys which is big in T3), ... .....  *</ul>  *  *<p>  * For each skew key, we first write all values to a local tmp file. At the time  * of ending the current group, the local tmp file will be uploaded to hdfs.  * Right now, we use one file per skew key.  *  *<p>  * For more info, please see https://issues.apache.org/jira/browse/HIVE-964.  *  */
end_comment

begin_class
specifier|public
class|class
name|SkewJoinHandler
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
name|SkewJoinHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|int
name|currBigKeyTag
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|rowNumber
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|currTag
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|skewKeyDefinition
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|StructObjectInspector
argument_list|>
name|skewKeysTableObjectInspector
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|SerDe
argument_list|>
name|tblSerializers
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|tblDesc
init|=
literal|null
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|Boolean
argument_list|>
name|bigKeysExistingMap
init|=
literal|null
decl_stmt|;
specifier|private
name|LongWritable
name|skewjoinFollowupJobs
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|noOuterJoin
decl_stmt|;
name|Configuration
name|hconf
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|dummyKey
init|=
literal|null
decl_stmt|;
name|String
name|taskId
decl_stmt|;
specifier|private
specifier|final
name|CommonJoinOperator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|joinOp
decl_stmt|;
specifier|private
specifier|final
name|int
name|numAliases
decl_stmt|;
specifier|private
specifier|final
name|JoinDesc
name|conf
decl_stmt|;
specifier|public
name|SkewJoinHandler
parameter_list|(
name|CommonJoinOperator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|joinOp
parameter_list|)
block|{
name|this
operator|.
name|joinOp
operator|=
name|joinOp
expr_stmt|;
name|numAliases
operator|=
name|joinOp
operator|.
name|numAliases
expr_stmt|;
name|conf
operator|=
name|joinOp
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|noOuterJoin
operator|=
name|joinOp
operator|.
name|noOuterJoin
expr_stmt|;
block|}
specifier|public
name|void
name|initiliaze
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
block|{
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|JoinDesc
name|desc
init|=
name|joinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|skewKeyDefinition
operator|=
name|desc
operator|.
name|getSkewKeyDefinition
argument_list|()
expr_stmt|;
name|skewKeysTableObjectInspector
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|StructObjectInspector
argument_list|>
argument_list|(
name|numAliases
argument_list|)
expr_stmt|;
name|tblDesc
operator|=
name|desc
operator|.
name|getSkewKeysValuesTables
argument_list|()
expr_stmt|;
name|tblSerializers
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SerDe
argument_list|>
argument_list|(
name|numAliases
argument_list|)
expr_stmt|;
name|bigKeysExistingMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Boolean
argument_list|>
argument_list|(
name|numAliases
argument_list|)
expr_stmt|;
name|taskId
operator|=
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|Byte
name|alias
init|=
name|conf
operator|.
name|getTagOrder
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|skewTableKeyInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|joinOp
operator|.
name|inputObjInspectors
index|[
name|alias
index|]
decl_stmt|;
name|StructField
name|sf
init|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|keyFields
init|=
operator|(
operator|(
name|StructObjectInspector
operator|)
name|sf
operator|.
name|getFieldObjectInspector
argument_list|()
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|int
name|keyFieldSize
init|=
name|keyFields
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|keyFieldSize
condition|;
name|k
operator|++
control|)
block|{
name|skewTableKeyInspectors
operator|.
name|add
argument_list|(
name|keyFields
operator|.
name|get
argument_list|(
name|k
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TableDesc
name|joinKeyDesc
init|=
name|desc
operator|.
name|getKeyTableDesc
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|keyColNames
init|=
name|Utilities
operator|.
name|getColumnNames
argument_list|(
name|joinKeyDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
decl_stmt|;
name|StructObjectInspector
name|structTblKeyInpector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|keyColNames
argument_list|,
name|skewTableKeyInspectors
argument_list|)
decl_stmt|;
try|try
block|{
name|SerDe
name|serializer
init|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|tblDesc
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|serializer
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|tblDesc
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|tblSerializers
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|,
name|serializer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Skewjoin will be disabled due to "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|joinOp
operator|.
name|handleSkewJoin
operator|=
literal|false
expr_stmt|;
break|break;
block|}
name|TableDesc
name|valTblDesc
init|=
name|JoinUtil
operator|.
name|getSpillTableDesc
argument_list|(
name|alias
argument_list|,
name|joinOp
operator|.
name|spillTableDesc
argument_list|,
name|conf
argument_list|,
name|noOuterJoin
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|valColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|valTblDesc
operator|!=
literal|null
condition|)
block|{
name|valColNames
operator|=
name|Utilities
operator|.
name|getColumnNames
argument_list|(
name|valTblDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|structTblValInpector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|valColNames
argument_list|,
name|joinOp
operator|.
name|joinValuesStandardObjectInspectors
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|StructObjectInspector
name|structTblInpector
init|=
name|ObjectInspectorFactory
operator|.
name|getUnionStructObjectInspector
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StructObjectInspector
index|[]
block|{
name|structTblValInpector
block|,
name|structTblKeyInpector
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|skewKeysTableObjectInspector
operator|.
name|put
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|,
name|structTblInpector
argument_list|)
expr_stmt|;
block|}
comment|// reset rowcontainer's serde, objectinspector, and tableDesc.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|Byte
name|alias
init|=
name|conf
operator|.
name|getTagOrder
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rc
init|=
operator|(
name|RowContainer
operator|)
name|joinOp
operator|.
name|storage
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|rc
operator|!=
literal|null
condition|)
block|{
name|rc
operator|.
name|setSerDe
argument_list|(
name|tblSerializers
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|,
name|skewKeysTableObjectInspector
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|rc
operator|.
name|setTableDesc
argument_list|(
name|tblDesc
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|void
name|endGroup
parameter_list|()
throws|throws
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
name|skewKeyInCurrentGroup
condition|)
block|{
name|String
name|specPath
init|=
name|conf
operator|.
name|getBigKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|currBigKeyTag
argument_list|)
decl_stmt|;
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|bigKey
init|=
operator|(
name|RowContainer
operator|)
name|joinOp
operator|.
name|storage
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|currBigKeyTag
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|outputPath
init|=
name|getOperatorOutputPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|FileSystem
name|destFs
init|=
name|outputPath
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|bigKey
operator|.
name|copyToDFSDirecory
argument_list|(
name|destFs
argument_list|,
name|outputPath
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|(
operator|(
name|byte
operator|)
name|i
operator|)
operator|==
name|currBigKeyTag
condition|)
block|{
continue|continue;
block|}
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|values
init|=
operator|(
name|RowContainer
operator|)
name|joinOp
operator|.
name|storage
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|!=
literal|null
condition|)
block|{
name|specPath
operator|=
name|conf
operator|.
name|getSmallKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|currBigKeyTag
argument_list|)
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
name|values
operator|.
name|copyToDFSDirecory
argument_list|(
name|destFs
argument_list|,
name|getOperatorOutputPath
argument_list|(
name|specPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|skewKeyInCurrentGroup
operator|=
literal|false
expr_stmt|;
block|}
name|boolean
name|skewKeyInCurrentGroup
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|handleSkew
parameter_list|(
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|joinOp
operator|.
name|newGroupStarted
operator|||
name|tag
operator|!=
name|currTag
condition|)
block|{
name|rowNumber
operator|=
literal|0
expr_stmt|;
name|currTag
operator|=
name|tag
expr_stmt|;
block|}
if|if
condition|(
name|joinOp
operator|.
name|newGroupStarted
condition|)
block|{
name|currBigKeyTag
operator|=
operator|-
literal|1
expr_stmt|;
name|joinOp
operator|.
name|newGroupStarted
operator|=
literal|false
expr_stmt|;
name|dummyKey
operator|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|joinOp
operator|.
name|getGroupKeyObject
argument_list|()
expr_stmt|;
name|skewKeyInCurrentGroup
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numAliases
condition|;
name|i
operator|++
control|)
block|{
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rc
init|=
operator|(
name|RowContainer
operator|)
name|joinOp
operator|.
name|storage
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|rc
operator|!=
literal|null
condition|)
block|{
name|rc
operator|.
name|setKeyObject
argument_list|(
name|dummyKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|rowNumber
operator|++
expr_stmt|;
if|if
condition|(
name|currBigKeyTag
operator|==
operator|-
literal|1
operator|&&
operator|(
name|tag
operator|<
name|numAliases
operator|-
literal|1
operator|)
operator|&&
name|rowNumber
operator|>=
name|skewKeyDefinition
condition|)
block|{
comment|// the first time we see a big key. If this key is not in the last
comment|// table (the last table can always be streamed), we define that we get
comment|// a skew key now.
name|currBigKeyTag
operator|=
name|tag
expr_stmt|;
name|updateSkewJoinJobCounter
argument_list|(
name|tag
argument_list|)
expr_stmt|;
comment|// right now we assume that the group by is an ArrayList object. It may
comment|// change in future.
if|if
condition|(
operator|!
operator|(
name|dummyKey
operator|instanceof
name|List
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Bug in handle skew key in a seperate job."
argument_list|)
throw|;
block|}
name|skewKeyInCurrentGroup
operator|=
literal|true
expr_stmt|;
name|bigKeysExistingMap
operator|.
name|put
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|currBigKeyTag
argument_list|)
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
try|try
block|{
name|endGroup
argument_list|()
expr_stmt|;
name|commit
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|bigKeyTbl
init|=
literal|0
init|;
name|bigKeyTbl
operator|<
name|numAliases
condition|;
name|bigKeyTbl
operator|++
control|)
block|{
comment|// if we did not see a skew key in this table, continue to next
comment|// table
if|if
condition|(
operator|!
name|bigKeysExistingMap
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|bigKeyTbl
argument_list|)
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|String
name|specPath
init|=
name|conf
operator|.
name|getBigKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|bigKeyTbl
argument_list|)
decl_stmt|;
name|Path
name|bigKeyPath
init|=
name|getOperatorOutputPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|bigKeyPath
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|delete
argument_list|(
name|bigKeyPath
argument_list|,
name|fs
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|smallKeyTbl
init|=
literal|0
init|;
name|smallKeyTbl
operator|<
name|numAliases
condition|;
name|smallKeyTbl
operator|++
control|)
block|{
if|if
condition|(
operator|(
operator|(
name|byte
operator|)
name|smallKeyTbl
operator|)
operator|==
name|bigKeyTbl
condition|)
block|{
continue|continue;
block|}
name|specPath
operator|=
name|conf
operator|.
name|getSmallKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|bigKeyTbl
argument_list|)
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|smallKeyTbl
argument_list|)
expr_stmt|;
name|delete
argument_list|(
name|getOperatorOutputPath
argument_list|(
name|specPath
argument_list|)
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|delete
parameter_list|(
name|Path
name|operatorOutputPath
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|operatorOutputPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|commit
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|bigKeyTbl
init|=
literal|0
init|;
name|bigKeyTbl
operator|<
name|numAliases
condition|;
name|bigKeyTbl
operator|++
control|)
block|{
comment|// if we did not see a skew key in this table, continue to next table
comment|// we are trying to avoid an extra call of FileSystem.exists()
name|Boolean
name|existing
init|=
name|bigKeysExistingMap
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|bigKeyTbl
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|==
literal|null
operator|||
operator|!
name|existing
condition|)
block|{
continue|continue;
block|}
name|String
name|specPath
init|=
name|conf
operator|.
name|getBigKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|bigKeyTbl
argument_list|)
argument_list|)
decl_stmt|;
name|commitOutputPathToFinalPath
argument_list|(
name|specPath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|smallKeyTbl
init|=
literal|0
init|;
name|smallKeyTbl
operator|<
name|numAliases
condition|;
name|smallKeyTbl
operator|++
control|)
block|{
if|if
condition|(
name|smallKeyTbl
operator|==
name|bigKeyTbl
condition|)
block|{
continue|continue;
block|}
name|specPath
operator|=
name|conf
operator|.
name|getSmallKeysDirMap
argument_list|()
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|bigKeyTbl
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|smallKeyTbl
argument_list|)
argument_list|)
expr_stmt|;
comment|// the file may not exist, and we just ignore this
name|commitOutputPathToFinalPath
argument_list|(
name|specPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|commitOutputPathToFinalPath
parameter_list|(
name|String
name|specPath
parameter_list|,
name|boolean
name|ignoreNonExisting
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|outPath
init|=
name|getOperatorOutputPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|Path
name|finalPath
init|=
name|getOperatorFinalPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|outPath
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
comment|// for local file system in Hadoop-0.17.2.1, it will throw IOException when
comment|// file not existing.
try|try
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|outPath
argument_list|,
name|finalPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to rename output to: "
operator|+
name|finalPath
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|ignoreNonExisting
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|outPath
argument_list|)
operator|&&
name|ignoreNonExisting
condition|)
block|{
return|return;
block|}
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|Path
name|getOperatorOutputPath
parameter_list|(
name|String
name|specPath
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|taskId
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Path
name|getOperatorFinalPath
parameter_list|(
name|String
name|specPath
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|toTempPath
argument_list|(
name|specPath
argument_list|)
decl_stmt|;
return|return
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|taskId
argument_list|)
return|;
block|}
specifier|public
name|void
name|setSkewJoinJobCounter
parameter_list|(
name|LongWritable
name|skewjoinFollowupJobs
parameter_list|)
block|{
name|this
operator|.
name|skewjoinFollowupJobs
operator|=
name|skewjoinFollowupJobs
expr_stmt|;
block|}
specifier|public
name|void
name|updateSkewJoinJobCounter
parameter_list|(
name|int
name|tag
parameter_list|)
block|{
name|this
operator|.
name|skewjoinFollowupJobs
operator|.
name|set
argument_list|(
name|this
operator|.
name|skewjoinFollowupJobs
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

