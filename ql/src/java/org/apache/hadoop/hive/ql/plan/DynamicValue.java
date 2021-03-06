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
name|ql
operator|.
name|plan
package|;
end_package

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
name|hive
operator|.
name|common
operator|.
name|NoDynamicValuesException
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
name|exec
operator|.
name|DynamicValueRegistry
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
name|exec
operator|.
name|ObjectCache
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
name|exec
operator|.
name|ObjectCacheFactory
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
name|exec
operator|.
name|Utilities
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
name|io
operator|.
name|sarg
operator|.
name|LiteralDelegate
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
name|PrimitiveObjectInspector
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
name|typeinfo
operator|.
name|TypeInfo
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
name|typeinfo
operator|.
name|TypeInfoUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_class
specifier|public
class|class
name|DynamicValue
implements|implements
name|LiteralDelegate
implements|,
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
specifier|final
name|String
name|DYNAMIC_VALUE_REGISTRY_CACHE_KEY
init|=
literal|"DynamicValueRegistry"
decl_stmt|;
specifier|protected
specifier|transient
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|String
name|id
decl_stmt|;
name|TypeInfo
name|typeInfo
decl_stmt|;
name|PrimitiveObjectInspector
name|objectInspector
decl_stmt|;
specifier|transient
specifier|protected
name|Object
name|val
decl_stmt|;
specifier|transient
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
specifier|public
name|DynamicValue
parameter_list|(
name|String
name|id
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
name|this
operator|.
name|objectInspector
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|TypeInfo
name|getTypeInfo
parameter_list|()
block|{
return|return
name|typeInfo
return|;
block|}
specifier|public
name|void
name|setTypeInfo
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
block|{
name|this
operator|.
name|typeInfo
operator|=
name|typeInfo
expr_stmt|;
block|}
specifier|public
name|PrimitiveObjectInspector
name|getObjectInspector
parameter_list|()
block|{
return|return
name|objectInspector
return|;
block|}
specifier|public
name|void
name|setObjectInspector
parameter_list|(
name|PrimitiveObjectInspector
name|objectInspector
parameter_list|)
block|{
name|this
operator|.
name|objectInspector
operator|=
name|objectInspector
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
name|void
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getLiteral
parameter_list|()
block|{
return|return
name|getJavaValue
argument_list|()
return|;
block|}
specifier|public
name|Object
name|getJavaValue
parameter_list|()
block|{
return|return
name|objectInspector
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|getValue
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Object
name|getWritableValue
parameter_list|()
block|{
return|return
name|objectInspector
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|getValue
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Object
name|getValue
parameter_list|()
block|{
if|if
condition|(
name|initialized
condition|)
block|{
return|return
name|val
return|;
block|}
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoDynamicValuesException
argument_list|(
literal|"Cannot retrieve dynamic value "
operator|+
name|id
operator|+
literal|" - no conf set"
argument_list|)
throw|;
block|}
try|try
block|{
comment|// Get object cache
name|String
name|queryId
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
decl_stmt|;
name|ObjectCache
name|cache
init|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|conf
argument_list|,
name|queryId
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|cache
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Get the registry
name|DynamicValueRegistry
name|valueRegistry
init|=
name|cache
operator|.
name|retrieve
argument_list|(
name|DYNAMIC_VALUE_REGISTRY_CACHE_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueRegistry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoDynamicValuesException
argument_list|(
literal|"DynamicValueRegistry not available"
argument_list|)
throw|;
block|}
name|val
operator|=
name|valueRegistry
operator|.
name|getValue
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoDynamicValuesException
name|err
parameter_list|)
block|{
throw|throw
name|err
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to retrieve dynamic value for "
operator|+
name|id
argument_list|,
name|err
argument_list|)
throw|;
block|}
return|return
name|val
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
comment|// If the id is a generated unique ID then this could affect .q file golden files for tests that run EXPLAIN queries.
return|return
literal|"DynamicValue("
operator|+
name|id
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

