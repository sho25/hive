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
name|parse
package|;
end_package

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
name|hive
operator|.
name|ql
operator|.
name|lib
operator|.
name|NodeProcessorCtx
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
name|ImmutableList
import|;
end_import

begin_class
specifier|public
class|class
name|JoinTypeCheckCtx
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * Potential typecheck error reason.    */
specifier|private
name|String
name|error
decl_stmt|;
comment|/**    * The node that generated the potential typecheck error    */
specifier|private
name|ASTNode
name|errorSrcNode
decl_stmt|;
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|RowResolver
argument_list|>
name|m_inputRRLst
decl_stmt|;
specifier|public
name|JoinTypeCheckCtx
parameter_list|(
name|RowResolver
modifier|...
name|inputRRLst
parameter_list|)
block|{
name|m_inputRRLst
operator|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|RowResolver
argument_list|>
argument_list|()
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|inputRRLst
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|/**    * @return the inputRR List    */
specifier|public
name|List
argument_list|<
name|RowResolver
argument_list|>
name|getInputRRList
parameter_list|()
block|{
return|return
name|m_inputRRLst
return|;
block|}
comment|/**    * @param error    *          the error to set    *    */
specifier|public
name|void
name|setError
parameter_list|(
name|String
name|error
parameter_list|,
name|ASTNode
name|errorSrcNode
parameter_list|)
block|{
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
name|this
operator|.
name|errorSrcNode
operator|=
name|errorSrcNode
expr_stmt|;
block|}
comment|/**    * @return the error    */
specifier|public
name|String
name|getError
parameter_list|()
block|{
return|return
name|error
return|;
block|}
specifier|public
name|ASTNode
name|getErrorSrcNode
parameter_list|()
block|{
return|return
name|errorSrcNode
return|;
block|}
block|}
end_class

end_unit

