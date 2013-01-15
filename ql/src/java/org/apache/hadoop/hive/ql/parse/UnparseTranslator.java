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
name|parse
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
name|NavigableMap
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
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|TokenRewriteStream
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
name|HiveUtils
import|;
end_import

begin_comment
comment|/**  * UnparseTranslator is used to "unparse" objects such as views when their  * definition is stored. It has a translations map where its possible to replace all the  * text with the appropriate escaped version [say invites.ds will be replaced with  * `invites`.`ds` and the entire query is processed like this and stored as  * Extended text in table's metadata]. This holds all individual translations and  * where they apply in the stream. The unparse is lazy and happens when  * SemanticAnalyzer.saveViewDefinition() calls TokenRewriteStream.toString().  *  */
end_comment

begin_class
class|class
name|UnparseTranslator
block|{
comment|// key is token start index
specifier|private
specifier|final
name|NavigableMap
argument_list|<
name|Integer
argument_list|,
name|Translation
argument_list|>
name|translations
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|CopyTranslation
argument_list|>
name|copyTranslations
decl_stmt|;
specifier|private
name|boolean
name|enabled
decl_stmt|;
specifier|public
name|UnparseTranslator
parameter_list|()
block|{
name|translations
operator|=
operator|new
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Translation
argument_list|>
argument_list|()
expr_stmt|;
name|copyTranslations
operator|=
operator|new
name|ArrayList
argument_list|<
name|CopyTranslation
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/**    * Enable this translator.    */
name|void
name|enable
parameter_list|()
block|{
name|enabled
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * @return whether this translator has been enabled    */
name|boolean
name|isEnabled
parameter_list|()
block|{
return|return
name|enabled
return|;
block|}
comment|/**    * Register a translation to be performed as part of unparse. ANTLR imposes    * strict conditions on the translations and errors out during    * TokenRewriteStream.toString() if there is an overlap. It expects all    * the translations to be disjoint (See HIVE-2439).    * If the translation overlaps with any previously    * registered translation, then it must be either    * identical or a prefix (in which cases it is ignored),    * or else it must extend the existing translation (i.e.    * the existing translation must be a prefix/suffix of the new translation).    * All other overlap cases result in assertion failures.    *    * @param node    *          target node whose subtree is to be replaced    *    * @param replacementText    *          text to use as replacement    */
name|void
name|addTranslation
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|String
name|replacementText
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|node
operator|.
name|getOrigin
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// This node was parsed while loading the definition of another view
comment|// being referenced by the one being created, and we don't want
comment|// to track any expansions for the underlying view.
return|return;
block|}
name|int
name|tokenStartIndex
init|=
name|node
operator|.
name|getTokenStartIndex
argument_list|()
decl_stmt|;
name|int
name|tokenStopIndex
init|=
name|node
operator|.
name|getTokenStopIndex
argument_list|()
decl_stmt|;
name|Translation
name|translation
init|=
operator|new
name|Translation
argument_list|()
decl_stmt|;
name|translation
operator|.
name|tokenStopIndex
operator|=
name|tokenStopIndex
expr_stmt|;
name|translation
operator|.
name|replacementText
operator|=
name|replacementText
expr_stmt|;
comment|// Sanity check for overlap with regions already being expanded
assert|assert
operator|(
name|tokenStopIndex
operator|>=
name|tokenStartIndex
operator|)
assert|;
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Translation
argument_list|>
name|existingEntry
decl_stmt|;
name|existingEntry
operator|=
name|translations
operator|.
name|floorEntry
argument_list|(
name|tokenStartIndex
argument_list|)
expr_stmt|;
name|boolean
name|prefix
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|existingEntry
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|existingEntry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|tokenStartIndex
argument_list|)
condition|)
block|{
if|if
condition|(
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|tokenStopIndex
operator|==
name|tokenStopIndex
condition|)
block|{
if|if
condition|(
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|replacementText
operator|.
name|equals
argument_list|(
name|replacementText
argument_list|)
condition|)
block|{
comment|// exact match for existing mapping: somebody is doing something
comment|// redundant, but we'll let it pass
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|tokenStopIndex
operator|>
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|tokenStopIndex
condition|)
block|{
comment|// is existing mapping a prefix for new mapping? if so, that's also
comment|// redundant, but in this case we need to expand it
name|prefix
operator|=
name|replacementText
operator|.
name|startsWith
argument_list|(
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|replacementText
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|prefix
operator|)
assert|;
block|}
else|else
block|{
comment|// new mapping is a prefix for existing mapping:  ignore it
name|prefix
operator|=
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|replacementText
operator|.
name|startsWith
argument_list|(
name|replacementText
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|prefix
operator|)
assert|;
return|return;
block|}
block|}
if|if
condition|(
operator|!
name|prefix
condition|)
block|{
assert|assert
operator|(
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|tokenStopIndex
operator|<
name|tokenStartIndex
operator|)
assert|;
block|}
block|}
if|if
condition|(
operator|!
name|prefix
condition|)
block|{
name|existingEntry
operator|=
name|translations
operator|.
name|ceilingEntry
argument_list|(
name|tokenStartIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|existingEntry
operator|!=
literal|null
condition|)
block|{
assert|assert
operator|(
name|existingEntry
operator|.
name|getKey
argument_list|()
operator|>
name|tokenStopIndex
operator|)
assert|;
block|}
block|}
comment|// Is existing entry a suffix of the newer entry and a subset of it?
name|existingEntry
operator|=
name|translations
operator|.
name|floorEntry
argument_list|(
name|tokenStopIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|existingEntry
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|existingEntry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|tokenStopIndex
argument_list|)
condition|)
block|{
if|if
condition|(
name|tokenStartIndex
operator|<
name|existingEntry
operator|.
name|getKey
argument_list|()
operator|&&
name|tokenStopIndex
operator|==
name|existingEntry
operator|.
name|getKey
argument_list|()
condition|)
block|{
comment|// Seems newer entry is a super-set of existing entry, remove existing entry
assert|assert
operator|(
name|replacementText
operator|.
name|endsWith
argument_list|(
name|existingEntry
operator|.
name|getValue
argument_list|()
operator|.
name|replacementText
argument_list|)
operator|)
assert|;
name|translations
operator|.
name|remove
argument_list|(
name|tokenStopIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// It's all good: create a new entry in the map (or update existing one)
name|translations
operator|.
name|put
argument_list|(
name|tokenStartIndex
argument_list|,
name|translation
argument_list|)
expr_stmt|;
block|}
comment|/**    * Register a translation for an tabName.    *    * @param node    *          source node (which must be an tabName) to be replaced    */
name|void
name|addTableNameTranslation
parameter_list|(
name|ASTNode
name|tableName
parameter_list|,
name|String
name|currentDatabaseName
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|tableName
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|Identifier
condition|)
block|{
name|addIdentifierTranslation
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
return|return;
block|}
assert|assert
operator|(
name|tableName
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABNAME
operator|)
assert|;
assert|assert
operator|(
name|tableName
operator|.
name|getChildCount
argument_list|()
operator|<=
literal|2
operator|)
assert|;
if|if
condition|(
name|tableName
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|addIdentifierTranslation
argument_list|(
operator|(
name|ASTNode
operator|)
name|tableName
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|addIdentifierTranslation
argument_list|(
operator|(
name|ASTNode
operator|)
name|tableName
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// transform the table reference to an absolute reference (i.e., "db.table")
name|StringBuilder
name|replacementText
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|replacementText
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|currentDatabaseName
argument_list|)
argument_list|)
expr_stmt|;
name|replacementText
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|ASTNode
name|identifier
init|=
operator|(
name|ASTNode
operator|)
name|tableName
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|identifierText
init|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
name|identifier
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|replacementText
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|identifierText
argument_list|)
argument_list|)
expr_stmt|;
name|addTranslation
argument_list|(
name|identifier
argument_list|,
name|replacementText
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Register a translation for an identifier.    *    * @param node    *          source node (which must be an identifier) to be replaced    */
name|void
name|addIdentifierTranslation
parameter_list|(
name|ASTNode
name|identifier
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return;
block|}
assert|assert
operator|(
name|identifier
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|Identifier
operator|)
assert|;
name|String
name|replacementText
init|=
name|identifier
operator|.
name|getText
argument_list|()
decl_stmt|;
name|replacementText
operator|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
name|replacementText
argument_list|)
expr_stmt|;
name|replacementText
operator|=
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|replacementText
argument_list|)
expr_stmt|;
name|addTranslation
argument_list|(
name|identifier
argument_list|,
name|replacementText
argument_list|)
expr_stmt|;
block|}
comment|/**    * Register a "copy" translation in which a node will be translated into    * whatever the translation turns out to be for another node (after    * previously registered translations have already been performed).  Deferred    * translations are performed in the order they are registered, and follow    * the same rules regarding overlap as non-copy translations.    *    * @param targetNode node whose subtree is to be replaced    *    * @param sourceNode the node providing the replacement text    *    */
name|void
name|addCopyTranslation
parameter_list|(
name|ASTNode
name|targetNode
parameter_list|,
name|ASTNode
name|sourceNode
parameter_list|)
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|targetNode
operator|.
name|getOrigin
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
name|CopyTranslation
name|copyTranslation
init|=
operator|new
name|CopyTranslation
argument_list|()
decl_stmt|;
name|copyTranslation
operator|.
name|targetNode
operator|=
name|targetNode
expr_stmt|;
name|copyTranslation
operator|.
name|sourceNode
operator|=
name|sourceNode
expr_stmt|;
name|copyTranslations
operator|.
name|add
argument_list|(
name|copyTranslation
argument_list|)
expr_stmt|;
block|}
comment|/**    * Apply all translations on the given token stream.    *    * @param tokenRewriteStream    *          rewrite-capable stream    */
name|void
name|applyTranslations
parameter_list|(
name|TokenRewriteStream
name|tokenRewriteStream
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Translation
argument_list|>
name|entry
range|:
name|translations
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|tokenRewriteStream
operator|.
name|replace
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|tokenStopIndex
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|replacementText
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|CopyTranslation
name|copyTranslation
range|:
name|copyTranslations
control|)
block|{
name|String
name|replacementText
init|=
name|tokenRewriteStream
operator|.
name|toString
argument_list|(
name|copyTranslation
operator|.
name|sourceNode
operator|.
name|getTokenStartIndex
argument_list|()
argument_list|,
name|copyTranslation
operator|.
name|sourceNode
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|currentText
init|=
name|tokenRewriteStream
operator|.
name|toString
argument_list|(
name|copyTranslation
operator|.
name|targetNode
operator|.
name|getTokenStartIndex
argument_list|()
argument_list|,
name|copyTranslation
operator|.
name|targetNode
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentText
operator|.
name|equals
argument_list|(
name|replacementText
argument_list|)
condition|)
block|{
comment|// copy is a nop, so skip it--this is important for avoiding
comment|// spurious overlap assertions
continue|continue;
block|}
comment|// Call addTranslation just to get the assertions for overlap
comment|// checking.
name|addTranslation
argument_list|(
name|copyTranslation
operator|.
name|targetNode
argument_list|,
name|replacementText
argument_list|)
expr_stmt|;
name|tokenRewriteStream
operator|.
name|replace
argument_list|(
name|copyTranslation
operator|.
name|targetNode
operator|.
name|getTokenStartIndex
argument_list|()
argument_list|,
name|copyTranslation
operator|.
name|targetNode
operator|.
name|getTokenStopIndex
argument_list|()
argument_list|,
name|replacementText
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|Translation
block|{
name|int
name|tokenStopIndex
decl_stmt|;
name|String
name|replacementText
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
name|tokenStopIndex
operator|+
literal|" -> "
operator|+
name|replacementText
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|CopyTranslation
block|{
name|ASTNode
name|targetNode
decl_stmt|;
name|ASTNode
name|sourceNode
decl_stmt|;
block|}
block|}
end_class

end_unit

