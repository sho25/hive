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
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|mapredWork
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
name|partitionDesc
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
name|tableDesc
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|io
operator|.
name|Text
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

begin_comment
comment|/**  * Map operator. This triggers overall map side processing.  * This is a little different from regular operators in that  * it starts off by processing a Writable data structure from  * a Table (instead of a Hive Object).  **/
end_comment

begin_class
specifier|public
class|class
name|MapOperator
extends|extends
name|Operator
argument_list|<
name|mapredWork
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
specifier|public
specifier|static
enum|enum
name|Counter
block|{
name|DESERIALIZE_ERRORS
block|}
specifier|transient
specifier|private
name|LongWritable
name|deserialize_error_count
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
specifier|transient
specifier|private
name|Deserializer
name|deserializer
decl_stmt|;
specifier|transient
specifier|private
name|Object
index|[]
name|rowWithPart
decl_stmt|;
specifier|transient
specifier|private
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
specifier|transient
specifier|private
name|boolean
name|isPartitioned
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|MapInputPath
argument_list|,
name|MapOpCtx
argument_list|>
name|opCtxMap
decl_stmt|;
specifier|private
specifier|static
class|class
name|MapInputPath
block|{
name|String
name|path
decl_stmt|;
name|String
name|alias
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
decl_stmt|;
comment|/**      * @param path      * @param alias      * @param op      */
specifier|public
name|MapInputPath
parameter_list|(
name|String
name|path
parameter_list|,
name|String
name|alias
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|op
operator|=
name|op
expr_stmt|;
block|}
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|MapInputPath
condition|)
block|{
name|MapInputPath
name|mObj
init|=
operator|(
name|MapInputPath
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|mObj
operator|==
literal|null
condition|)
return|return
literal|false
return|;
return|return
name|path
operator|.
name|equals
argument_list|(
name|mObj
operator|.
name|path
argument_list|)
operator|&&
name|alias
operator|.
name|equals
argument_list|(
name|mObj
operator|.
name|alias
argument_list|)
operator|&&
name|op
operator|.
name|equals
argument_list|(
name|mObj
operator|.
name|op
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
operator|(
name|op
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|op
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MapOpCtx
block|{
name|boolean
name|isPartitioned
decl_stmt|;
name|StructObjectInspector
name|rowObjectInspector
decl_stmt|;
name|Object
index|[]
name|rowWithPart
decl_stmt|;
name|Deserializer
name|deserializer
decl_stmt|;
specifier|public
name|String
name|tableName
decl_stmt|;
specifier|public
name|String
name|partName
decl_stmt|;
comment|/**      * @param isPartitioned      * @param rowObjectInspector      * @param rowWithPart      */
specifier|public
name|MapOpCtx
parameter_list|(
name|boolean
name|isPartitioned
parameter_list|,
name|StructObjectInspector
name|rowObjectInspector
parameter_list|,
name|Object
index|[]
name|rowWithPart
parameter_list|,
name|Deserializer
name|deserializer
parameter_list|)
block|{
name|this
operator|.
name|isPartitioned
operator|=
name|isPartitioned
expr_stmt|;
name|this
operator|.
name|rowObjectInspector
operator|=
name|rowObjectInspector
expr_stmt|;
name|this
operator|.
name|rowWithPart
operator|=
name|rowWithPart
expr_stmt|;
name|this
operator|.
name|deserializer
operator|=
name|deserializer
expr_stmt|;
block|}
comment|/**      * @return the isPartitioned      */
specifier|public
name|boolean
name|isPartitioned
parameter_list|()
block|{
return|return
name|isPartitioned
return|;
block|}
comment|/**      * @return the rowObjectInspector      */
specifier|public
name|StructObjectInspector
name|getRowObjectInspector
parameter_list|()
block|{
return|return
name|rowObjectInspector
return|;
block|}
comment|/**      * @return the rowWithPart      */
specifier|public
name|Object
index|[]
name|getRowWithPart
parameter_list|()
block|{
return|return
name|rowWithPart
return|;
block|}
comment|/**      * @return the deserializer      */
specifier|public
name|Deserializer
name|getDeserializer
parameter_list|()
block|{
return|return
name|deserializer
return|;
block|}
block|}
comment|/**    * Initializes this map op as the root of the tree. It sets JobConf& MapRedWork    * and starts initialization of the operator tree rooted at this op.    * @param hconf    * @param mrwork    * @throws HiveException    */
specifier|public
name|void
name|initializeAsRoot
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|mapredWork
name|mrwork
parameter_list|)
throws|throws
name|HiveException
block|{
name|setConf
argument_list|(
name|mrwork
argument_list|)
expr_stmt|;
name|setChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|hconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|MapOpCtx
name|initObjectInspector
parameter_list|(
name|mapredWork
name|conf
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|String
name|onefile
parameter_list|)
throws|throws
name|HiveException
throws|,
name|ClassNotFoundException
throws|,
name|InstantiationException
throws|,
name|IllegalAccessException
throws|,
name|SerDeException
block|{
name|partitionDesc
name|pd
init|=
name|conf
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|onefile
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|pd
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|tableDesc
name|td
init|=
name|pd
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|Properties
name|tblProps
init|=
name|td
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|Class
name|sdclass
init|=
name|td
operator|.
name|getDeserializerClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|sdclass
operator|==
literal|null
condition|)
block|{
name|String
name|className
init|=
name|td
operator|.
name|getSerdeClassName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|className
operator|==
literal|""
operator|)
operator|||
operator|(
name|className
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"SerDe class or the SerDe class name is not set for table: "
operator|+
name|td
operator|.
name|getProperties
argument_list|()
operator|.
name|getProperty
argument_list|(
literal|"name"
argument_list|)
argument_list|)
throw|;
block|}
name|sdclass
operator|=
name|hconf
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
name|String
name|tableName
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|tblProps
operator|.
name|getProperty
argument_list|(
literal|"name"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|partName
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|partSpec
argument_list|)
decl_stmt|;
comment|//HiveConf.setVar(hconf, HiveConf.ConfVars.HIVETABLENAME, tableName);
comment|//HiveConf.setVar(hconf, HiveConf.ConfVars.HIVEPARTITIONNAME, partName);
name|Deserializer
name|deserializer
init|=
operator|(
name|Deserializer
operator|)
name|sdclass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|deserializer
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
name|tblProps
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|rowObjectInspector
init|=
operator|(
name|StructObjectInspector
operator|)
name|deserializer
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|MapOpCtx
name|opCtx
init|=
literal|null
decl_stmt|;
comment|// Next check if this table has partitions and if so
comment|// get the list of partition names as well as allocate
comment|// the serdes for the partition columns
name|String
name|pcols
init|=
name|tblProps
operator|.
name|getProperty
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Constants
operator|.
name|META_TABLE_PARTITION_COLUMNS
argument_list|)
decl_stmt|;
comment|//Log LOG = LogFactory.getLog(MapOperator.class.getName());
if|if
condition|(
name|pcols
operator|!=
literal|null
operator|&&
name|pcols
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|partKeys
init|=
name|pcols
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|partKeys
operator|.
name|length
argument_list|)
decl_stmt|;
name|Object
index|[]
name|partValues
init|=
operator|new
name|Object
index|[
name|partKeys
operator|.
name|length
index|]
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|partObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|partKeys
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|partKeys
index|[
name|i
index|]
decl_stmt|;
name|partNames
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|partValues
index|[
name|i
index|]
operator|=
operator|new
name|Text
argument_list|(
name|partSpec
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|partObjectInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|partObjectInspector
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|partNames
argument_list|,
name|partObjectInspectors
argument_list|)
decl_stmt|;
name|Object
index|[]
name|rowWithPart
init|=
operator|new
name|Object
index|[
literal|2
index|]
decl_stmt|;
name|rowWithPart
index|[
literal|1
index|]
operator|=
name|partValues
expr_stmt|;
name|rowObjectInspector
operator|=
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
name|rowObjectInspector
block|,
name|partObjectInspector
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|//LOG.info("dump " + tableName + " " + partName + " " + rowObjectInspector.getTypeName());
name|opCtx
operator|=
operator|new
name|MapOpCtx
argument_list|(
literal|true
argument_list|,
name|rowObjectInspector
argument_list|,
name|rowWithPart
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//LOG.info("dump2 " + tableName + " " + partName + " " + rowObjectInspector.getTypeName());
name|opCtx
operator|=
operator|new
name|MapOpCtx
argument_list|(
literal|false
argument_list|,
name|rowObjectInspector
argument_list|,
literal|null
argument_list|,
name|deserializer
argument_list|)
expr_stmt|;
block|}
name|opCtx
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|opCtx
operator|.
name|partName
operator|=
name|partName
expr_stmt|;
return|return
name|opCtx
return|;
block|}
specifier|public
name|void
name|setChildren
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|Path
name|fpath
init|=
operator|new
name|Path
argument_list|(
operator|(
operator|new
name|Path
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPMAPFILENAME
argument_list|)
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|opCtxMap
operator|=
operator|new
name|HashMap
argument_list|<
name|MapInputPath
argument_list|,
name|MapOpCtx
argument_list|>
argument_list|()
expr_stmt|;
name|statsMap
operator|.
name|put
argument_list|(
name|Counter
operator|.
name|DESERIALIZE_ERRORS
argument_list|,
name|deserialize_error_count
argument_list|)
expr_stmt|;
try|try
block|{
name|boolean
name|done
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|onefile
range|:
name|conf
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|MapOpCtx
name|opCtx
init|=
name|initObjectInspector
argument_list|(
name|conf
argument_list|,
name|hconf
argument_list|,
name|onefile
argument_list|)
decl_stmt|;
name|Path
name|onepath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|onefile
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|aliases
init|=
name|conf
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|onefile
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|onealias
range|:
name|aliases
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
name|conf
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|onealias
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Adding alias "
operator|+
name|onealias
operator|+
literal|" to work list for file "
operator|+
name|fpath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|MapInputPath
name|inp
init|=
operator|new
name|MapInputPath
argument_list|(
name|onefile
argument_list|,
name|onealias
argument_list|,
name|op
argument_list|)
decl_stmt|;
name|opCtxMap
operator|.
name|put
argument_list|(
name|inp
argument_list|,
name|opCtx
argument_list|)
expr_stmt|;
name|op
operator|.
name|setParentOperators
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|// check for the operators who will process rows coming to this Map Operator
if|if
condition|(
operator|!
name|onepath
operator|.
name|toUri
argument_list|()
operator|.
name|relativize
argument_list|(
name|fpath
operator|.
name|toUri
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|fpath
operator|.
name|toUri
argument_list|()
argument_list|)
condition|)
block|{
name|children
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"dump "
operator|+
name|op
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|opCtxMap
operator|.
name|get
argument_list|(
name|inp
argument_list|)
operator|.
name|getRowObjectInspector
argument_list|()
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|done
condition|)
block|{
name|deserializer
operator|=
name|opCtxMap
operator|.
name|get
argument_list|(
name|inp
argument_list|)
operator|.
name|getDeserializer
argument_list|()
expr_stmt|;
name|isPartitioned
operator|=
name|opCtxMap
operator|.
name|get
argument_list|(
name|inp
argument_list|)
operator|.
name|isPartitioned
argument_list|()
expr_stmt|;
name|rowWithPart
operator|=
name|opCtxMap
operator|.
name|get
argument_list|(
name|inp
argument_list|)
operator|.
name|getRowWithPart
argument_list|()
expr_stmt|;
name|rowObjectInspector
operator|=
name|opCtxMap
operator|.
name|get
argument_list|(
name|inp
argument_list|)
operator|.
name|getRowObjectInspector
argument_list|()
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// didn't find match for input file path in configuration!
comment|// serious problem ..
name|LOG
operator|.
name|error
argument_list|(
literal|"Configuration does not have any alias for path: "
operator|+
name|fpath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Configuration and input path are inconsistent"
argument_list|)
throw|;
block|}
comment|// we found all the operators that we are supposed to process.
name|setChildOperators
argument_list|(
name|children
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// set that parent initialization is done and call initialize on children
name|state
operator|=
name|State
operator|.
name|INIT
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|MapInputPath
argument_list|,
name|MapOpCtx
argument_list|>
name|entry
range|:
name|opCtxMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// Add alias, table name, and partitions to hadoop conf so that their children will
comment|// inherit these
name|HiveConf
operator|.
name|setVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVETABLENAME
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|tableName
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEPARTITIONNAME
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|partName
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|op
decl_stmt|;
name|op
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getRowObjectInspector
argument_list|()
block|}
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|process
parameter_list|(
name|Writable
name|value
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|isPartitioned
condition|)
block|{
name|Object
name|row
init|=
name|deserializer
operator|.
name|deserialize
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|forward
argument_list|(
name|row
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowWithPart
index|[
literal|0
index|]
operator|=
name|deserializer
operator|.
name|deserialize
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|forward
argument_list|(
name|rowWithPart
argument_list|,
name|rowObjectInspector
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
comment|// TODO: policy on deserialization errors
name|deserialize_error_count
operator|.
name|set
argument_list|(
name|deserialize_error_count
operator|.
name|get
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive 2 Internal error: should not be called!"
argument_list|)
throw|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"MAP"
return|;
block|}
block|}
end_class

end_unit

