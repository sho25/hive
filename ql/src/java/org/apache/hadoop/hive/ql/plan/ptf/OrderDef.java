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
name|plan
operator|.
name|ptf
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

begin_class
specifier|public
class|class
name|OrderDef
block|{
name|List
argument_list|<
name|OrderExpressionDef
argument_list|>
name|expressions
decl_stmt|;
specifier|public
name|OrderDef
parameter_list|()
block|{}
specifier|public
name|OrderDef
parameter_list|(
name|PartitionDef
name|pDef
parameter_list|)
block|{
for|for
control|(
name|PTFExpressionDef
name|eDef
range|:
name|pDef
operator|.
name|getExpressions
argument_list|()
control|)
block|{
name|addExpression
argument_list|(
operator|new
name|OrderExpressionDef
argument_list|(
name|eDef
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|OrderExpressionDef
argument_list|>
name|getExpressions
parameter_list|()
block|{
return|return
name|expressions
return|;
block|}
specifier|public
name|void
name|setExpressions
parameter_list|(
name|ArrayList
argument_list|<
name|OrderExpressionDef
argument_list|>
name|expressions
parameter_list|)
block|{
name|this
operator|.
name|expressions
operator|=
name|expressions
expr_stmt|;
block|}
specifier|public
name|void
name|addExpression
parameter_list|(
name|OrderExpressionDef
name|e
parameter_list|)
block|{
name|expressions
operator|=
name|expressions
operator|==
literal|null
condition|?
operator|new
name|ArrayList
argument_list|<
name|OrderExpressionDef
argument_list|>
argument_list|()
else|:
name|expressions
expr_stmt|;
name|expressions
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

