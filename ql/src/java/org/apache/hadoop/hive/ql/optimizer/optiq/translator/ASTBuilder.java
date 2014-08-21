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
name|RelOptHiveTable
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
name|JoinRelType
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
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
import|;
end_import

begin_class
class|class
name|ASTBuilder
block|{
specifier|static
name|ASTBuilder
name|construct
parameter_list|(
name|int
name|tokenType
parameter_list|,
name|String
name|text
parameter_list|)
block|{
name|ASTBuilder
name|b
init|=
operator|new
name|ASTBuilder
argument_list|()
decl_stmt|;
name|b
operator|.
name|curr
operator|=
name|createAST
argument_list|(
name|tokenType
argument_list|,
name|text
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
specifier|static
name|ASTNode
name|createAST
parameter_list|(
name|int
name|tokenType
parameter_list|,
name|String
name|text
parameter_list|)
block|{
return|return
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|tokenType
argument_list|,
name|text
argument_list|)
return|;
block|}
specifier|static
name|ASTNode
name|destNode
parameter_list|()
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_DESTINATION
argument_list|,
literal|"TOK_DESTINATION"
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
name|TOK_DIR
argument_list|,
literal|"TOK_DIR"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|TOK_TMP_FILE
argument_list|,
literal|"TOK_TMP_FILE"
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|table
parameter_list|(
name|TableAccessRelBase
name|scan
parameter_list|)
block|{
name|RelOptHiveTable
name|hTbl
init|=
operator|(
name|RelOptHiveTable
operator|)
name|scan
operator|.
name|getTable
argument_list|()
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
name|TOK_TABREF
argument_list|,
literal|"TOK_TABREF"
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
name|TOK_TABNAME
argument_list|,
literal|"TOK_TABNAME"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|hTbl
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|hTbl
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|hTbl
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|join
parameter_list|(
name|ASTNode
name|left
parameter_list|,
name|ASTNode
name|right
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|ASTNode
name|cond
parameter_list|,
name|boolean
name|semiJoin
parameter_list|)
block|{
name|ASTBuilder
name|b
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|joinType
condition|)
block|{
case|case
name|INNER
case|:
if|if
condition|(
name|semiJoin
condition|)
block|{
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_LEFTSEMIJOIN
argument_list|,
literal|"TOK_LEFTSEMIJOIN"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_JOIN
argument_list|,
literal|"TOK_JOIN"
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|LEFT
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_LEFTOUTERJOIN
argument_list|,
literal|"TOK_LEFTOUTERJOIN"
argument_list|)
expr_stmt|;
break|break;
case|case
name|RIGHT
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_RIGHTOUTERJOIN
argument_list|,
literal|"TOK_RIGHTOUTERJOIN"
argument_list|)
expr_stmt|;
break|break;
case|case
name|FULL
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_FULLOUTERJOIN
argument_list|,
literal|"TOK_FULLOUTERJOIN"
argument_list|)
expr_stmt|;
break|break;
block|}
name|b
operator|.
name|add
argument_list|(
name|left
argument_list|)
operator|.
name|add
argument_list|(
name|right
argument_list|)
operator|.
name|add
argument_list|(
name|cond
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|subQuery
parameter_list|(
name|ASTNode
name|qry
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_SUBQUERY
argument_list|,
literal|"TOK_SUBQUERY"
argument_list|)
operator|.
name|add
argument_list|(
name|qry
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|alias
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|qualifiedName
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|colName
parameter_list|)
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
name|DOT
argument_list|,
literal|"."
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
name|TOK_TABLE_OR_COL
argument_list|,
literal|"TOK_TABLE_OR_COL"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|tableName
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|colName
argument_list|)
decl_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|unqualifiedName
parameter_list|(
name|String
name|colName
parameter_list|)
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
name|TOK_TABLE_OR_COL
argument_list|,
literal|"TOK_TABLE_OR_COL"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|colName
argument_list|)
decl_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|where
parameter_list|(
name|ASTNode
name|cond
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_WHERE
argument_list|,
literal|"TOK_WHERE"
argument_list|)
operator|.
name|add
argument_list|(
name|cond
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|having
parameter_list|(
name|ASTNode
name|cond
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_HAVING
argument_list|,
literal|"TOK_HAVING"
argument_list|)
operator|.
name|add
argument_list|(
name|cond
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|limit
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_LIMIT
argument_list|,
literal|"TOK_LIMIT"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Number
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|selectExpr
parameter_list|(
name|ASTNode
name|expr
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_SELEXPR
argument_list|,
literal|"TOK_SELEXPR"
argument_list|)
operator|.
name|add
argument_list|(
name|expr
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|alias
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|static
name|ASTNode
name|literal
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
name|Object
name|val
init|=
name|literal
operator|.
name|getValue3
argument_list|()
decl_stmt|;
name|int
name|type
init|=
literal|0
decl_stmt|;
name|SqlTypeName
name|sqlType
init|=
name|literal
operator|.
name|getType
argument_list|()
operator|.
name|getSqlTypeName
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|sqlType
condition|)
block|{
case|case
name|TINYINT
case|:
name|type
operator|=
name|HiveParser
operator|.
name|TinyintLiteral
expr_stmt|;
break|break;
case|case
name|SMALLINT
case|:
name|type
operator|=
name|HiveParser
operator|.
name|SmallintLiteral
expr_stmt|;
break|break;
case|case
name|INTEGER
case|:
case|case
name|BIGINT
case|:
name|type
operator|=
name|HiveParser
operator|.
name|BigintLiteral
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|REAL
case|:
name|type
operator|=
name|HiveParser
operator|.
name|Number
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
name|type
operator|=
name|HiveParser
operator|.
name|StringLiteral
expr_stmt|;
name|val
operator|=
literal|"'"
operator|+
name|String
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
operator|+
literal|"'"
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|type
operator|=
operator|(
operator|(
name|Boolean
operator|)
name|val
operator|)
operator|.
name|booleanValue
argument_list|()
condition|?
name|HiveParser
operator|.
name|KW_TRUE
else|:
name|HiveParser
operator|.
name|KW_FALSE
expr_stmt|;
break|break;
case|case
name|NULL
case|:
name|type
operator|=
name|HiveParser
operator|.
name|TOK_NULL
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported Type: "
operator|+
name|sqlType
argument_list|)
throw|;
block|}
return|return
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|type
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
name|ASTNode
name|curr
decl_stmt|;
name|ASTNode
name|node
parameter_list|()
block|{
return|return
name|curr
return|;
block|}
name|ASTBuilder
name|add
parameter_list|(
name|int
name|tokenType
parameter_list|,
name|String
name|text
parameter_list|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curr
argument_list|,
name|createAST
argument_list|(
name|tokenType
argument_list|,
name|text
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|ASTBuilder
name|add
parameter_list|(
name|ASTBuilder
name|b
parameter_list|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curr
argument_list|,
name|b
operator|.
name|curr
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|ASTBuilder
name|add
parameter_list|(
name|ASTNode
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|!=
literal|null
condition|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curr
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

