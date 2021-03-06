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
name|ddl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
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
name|Modifier
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
name|Map
import|;
end_import

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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|QueryState
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
name|parse
operator|.
name|ASTNode
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
name|BaseSemanticAnalyzer
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
name|CalcitePlanner
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
name|HiveOperation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|reflections
operator|.
name|Reflections
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
name|annotations
operator|.
name|VisibleForTesting
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
name|collect
operator|.
name|Sets
import|;
end_import

begin_comment
comment|/**  * Manages the DDL command analyzers.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|DDLSemanticAnalyzerFactory
block|{
specifier|private
name|DDLSemanticAnalyzerFactory
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"DDLSemanticAnalyzerFactory should not be instantiated"
argument_list|)
throw|;
block|}
comment|/**    * Annotation for the handled type by the analyzer.    */
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
specifier|public
annotation_defn|@interface
name|DDLType
block|{
name|int
index|[]
name|types
argument_list|()
expr|default
block|{}
expr_stmt|;
block|}
comment|/**    * Reveals the actual type of an ASTTree that has a category as main element.    */
specifier|public
interface|interface
name|DDLSemanticAnalyzerCategory
block|{
name|int
name|getType
parameter_list|(
name|ASTNode
name|root
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|DDL_ROOT
init|=
literal|"org.apache.hadoop.hive.ql.ddl"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
argument_list|>
name|TYPE_TO_ANALYZER
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|DDLSemanticAnalyzerCategory
argument_list|>
argument_list|>
name|TYPE_TO_ANALYZERCATEGORY
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
argument_list|>
name|analyzerClasses1
init|=
operator|new
name|Reflections
argument_list|(
name|DDL_ROOT
argument_list|)
operator|.
name|getSubTypesOf
argument_list|(
name|BaseSemanticAnalyzer
operator|.
name|class
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|CalcitePlanner
argument_list|>
argument_list|>
name|analyzerClasses2
init|=
operator|new
name|Reflections
argument_list|(
name|DDL_ROOT
argument_list|)
operator|.
name|getSubTypesOf
argument_list|(
name|CalcitePlanner
operator|.
name|class
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
argument_list|>
name|analyzerClasses
init|=
name|Sets
operator|.
name|union
argument_list|(
name|analyzerClasses1
argument_list|,
name|analyzerClasses2
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
name|analyzerClass
range|:
name|analyzerClasses
control|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isAbstract
argument_list|(
name|analyzerClass
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|DDLType
name|ddlType
init|=
name|analyzerClass
operator|.
name|getAnnotation
argument_list|(
name|DDLType
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|type
range|:
name|ddlType
operator|.
name|types
argument_list|()
control|)
block|{
if|if
condition|(
name|TYPE_TO_ANALYZER
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Type "
operator|+
name|type
operator|+
literal|" is declared more than once in different DDLType annotations."
argument_list|)
throw|;
block|}
name|TYPE_TO_ANALYZER
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|analyzerClass
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|DDLSemanticAnalyzerCategory
argument_list|>
argument_list|>
name|analyzerCategoryClasses
init|=
operator|new
name|Reflections
argument_list|(
name|DDL_ROOT
argument_list|)
operator|.
name|getSubTypesOf
argument_list|(
name|DDLSemanticAnalyzerCategory
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|DDLSemanticAnalyzerCategory
argument_list|>
name|analyzerCategoryClass
range|:
name|analyzerCategoryClasses
control|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isAbstract
argument_list|(
name|analyzerCategoryClass
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|DDLType
name|ddlType
init|=
name|analyzerCategoryClass
operator|.
name|getAnnotation
argument_list|(
name|DDLType
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|type
range|:
name|ddlType
operator|.
name|types
argument_list|()
control|)
block|{
if|if
condition|(
name|TYPE_TO_ANALYZERCATEGORY
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Type "
operator|+
name|type
operator|+
literal|" is declared more than once in different DDLType annotations for categories."
argument_list|)
throw|;
block|}
name|TYPE_TO_ANALYZERCATEGORY
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|analyzerCategoryClass
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|boolean
name|handles
parameter_list|(
name|ASTNode
name|root
parameter_list|)
block|{
return|return
name|getAnalyzerClass
argument_list|(
name|root
argument_list|,
literal|null
argument_list|)
operator|!=
literal|null
return|;
block|}
specifier|public
specifier|static
name|BaseSemanticAnalyzer
name|getAnalyzer
parameter_list|(
name|ASTNode
name|root
parameter_list|,
name|QueryState
name|queryState
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
name|analyzerClass
init|=
name|getAnalyzerClass
argument_list|(
name|root
argument_list|,
name|queryState
argument_list|)
decl_stmt|;
try|try
block|{
name|BaseSemanticAnalyzer
name|analyzer
init|=
name|analyzerClass
operator|.
name|getConstructor
argument_list|(
name|QueryState
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|queryState
argument_list|)
decl_stmt|;
return|return
name|analyzer
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
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
name|VisibleForTesting
specifier|public
specifier|static
name|BaseSemanticAnalyzer
name|getAnalyzer
parameter_list|(
name|ASTNode
name|root
parameter_list|,
name|QueryState
name|queryState
parameter_list|,
name|Hive
name|db
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
name|analyzerClass
init|=
name|getAnalyzerClass
argument_list|(
name|root
argument_list|,
name|queryState
argument_list|)
decl_stmt|;
try|try
block|{
name|BaseSemanticAnalyzer
name|analyzer
init|=
name|analyzerClass
operator|.
name|getConstructor
argument_list|(
name|QueryState
operator|.
name|class
argument_list|,
name|Hive
operator|.
name|class
argument_list|)
operator|.
name|newInstance
argument_list|(
name|queryState
argument_list|,
name|db
argument_list|)
decl_stmt|;
return|return
name|analyzer
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|BaseSemanticAnalyzer
argument_list|>
name|getAnalyzerClass
parameter_list|(
name|ASTNode
name|root
parameter_list|,
name|QueryState
name|queryState
parameter_list|)
block|{
if|if
condition|(
name|TYPE_TO_ANALYZER
operator|.
name|containsKey
argument_list|(
name|root
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|TYPE_TO_ANALYZER
operator|.
name|get
argument_list|(
name|root
operator|.
name|getType
argument_list|()
argument_list|)
return|;
block|}
if|if
condition|(
name|TYPE_TO_ANALYZERCATEGORY
operator|.
name|containsKey
argument_list|(
name|root
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|DDLSemanticAnalyzerCategory
argument_list|>
name|analyzerCategoryClass
init|=
name|TYPE_TO_ANALYZERCATEGORY
operator|.
name|get
argument_list|(
name|root
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|DDLSemanticAnalyzerCategory
name|analyzerCategory
init|=
name|analyzerCategoryClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|int
name|actualType
init|=
name|analyzerCategory
operator|.
name|getType
argument_list|(
name|root
argument_list|)
decl_stmt|;
if|if
condition|(
name|TYPE_TO_ANALYZER
operator|.
name|containsKey
argument_list|(
name|actualType
argument_list|)
condition|)
block|{
if|if
condition|(
name|queryState
operator|!=
literal|null
condition|)
block|{
name|queryState
operator|.
name|setCommandType
argument_list|(
name|HiveOperation
operator|.
name|operationForToken
argument_list|(
name|actualType
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|TYPE_TO_ANALYZER
operator|.
name|get
argument_list|(
name|actualType
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

