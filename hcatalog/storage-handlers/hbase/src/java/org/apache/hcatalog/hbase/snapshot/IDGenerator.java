begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
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
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|lock
operator|.
name|LockListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|lock
operator|.
name|WriteLock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooDefs
operator|.
name|Ids
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|ZooKeeper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|data
operator|.
name|Stat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * This class generates revision id's for transactions.  */
end_comment

begin_class
class|class
name|IDGenerator
implements|implements
name|LockListener
block|{
specifier|private
name|ZooKeeper
name|zookeeper
decl_stmt|;
specifier|private
name|String
name|zNodeDataLoc
decl_stmt|;
specifier|private
name|String
name|zNodeLockBasePath
decl_stmt|;
specifier|private
name|long
name|id
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|IDGenerator
operator|.
name|class
argument_list|)
decl_stmt|;
name|IDGenerator
parameter_list|(
name|ZooKeeper
name|zookeeper
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|idGenNode
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|zookeeper
operator|=
name|zookeeper
expr_stmt|;
name|this
operator|.
name|zNodeDataLoc
operator|=
name|idGenNode
expr_stmt|;
name|this
operator|.
name|zNodeLockBasePath
operator|=
name|PathUtil
operator|.
name|getLockManagementNode
argument_list|(
name|idGenNode
argument_list|)
expr_stmt|;
block|}
comment|/**    * This method obtains a revision id for a transaction.    *    * @return revision ID    * @throws IOException    */
specifier|public
name|long
name|obtainID
parameter_list|()
throws|throws
name|IOException
block|{
name|WriteLock
name|wLock
init|=
operator|new
name|WriteLock
argument_list|(
name|zookeeper
argument_list|,
name|zNodeLockBasePath
argument_list|,
name|Ids
operator|.
name|OPEN_ACL_UNSAFE
argument_list|)
decl_stmt|;
name|wLock
operator|.
name|setLockListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
try|try
block|{
name|boolean
name|lockGrabbed
init|=
name|wLock
operator|.
name|lock
argument_list|()
decl_stmt|;
if|if
condition|(
name|lockGrabbed
operator|==
literal|false
condition|)
block|{
comment|//TO DO : Let this request queue up and try obtaining lock.
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to obtain lock to obtain id."
argument_list|)
throw|;
block|}
else|else
block|{
name|id
operator|=
name|incrementAndReadCounter
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while obtaining lock for ID."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception while obtaining lock for ID."
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while obtaining lock for ID."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception while obtaining lock for ID."
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|wLock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
return|return
name|id
return|;
block|}
comment|/**    * This method reads the latest revision ID that has been used. The ID    * returned by this method cannot be used for transaction.    * @return revision ID    * @throws IOException    */
specifier|public
name|long
name|readID
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|curId
decl_stmt|;
try|try
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|zookeeper
operator|.
name|getData
argument_list|(
name|this
operator|.
name|zNodeDataLoc
argument_list|,
literal|false
argument_list|,
name|stat
argument_list|)
decl_stmt|;
name|curId
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
operator|new
name|String
argument_list|(
name|data
argument_list|,
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while reading current revision id."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception while reading current revision id."
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while reading current revision id."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception while reading current revision id."
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|curId
return|;
block|}
specifier|private
name|long
name|incrementAndReadCounter
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|curId
decl_stmt|,
name|usedId
decl_stmt|;
try|try
block|{
name|Stat
name|stat
init|=
operator|new
name|Stat
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|zookeeper
operator|.
name|getData
argument_list|(
name|this
operator|.
name|zNodeDataLoc
argument_list|,
literal|false
argument_list|,
name|stat
argument_list|)
decl_stmt|;
name|usedId
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
operator|(
operator|new
name|String
argument_list|(
name|data
argument_list|,
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
operator|)
argument_list|)
expr_stmt|;
name|curId
operator|=
name|usedId
operator|+
literal|1
expr_stmt|;
name|String
name|lastUsedID
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|curId
argument_list|)
decl_stmt|;
name|zookeeper
operator|.
name|setData
argument_list|(
name|this
operator|.
name|zNodeDataLoc
argument_list|,
name|lastUsedID
operator|.
name|getBytes
argument_list|(
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while incrementing revision id."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception while incrementing revision id. "
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception while incrementing revision id."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception while incrementing revision id. "
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|curId
return|;
block|}
comment|/*    * @see org.apache.hcatalog.hbase.snapshot.lock.LockListener#lockAcquired()    */
annotation|@
name|Override
specifier|public
name|void
name|lockAcquired
parameter_list|()
block|{     }
comment|/*    * @see org.apache.hcatalog.hbase.snapshot.lock.LockListener#lockReleased()    */
annotation|@
name|Override
specifier|public
name|void
name|lockReleased
parameter_list|()
block|{    }
block|}
end_class

end_unit

