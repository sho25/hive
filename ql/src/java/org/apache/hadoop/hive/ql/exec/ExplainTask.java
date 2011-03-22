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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintStream
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

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|fs
operator|.
name|Path
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
name|Context
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
name|DriverContext
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
name|Explain
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
name|ExplainWork
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
name|api
operator|.
name|StageType
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
name|IOUtils
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * ExplainTask implementation.  *  **/
end_comment

begin_class
specifier|public
class|class
name|ExplainTask
extends|extends
name|Task
argument_list|<
name|ExplainWork
argument_list|>
implements|implements
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
name|ExplainTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|PrintStream
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Path
name|resFile
init|=
operator|new
name|Path
argument_list|(
name|work
operator|.
name|getResFile
argument_list|()
argument_list|)
decl_stmt|;
name|OutputStream
name|outS
init|=
name|resFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|create
argument_list|(
name|resFile
argument_list|)
decl_stmt|;
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
name|outS
argument_list|)
expr_stmt|;
comment|// Print out the parse AST
name|outputAST
argument_list|(
name|work
operator|.
name|getAstStringTree
argument_list|()
argument_list|,
name|out
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
name|outputDependencies
argument_list|(
name|out
argument_list|,
name|work
operator|.
name|getRootTasks
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
comment|// Go over all the tasks and dump out the plans
name|outputStagePlans
argument_list|(
name|out
argument_list|,
name|work
operator|.
name|getRootTasks
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|=
literal|null
expr_stmt|;
return|return
operator|(
literal|0
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed with exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"\n"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|(
literal|1
operator|)
return|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|indentString
parameter_list|(
name|int
name|indent
parameter_list|)
block|{
name|StringBuilder
name|sb
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
name|indent
condition|;
operator|++
name|i
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
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
specifier|private
name|void
name|outputMap
parameter_list|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|mp
parameter_list|,
name|String
name|header
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|boolean
name|extended
parameter_list|,
name|int
name|indent
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|first_el
init|=
literal|true
decl_stmt|;
name|TreeMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|tree
init|=
operator|new
name|TreeMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|tree
operator|.
name|putAll
argument_list|(
name|mp
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|ent
range|:
name|tree
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|first_el
condition|)
block|{
name|out
operator|.
name|println
argument_list|(
name|header
argument_list|)
expr_stmt|;
block|}
name|first_el
operator|=
literal|false
expr_stmt|;
comment|// Print the key
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|printf
argument_list|(
literal|"%s "
argument_list|,
name|ent
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Print the value
if|if
condition|(
name|isPrintable
argument_list|(
name|ent
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ent
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ent
operator|.
name|getValue
argument_list|()
operator|instanceof
name|List
operator|||
name|ent
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Map
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
name|ent
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ent
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Serializable
condition|)
block|{
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
name|outputPlan
argument_list|(
operator|(
name|Serializable
operator|)
name|ent
operator|.
name|getValue
argument_list|()
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|outputList
parameter_list|(
name|List
argument_list|<
name|?
argument_list|>
name|l
parameter_list|,
name|String
name|header
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|boolean
name|extended
parameter_list|,
name|int
name|indent
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|first_el
init|=
literal|true
decl_stmt|;
name|boolean
name|nl
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|l
control|)
block|{
if|if
condition|(
name|first_el
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
name|header
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isPrintable
argument_list|(
name|o
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|first_el
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|print
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|print
argument_list|(
name|o
argument_list|)
expr_stmt|;
name|nl
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|o
operator|instanceof
name|Serializable
condition|)
block|{
if|if
condition|(
name|first_el
condition|)
block|{
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
name|outputPlan
argument_list|(
operator|(
name|Serializable
operator|)
name|o
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
block|}
name|first_el
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|nl
condition|)
block|{
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|isPrintable
parameter_list|(
name|Object
name|val
parameter_list|)
block|{
if|if
condition|(
name|val
operator|instanceof
name|Boolean
operator|||
name|val
operator|instanceof
name|String
operator|||
name|val
operator|instanceof
name|Integer
operator|||
name|val
operator|instanceof
name|Byte
operator|||
name|val
operator|instanceof
name|Float
operator|||
name|val
operator|instanceof
name|Double
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|val
operator|!=
literal|null
operator|&&
name|val
operator|.
name|getClass
argument_list|()
operator|.
name|isPrimitive
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|void
name|outputPlan
parameter_list|(
name|Serializable
name|work
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|boolean
name|extended
parameter_list|,
name|int
name|indent
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Check if work has an explain annotation
name|Annotation
name|note
init|=
name|work
operator|.
name|getClass
argument_list|()
operator|.
name|getAnnotation
argument_list|(
name|Explain
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|note
operator|instanceof
name|Explain
condition|)
block|{
name|Explain
name|xpl_note
init|=
operator|(
name|Explain
operator|)
name|note
decl_stmt|;
if|if
condition|(
name|extended
operator|||
name|xpl_note
operator|.
name|normalExplain
argument_list|()
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|xpl_note
operator|.
name|displayName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If this is an operator then we need to call the plan generation on the
comment|// conf and then
comment|// the children
if|if
condition|(
name|work
operator|instanceof
name|Operator
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|operator
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|work
decl_stmt|;
if|if
condition|(
name|operator
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|outputPlan
argument_list|(
name|operator
operator|.
name|getConf
argument_list|()
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|indent
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
range|:
name|operator
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|outputPlan
argument_list|(
name|op
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
block|}
comment|// We look at all methods that generate values for explain
name|Method
index|[]
name|methods
init|=
name|work
operator|.
name|getClass
argument_list|()
operator|.
name|getMethods
argument_list|()
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|methods
argument_list|,
operator|new
name|MethodComparator
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Method
name|m
range|:
name|methods
control|)
block|{
name|int
name|prop_indents
init|=
name|indent
operator|+
literal|2
decl_stmt|;
name|note
operator|=
name|m
operator|.
name|getAnnotation
argument_list|(
name|Explain
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|note
operator|instanceof
name|Explain
condition|)
block|{
name|Explain
name|xpl_note
init|=
operator|(
name|Explain
operator|)
name|note
decl_stmt|;
if|if
condition|(
name|extended
operator|||
name|xpl_note
operator|.
name|normalExplain
argument_list|()
condition|)
block|{
name|Object
name|val
init|=
name|m
operator|.
name|invoke
argument_list|(
name|work
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|String
name|header
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|xpl_note
operator|.
name|displayName
argument_list|()
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|header
operator|=
name|indentString
argument_list|(
name|prop_indents
argument_list|)
operator|+
name|xpl_note
operator|.
name|displayName
argument_list|()
operator|+
literal|":"
expr_stmt|;
block|}
else|else
block|{
name|prop_indents
operator|=
name|indent
expr_stmt|;
name|header
operator|=
name|indentString
argument_list|(
name|prop_indents
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isPrintable
argument_list|(
name|val
argument_list|)
condition|)
block|{
name|out
operator|.
name|printf
argument_list|(
literal|"%s "
argument_list|,
name|header
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|val
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Try this as a map
try|try
block|{
comment|// Go through the map and print out the stuff
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|mp
init|=
operator|(
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|val
decl_stmt|;
name|outputMap
argument_list|(
name|mp
argument_list|,
name|header
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|prop_indents
operator|+
literal|2
argument_list|)
expr_stmt|;
continue|continue;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|ce
parameter_list|)
block|{
comment|// Ignore - all this means is that this is not a map
block|}
comment|// Try this as a list
try|try
block|{
name|List
argument_list|<
name|?
argument_list|>
name|l
init|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|val
decl_stmt|;
name|outputList
argument_list|(
name|l
argument_list|,
name|header
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|prop_indents
operator|+
literal|2
argument_list|)
expr_stmt|;
continue|continue;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|ce
parameter_list|)
block|{
comment|// Ignore
block|}
comment|// Finally check if it is serializable
try|try
block|{
name|Serializable
name|s
init|=
operator|(
name|Serializable
operator|)
name|val
decl_stmt|;
name|out
operator|.
name|println
argument_list|(
name|header
argument_list|)
expr_stmt|;
name|outputPlan
argument_list|(
name|s
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|prop_indents
operator|+
literal|2
argument_list|)
expr_stmt|;
continue|continue;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|ce
parameter_list|)
block|{
comment|// Ignore
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|outputPlan
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|boolean
name|extended
parameter_list|,
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|displayedSet
parameter_list|,
name|int
name|indent
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|displayedSet
operator|.
name|contains
argument_list|(
name|task
argument_list|)
condition|)
block|{
return|return;
block|}
name|displayedSet
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|printf
argument_list|(
literal|"Stage: %s\n"
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Start by getting the work part of the task and call the output plan for
comment|// the work
name|outputPlan
argument_list|(
name|task
operator|.
name|getWork
argument_list|()
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
if|if
condition|(
name|task
operator|instanceof
name|ConditionalTask
operator|&&
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|con
range|:
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
control|)
block|{
name|outputPlan
argument_list|(
name|con
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|displayedSet
argument_list|,
name|indent
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|task
operator|.
name|getChildTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|child
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
name|outputPlan
argument_list|(
name|child
argument_list|,
name|out
argument_list|,
name|extended
argument_list|,
name|displayedSet
argument_list|,
name|indent
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|final
name|Set
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|dependeciesTaskSet
init|=
operator|new
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|void
name|outputDependencies
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|int
name|indent
parameter_list|,
name|boolean
name|rootTskCandidate
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|dependeciesTaskSet
operator|.
name|contains
argument_list|(
name|task
argument_list|)
condition|)
block|{
return|return;
block|}
name|dependeciesTaskSet
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|printf
argument_list|(
literal|"%s"
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|task
operator|.
name|getParentTasks
argument_list|()
operator|==
literal|null
operator|||
name|task
operator|.
name|getParentTasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
if|if
condition|(
name|rootTskCandidate
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|" is a root stage"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|print
argument_list|(
literal|" depends on stages: "
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parent
range|:
name|task
operator|.
name|getParentTasks
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|parent
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|cuurBackupTask
init|=
name|task
operator|.
name|getBackupTask
argument_list|()
decl_stmt|;
if|if
condition|(
name|cuurBackupTask
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|" has a backup stage: "
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|cuurBackupTask
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|task
operator|instanceof
name|ConditionalTask
operator|&&
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|" , consists of "
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|con
range|:
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|out
operator|.
name|print
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|con
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
if|if
condition|(
name|task
operator|instanceof
name|ConditionalTask
operator|&&
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|con
range|:
operator|(
operator|(
name|ConditionalTask
operator|)
name|task
operator|)
operator|.
name|getListTasks
argument_list|()
control|)
block|{
name|outputDependencies
argument_list|(
name|con
argument_list|,
name|out
argument_list|,
name|indent
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|task
operator|.
name|getChildTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|child
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
name|outputDependencies
argument_list|(
name|child
argument_list|,
name|out
argument_list|,
name|indent
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|outputAST
parameter_list|(
name|String
name|treeString
parameter_list|,
name|PrintStream
name|out
parameter_list|,
name|int
name|indent
parameter_list|)
block|{
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"ABSTRACT SYNTAX TREE:"
argument_list|)
expr_stmt|;
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
operator|+
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
name|treeString
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|outputDependencies
parameter_list|(
name|PrintStream
name|out
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|int
name|indent
parameter_list|)
throws|throws
name|Exception
block|{
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"STAGE DEPENDENCIES:"
argument_list|)
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
range|:
name|rootTasks
control|)
block|{
name|outputDependencies
argument_list|(
name|rootTask
argument_list|,
name|out
argument_list|,
name|indent
operator|+
literal|2
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|outputStagePlans
parameter_list|(
name|PrintStream
name|out
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|int
name|indent
parameter_list|)
throws|throws
name|Exception
block|{
name|out
operator|.
name|print
argument_list|(
name|indentString
argument_list|(
name|indent
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|println
argument_list|(
literal|"STAGE PLANS:"
argument_list|)
expr_stmt|;
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|displayedSet
init|=
operator|new
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
range|:
name|rootTasks
control|)
block|{
name|outputPlan
argument_list|(
name|rootTask
argument_list|,
name|out
argument_list|,
name|work
operator|.
name|getExtended
argument_list|()
argument_list|,
name|displayedSet
argument_list|,
name|indent
operator|+
literal|2
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * MethodComparator.    *    */
specifier|public
specifier|static
class|class
name|MethodComparator
implements|implements
name|Comparator
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|Object
name|o1
parameter_list|,
name|Object
name|o2
parameter_list|)
block|{
name|Method
name|m1
init|=
operator|(
name|Method
operator|)
name|o1
decl_stmt|;
name|Method
name|m2
init|=
operator|(
name|Method
operator|)
name|o2
decl_stmt|;
return|return
name|m1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|m2
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|EXPLAIN
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"EXPLAIN"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|localizeMRTmpFilesImpl
parameter_list|(
name|Context
name|ctx
parameter_list|)
block|{
comment|// explain task has nothing to localize
comment|// we don't expect to enter this code path at all
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected call"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

