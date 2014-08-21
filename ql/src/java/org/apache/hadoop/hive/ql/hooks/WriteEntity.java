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
name|hooks
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|DummyPartition
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
name|Partition
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
name|Table
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
name|AlterTableDesc
import|;
end_import

begin_comment
comment|/**  * This class encapsulates an object that is being written to by the query. This  * object may be a table, partition, dfs directory or a local directory.  */
end_comment

begin_class
specifier|public
class|class
name|WriteEntity
extends|extends
name|Entity
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|WriteEntity
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|isTempURI
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|WriteType
block|{
name|DDL_EXCLUSIVE
block|,
comment|// for use in DDL statements that require an exclusive lock,
comment|// such as dropping a table or partition
name|DDL_SHARED
block|,
comment|// for use in DDL operations that only need a shared lock, such as creating a table
name|DDL_NO_LOCK
block|,
comment|// for use in DDL statements that do not require a lock
name|INSERT
block|,
name|INSERT_OVERWRITE
block|,
name|UPDATE
block|,
name|DELETE
block|,
name|PATH_WRITE
block|,
comment|// Write to a URI, no locking done for this
block|}
empty_stmt|;
specifier|private
name|WriteType
name|writeType
decl_stmt|;
comment|/**    * Only used by serialization.    */
specifier|public
name|WriteEntity
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|WriteEntity
parameter_list|(
name|Database
name|database
parameter_list|,
name|WriteType
name|type
parameter_list|)
block|{
name|super
argument_list|(
name|database
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|writeType
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Constructor for a table.    *    * @param t    *          Table that is written to.    */
specifier|public
name|WriteEntity
parameter_list|(
name|Table
name|t
parameter_list|,
name|WriteType
name|type
parameter_list|)
block|{
name|super
argument_list|(
name|t
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|writeType
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|WriteEntity
parameter_list|(
name|Table
name|t
parameter_list|,
name|WriteType
name|type
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|super
argument_list|(
name|t
argument_list|,
name|complete
argument_list|)
expr_stmt|;
name|writeType
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Constructor for objects represented as String.    * Currently applicable only for function names.    * @param db    * @param objName    * @param type    * @param writeType    */
specifier|public
name|WriteEntity
parameter_list|(
name|Database
name|db
parameter_list|,
name|String
name|objName
parameter_list|,
name|Type
name|type
parameter_list|,
name|WriteType
name|writeType
parameter_list|)
block|{
name|super
argument_list|(
name|db
argument_list|,
name|objName
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|writeType
operator|=
name|writeType
expr_stmt|;
block|}
comment|/**    * Constructor for a partition.    *    * @param p    *          Partition that is written to.    */
specifier|public
name|WriteEntity
parameter_list|(
name|Partition
name|p
parameter_list|,
name|WriteType
name|type
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|writeType
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|WriteEntity
parameter_list|(
name|DummyPartition
name|p
parameter_list|,
name|WriteType
name|type
parameter_list|,
name|boolean
name|complete
parameter_list|)
block|{
name|super
argument_list|(
name|p
argument_list|,
name|complete
argument_list|)
expr_stmt|;
name|writeType
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Constructor for a file.    *    * @param d    *          The name of the directory that is being written to.    * @param islocal    *          Flag to decide whether this directory is local or in dfs.    */
specifier|public
name|WriteEntity
parameter_list|(
name|Path
name|d
parameter_list|,
name|boolean
name|islocal
parameter_list|)
block|{
name|this
argument_list|(
name|d
argument_list|,
name|islocal
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for a file.    *    * @param d    *          The name of the directory that is being written to.    * @param islocal    *          Flag to decide whether this directory is local or in dfs.    * @param isTemp    *          True if this is a temporary location such as scratch dir    */
specifier|public
name|WriteEntity
parameter_list|(
name|Path
name|d
parameter_list|,
name|boolean
name|islocal
parameter_list|,
name|boolean
name|isTemp
parameter_list|)
block|{
name|super
argument_list|(
name|d
operator|.
name|toString
argument_list|()
argument_list|,
name|islocal
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|isTempURI
operator|=
name|isTemp
expr_stmt|;
name|this
operator|.
name|writeType
operator|=
name|WriteType
operator|.
name|PATH_WRITE
expr_stmt|;
block|}
comment|/**    * Determine which type of write this is.  This is needed by the lock    * manager so it can understand what kind of lock to acquire.    * @return write type    */
specifier|public
name|WriteType
name|getWriteType
parameter_list|()
block|{
return|return
name|writeType
return|;
block|}
comment|/**    * Equals function.    */
annotation|@
name|Override
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
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|o
operator|instanceof
name|WriteEntity
condition|)
block|{
name|WriteEntity
name|ore
init|=
operator|(
name|WriteEntity
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ore
operator|.
name|toString
argument_list|()
argument_list|)
operator|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|boolean
name|isTempURI
parameter_list|()
block|{
return|return
name|isTempURI
return|;
block|}
comment|/**    * Determine the type of lock to request for a given alter table type.    * @param op Operation type from the alter table description    * @return the write type this should use.    */
specifier|public
specifier|static
name|WriteType
name|determineAlterTableWriteType
parameter_list|(
name|AlterTableDesc
operator|.
name|AlterTableTypes
name|op
parameter_list|)
block|{
switch|switch
condition|(
name|op
condition|)
block|{
case|case
name|RENAMECOLUMN
case|:
case|case
name|ADDCLUSTERSORTCOLUMN
case|:
case|case
name|ADDFILEFORMAT
case|:
case|case
name|ADDSERDE
case|:
case|case
name|DROPPROPS
case|:
case|case
name|REPLACECOLS
case|:
case|case
name|ARCHIVE
case|:
case|case
name|UNARCHIVE
case|:
case|case
name|ALTERPROTECTMODE
case|:
case|case
name|ALTERPARTITIONPROTECTMODE
case|:
case|case
name|ALTERLOCATION
case|:
case|case
name|DROPPARTITION
case|:
case|case
name|RENAMEPARTITION
case|:
case|case
name|ADDSKEWEDBY
case|:
case|case
name|ALTERSKEWEDLOCATION
case|:
case|case
name|ALTERBUCKETNUM
case|:
case|case
name|ALTERPARTITION
case|:
case|case
name|ADDCOLS
case|:
case|case
name|RENAME
case|:
return|return
name|WriteType
operator|.
name|DDL_EXCLUSIVE
return|;
case|case
name|ADDPARTITION
case|:
case|case
name|ADDSERDEPROPS
case|:
case|case
name|ADDPROPS
case|:
return|return
name|WriteType
operator|.
name|DDL_SHARED
return|;
case|case
name|COMPACT
case|:
case|case
name|TOUCH
case|:
return|return
name|WriteType
operator|.
name|DDL_NO_LOCK
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown operation "
operator|+
name|op
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

