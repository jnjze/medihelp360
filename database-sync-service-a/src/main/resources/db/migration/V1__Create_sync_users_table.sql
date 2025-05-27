-- Create sync_users table for Database Sync Service A
CREATE TABLE sync_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_user_id UUID NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_status CHAR(1) NOT NULL CHECK (user_status IN ('A', 'I', 'D', 'P', 'U')),
    user_roles TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    synced_at TIMESTAMP NOT NULL,
    last_event_version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_sync_users_original_user_id ON sync_users(original_user_id);
CREATE INDEX idx_sync_users_user_email ON sync_users(user_email);
CREATE INDEX idx_sync_users_user_status ON sync_users(user_status);
CREATE INDEX idx_sync_users_is_active ON sync_users(is_active);
CREATE INDEX idx_sync_users_synced_at ON sync_users(synced_at);
CREATE INDEX idx_sync_users_last_event_version ON sync_users(last_event_version);

-- Add comments for documentation
COMMENT ON TABLE sync_users IS 'Synchronized user data from the main user management service';
COMMENT ON COLUMN sync_users.original_user_id IS 'Original user ID from the main service';
COMMENT ON COLUMN sync_users.user_status IS 'User status: A=Active, I=Inactive, D=Disabled, P=Pending, U=Unknown';
COMMENT ON COLUMN sync_users.user_roles IS 'Comma-separated list of user roles';
COMMENT ON COLUMN sync_users.last_event_version IS 'Version number of the last processed event for this user'; 