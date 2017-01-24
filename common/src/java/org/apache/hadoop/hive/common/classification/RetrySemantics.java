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
name|common
operator|.
name|classification
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|ElementType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Target
import|;
end_import

begin_comment
comment|/**  * These annotations are meant to indicate how to handle retry logic.  * Initially meant for Metastore API when made across a network, i.e. asynchronously where  * the response may not reach the caller and thus it cannot know if the operation was actually  * performed on the server.  * @see RetryingMetastoreClient  */
end_comment

begin_class
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
literal|"Hive developer"
argument_list|)
specifier|public
class|class
name|RetrySemantics
block|{
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|METHOD
argument_list|)
specifier|public
annotation_defn|@interface
name|Idempotent
block|{
name|String
index|[]
name|value
parameter_list|()
default|default
literal|""
function_decl|;
name|int
name|maxRetryCount
parameter_list|()
default|default
name|Integer
operator|.
name|MAX_VALUE
function_decl|;
name|int
name|delayMs
parameter_list|()
default|default
literal|100
function_decl|;
block|}
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|METHOD
argument_list|)
specifier|public
annotation_defn|@interface
name|ReadOnly
block|{
comment|/*trivially retry-able*/
block|}
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|METHOD
argument_list|)
specifier|public
annotation_defn|@interface
name|CannotRetry
block|{}
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
annotation|@
name|Target
argument_list|(
name|ElementType
operator|.
name|METHOD
argument_list|)
specifier|public
annotation_defn|@interface
name|SafeToRetry
block|{
comment|/*may not be Idempotent but is safe to retry*/
name|String
index|[]
name|value
parameter_list|()
default|default
literal|""
function_decl|;
block|}
block|}
end_class

end_unit

