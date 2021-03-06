begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
package|;
end_package

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
name|Iterator
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
name|hive
operator|.
name|metastore
operator|.
name|api
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * Use this to get Table objects for a table list. It provides an iterator to  * on the resulting Table objects. It batches the calls to  * IMetaStoreClient.getTableObjectsByName to avoid OOM issues in HS2 (with  * embedded metastore) or MetaStore server (if HS2 is using remote metastore).  *  */
end_comment

begin_class
specifier|public
class|class
name|TableIterable
implements|implements
name|Iterable
argument_list|<
name|Table
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Table
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|Table
argument_list|>
argument_list|()
block|{
specifier|private
specifier|final
name|Iterator
argument_list|<
name|String
argument_list|>
name|tableNamesIter
init|=
name|tableNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
specifier|private
name|Iterator
argument_list|<
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
name|Table
argument_list|>
name|batchIter
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
operator|(
operator|(
name|batchIter
operator|!=
literal|null
operator|)
operator|&&
name|batchIter
operator|.
name|hasNext
argument_list|()
operator|)
operator|||
name|tableNamesIter
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|next
parameter_list|()
block|{
if|if
condition|(
operator|(
name|batchIter
operator|==
literal|null
operator|)
operator|||
operator|!
name|batchIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|getNextBatch
argument_list|()
expr_stmt|;
block|}
return|return
name|batchIter
operator|.
name|next
argument_list|()
return|;
block|}
specifier|private
name|void
name|getNextBatch
parameter_list|()
block|{
comment|// get next batch of table names in this list
name|List
argument_list|<
name|String
argument_list|>
name|nameBatch
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|batchCounter
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|batchCounter
operator|<
name|batchSize
operator|&&
name|tableNamesIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|nameBatch
operator|.
name|add
argument_list|(
name|tableNamesIter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|batchCounter
operator|++
expr_stmt|;
block|}
comment|// get the Table objects for this batch of table names and get iterator
comment|// on it
try|try
block|{
if|if
condition|(
name|catName
operator|!=
literal|null
condition|)
block|{
name|batchIter
operator|=
name|msc
operator|.
name|getTableObjectsByName
argument_list|(
name|catName
argument_list|,
name|dbname
argument_list|,
name|nameBatch
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|batchIter
operator|=
name|msc
operator|.
name|getTableObjectsByName
argument_list|(
name|dbname
argument_list|,
name|nameBatch
argument_list|)
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"TableIterable is a read-only iterable and remove() is unsupported"
argument_list|)
throw|;
block|}
block|}
return|;
block|}
specifier|private
specifier|final
name|IMetaStoreClient
name|msc
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbname
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
decl_stmt|;
specifier|private
specifier|final
name|int
name|batchSize
decl_stmt|;
specifier|private
specifier|final
name|String
name|catName
decl_stmt|;
comment|/**    * Primary constructor that fetches all tables in a given msc, given a Hive    * object,a db name and a table name list.    */
specifier|public
name|TableIterable
parameter_list|(
name|IMetaStoreClient
name|msc
parameter_list|,
name|String
name|dbname
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
parameter_list|,
name|int
name|batchSize
parameter_list|)
throws|throws
name|TException
block|{
name|this
operator|.
name|msc
operator|=
name|msc
expr_stmt|;
name|this
operator|.
name|catName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|dbname
operator|=
name|dbname
expr_stmt|;
name|this
operator|.
name|tableNames
operator|=
name|tableNames
expr_stmt|;
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
block|}
specifier|public
name|TableIterable
parameter_list|(
name|IMetaStoreClient
name|msc
parameter_list|,
name|String
name|catName
parameter_list|,
name|String
name|dbname
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
parameter_list|,
name|int
name|batchSize
parameter_list|)
throws|throws
name|TException
block|{
name|this
operator|.
name|msc
operator|=
name|msc
expr_stmt|;
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
name|this
operator|.
name|dbname
operator|=
name|dbname
expr_stmt|;
name|this
operator|.
name|tableNames
operator|=
name|tableNames
expr_stmt|;
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
block|}
block|}
end_class

end_unit

