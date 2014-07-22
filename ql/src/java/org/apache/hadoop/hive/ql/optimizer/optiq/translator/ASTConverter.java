begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|optimizer
operator|.
name|optiq
operator|.
name|translator
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
name|LinkedList
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_import
import|import
name|net
operator|.
name|hydromatic
operator|.
name|optiq
operator|.
name|util
operator|.
name|BitSets
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
name|FieldSchema
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
name|optimizer
operator|.
name|optiq
operator|.
name|reloperators
operator|.
name|HiveJoinRel
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
name|optimizer
operator|.
name|optiq
operator|.
name|reloperators
operator|.
name|HiveSortRel
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
name|optimizer
operator|.
name|optiq
operator|.
name|translator
operator|.
name|SqlFunctionConverter
operator|.
name|HiveToken
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
name|HiveParser
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
name|ParseDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|AggregateCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|AggregateRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|FilterRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|JoinRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|ProjectRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelFieldCollation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|RelVisitor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|SortRel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rel
operator|.
name|TableAccessRelBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataTypeField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexCall
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexInputRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexLiteral
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|rex
operator|.
name|RexVisitorImpl
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|SqlKind
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|SqlOperator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|type
operator|.
name|BasicSqlType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
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
name|Iterables
import|;
end_import

begin_class
specifier|public
class|class
name|ASTConverter
block|{
name|RelNode
name|root
decl_stmt|;
name|HiveAST
name|hiveAST
decl_stmt|;
name|RelNode
name|from
decl_stmt|;
name|FilterRelBase
name|where
decl_stmt|;
name|AggregateRelBase
name|groupBy
decl_stmt|;
name|FilterRelBase
name|having
decl_stmt|;
name|ProjectRelBase
name|select
decl_stmt|;
name|SortRel
name|order
decl_stmt|;
name|Schema
name|schema
decl_stmt|;
name|ASTConverter
parameter_list|(
name|RelNode
name|root
parameter_list|)
block|{
name|this
operator|.
name|root
operator|=
name|root
expr_stmt|;
name|hiveAST
operator|=
operator|new
name|HiveAST
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|ASTNode
name|convert
parameter_list|(
specifier|final
name|RelNode
name|relNode
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|resultSchema
parameter_list|)
block|{
name|SortRel
name|sortrel
init|=
literal|null
decl_stmt|;
name|RelNode
name|root
init|=
name|DerivedTableInjector
operator|.
name|convertOpTree
argument_list|(
name|relNode
argument_list|,
name|resultSchema
argument_list|)
decl_stmt|;
if|if
condition|(
name|root
operator|instanceof
name|SortRel
condition|)
block|{
name|sortrel
operator|=
operator|(
name|SortRel
operator|)
name|root
expr_stmt|;
name|root
operator|=
name|sortrel
operator|.
name|getChild
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|root
operator|instanceof
name|ProjectRelBase
operator|)
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Child of root sort node is not a project"
argument_list|)
throw|;
block|}
name|ASTConverter
name|c
init|=
operator|new
name|ASTConverter
argument_list|(
name|root
argument_list|)
decl_stmt|;
return|return
name|c
operator|.
name|convert
argument_list|(
name|sortrel
argument_list|)
return|;
block|}
specifier|public
name|ASTNode
name|convert
parameter_list|(
name|SortRel
name|sortrel
parameter_list|)
block|{
comment|/*      * 1. Walk RelNode Graph; note from, where, gBy.. nodes.      */
operator|new
name|QBVisitor
argument_list|()
operator|.
name|go
argument_list|(
name|root
argument_list|)
expr_stmt|;
comment|/*      * 2. convert from node.      */
name|QueryBlockInfo
name|qb
init|=
name|convertSource
argument_list|(
name|from
argument_list|)
decl_stmt|;
name|schema
operator|=
name|qb
operator|.
name|schema
expr_stmt|;
name|hiveAST
operator|.
name|from
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_FROM
argument_list|,
literal|"TOK_FROM"
argument_list|)
operator|.
name|add
argument_list|(
name|qb
operator|.
name|ast
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
comment|/*      * 3. convert filterNode      */
if|if
condition|(
name|where
operator|!=
literal|null
condition|)
block|{
name|ASTNode
name|cond
init|=
name|where
operator|.
name|getCondition
argument_list|()
operator|.
name|accept
argument_list|(
operator|new
name|RexVisitor
argument_list|(
name|schema
argument_list|)
argument_list|)
decl_stmt|;
name|hiveAST
operator|.
name|where
operator|=
name|ASTBuilder
operator|.
name|where
argument_list|(
name|cond
argument_list|)
expr_stmt|;
block|}
comment|/*      * 4. GBy      */
if|if
condition|(
name|groupBy
operator|!=
literal|null
condition|)
block|{
name|ASTBuilder
name|b
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_GROUPBY
argument_list|,
literal|"TOK_GROUPBY"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
range|:
name|BitSets
operator|.
name|toIter
argument_list|(
name|groupBy
operator|.
name|getGroupSet
argument_list|()
argument_list|)
control|)
block|{
name|RexInputRef
name|iRef
init|=
operator|new
name|RexInputRef
argument_list|(
name|i
argument_list|,
operator|new
name|BasicSqlType
argument_list|(
name|SqlTypeName
operator|.
name|ANY
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|add
argument_list|(
name|iRef
operator|.
name|accept
argument_list|(
operator|new
name|RexVisitor
argument_list|(
name|schema
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|groupBy
operator|.
name|getGroupSet
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
name|hiveAST
operator|.
name|groupBy
operator|=
name|b
operator|.
name|node
argument_list|()
expr_stmt|;
name|schema
operator|=
operator|new
name|Schema
argument_list|(
name|schema
argument_list|,
name|groupBy
argument_list|)
expr_stmt|;
block|}
comment|/*      * 5. Having      */
if|if
condition|(
name|having
operator|!=
literal|null
condition|)
block|{
name|ASTNode
name|cond
init|=
name|having
operator|.
name|getCondition
argument_list|()
operator|.
name|accept
argument_list|(
operator|new
name|RexVisitor
argument_list|(
name|schema
argument_list|)
argument_list|)
decl_stmt|;
name|hiveAST
operator|.
name|having
operator|=
name|ASTBuilder
operator|.
name|having
argument_list|(
name|cond
argument_list|)
expr_stmt|;
block|}
comment|/*      * 6. Project      */
name|int
name|i
init|=
literal|0
decl_stmt|;
name|ASTBuilder
name|b
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_SELECT
argument_list|,
literal|"TOK_SELECT"
argument_list|)
decl_stmt|;
for|for
control|(
name|RexNode
name|r
range|:
name|select
operator|.
name|getChildExps
argument_list|()
control|)
block|{
name|ASTNode
name|selectExpr
init|=
name|ASTBuilder
operator|.
name|selectExpr
argument_list|(
name|r
operator|.
name|accept
argument_list|(
operator|new
name|RexVisitor
argument_list|(
name|schema
argument_list|)
argument_list|)
argument_list|,
name|select
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
operator|.
name|get
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|add
argument_list|(
name|selectExpr
argument_list|)
expr_stmt|;
block|}
name|hiveAST
operator|.
name|select
operator|=
name|b
operator|.
name|node
argument_list|()
expr_stmt|;
comment|/*      * 7. Order      * Use in Order By from the block above. RelNode has no pointer to parent      * hence we need to go top down; but OB at each block really belong to its      * src/from. Hence the need to pass in sortRel for each block from its parent.      */
if|if
condition|(
name|sortrel
operator|!=
literal|null
condition|)
block|{
name|HiveSortRel
name|hiveSort
init|=
operator|(
name|HiveSortRel
operator|)
name|sortrel
decl_stmt|;
if|if
condition|(
operator|!
name|hiveSort
operator|.
name|getCollation
argument_list|()
operator|.
name|getFieldCollations
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ASTNode
name|orderAst
init|=
name|ASTBuilder
operator|.
name|createAST
argument_list|(
name|HiveParser
operator|.
name|TOK_ORDERBY
argument_list|,
literal|"TOK_ORDERBY"
argument_list|)
decl_stmt|;
name|schema
operator|=
operator|new
name|Schema
argument_list|(
operator|(
name|HiveSortRel
operator|)
name|sortrel
argument_list|)
expr_stmt|;
for|for
control|(
name|RelFieldCollation
name|c
range|:
name|hiveSort
operator|.
name|getCollation
argument_list|()
operator|.
name|getFieldCollations
argument_list|()
control|)
block|{
name|ColumnInfo
name|cI
init|=
name|schema
operator|.
name|get
argument_list|(
name|c
operator|.
name|getFieldIndex
argument_list|()
argument_list|)
decl_stmt|;
comment|/*            * The RowResolver setup for Select drops Table associations. So setup            * ASTNode on unqualified name.            */
name|ASTNode
name|astCol
init|=
name|ASTBuilder
operator|.
name|unqualifiedName
argument_list|(
name|cI
operator|.
name|column
argument_list|)
decl_stmt|;
name|ASTNode
name|astNode
init|=
name|c
operator|.
name|getDirection
argument_list|()
operator|==
name|RelFieldCollation
operator|.
name|Direction
operator|.
name|ASCENDING
condition|?
name|ASTBuilder
operator|.
name|createAST
argument_list|(
name|HiveParser
operator|.
name|TOK_TABSORTCOLNAMEASC
argument_list|,
literal|"TOK_TABSORTCOLNAMEASC"
argument_list|)
else|:
name|ASTBuilder
operator|.
name|createAST
argument_list|(
name|HiveParser
operator|.
name|TOK_TABSORTCOLNAMEDESC
argument_list|,
literal|"TOK_TABSORTCOLNAMEDESC"
argument_list|)
decl_stmt|;
name|astNode
operator|.
name|addChild
argument_list|(
name|astCol
argument_list|)
expr_stmt|;
name|orderAst
operator|.
name|addChild
argument_list|(
name|astNode
argument_list|)
expr_stmt|;
block|}
name|hiveAST
operator|.
name|order
operator|=
name|orderAst
expr_stmt|;
block|}
name|RexNode
name|limitExpr
init|=
name|hiveSort
operator|.
name|getFetchExpr
argument_list|()
decl_stmt|;
if|if
condition|(
name|limitExpr
operator|!=
literal|null
condition|)
block|{
name|Object
name|val
init|=
operator|(
operator|(
name|RexLiteral
operator|)
name|limitExpr
operator|)
operator|.
name|getValue2
argument_list|()
decl_stmt|;
name|hiveAST
operator|.
name|limit
operator|=
name|ASTBuilder
operator|.
name|limit
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|hiveAST
operator|.
name|getAST
argument_list|()
return|;
block|}
specifier|private
name|Schema
name|getRowSchema
parameter_list|(
name|String
name|tblAlias
parameter_list|)
block|{
return|return
operator|new
name|Schema
argument_list|(
name|select
argument_list|,
name|tblAlias
argument_list|)
return|;
block|}
specifier|private
name|QueryBlockInfo
name|convertSource
parameter_list|(
name|RelNode
name|r
parameter_list|)
block|{
name|Schema
name|s
decl_stmt|;
name|ASTNode
name|ast
decl_stmt|;
if|if
condition|(
name|r
operator|instanceof
name|TableAccessRelBase
condition|)
block|{
name|TableAccessRelBase
name|f
init|=
operator|(
name|TableAccessRelBase
operator|)
name|r
decl_stmt|;
name|s
operator|=
operator|new
name|Schema
argument_list|(
name|f
argument_list|)
expr_stmt|;
name|ast
operator|=
name|ASTBuilder
operator|.
name|table
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|r
operator|instanceof
name|JoinRelBase
condition|)
block|{
name|JoinRelBase
name|join
init|=
operator|(
name|JoinRelBase
operator|)
name|r
decl_stmt|;
name|QueryBlockInfo
name|left
init|=
name|convertSource
argument_list|(
name|join
operator|.
name|getLeft
argument_list|()
argument_list|)
decl_stmt|;
name|QueryBlockInfo
name|right
init|=
name|convertSource
argument_list|(
name|join
operator|.
name|getRight
argument_list|()
argument_list|)
decl_stmt|;
name|s
operator|=
operator|new
name|Schema
argument_list|(
name|left
operator|.
name|schema
argument_list|,
name|right
operator|.
name|schema
argument_list|)
expr_stmt|;
name|ASTNode
name|cond
init|=
name|join
operator|.
name|getCondition
argument_list|()
operator|.
name|accept
argument_list|(
operator|new
name|RexVisitor
argument_list|(
name|s
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|semiJoin
init|=
operator|(
operator|(
name|join
operator|instanceof
name|HiveJoinRel
operator|)
operator|&&
operator|(
operator|(
name|HiveJoinRel
operator|)
name|join
operator|)
operator|.
name|isLeftSemiJoin
argument_list|()
operator|)
condition|?
literal|true
else|:
literal|false
decl_stmt|;
name|ast
operator|=
name|ASTBuilder
operator|.
name|join
argument_list|(
name|left
operator|.
name|ast
argument_list|,
name|right
operator|.
name|ast
argument_list|,
name|join
operator|.
name|getJoinType
argument_list|()
argument_list|,
name|cond
argument_list|,
name|semiJoin
argument_list|)
expr_stmt|;
if|if
condition|(
name|semiJoin
condition|)
name|s
operator|=
name|left
operator|.
name|schema
expr_stmt|;
block|}
else|else
block|{
name|ASTConverter
name|src
init|=
operator|new
name|ASTConverter
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|ASTNode
name|srcAST
init|=
name|src
operator|.
name|convert
argument_list|(
name|order
argument_list|)
decl_stmt|;
name|String
name|sqAlias
init|=
name|ASTConverter
operator|.
name|nextAlias
argument_list|()
decl_stmt|;
name|s
operator|=
name|src
operator|.
name|getRowSchema
argument_list|(
name|sqAlias
argument_list|)
expr_stmt|;
name|ast
operator|=
name|ASTBuilder
operator|.
name|subQuery
argument_list|(
name|srcAST
argument_list|,
name|sqAlias
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|QueryBlockInfo
argument_list|(
name|s
argument_list|,
name|ast
argument_list|)
return|;
block|}
class|class
name|QBVisitor
extends|extends
name|RelVisitor
block|{
specifier|public
name|void
name|handle
parameter_list|(
name|FilterRelBase
name|filter
parameter_list|)
block|{
name|RelNode
name|child
init|=
name|filter
operator|.
name|getChild
argument_list|()
decl_stmt|;
if|if
condition|(
name|child
operator|instanceof
name|AggregateRelBase
condition|)
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|having
operator|=
name|filter
expr_stmt|;
block|}
else|else
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|where
operator|=
name|filter
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|handle
parameter_list|(
name|ProjectRelBase
name|project
parameter_list|)
block|{
if|if
condition|(
name|ASTConverter
operator|.
name|this
operator|.
name|select
operator|==
literal|null
condition|)
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|select
operator|=
name|project
expr_stmt|;
block|}
else|else
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|from
operator|=
name|project
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|RelNode
name|node
parameter_list|,
name|int
name|ordinal
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|TableAccessRelBase
condition|)
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|from
operator|=
name|node
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|FilterRelBase
condition|)
block|{
name|handle
argument_list|(
operator|(
name|FilterRelBase
operator|)
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|ProjectRelBase
condition|)
block|{
name|handle
argument_list|(
operator|(
name|ProjectRelBase
operator|)
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|JoinRelBase
condition|)
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|from
operator|=
name|node
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|AggregateRelBase
condition|)
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|groupBy
operator|=
operator|(
name|AggregateRelBase
operator|)
name|node
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|SortRel
condition|)
block|{
name|ASTConverter
operator|.
name|this
operator|.
name|order
operator|=
operator|(
name|SortRel
operator|)
name|node
expr_stmt|;
block|}
comment|/*        * once the source node is reached; stop traversal for this QB        */
if|if
condition|(
name|ASTConverter
operator|.
name|this
operator|.
name|from
operator|==
literal|null
condition|)
block|{
name|node
operator|.
name|childrenAccept
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|RexVisitor
extends|extends
name|RexVisitorImpl
argument_list|<
name|ASTNode
argument_list|>
block|{
specifier|private
specifier|final
name|Schema
name|schema
decl_stmt|;
specifier|protected
name|RexVisitor
parameter_list|(
name|Schema
name|schema
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ASTNode
name|visitInputRef
parameter_list|(
name|RexInputRef
name|inputRef
parameter_list|)
block|{
name|ColumnInfo
name|cI
init|=
name|schema
operator|.
name|get
argument_list|(
name|inputRef
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cI
operator|.
name|agg
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|dupTree
argument_list|(
name|cI
operator|.
name|agg
argument_list|)
return|;
block|}
return|return
name|ASTBuilder
operator|.
name|qualifiedName
argument_list|(
name|cI
operator|.
name|table
argument_list|,
name|cI
operator|.
name|column
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ASTNode
name|visitLiteral
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|literal
argument_list|(
name|literal
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ASTNode
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
if|if
condition|(
operator|!
name|deep
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SqlOperator
name|op
init|=
name|call
operator|.
name|getOperator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ASTNode
argument_list|>
name|astNodeLst
init|=
operator|new
name|LinkedList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|kind
operator|==
name|SqlKind
operator|.
name|CAST
condition|)
block|{
name|HiveToken
name|ht
init|=
name|TypeConverter
operator|.
name|hiveToken
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|ASTBuilder
name|astBldr
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|ht
operator|.
name|type
argument_list|,
name|ht
operator|.
name|text
argument_list|)
decl_stmt|;
if|if
condition|(
name|ht
operator|.
name|args
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|castArg
range|:
name|ht
operator|.
name|args
control|)
name|astBldr
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|castArg
argument_list|)
expr_stmt|;
block|}
name|astNodeLst
operator|.
name|add
argument_list|(
name|astBldr
operator|.
name|node
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|RexNode
name|operand
range|:
name|call
operator|.
name|operands
control|)
block|{
name|astNodeLst
operator|.
name|add
argument_list|(
name|operand
operator|.
name|accept
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isFlat
argument_list|(
name|call
argument_list|)
condition|)
return|return
name|SqlFunctionConverter
operator|.
name|buildAST
argument_list|(
name|op
argument_list|,
name|astNodeLst
argument_list|,
literal|0
argument_list|)
return|;
else|else
return|return
name|SqlFunctionConverter
operator|.
name|buildAST
argument_list|(
name|op
argument_list|,
name|astNodeLst
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|QueryBlockInfo
block|{
name|Schema
name|schema
decl_stmt|;
name|ASTNode
name|ast
decl_stmt|;
specifier|public
name|QueryBlockInfo
parameter_list|(
name|Schema
name|schema
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
block|}
block|}
comment|/*    * represents the schema exposed by a QueryBlock.    */
specifier|static
class|class
name|Schema
extends|extends
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|Schema
parameter_list|(
name|TableAccessRelBase
name|scan
parameter_list|)
block|{
name|String
name|tabName
init|=
name|scan
operator|.
name|getTable
argument_list|()
operator|.
name|getQualifiedName
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|RelDataTypeField
name|field
range|:
name|scan
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
control|)
block|{
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
name|tabName
argument_list|,
name|field
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Schema
parameter_list|(
name|ProjectRelBase
name|select
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
for|for
control|(
name|RelDataTypeField
name|field
range|:
name|select
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
control|)
block|{
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
name|alias
argument_list|,
name|field
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Schema
parameter_list|(
name|Schema
name|left
parameter_list|,
name|Schema
name|right
parameter_list|)
block|{
for|for
control|(
name|ColumnInfo
name|cI
range|:
name|Iterables
operator|.
name|concat
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
control|)
block|{
name|add
argument_list|(
name|cI
argument_list|)
expr_stmt|;
block|}
block|}
name|Schema
parameter_list|(
name|Schema
name|src
parameter_list|,
name|AggregateRelBase
name|gBy
parameter_list|)
block|{
for|for
control|(
name|int
name|i
range|:
name|BitSets
operator|.
name|toIter
argument_list|(
name|gBy
operator|.
name|getGroupSet
argument_list|()
argument_list|)
control|)
block|{
name|ColumnInfo
name|cI
init|=
name|src
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|cI
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|AggregateCall
argument_list|>
name|aggs
init|=
name|gBy
operator|.
name|getAggCallList
argument_list|()
decl_stmt|;
for|for
control|(
name|AggregateCall
name|agg
range|:
name|aggs
control|)
block|{
name|int
name|argCount
init|=
name|agg
operator|.
name|getArgList
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|ASTBuilder
name|b
init|=
name|agg
operator|.
name|isDistinct
argument_list|()
condition|?
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_FUNCTIONDI
argument_list|,
literal|"TOK_FUNCTIONDI"
argument_list|)
else|:
name|argCount
operator|==
literal|0
condition|?
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_FUNCTIONSTAR
argument_list|,
literal|"TOK_FUNCTIONSTAR"
argument_list|)
else|:
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_FUNCTION
argument_list|,
literal|"TOK_FUNCTION"
argument_list|)
decl_stmt|;
name|b
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|agg
operator|.
name|getAggregation
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
range|:
name|agg
operator|.
name|getArgList
argument_list|()
control|)
block|{
name|RexInputRef
name|iRef
init|=
operator|new
name|RexInputRef
argument_list|(
name|i
argument_list|,
operator|new
name|BasicSqlType
argument_list|(
name|SqlTypeName
operator|.
name|ANY
argument_list|)
argument_list|)
decl_stmt|;
name|b
operator|.
name|add
argument_list|(
name|iRef
operator|.
name|accept
argument_list|(
operator|new
name|RexVisitor
argument_list|(
name|src
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
literal|null
argument_list|,
name|b
operator|.
name|node
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Assumption:<br>      * 1. ProjectRel will always be child of SortRel.<br>      * 2. In Optiq every projection in ProjectRelBase is uniquely named      * (unambigous) without using table qualifier (table name).<br>      *       * @param order      *          Hive Sort Rel Node      * @return Schema      */
specifier|public
name|Schema
parameter_list|(
name|HiveSortRel
name|order
parameter_list|)
block|{
name|ProjectRelBase
name|select
init|=
operator|(
name|ProjectRelBase
operator|)
name|order
operator|.
name|getChild
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|projName
range|:
name|select
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
control|)
block|{
name|add
argument_list|(
operator|new
name|ColumnInfo
argument_list|(
literal|null
argument_list|,
name|projName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * represents Column information exposed by a QueryBlock.    */
specifier|static
class|class
name|ColumnInfo
block|{
name|String
name|table
decl_stmt|;
name|String
name|column
decl_stmt|;
name|ASTNode
name|agg
decl_stmt|;
name|ColumnInfo
parameter_list|(
name|String
name|table
parameter_list|,
name|String
name|column
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
block|}
name|ColumnInfo
parameter_list|(
name|String
name|table
parameter_list|,
name|ASTNode
name|agg
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|agg
operator|=
name|agg
expr_stmt|;
block|}
name|ColumnInfo
parameter_list|(
name|String
name|alias
parameter_list|,
name|ColumnInfo
name|srcCol
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|srcCol
operator|.
name|column
expr_stmt|;
name|this
operator|.
name|agg
operator|=
name|srcCol
operator|.
name|agg
expr_stmt|;
block|}
block|}
specifier|static
name|String
name|nextAlias
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"$hdt$_%d"
argument_list|,
name|derivedTableCounter
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|AtomicLong
name|derivedTableCounter
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|static
class|class
name|HiveAST
block|{
name|ASTNode
name|from
decl_stmt|;
name|ASTNode
name|where
decl_stmt|;
name|ASTNode
name|groupBy
decl_stmt|;
name|ASTNode
name|having
decl_stmt|;
name|ASTNode
name|select
decl_stmt|;
name|ASTNode
name|order
decl_stmt|;
name|ASTNode
name|limit
decl_stmt|;
specifier|public
name|ASTNode
name|getAST
parameter_list|()
block|{
name|ASTBuilder
name|b
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_QUERY
argument_list|,
literal|"TOK_QUERY"
argument_list|)
operator|.
name|add
argument_list|(
name|from
argument_list|)
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_INSERT
argument_list|,
literal|"TOK_INSERT"
argument_list|)
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|destNode
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|select
argument_list|)
operator|.
name|add
argument_list|(
name|where
argument_list|)
operator|.
name|add
argument_list|(
name|groupBy
argument_list|)
operator|.
name|add
argument_list|(
name|having
argument_list|)
operator|.
name|add
argument_list|(
name|order
argument_list|)
operator|.
name|add
argument_list|(
name|limit
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|isFlat
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|boolean
name|flat
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|call
operator|.
name|operands
operator|!=
literal|null
operator|&&
name|call
operator|.
name|operands
operator|.
name|size
argument_list|()
operator|>
literal|2
condition|)
block|{
name|SqlOperator
name|op
init|=
name|call
operator|.
name|getOperator
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|AND
operator|||
name|op
operator|.
name|getKind
argument_list|()
operator|==
name|SqlKind
operator|.
name|OR
condition|)
block|{
name|flat
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|flat
return|;
block|}
block|}
end_class

end_unit

