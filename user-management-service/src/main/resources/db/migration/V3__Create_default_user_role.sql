-- Migration V3: Create default USER role
-- Description: Creates the default USER role for public registration

-- Insert default USER role if it doesn't exist
INSERT INTO roles (id, name, description) 
VALUES (
    gen_random_uuid(),
    'USER',
    'Default role for registered users with basic permissions'
) ON CONFLICT (name) DO NOTHING;

-- Ensure ADMIN role exists as well (should already exist from previous migrations)
INSERT INTO roles (id, name, description) 
VALUES (
    gen_random_uuid(),
    'ADMIN',
    'Administrator role with full system access'
) ON CONFLICT (name) DO NOTHING;

-- Create additional common roles
INSERT INTO roles (id, name, description) 
VALUES (
    gen_random_uuid(),
    'DOCTOR',
    'Medical doctor role with patient management permissions'
) ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description) 
VALUES (
    gen_random_uuid(),
    'NURSE',
    'Nurse role with patient care permissions'
) ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, name, description) 
VALUES (
    gen_random_uuid(),
    'PATIENT',
    'Patient role with access to own medical records'
) ON CONFLICT (name) DO NOTHING;

-- Create audit log for role creation
INSERT INTO access_logs (id, action, ip_address, user_agent, success, details, timestamp)
VALUES (
    gen_random_uuid(),
    'SYSTEM_ROLE_CREATION',
    'system',
    'Database Migration V3',
    TRUE,
    'Created default roles: USER, ADMIN, DOCTOR, NURSE, PATIENT',
    CURRENT_TIMESTAMP
);
