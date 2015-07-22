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
name|client
package|;
end_package

begin_enum
specifier|public
enum|enum
name|TableType
block|{
name|SOURCE
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
name|SINK
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|;
specifier|private
specifier|static
specifier|final
name|TableType
index|[]
name|INDEX
init|=
name|buildIndex
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableType
index|[]
name|buildIndex
parameter_list|()
block|{
name|TableType
index|[]
name|index
init|=
operator|new
name|TableType
index|[
name|TableType
operator|.
name|values
argument_list|()
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|TableType
name|type
range|:
name|values
argument_list|()
control|)
block|{
name|byte
name|position
init|=
name|type
operator|.
name|getId
argument_list|()
decl_stmt|;
if|if
condition|(
name|index
index|[
name|position
index|]
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Overloaded index: "
operator|+
name|position
argument_list|)
throw|;
block|}
name|index
index|[
name|position
index|]
operator|=
name|type
expr_stmt|;
block|}
return|return
name|index
return|;
block|}
specifier|private
name|byte
name|id
decl_stmt|;
specifier|private
name|TableType
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
specifier|public
name|byte
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
specifier|static
name|TableType
name|valueOf
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
if|if
condition|(
name|id
operator|<
literal|0
operator|||
name|id
operator|>=
name|INDEX
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid id: "
operator|+
name|id
argument_list|)
throw|;
block|}
return|return
name|INDEX
index|[
name|id
index|]
return|;
block|}
block|}
end_enum

end_unit

