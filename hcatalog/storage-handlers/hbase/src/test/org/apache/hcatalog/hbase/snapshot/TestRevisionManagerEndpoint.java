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
name|Collections
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ToStringBuilder
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
name|ToStringStyle
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
name|hbase
operator|.
name|coprocessor
operator|.
name|CoprocessorHost
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
name|SkeletonHBaseTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestRevisionManagerEndpoint
extends|extends
name|SkeletonHBaseTest
block|{
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// test case specific mini cluster settings
name|testConf
operator|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|testConf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
literal|"org.apache.hcatalog.hbase.snapshot.RevisionManagerEndpoint"
argument_list|,
literal|"org.apache.hadoop.hbase.coprocessor.GenericEndpoint"
argument_list|)
expr_stmt|;
name|testConf
operator|.
name|set
argument_list|(
name|RMConstants
operator|.
name|REVISION_MGR_ENDPOINT_IMPL_CLASS
argument_list|,
name|MockRM
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|setupSkeletonHBaseTest
argument_list|()
expr_stmt|;
block|}
comment|/**    * Mock implementation to test the protocol/serialization    */
specifier|public
specifier|static
class|class
name|MockRM
implements|implements
name|RevisionManager
block|{
specifier|private
specifier|static
class|class
name|Invocation
block|{
name|Invocation
parameter_list|(
name|String
name|methodName
parameter_list|,
name|Object
name|ret
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|this
operator|.
name|methodName
operator|=
name|methodName
expr_stmt|;
name|this
operator|.
name|args
operator|=
name|args
expr_stmt|;
name|this
operator|.
name|ret
operator|=
name|ret
expr_stmt|;
block|}
name|String
name|methodName
decl_stmt|;
name|Object
index|[]
name|args
decl_stmt|;
name|Object
name|ret
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|equals
parameter_list|(
name|Object
name|obj1
parameter_list|,
name|Object
name|obj2
parameter_list|)
block|{
if|if
condition|(
name|obj1
operator|==
name|obj2
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|obj1
operator|==
literal|null
operator|||
name|obj2
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|obj1
operator|instanceof
name|Transaction
operator|||
name|obj1
operator|instanceof
name|TableSnapshot
condition|)
block|{
return|return
name|obj1
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|obj2
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
return|return
name|obj1
operator|.
name|equals
argument_list|(
name|obj2
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|Invocation
name|other
init|=
operator|(
name|Invocation
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|this
operator|==
name|other
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|other
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|args
operator|!=
name|other
operator|.
name|args
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|args
operator|==
literal|null
operator|||
name|other
operator|.
name|args
operator|==
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|this
operator|.
name|args
operator|.
name|length
operator|!=
name|other
operator|.
name|args
operator|.
name|length
condition|)
return|return
literal|false
return|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
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
name|equals
argument_list|(
name|this
operator|.
name|args
index|[
name|i
index|]
argument_list|,
name|other
operator|.
name|args
index|[
name|i
index|]
argument_list|)
condition|)
return|return
literal|false
return|;
block|}
block|}
return|return
name|equals
argument_list|(
name|this
operator|.
name|ret
argument_list|,
name|other
operator|.
name|ret
argument_list|)
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
operator|new
name|ToStringBuilder
argument_list|(
name|this
argument_list|,
name|ToStringStyle
operator|.
name|SHORT_PREFIX_STYLE
argument_list|)
operator|.
name|append
argument_list|(
literal|"method"
argument_list|,
name|this
operator|.
name|methodName
argument_list|)
operator|.
name|append
argument_list|(
literal|"args"
argument_list|,
name|this
operator|.
name|args
argument_list|)
operator|.
name|append
argument_list|(
literal|"returns"
argument_list|,
name|this
operator|.
name|ret
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|final
specifier|static
name|String
name|DEFAULT_INSTANCE
init|=
literal|"default"
decl_stmt|;
specifier|final
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|MockRM
argument_list|>
name|INSTANCES
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|MockRM
argument_list|>
argument_list|()
decl_stmt|;
name|Invocation
name|lastCall
decl_stmt|;
name|boolean
name|isOpen
init|=
literal|false
decl_stmt|;
specifier|private
parameter_list|<
name|T
extends|extends
name|Object
parameter_list|>
name|T
name|recordCall
parameter_list|(
name|T
name|result
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|StackTraceElement
index|[]
name|stackTrace
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
name|lastCall
operator|=
operator|new
name|Invocation
argument_list|(
name|stackTrace
index|[
literal|2
index|]
operator|.
name|getMethodName
argument_list|()
argument_list|,
name|result
argument_list|,
name|args
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|!
name|INSTANCES
operator|.
name|containsKey
argument_list|(
name|DEFAULT_INSTANCE
argument_list|)
condition|)
name|INSTANCES
operator|.
name|put
argument_list|(
name|DEFAULT_INSTANCE
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|IOException
block|{
name|isOpen
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|isOpen
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createTable
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columnFamilies
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|dropTable
parameter_list|(
name|String
name|table
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|Transaction
name|beginWriteTransaction
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|families
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|recordCall
argument_list|(
operator|new
name|Transaction
argument_list|(
name|table
argument_list|,
name|families
argument_list|,
literal|0L
argument_list|,
literal|0L
argument_list|)
argument_list|,
name|table
argument_list|,
name|families
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Transaction
name|beginWriteTransaction
parameter_list|(
name|String
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|families
parameter_list|,
name|Long
name|keepAlive
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|recordCall
argument_list|(
operator|new
name|Transaction
argument_list|(
name|table
argument_list|,
name|families
argument_list|,
literal|0L
argument_list|,
literal|0L
argument_list|)
argument_list|,
name|table
argument_list|,
name|families
argument_list|,
name|keepAlive
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitWriteTransaction
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|abortWriteTransaction
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|FamilyRevision
argument_list|>
name|getAbortedWriteTransactions
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|columnFamily
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|FamilyRevision
argument_list|(
literal|0L
argument_list|,
literal|0L
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableSnapshot
name|createSnapshot
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createSnapshot
argument_list|(
name|tableName
argument_list|,
literal|0L
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableSnapshot
name|createSnapshot
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Long
name|revision
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSnapshot
name|ret
init|=
operator|new
name|TableSnapshot
argument_list|(
name|tableName
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|()
argument_list|,
name|revision
argument_list|)
decl_stmt|;
return|return
name|recordCall
argument_list|(
name|ret
argument_list|,
name|tableName
argument_list|,
name|revision
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|keepAlive
parameter_list|(
name|Transaction
name|transaction
parameter_list|)
throws|throws
name|IOException
block|{
name|recordCall
argument_list|(
literal|null
argument_list|,
name|transaction
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRevisionManagerProtocol
parameter_list|()
throws|throws
name|Throwable
block|{
name|Configuration
name|conf
init|=
name|getHbaseConf
argument_list|()
decl_stmt|;
name|RevisionManager
name|rm
init|=
name|RevisionManagerFactory
operator|.
name|getOpenedRevisionManager
argument_list|(
name|RevisionManagerEndpointClient
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|MockRM
name|mockImpl
init|=
name|MockRM
operator|.
name|INSTANCES
operator|.
name|get
argument_list|(
name|MockRM
operator|.
name|DEFAULT_INSTANCE
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|mockImpl
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|mockImpl
operator|.
name|isOpen
argument_list|)
expr_stmt|;
name|Transaction
name|t
init|=
operator|new
name|Transaction
argument_list|(
literal|"t1"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"f1"
argument_list|,
literal|"f2"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|MockRM
operator|.
name|Invocation
name|call
init|=
operator|new
name|MockRM
operator|.
name|Invocation
argument_list|(
literal|"keepAlive"
argument_list|,
literal|null
argument_list|,
name|t
argument_list|)
decl_stmt|;
name|rm
operator|.
name|keepAlive
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|call
operator|.
name|methodName
argument_list|,
name|call
argument_list|,
name|mockImpl
operator|.
name|lastCall
argument_list|)
expr_stmt|;
name|t
operator|=
operator|new
name|Transaction
argument_list|(
literal|"t2"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"f21"
argument_list|,
literal|"f22"
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|call
operator|=
operator|new
name|MockRM
operator|.
name|Invocation
argument_list|(
literal|"beginWriteTransaction"
argument_list|,
literal|null
argument_list|,
name|t
operator|.
name|getTableName
argument_list|()
argument_list|,
name|t
operator|.
name|getColumnFamilies
argument_list|()
argument_list|)
expr_stmt|;
name|call
operator|.
name|ret
operator|=
name|rm
operator|.
name|beginWriteTransaction
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|,
name|t
operator|.
name|getColumnFamilies
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|call
operator|.
name|methodName
argument_list|,
name|call
argument_list|,
name|mockImpl
operator|.
name|lastCall
argument_list|)
expr_stmt|;
name|call
operator|=
operator|new
name|MockRM
operator|.
name|Invocation
argument_list|(
literal|"createSnapshot"
argument_list|,
literal|null
argument_list|,
literal|"t3"
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|call
operator|.
name|ret
operator|=
name|rm
operator|.
name|createSnapshot
argument_list|(
literal|"t3"
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|call
operator|.
name|methodName
argument_list|,
name|call
argument_list|,
name|mockImpl
operator|.
name|lastCall
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

