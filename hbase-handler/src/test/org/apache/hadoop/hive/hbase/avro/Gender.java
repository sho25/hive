begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Avro  *   * DO NOT EDIT DIRECTLY  */
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
name|hbase
operator|.
name|avro
package|;
end_package

begin_enum
annotation|@
name|SuppressWarnings
argument_list|(
literal|"all"
argument_list|)
annotation|@
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|specific
operator|.
name|AvroGenerated
specifier|public
enum|enum
name|Gender
block|{
name|MALE
block|,
name|FEMALE
block|;
specifier|public
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
name|SCHEMA$
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
operator|.
name|Parser
argument_list|()
operator|.
name|parse
argument_list|(
literal|"{\"type\":\"enum\",\"name\":\"Gender\",\"namespace\":\"org.apache.hadoop.hive.hbase.avro\",\"symbols\":[\"MALE\",\"FEMALE\"]}"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
name|getClassSchema
parameter_list|()
block|{
return|return
name|SCHEMA$
return|;
block|}
block|}
end_enum

end_unit

