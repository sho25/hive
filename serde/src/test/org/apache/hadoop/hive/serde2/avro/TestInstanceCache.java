begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|serde2
operator|.
name|avro
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertSame
import|;
end_import

begin_class
specifier|public
class|class
name|TestInstanceCache
block|{
specifier|private
specifier|static
class|class
name|Foo
block|{
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|42
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Wrapper
parameter_list|<
name|T
parameter_list|>
block|{
specifier|public
specifier|final
name|T
name|wrapped
decl_stmt|;
specifier|private
name|Wrapper
parameter_list|(
name|T
name|wrapped
parameter_list|)
block|{
name|this
operator|.
name|wrapped
operator|=
name|wrapped
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|instanceCachesOnlyCreateOneInstance
parameter_list|()
throws|throws
name|AvroSerdeException
block|{
name|InstanceCache
argument_list|<
name|Foo
argument_list|,
name|Wrapper
argument_list|<
name|Foo
argument_list|>
argument_list|>
name|ic
init|=
operator|new
name|InstanceCache
argument_list|<
name|Foo
argument_list|,
name|Wrapper
argument_list|<
name|Foo
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Wrapper
name|makeInstance
parameter_list|(
name|Foo
name|hv
parameter_list|,
name|Set
argument_list|<
name|Foo
argument_list|>
name|seenSchemas
parameter_list|)
block|{
return|return
operator|new
name|Wrapper
argument_list|(
name|hv
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|Foo
name|f1
init|=
operator|new
name|Foo
argument_list|()
decl_stmt|;
name|Wrapper
name|fc
init|=
name|ic
operator|.
name|retrieve
argument_list|(
name|f1
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|f1
argument_list|,
name|fc
operator|.
name|wrapped
argument_list|)
expr_stmt|;
comment|// Our original foo should be in the wrapper
name|Foo
name|f2
init|=
operator|new
name|Foo
argument_list|()
decl_stmt|;
comment|// Different instance, same value
name|Wrapper
name|fc2
init|=
name|ic
operator|.
name|retrieve
argument_list|(
name|f2
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|fc2
argument_list|,
name|fc
argument_list|)
expr_stmt|;
comment|// Since equiv f, should get back first container
name|assertSame
argument_list|(
name|fc2
operator|.
name|wrapped
argument_list|,
name|f1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|instanceCacheReturnsCorrectInstances
parameter_list|()
throws|throws
name|AvroSerdeException
block|{
name|InstanceCache
argument_list|<
name|String
argument_list|,
name|Wrapper
argument_list|<
name|String
argument_list|>
argument_list|>
name|ic
init|=
operator|new
name|InstanceCache
argument_list|<
name|String
argument_list|,
name|Wrapper
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Wrapper
argument_list|<
name|String
argument_list|>
name|makeInstance
parameter_list|(
name|String
name|hv
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|seenSchemas
parameter_list|)
block|{
return|return
operator|new
name|Wrapper
argument_list|<
name|String
argument_list|>
argument_list|(
name|hv
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|Wrapper
argument_list|<
name|String
argument_list|>
name|one
init|=
name|ic
operator|.
name|retrieve
argument_list|(
literal|"one"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Wrapper
argument_list|<
name|String
argument_list|>
name|two
init|=
name|ic
operator|.
name|retrieve
argument_list|(
literal|"two"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Wrapper
argument_list|<
name|String
argument_list|>
name|anotherOne
init|=
name|ic
operator|.
name|retrieve
argument_list|(
literal|"one"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|one
argument_list|,
name|anotherOne
argument_list|)
expr_stmt|;
name|Wrapper
argument_list|<
name|String
argument_list|>
name|anotherTwo
init|=
name|ic
operator|.
name|retrieve
argument_list|(
literal|"two"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|two
argument_list|,
name|anotherTwo
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

