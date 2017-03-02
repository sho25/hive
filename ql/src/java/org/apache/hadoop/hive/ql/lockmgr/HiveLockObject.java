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
name|lockmgr
package|;
end_package

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
name|lang
operator|.
name|StringUtils
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
name|lang
operator|.
name|builder
operator|.
name|HashCodeBuilder
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
name|common
operator|.
name|StringInternUtils
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
name|MetaStoreUtils
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
name|Hive
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

begin_class
specifier|public
class|class
name|HiveLockObject
block|{
name|String
index|[]
name|pathNames
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
class|class
name|HiveLockObjectData
block|{
specifier|private
name|String
name|queryId
decl_stmt|;
comment|// queryId of the command
specifier|private
name|String
name|lockTime
decl_stmt|;
comment|// time at which lock was acquired
comment|// mode of the lock: EXPLICIT(lock command)/IMPLICIT(query)
specifier|private
name|String
name|lockMode
decl_stmt|;
specifier|private
name|String
name|queryStr
decl_stmt|;
specifier|private
name|String
name|clientIp
decl_stmt|;
comment|/**      * Constructor      *      * Note: The parameters are used to uniquely identify a HiveLockObject.       * The parameters will be stripped off any ':' characters in order not       * to interfere with the way the data is serialized (':' delimited string).      */
specifier|public
name|HiveLockObjectData
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|lockTime
parameter_list|,
name|String
name|lockMode
parameter_list|,
name|String
name|queryStr
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|removeDelimiter
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
name|this
operator|.
name|lockTime
operator|=
name|StringInternUtils
operator|.
name|internIfNotNull
argument_list|(
name|removeDelimiter
argument_list|(
name|lockTime
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|lockMode
operator|=
name|removeDelimiter
argument_list|(
name|lockMode
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryStr
operator|=
name|StringInternUtils
operator|.
name|internIfNotNull
argument_list|(
name|removeDelimiter
argument_list|(
name|queryStr
operator|==
literal|null
condition|?
literal|null
else|:
name|queryStr
operator|.
name|trim
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructor      *       * @param data String of the form "queryId:lockTime:lockMode:queryStr".       * No ':' characters are allowed in any of the components.      */
specifier|public
name|HiveLockObjectData
parameter_list|(
name|String
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|String
index|[]
name|elem
init|=
name|data
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|queryId
operator|=
name|elem
index|[
literal|0
index|]
expr_stmt|;
name|lockTime
operator|=
name|StringInternUtils
operator|.
name|internIfNotNull
argument_list|(
name|elem
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|lockMode
operator|=
name|elem
index|[
literal|2
index|]
expr_stmt|;
name|queryStr
operator|=
name|StringInternUtils
operator|.
name|internIfNotNull
argument_list|(
name|elem
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|elem
operator|.
name|length
operator|>=
literal|5
condition|)
block|{
name|clientIp
operator|=
name|elem
index|[
literal|4
index|]
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getQueryId
parameter_list|()
block|{
return|return
name|queryId
return|;
block|}
specifier|public
name|String
name|getLockTime
parameter_list|()
block|{
return|return
name|lockTime
return|;
block|}
specifier|public
name|String
name|getLockMode
parameter_list|()
block|{
return|return
name|lockMode
return|;
block|}
specifier|public
name|String
name|getQueryStr
parameter_list|()
block|{
return|return
name|queryStr
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|queryId
operator|+
literal|":"
operator|+
name|lockTime
operator|+
literal|":"
operator|+
name|lockMode
operator|+
literal|":"
operator|+
name|queryStr
operator|+
literal|":"
operator|+
name|clientIp
return|;
block|}
specifier|public
name|String
name|getClientIp
parameter_list|()
block|{
return|return
name|this
operator|.
name|clientIp
return|;
block|}
specifier|public
name|void
name|setClientIp
parameter_list|(
name|String
name|clientIp
parameter_list|)
block|{
name|this
operator|.
name|clientIp
operator|=
name|clientIp
expr_stmt|;
block|}
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
operator|!
operator|(
name|o
operator|instanceof
name|HiveLockObjectData
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HiveLockObjectData
name|target
init|=
operator|(
name|HiveLockObjectData
operator|)
name|o
decl_stmt|;
name|boolean
name|ret
init|=
operator|(
name|queryId
operator|==
literal|null
condition|?
name|target
operator|.
name|queryId
operator|==
literal|null
else|:
name|target
operator|.
name|queryId
operator|!=
literal|null
operator|&&
name|queryId
operator|.
name|equals
argument_list|(
name|target
operator|.
name|queryId
argument_list|)
operator|)
decl_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|lockTime
operator|==
literal|null
condition|?
name|target
operator|.
name|lockTime
operator|==
literal|null
else|:
name|target
operator|.
name|lockTime
operator|!=
literal|null
operator|&&
name|lockTime
operator|.
name|equals
argument_list|(
name|target
operator|.
name|lockTime
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|lockMode
operator|==
literal|null
condition|?
name|target
operator|.
name|lockMode
operator|==
literal|null
else|:
name|target
operator|.
name|lockMode
operator|!=
literal|null
operator|&&
name|lockMode
operator|.
name|equals
argument_list|(
name|target
operator|.
name|lockMode
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|queryStr
operator|==
literal|null
condition|?
name|target
operator|.
name|queryStr
operator|==
literal|null
else|:
name|target
operator|.
name|queryStr
operator|!=
literal|null
operator|&&
name|queryStr
operator|.
name|equals
argument_list|(
name|target
operator|.
name|queryStr
argument_list|)
operator|)
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|clientIp
operator|==
literal|null
condition|?
name|target
operator|.
name|clientIp
operator|==
literal|null
else|:
name|target
operator|.
name|clientIp
operator|!=
literal|null
operator|&&
name|clientIp
operator|.
name|equals
argument_list|(
name|target
operator|.
name|clientIp
argument_list|)
operator|)
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|HashCodeBuilder
name|builder
init|=
operator|new
name|HashCodeBuilder
argument_list|()
decl_stmt|;
name|boolean
name|queryId_present
init|=
name|queryId
operator|==
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|queryId_present
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryId_present
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
block|}
name|boolean
name|lockTime_present
init|=
name|lockTime
operator|==
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|lockTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|lockTime_present
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|lockTime
argument_list|)
expr_stmt|;
block|}
name|boolean
name|lockMode_present
init|=
name|lockMode
operator|==
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|lockMode
argument_list|)
expr_stmt|;
if|if
condition|(
name|lockMode_present
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|lockMode
argument_list|)
expr_stmt|;
block|}
name|boolean
name|queryStr_present
init|=
name|queryStr
operator|==
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|queryStr
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryStr_present
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|queryStr
argument_list|)
expr_stmt|;
block|}
name|boolean
name|clienIp_present
init|=
name|clientIp
operator|==
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|clientIp
argument_list|)
expr_stmt|;
if|if
condition|(
name|clienIp_present
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|clientIp
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toHashCode
argument_list|()
return|;
block|}
block|}
comment|/* user supplied data for that object */
specifier|private
name|HiveLockObjectData
name|data
decl_stmt|;
specifier|public
name|HiveLockObject
parameter_list|()
block|{
name|this
operator|.
name|data
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|String
name|path
parameter_list|,
name|HiveLockObjectData
name|lockData
parameter_list|)
block|{
name|this
operator|.
name|pathNames
operator|=
operator|new
name|String
index|[
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|pathNames
index|[
literal|0
index|]
operator|=
name|StringInternUtils
operator|.
name|internIfNotNull
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|data
operator|=
name|lockData
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|String
index|[]
name|paths
parameter_list|,
name|HiveLockObjectData
name|lockData
parameter_list|)
block|{
name|this
operator|.
name|pathNames
operator|=
name|StringInternUtils
operator|.
name|internStringsInArray
argument_list|(
name|paths
argument_list|)
expr_stmt|;
name|this
operator|.
name|data
operator|=
name|lockData
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|HiveLockObjectData
name|lockData
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|String
index|[]
block|{
name|tbl
operator|.
name|getDbName
argument_list|()
block|,
name|MetaStoreUtils
operator|.
name|encodeTableName
argument_list|(
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
block|}
argument_list|,
name|lockData
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|Partition
name|par
parameter_list|,
name|HiveLockObjectData
name|lockData
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|String
index|[]
block|{
name|par
operator|.
name|getTable
argument_list|()
operator|.
name|getDbName
argument_list|()
block|,
name|MetaStoreUtils
operator|.
name|encodeTableName
argument_list|(
name|par
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
block|,
name|par
operator|.
name|getName
argument_list|()
block|}
argument_list|,
name|lockData
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HiveLockObject
parameter_list|(
name|DummyPartition
name|par
parameter_list|,
name|HiveLockObjectData
name|lockData
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|String
index|[]
block|{
name|par
operator|.
name|getName
argument_list|()
block|}
argument_list|,
name|lockData
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a locking object for a table (when partition spec is not provided)    * or a table partition    * @param hiveDB    an object to communicate with the metastore    * @param tableName the table to create the locking object on    * @param partSpec  the spec of a partition to create the locking object on    * @return  the locking object    * @throws HiveException    */
specifier|public
specifier|static
name|HiveLockObject
name|createFrom
parameter_list|(
name|Hive
name|hiveDB
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|)
throws|throws
name|HiveException
block|{
name|Table
name|tbl
init|=
name|hiveDB
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tbl
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" does not exist "
argument_list|)
throw|;
block|}
name|HiveLockObject
name|obj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
name|obj
operator|=
operator|new
name|HiveLockObject
argument_list|(
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Partition
name|par
init|=
name|hiveDB
operator|.
name|getPartition
argument_list|(
name|tbl
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|par
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Partition "
operator|+
name|partSpec
operator|+
literal|" for table "
operator|+
name|tableName
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
name|obj
operator|=
operator|new
name|HiveLockObject
argument_list|(
name|par
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|obj
return|;
block|}
specifier|public
name|String
index|[]
name|getPaths
parameter_list|()
block|{
return|return
name|pathNames
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
if|if
condition|(
name|pathNames
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|pathNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|append
argument_list|(
name|pathNames
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getDisplayName
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|pathNames
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|pathNames
operator|.
name|length
operator|==
literal|1
condition|)
block|{
return|return
name|pathNames
index|[
literal|0
index|]
return|;
block|}
elseif|else
if|if
condition|(
name|pathNames
operator|.
name|length
operator|==
literal|2
condition|)
block|{
return|return
name|pathNames
index|[
literal|0
index|]
operator|+
literal|"@"
operator|+
name|pathNames
index|[
literal|1
index|]
return|;
block|}
name|String
name|ret
init|=
name|pathNames
index|[
literal|0
index|]
operator|+
literal|"@"
operator|+
name|pathNames
index|[
literal|1
index|]
operator|+
literal|"@"
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|2
init|;
name|i
operator|<
name|pathNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|ret
operator|=
name|ret
operator|+
literal|"/"
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|ret
operator|=
name|ret
operator|+
name|pathNames
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|HiveLockObjectData
name|getData
parameter_list|()
block|{
return|return
name|data
return|;
block|}
specifier|public
name|void
name|setData
parameter_list|(
name|HiveLockObjectData
name|data
parameter_list|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
block|}
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
operator|!
operator|(
name|o
operator|instanceof
name|HiveLockObject
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HiveLockObject
name|tgt
init|=
operator|(
name|HiveLockObject
operator|)
name|o
decl_stmt|;
return|return
name|StringUtils
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getName
argument_list|()
argument_list|,
name|tgt
operator|.
name|getName
argument_list|()
argument_list|)
operator|&&
operator|(
name|data
operator|==
literal|null
condition|?
name|tgt
operator|.
name|getData
argument_list|()
operator|==
literal|null
else|:
name|data
operator|.
name|equals
argument_list|(
name|tgt
operator|.
name|getData
argument_list|()
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|HashCodeBuilder
name|builder
init|=
operator|new
name|HashCodeBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|pathNames
argument_list|)
expr_stmt|;
name|boolean
name|data_present
init|=
name|data
operator|==
literal|null
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|data_present
argument_list|)
expr_stmt|;
if|if
condition|(
name|data_present
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|toHashCode
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|String
name|removeDelimiter
parameter_list|(
name|String
name|in
parameter_list|)
block|{
if|if
condition|(
name|in
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|in
operator|.
name|replaceAll
argument_list|(
literal|":"
argument_list|,
literal|""
argument_list|)
return|;
block|}
block|}
end_class

end_unit

