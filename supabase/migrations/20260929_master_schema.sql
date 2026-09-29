-- ============================================================================
-- Supabase Schema Migration: Complete Noosh Backend Schema
-- Migration File: 20260929_master_schema.sql
-- ============================================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. Table: water_intakes (Offline-first water consumption logs)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.water_intakes (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    amount_ml INTEGER NOT NULL CHECK (amount_ml > 0),
    consumed_at BIGINT NOT NULL,
    source TEXT NOT NULL DEFAULT 'app_quick',
    created_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_water_intakes_user ON public.water_intakes(user_id);
CREATE INDEX IF NOT EXISTS idx_water_intakes_consumed ON public.water_intakes(consumed_at);

-- ============================================================================
-- 2. Table: health_sync_events (Daily hydration metrics & streaks)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.health_sync_events (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    daily_intake_ml INTEGER NOT NULL,
    daily_goal_ml INTEGER NOT NULL,
    streak_days INTEGER NOT NULL DEFAULT 1,
    timestamp BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_health_sync_user ON public.health_sync_events(user_id);
CREATE INDEX IF NOT EXISTS idx_health_sync_timestamp ON public.health_sync_events(timestamp DESC);

-- ============================================================================
-- 3. Table: user_devices (FCM Push Notification Device Tokens)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.user_devices (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    fcm_token TEXT NOT NULL,
    platform TEXT DEFAULT 'android',
    device_name TEXT,
    app_version TEXT DEFAULT '1.0',
    last_seen_at BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_user_devices_user ON public.user_devices(user_id);
CREATE INDEX IF NOT EXISTS idx_user_devices_token ON public.user_devices(fcm_token);

-- ============================================================================
-- 4. Table: health_alert_events (Health Companion Real-Time Alerts)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.health_alert_events (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    severity TEXT NOT NULL DEFAULT 'LOW',
    payload JSONB,
    created_at BIGINT NOT NULL,
    sent_at BIGINT,
    acknowledged_at BIGINT
);

CREATE INDEX IF NOT EXISTS idx_health_alert_user ON public.health_alert_events(user_id);
CREATE INDEX IF NOT EXISTS idx_health_alert_ack ON public.health_alert_events(acknowledged_at);
CREATE INDEX IF NOT EXISTS idx_health_alert_created ON public.health_alert_events(created_at);

-- ============================================================================
-- 5. Table: health_companion_connections (Companion Connections & Permissions)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.health_companion_connections (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    companion_user_id TEXT NOT NULL,
    companion_name TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'CONNECTED', -- CONNECTED, PAUSED, DISCONNECTED
    alert_policy TEXT NOT NULL DEFAULT 'ALL', -- ALL, MEDIUM_AND_HIGH, HIGH_ONLY
    last_active_at BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_companion_user ON public.health_companion_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_companion_status ON public.health_companion_connections(status);

-- ============================================================================
-- 6. Table: companion_rooms (6-digit Invite Code Pairing Rooms)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.companion_rooms (
    room_code TEXT PRIMARY KEY,
    host_user_id TEXT NOT NULL,
    host_name TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'WAITING', -- WAITING, JOINED, EXPIRED
    created_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_companion_rooms_host ON public.companion_rooms(host_user_id);
CREATE INDEX IF NOT EXISTS idx_companion_rooms_code ON public.companion_rooms(room_code);

-- ============================================================================
-- 7. Row Level Security (RLS) & Access Policies
-- ============================================================================
ALTER TABLE public.water_intakes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_sync_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_alert_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.health_companion_connections ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.companion_rooms ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public water_intakes' AND tablename = 'water_intakes') THEN
        CREATE POLICY "Allow public water_intakes" ON public.water_intakes FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public health_sync_events' AND tablename = 'health_sync_events') THEN
        CREATE POLICY "Allow public health_sync_events" ON public.health_sync_events FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public user_devices' AND tablename = 'user_devices') THEN
        CREATE POLICY "Allow public user_devices" ON public.user_devices FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public health_alert_events' AND tablename = 'health_alert_events') THEN
        CREATE POLICY "Allow public health_alert_events" ON public.health_alert_events FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public health_companion_connections' AND tablename = 'health_companion_connections') THEN
        CREATE POLICY "Allow public health_companion_connections" ON public.health_companion_connections FOR ALL USING (true) WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Allow public companion_rooms' AND tablename = 'companion_rooms') THEN
        CREATE POLICY "Allow public companion_rooms" ON public.companion_rooms FOR ALL USING (true) WITH CHECK (true);
    END IF;
END $$;

-- ============================================================================
-- 8. Storage: Avatars Bucket
-- ============================================================================
INSERT INTO storage.buckets (id, name, public)
VALUES ('avatars', 'avatars', true)
ON CONFLICT (id) DO NOTHING;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Public Avatar Access' AND tablename = 'objects') THEN
        CREATE POLICY "Public Avatar Access" ON storage.objects FOR SELECT USING (bucket_id = 'avatars');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Avatar Upload Access' AND tablename = 'objects') THEN
        CREATE POLICY "Avatar Upload Access" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'avatars');
    END IF;
END $$;
