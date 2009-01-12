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
package|;
end_package

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

begin_class
specifier|public
class|class
name|MsckDesc
block|{
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitionSpec
decl_stmt|;
specifier|private
name|Path
name|resFile
decl_stmt|;
comment|/**    * Description of a msck command.    * @param tableName Table to check, can be null.    * @param partSpecs Partition specification, can be null.     * @param resFile Where to save the output of the command    */
specifier|public
name|MsckDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partSpecs
parameter_list|,
name|Path
name|resFile
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|partitionSpec
operator|=
name|partSpecs
expr_stmt|;
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
block|}
comment|/**    * @return the table to check    */
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * @param tableName the table to check    */
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
comment|/**    * @return partitions to check.    */
specifier|public
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|getPartitionSpec
parameter_list|()
block|{
return|return
name|partitionSpec
return|;
block|}
comment|/**    * @param partitionSpec partitions to check.    */
specifier|public
name|void
name|setPartitionSpec
parameter_list|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitionSpec
parameter_list|)
block|{
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
block|}
comment|/**    * @return file to save command output to    */
specifier|public
name|Path
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
comment|/**    * @param resFile file to save command output to    */
specifier|public
name|void
name|setResFile
parameter_list|(
name|Path
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
block|}
block|}
end_class

end_unit

