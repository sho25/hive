begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|streaming
operator|.
name|mutate
operator|.
name|worker
package|;
end_package

begin_comment
comment|/** Computes and appends bucket ids to records that are due to be inserted. */
end_comment

begin_interface
specifier|public
interface|interface
name|BucketIdResolver
block|{
name|Object
name|attachBucketIdToRecord
parameter_list|(
name|Object
name|record
parameter_list|)
function_decl|;
comment|/** See: {@link org.apache.hadoop.hive.ql.exec.ReduceSinkOperator#computeBucketNumber(Object, int)}. */
name|int
name|computeBucketId
parameter_list|(
name|Object
name|record
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

