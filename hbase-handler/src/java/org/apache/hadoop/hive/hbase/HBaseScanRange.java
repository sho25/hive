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
name|hbase
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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|client
operator|.
name|Scan
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
name|filter
operator|.
name|Filter
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
name|filter
operator|.
name|FilterList
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
name|BytesWritable
import|;
end_import

begin_class
specifier|public
class|class
name|HBaseScanRange
implements|implements
name|Serializable
block|{
specifier|private
name|byte
index|[]
name|startRow
decl_stmt|;
specifier|private
name|byte
index|[]
name|stopRow
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FilterDesc
argument_list|>
name|filterDescs
init|=
operator|new
name|ArrayList
argument_list|<
name|FilterDesc
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|startRow
return|;
block|}
specifier|public
name|void
name|setStartRow
parameter_list|(
name|byte
index|[]
name|startRow
parameter_list|)
block|{
name|this
operator|.
name|startRow
operator|=
name|startRow
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getStopRow
parameter_list|()
block|{
return|return
name|stopRow
return|;
block|}
specifier|public
name|void
name|setStopRow
parameter_list|(
name|byte
index|[]
name|stopRow
parameter_list|)
block|{
name|this
operator|.
name|stopRow
operator|=
name|stopRow
expr_stmt|;
block|}
specifier|public
name|void
name|addFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Filter
argument_list|>
name|clazz
init|=
name|filter
operator|.
name|getClass
argument_list|()
decl_stmt|;
name|clazz
operator|.
name|getMethod
argument_list|(
literal|"parseFrom"
argument_list|,
name|byte
index|[]
operator|.
expr|class
argument_list|)
expr_stmt|;
comment|// valiade
name|filterDescs
operator|.
name|add
argument_list|(
operator|new
name|FilterDesc
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
argument_list|,
name|filter
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setup
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|setup
argument_list|(
name|scan
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setup
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|boolean
name|filterOnly
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|filterOnly
condition|)
block|{
comment|// Set the start and stop rows only if asked to
if|if
condition|(
name|startRow
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|startRow
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|stopRow
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|stopRow
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|filterDescs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|filterDescs
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|scan
operator|.
name|setFilter
argument_list|(
name|filterDescs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toFilter
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
operator|new
name|ArrayList
argument_list|<
name|Filter
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|FilterDesc
name|filter
range|:
name|filterDescs
control|)
block|{
name|filters
operator|.
name|add
argument_list|(
name|filter
operator|.
name|toFilter
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|FilterList
argument_list|(
name|filters
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|(
name|startRow
operator|==
literal|null
condition|?
literal|""
else|:
operator|new
name|BytesWritable
argument_list|(
name|startRow
argument_list|)
operator|.
name|toString
argument_list|()
operator|)
operator|+
literal|" ~ "
operator|+
operator|(
name|stopRow
operator|==
literal|null
condition|?
literal|""
else|:
operator|new
name|BytesWritable
argument_list|(
name|stopRow
argument_list|)
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
specifier|private
specifier|static
class|class
name|FilterDesc
implements|implements
name|Serializable
block|{
specifier|private
name|String
name|className
decl_stmt|;
specifier|private
name|byte
index|[]
name|binary
decl_stmt|;
specifier|public
name|FilterDesc
parameter_list|(
name|String
name|className
parameter_list|,
name|byte
index|[]
name|binary
parameter_list|)
block|{
name|this
operator|.
name|className
operator|=
name|className
expr_stmt|;
name|this
operator|.
name|binary
operator|=
name|binary
expr_stmt|;
block|}
specifier|public
name|Filter
name|toFilter
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|(
name|Filter
operator|)
name|getFactoryMethod
argument_list|(
name|className
argument_list|,
name|conf
argument_list|)
operator|.
name|invoke
argument_list|(
literal|null
argument_list|,
name|binary
argument_list|)
return|;
block|}
specifier|private
name|Method
name|getFactoryMethod
parameter_list|(
name|String
name|className
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|Exception
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|conf
operator|.
name|getClassByName
argument_list|(
name|className
argument_list|)
decl_stmt|;
return|return
name|clazz
operator|.
name|getMethod
argument_list|(
literal|"parseFrom"
argument_list|,
name|byte
index|[]
operator|.
expr|class
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

