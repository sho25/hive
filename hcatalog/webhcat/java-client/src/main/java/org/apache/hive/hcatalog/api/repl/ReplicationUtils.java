begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|repl
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Objects
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
name|binary
operator|.
name|Base64
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
name|io
operator|.
name|IOExceptionWithCause
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
name|ReplicationSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|HCatDatabase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|HCatPartition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|HCatTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|ReaderWriter
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|Map
import|;
end_import

begin_class
specifier|public
class|class
name|ReplicationUtils
block|{
specifier|public
specifier|final
specifier|static
name|String
name|REPL_STATE_ID
init|=
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|private
name|ReplicationUtils
parameter_list|()
block|{
comment|// dummy private constructor, since this class is a collection of static utility methods.
block|}
comment|/**    * Gets the last known replication state of this db. This is    * applicable only if it is the destination of a replication    * and has had data replicated into it via imports previously.    * Defaults to 0.    */
specifier|public
specifier|static
name|long
name|getLastReplicationId
parameter_list|(
name|HCatDatabase
name|db
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
init|=
name|db
operator|.
name|getProperties
argument_list|()
decl_stmt|;
if|if
condition|(
name|props
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|props
operator|.
name|containsKey
argument_list|(
name|REPL_STATE_ID
argument_list|)
condition|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|props
operator|.
name|get
argument_list|(
name|REPL_STATE_ID
argument_list|)
argument_list|)
return|;
block|}
block|}
return|return
literal|0l
return|;
comment|// default is to return earliest possible state.
block|}
comment|/**    * Gets the last known replication state of the provided table. This    * is applicable only if it is the destination of a replication    * and has had data replicated into it via imports previously.    * Defaults to 0.    */
specifier|public
specifier|static
name|long
name|getLastReplicationId
parameter_list|(
name|HCatTable
name|tbl
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
init|=
name|tbl
operator|.
name|getTblProps
argument_list|()
decl_stmt|;
if|if
condition|(
name|tblProps
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|tblProps
operator|.
name|containsKey
argument_list|(
name|REPL_STATE_ID
argument_list|)
condition|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|tblProps
operator|.
name|get
argument_list|(
name|REPL_STATE_ID
argument_list|)
argument_list|)
return|;
block|}
block|}
return|return
literal|0l
return|;
comment|// default is to return earliest possible state.
block|}
comment|/**    * Gets the last known replication state of the provided partition.    * This is applicable only if it is the destination of a replication    * and has had data replicated into it via imports previously.    * If that is not available, but parent table is provided,    * defaults to parent table's replication state. If that is also    * unknown, defaults to 0.    */
specifier|public
specifier|static
name|long
name|getLastReplicationId
parameter_list|(
name|HCatPartition
name|ptn
parameter_list|,
annotation|@
name|Nullable
name|HCatTable
name|parentTable
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|ptn
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parameters
operator|.
name|containsKey
argument_list|(
name|REPL_STATE_ID
argument_list|)
condition|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|parameters
operator|.
name|get
argument_list|(
name|REPL_STATE_ID
argument_list|)
argument_list|)
return|;
block|}
block|}
if|if
condition|(
name|parentTable
operator|!=
literal|null
condition|)
block|{
return|return
name|getLastReplicationId
argument_list|(
name|parentTable
argument_list|)
return|;
block|}
return|return
literal|0l
return|;
comment|// default is to return earliest possible state.
block|}
comment|/**    * Used to generate a unique key for a combination of given event id, dbname,    * tablename and partition keyvalues. This is used to feed in a name for creating    * staging directories for exports and imports. This should be idempotent given    * the same values, i.e. hashcode-like, but at the same time, be guaranteed to be    * different for every possible partition, while being "readable-ish". Basically,    * we concat the alphanumberic versions of all of the above, along with a hashcode    * of the db, tablename and ptn key-value pairs    */
specifier|public
specifier|static
name|String
name|getUniqueKey
parameter_list|(
name|long
name|eventId
parameter_list|,
name|String
name|db
parameter_list|,
name|String
name|table
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ptnDesc
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|eventId
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|toStringWordCharsOnly
argument_list|(
name|db
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|toStringWordCharsOnly
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|toStringWordCharsOnly
argument_list|(
name|ptnDesc
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Objects
operator|.
name|hashCode
argument_list|(
name|db
argument_list|,
name|table
argument_list|,
name|ptnDesc
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Return alphanumeric(and '_') representation of a Map<String,String>    *    */
specifier|private
specifier|static
name|String
name|toStringWordCharsOnly
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|)
block|{
if|if
condition|(
name|map
operator|==
literal|null
condition|)
block|{
return|return
literal|"null"
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|toStringWordCharsOnly
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|toStringWordCharsOnly
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Return alphanumeric(and '_') chars only of a string, lowercased    */
specifier|public
specifier|static
name|String
name|toStringWordCharsOnly
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
operator|(
name|s
operator|==
literal|null
operator|)
condition|?
literal|"null"
else|:
name|s
operator|.
name|replaceAll
argument_list|(
literal|"[\\W]"
argument_list|,
literal|""
argument_list|)
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
comment|/**    * Utility function to use in conjunction with .withDbNameMapping / .withTableNameMapping,    * if we desire usage of a Map<String,String> instead of implementing a Function<String,String>    */
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapBasedFunction
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
parameter_list|)
block|{
return|return
operator|new
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Nullable
annotation|@
name|Override
specifier|public
name|String
name|apply
parameter_list|(
annotation|@
name|Nullable
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
operator|(
name|m
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|m
operator|.
name|containsKey
argument_list|(
name|s
argument_list|)
operator|)
condition|)
block|{
return|return
name|s
return|;
block|}
return|return
name|m
operator|.
name|get
argument_list|(
name|s
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/**    * Return a mapping from a given map function if available, and the key itself if not.    */
specifier|public
specifier|static
name|String
name|mapIfMapAvailable
parameter_list|(
name|String
name|s
parameter_list|,
name|Function
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapping
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|mapping
operator|!=
literal|null
condition|)
block|{
return|return
name|mapping
operator|.
name|apply
argument_list|(
name|s
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
comment|// The key wasn't present in the mapping, and the function didn't
comment|// return a default value - ignore, and use our default.
block|}
comment|// We return the key itself, since no mapping was available/returned
return|return
name|s
return|;
block|}
specifier|public
specifier|static
name|String
name|partitionDescriptor
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ptnDesc
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|ptnDesc
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|ptnDesc
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" PARTITION ("
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
name|e
range|:
name|ptnDesc
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO : verify if any quoting is needed for keys
name|sb
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO : verify if any escaping is needed for values
name|sb
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Command implements Writable, but that's not terribly easy to use compared    * to String, even if it plugs in easily into the rest of Hadoop. Provide    * utility methods to easily serialize and deserialize Commands    *    * serializeCommand returns a base64 String representation of given command    */
specifier|public
specifier|static
name|String
name|serializeCommand
parameter_list|(
name|Command
name|command
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutput
name|dataOutput
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|ReaderWriter
operator|.
name|writeDatum
argument_list|(
name|dataOutput
argument_list|,
name|command
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|command
operator|.
name|write
argument_list|(
name|dataOutput
argument_list|)
expr_stmt|;
return|return
name|Base64
operator|.
name|encodeBase64URLSafeString
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Command implements Writable, but that's not terribly easy to use compared    * to String, even if it plugs in easily into the rest of Hadoop. Provide    * utility methods to easily serialize and deserialize Commands    *    * deserializeCommand instantiates a concrete Command and initializes it,    * given a base64 String representation of it.    */
specifier|public
specifier|static
name|Command
name|deserializeCommand
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInput
name|dataInput
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|Base64
operator|.
name|decodeBase64
argument_list|(
name|s
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|clazz
init|=
operator|(
name|String
operator|)
name|ReaderWriter
operator|.
name|readDatum
argument_list|(
name|dataInput
argument_list|)
decl_stmt|;
name|Command
name|cmd
decl_stmt|;
try|try
block|{
name|cmd
operator|=
operator|(
name|Command
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|clazz
argument_list|)
operator|.
name|newInstance
argument_list|()
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
name|IOExceptionWithCause
argument_list|(
literal|"Error instantiating class "
operator|+
name|clazz
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|cmd
operator|.
name|readFields
argument_list|(
name|dataInput
argument_list|)
expr_stmt|;
return|return
name|cmd
return|;
block|}
block|}
end_class

end_unit

