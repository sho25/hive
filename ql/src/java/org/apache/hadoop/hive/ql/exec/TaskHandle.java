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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|mapred
operator|.
name|Counters
import|;
end_import

begin_class
specifier|public
class|class
name|TaskHandle
block|{
comment|// The eventual goal is to monitor the progress of all the tasks, not only the map reduce task.
comment|// The execute() method of the tasks will return immediately, and return a task specific handle to
comment|// monitor the progress of that task.
comment|// Right now, the behavior is kind of broken, ExecDriver's execute method calls progress - instead it should
comment|// be invoked by Driver
specifier|public
name|Counters
name|getCounters
parameter_list|()
throws|throws
name|IOException
block|{
comment|// default implementation
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

