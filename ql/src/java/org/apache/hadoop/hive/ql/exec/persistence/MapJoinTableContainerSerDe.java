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
operator|.
name|persistence
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
name|io
operator|.
name|ObjectInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ConcurrentModificationException
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
name|io
operator|.
name|Writable
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|MapJoinTableContainerSerDe
block|{
specifier|private
specifier|final
name|MapJoinObjectSerDeContext
name|keyContext
decl_stmt|;
specifier|private
specifier|final
name|MapJoinObjectSerDeContext
name|valueContext
decl_stmt|;
specifier|public
name|MapJoinTableContainerSerDe
parameter_list|(
name|MapJoinObjectSerDeContext
name|keyContext
parameter_list|,
name|MapJoinObjectSerDeContext
name|valueContext
parameter_list|)
block|{
name|this
operator|.
name|keyContext
operator|=
name|keyContext
expr_stmt|;
name|this
operator|.
name|valueContext
operator|=
name|valueContext
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
specifier|public
name|MapJoinTableContainer
name|load
parameter_list|(
name|ObjectInputStream
name|in
parameter_list|)
throws|throws
name|HiveException
block|{
name|SerDe
name|keySerDe
init|=
name|keyContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|SerDe
name|valueSerDe
init|=
name|valueContext
operator|.
name|getSerDe
argument_list|()
decl_stmt|;
name|MapJoinTableContainer
name|tableContainer
decl_stmt|;
try|try
block|{
name|String
name|name
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|in
operator|.
name|readObject
argument_list|()
decl_stmt|;
name|tableContainer
operator|=
name|create
argument_list|(
name|name
argument_list|,
name|metaData
argument_list|)
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
literal|"IO error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Class Initialization error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|Writable
name|keyContainer
init|=
name|keySerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Writable
name|valueContainer
init|=
name|valueSerDe
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|int
name|numKeys
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|numKeys
condition|;
name|keyIndex
operator|++
control|)
block|{
name|MapJoinKey
name|key
init|=
operator|new
name|MapJoinKey
argument_list|()
decl_stmt|;
name|key
operator|.
name|read
argument_list|(
name|keyContext
argument_list|,
name|in
argument_list|,
name|keyContainer
argument_list|)
expr_stmt|;
name|MapJoinRowContainer
name|values
init|=
operator|new
name|MapJoinRowContainer
argument_list|()
decl_stmt|;
name|values
operator|.
name|read
argument_list|(
name|valueContext
argument_list|,
name|in
argument_list|,
name|valueContainer
argument_list|)
expr_stmt|;
name|tableContainer
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
return|return
name|tableContainer
return|;
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
literal|"IO error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
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
literal|"Error while trying to create table container"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|persist
parameter_list|(
name|ObjectOutputStream
name|out
parameter_list|,
name|MapJoinTableContainer
name|tableContainer
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|numKeys
init|=
name|tableContainer
operator|.
name|size
argument_list|()
decl_stmt|;
try|try
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|tableContainer
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeObject
argument_list|(
name|tableContainer
operator|.
name|getMetaData
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numKeys
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
name|entry
range|:
name|tableContainer
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|write
argument_list|(
name|keyContext
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|write
argument_list|(
name|valueContext
argument_list|,
name|out
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
name|String
name|msg
init|=
literal|"SerDe error while attempting to persist table container"
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"IO error while attempting to persist table container"
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|numKeys
operator|!=
name|tableContainer
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConcurrentModificationException
argument_list|(
literal|"TableContainer was modified while persisting: "
operator|+
name|tableContainer
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|persistDummyTable
parameter_list|(
name|ObjectOutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|MapJoinTableContainer
name|tableContainer
init|=
operator|new
name|HashMapWrapper
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|tableContainer
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeObject
argument_list|(
name|tableContainer
operator|.
name|getMetaData
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|tableContainer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MapJoinTableContainer
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Class
argument_list|<
name|?
extends|extends
name|MapJoinTableContainer
argument_list|>
name|clazz
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|MapJoinTableContainer
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
extends|extends
name|MapJoinTableContainer
argument_list|>
name|constructor
init|=
name|clazz
operator|.
name|getDeclaredConstructor
argument_list|(
name|Map
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|constructor
operator|.
name|newInstance
argument_list|(
name|metaData
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
literal|"Error while attemping to create table container"
operator|+
literal|" of type: "
operator|+
name|name
operator|+
literal|", with metaData: "
operator|+
name|metaData
decl_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

