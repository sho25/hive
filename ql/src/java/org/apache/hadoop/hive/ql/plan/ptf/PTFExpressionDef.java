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
name|ExprNodeEvaluator
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
name|PTFUtils
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
name|ExprNodeDesc
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

begin_class
specifier|public
class|class
name|PTFExpressionDef
block|{
name|String
name|expressionTreeString
decl_stmt|;
name|ExprNodeDesc
name|exprNode
decl_stmt|;
specifier|transient
name|ExprNodeEvaluator
name|exprEvaluator
decl_stmt|;
specifier|transient
name|ObjectInspector
name|OI
decl_stmt|;
static|static
block|{
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|PTFExpressionDef
operator|.
name|class
argument_list|,
literal|"exprEvaluator"
argument_list|,
literal|"OI"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PTFExpressionDef
parameter_list|()
block|{}
specifier|public
name|PTFExpressionDef
parameter_list|(
name|PTFExpressionDef
name|e
parameter_list|)
block|{
name|expressionTreeString
operator|=
name|e
operator|.
name|getExpressionTreeString
argument_list|()
expr_stmt|;
name|exprNode
operator|=
name|e
operator|.
name|getExprNode
argument_list|()
expr_stmt|;
name|exprEvaluator
operator|=
name|e
operator|.
name|getExprEvaluator
argument_list|()
expr_stmt|;
name|OI
operator|=
name|e
operator|.
name|getOI
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getExpressionTreeString
parameter_list|()
block|{
return|return
name|expressionTreeString
return|;
block|}
specifier|public
name|void
name|setExpressionTreeString
parameter_list|(
name|String
name|expressionTreeString
parameter_list|)
block|{
name|this
operator|.
name|expressionTreeString
operator|=
name|expressionTreeString
expr_stmt|;
block|}
specifier|public
name|ExprNodeDesc
name|getExprNode
parameter_list|()
block|{
return|return
name|exprNode
return|;
block|}
specifier|public
name|void
name|setExprNode
parameter_list|(
name|ExprNodeDesc
name|exprNode
parameter_list|)
block|{
name|this
operator|.
name|exprNode
operator|=
name|exprNode
expr_stmt|;
block|}
specifier|public
name|ExprNodeEvaluator
name|getExprEvaluator
parameter_list|()
block|{
return|return
name|exprEvaluator
return|;
block|}
specifier|public
name|void
name|setExprEvaluator
parameter_list|(
name|ExprNodeEvaluator
name|exprEvaluator
parameter_list|)
block|{
name|this
operator|.
name|exprEvaluator
operator|=
name|exprEvaluator
expr_stmt|;
block|}
specifier|public
name|ObjectInspector
name|getOI
parameter_list|()
block|{
return|return
name|OI
return|;
block|}
specifier|public
name|void
name|setOI
parameter_list|(
name|ObjectInspector
name|oI
parameter_list|)
block|{
name|OI
operator|=
name|oI
expr_stmt|;
block|}
block|}
end_class

end_unit

