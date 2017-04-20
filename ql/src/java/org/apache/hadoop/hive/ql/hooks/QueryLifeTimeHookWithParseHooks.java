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
name|hooks
package|;
end_package

begin_comment
comment|/**  * Extension of {@link QueryLifeTimeHook} that has hooks for pre and post parsing of a query.  */
end_comment

begin_interface
specifier|public
interface|interface
name|QueryLifeTimeHookWithParseHooks
extends|extends
name|QueryLifeTimeHook
block|{
comment|/**    * Invoked before a query enters the parse phase.    *    * @param ctx the context for the hook    */
name|void
name|beforeParse
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|)
function_decl|;
comment|/**    * Invoked after a query parsing. Note: if 'hasError' is true,    * the query won't enter the following compilation phase.    *    * @param ctx the context for the hook    * @param hasError whether any error occurred during compilation.    */
name|void
name|afterParse
parameter_list|(
name|QueryLifeTimeHookContext
name|ctx
parameter_list|,
name|boolean
name|hasError
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

