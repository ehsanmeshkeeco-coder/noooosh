-- ============================================================================
-- Supabase Schema Migration: Water Intake Tracking (user_profiles & water_logs)
-- Migration File: 20260928_water_tracking_tables.sql
-- ============================================================================

-- Ensure pgcrypto extension is enabled for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. Table: user_profiles
-- Stores user personal details, daily hydration targets, and schedule
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.user_profiles (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    daily_water_goal_ml INTEGER NOT NULL DEFAULT 2000 CHECK (daily_water_goal_ml > 0),
    weight_kg NUMERIC(5, 2),
    wake_time TEXT DEFAULT '07:00',
    sleep_time TEXT DEFAULT '23:00',
    avatar_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for user_profiles
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON public.user_profiles (email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_updated_at ON public.user_profiles (updated_at);

-- ============================================================================
-- 2. Table: water_logs
-- Stores individual water intake events logged by users
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.water_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL REFERENCES public.user_profiles (id) ON DELETE CASCADE,
    amount_ml INTEGER NOT NULL CHECK (amount_ml > 0),
    logged_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    source TEXT NOT NULL DEFAULT 'quick_add', -- 'quick_add', 'manual', 'smart_reminder', 'offline_sync'
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Optimized Indexes for Daily Progress Queries & Aggregations
-- 1) Composite index for fast daily queries (e.g. WHERE user_id = $1 AND log_date = $2)
CREATE INDEX IF NOT EXISTS idx_water_logs_user_date ON public.water_logs (user_id, log_date DESC);

-- 2) Composite index for chronological timeline feeds (e.g. today's history)
CREATE INDEX IF NOT EXISTS idx_water_logs_user_logged_at ON public.water_logs (user_id, logged_at DESC);

-- 3) Index on log_date for system-wide range queries
CREATE INDEX IF NOT EXISTS idx_water_logs_date ON public.water_logs (log_date);

-- ============================================================================
-- 3. Automatic updated_at Trigger for user_profiles
-- ============================================================================
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_user_profiles_updated_at ON public.user_profiles;
CREATE TRIGGER trigger_user_profiles_updated_at
BEFORE UPDATE ON public.user_profiles
FOR EACH ROW
EXECUTE FUNCTION public.handle_updated_at();

-- ============================================================================
-- 4. Analytical View: v_daily_water_progress
-- Aggregates daily consumption against user goals for quick dashboard queries
-- ============================================================================
CREATE OR REPLACE VIEW public.v_daily_water_progress AS
SELECT
    wl.user_id,
    wl.log_date,
    COALESCE(SUM(wl.amount_ml), 0) AS total_consumed_ml,
    up.daily_water_goal_ml,
    ROUND(
        (COALESCE(SUM(wl.amount_ml), 0)::NUMERIC / NULLIF(up.daily_water_goal_ml, 0)) * 100,
        1
    ) AS progress_percentage,
    COUNT(wl.id) AS total_logs_count,
    (COALESCE(SUM(wl.amount_ml), 0) >= up.daily_water_goal_ml) AS is_goal_achieved,
    MAX(wl.logged_at) AS last_log_time
FROM public.water_logs wl
JOIN public.user_profiles up ON wl.user_id = up.id
GROUP BY wl.user_id, wl.log_date, up.daily_water_goal_ml;

-- ============================================================================
-- 5. Row Level Security (RLS) & Access Policies
-- ============================================================================
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.water_logs ENABLE ROW LEVEL SECURITY;

-- Allow read & write access for authenticated users & service role
CREATE POLICY "Public Read Access for User Profiles" ON public.user_profiles
    FOR SELECT USING (true);

CREATE POLICY "Manage User Profiles" ON public.user_profiles
    FOR ALL USING (true);

CREATE POLICY "Public Read Access for Water Logs" ON public.water_logs
    FOR SELECT USING (true);

CREATE POLICY "Manage Water Logs" ON public.water_logs
    FOR ALL USING (true);
