-- ============================================================
-- Hospital Management System (HMS) — PostgreSQL Setup Script
-- ============================================================

-- ── Sequences ────────────────────────────────────────────────

CREATE SEQUENCE IF NOT EXISTS public.ad_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.dr_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.rc_user_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.appointment_appointment_id_seq AS integer START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.doctor_doctor_id_seq AS integer START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.patientrecords_record_id_seq AS integer START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.timeslots_slot_id_seq AS integer START WITH 1 INCREMENT BY 1;


-- ── Tables ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS public.hmsuser (
    user_role       VARCHAR(2)  NOT NULL,
    user_id         INTEGER     NOT NULL,
    user_username   VARCHAR(32) NOT NULL UNIQUE,
    user_password   VARCHAR(32) NOT NULL,
    user_name       TEXT        NOT NULL,
    CONSTRAINT hmsuser_pkey      PRIMARY KEY (user_role, user_id),
    CONSTRAINT rolechk           CHECK (user_role IN ('AD', 'DR', 'RC')),
    CONSTRAINT idchk             CHECK (user_id <= 999999),
    CONSTRAINT alphanumuser      CHECK (user_username ~ '^[A-Za-z0-9]+$'),
    CONSTRAINT alphanumpass      CHECK (user_password ~ '^[A-Za-z0-9]+$'),
    CONSTRAINT alphanumname      CHECK (user_name ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$')
);

CREATE TABLE IF NOT EXISTS public.doctor (
    doctor_id               INTEGER NOT NULL DEFAULT nextval('public.doctor_doctor_id_seq'),
    doctor_specialisation   TEXT,
    CONSTRAINT doctor_pkey PRIMARY KEY (doctor_id)
);
ALTER SEQUENCE public.doctor_doctor_id_seq OWNED BY public.doctor.doctor_id;

CREATE TABLE IF NOT EXISTS public.timeslots (
    slot_id     INTEGER  NOT NULL DEFAULT nextval('public.timeslots_slot_id_seq'),
    day         SMALLINT NOT NULL,
    start_time  TIME     NOT NULL,
    CONSTRAINT timeslots_pkey PRIMARY KEY (slot_id)
);
ALTER SEQUENCE public.timeslots_slot_id_seq OWNED BY public.timeslots.slot_id;

CREATE TABLE IF NOT EXISTS public.schedule (
    doctor_id    INTEGER NOT NULL,
    slot_id      INTEGER NOT NULL,
    availability BOOLEAN NOT NULL,
    CONSTRAINT schedule_pkey         PRIMARY KEY (doctor_id, slot_id),
    CONSTRAINT schedule_doctor_fkey  FOREIGN KEY (doctor_id) REFERENCES public.doctor(doctor_id),
    CONSTRAINT schedule_slot_fkey    FOREIGN KEY (slot_id)   REFERENCES public.timeslots(slot_id)
);

CREATE TABLE IF NOT EXISTS public.patientrecords (
    record_id       INTEGER  NOT NULL DEFAULT nextval('public.patientrecords_record_id_seq'),
    patient_name    TEXT     NOT NULL,
    patient_gender  CHAR(1)  NOT NULL DEFAULT 'X',
    patient_age     SMALLINT NOT NULL,
    medical_history TEXT,
    CONSTRAINT patientrecords_pkey PRIMARY KEY (record_id),
    CONSTRAINT genderchk           CHECK (patient_gender IN ('M', 'F', 'X')),
    CONSTRAINT agechk              CHECK (patient_age >= 0 AND patient_age <= 255),
    CONSTRAINT alphanumname        CHECK (patient_name ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$')
);
ALTER SEQUENCE public.patientrecords_record_id_seq OWNED BY public.patientrecords.record_id;

CREATE TABLE IF NOT EXISTS public.appointment (
    appointment_id   INTEGER   NOT NULL DEFAULT nextval('public.appointment_appointment_id_seq'),
    doctor_id        INTEGER   NOT NULL,
    record_id        INTEGER   NOT NULL,
    appointment_time TIMESTAMP NOT NULL,
    diagnosis        TEXT,
    prescription     TEXT,
    status           BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT appointment_pkey        PRIMARY KEY (appointment_id),
    CONSTRAINT uq_doctor_time          UNIQUE (doctor_id, appointment_time),
    CONSTRAINT appointment_doctor_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(doctor_id),
    CONSTRAINT appointment_record_fkey FOREIGN KEY (record_id) REFERENCES public.patientrecords(record_id)
);
ALTER SEQUENCE public.appointment_appointment_id_seq OWNED BY public.appointment.appointment_id;


-- ── Trigger: auto-assign user_id per role ────────────────────

CREATE OR REPLACE FUNCTION public.set_new_id() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.user_role = 'AD' THEN
        NEW.user_id := nextval('ad_user_id_seq');
    ELSIF NEW.user_role = 'DR' THEN
        NEW.user_id := nextval('dr_user_id_seq');
    ELSIF NEW.user_role = 'RC' THEN
        NEW.user_id := nextval('rc_user_id_seq');
    ELSE
        RAISE EXCEPTION 'Invalid user role: %', NEW.user_role;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS set_user_id ON public.hmsuser;
CREATE TRIGGER set_user_id
    BEFORE INSERT ON public.hmsuser
    FOR EACH ROW EXECUTE FUNCTION public.set_new_id();


-- ── Views ────────────────────────────────────────────────────

CREATE OR REPLACE VIEW public.authinfo AS
    SELECT user_role, user_id, user_username AS username, user_password AS password
    FROM public.hmsuser;

CREATE OR REPLACE VIEW public.admininfo AS
    SELECT user_id AS admin_id, user_username AS admin_username,
           user_password AS admin_password, user_name AS admin_name
    FROM public.hmsuser
    WHERE user_role = 'AD';

CREATE OR REPLACE VIEW public.receptionistinfo AS
    SELECT user_id AS receptionist_id, user_username AS receptionist_username,
           user_password AS receptionist_password, user_name AS receptionist_name
    FROM public.hmsuser
    WHERE user_role = 'RC';

CREATE OR REPLACE VIEW public.doctorinfo AS
    SELECT u.user_id AS doctor_id, u.user_username AS doctor_username,
           u.user_password AS doctor_password, u.user_name AS doctor_name,
           d.doctor_specialisation AS specialisation
    FROM public.hmsuser u
    JOIN public.doctor d ON u.user_id = d.doctor_id AND u.user_role = 'DR';

CREATE OR REPLACE VIEW public.activeappointments AS
    SELECT a.appointment_id, a.doctor_id, u.user_name AS doctor_name,
           a.record_id, r.patient_name, a.appointment_time
    FROM public.appointment a
    LEFT JOIN public.hmsuser u ON u.user_id = a.doctor_id AND u.user_role = 'DR'
    LEFT JOIN public.patientrecords r ON a.record_id = r.record_id
    WHERE a.status = TRUE;


-- ── Seed Data: timeslots (all 168 hourly slots, Mon–Sun) ─────

INSERT INTO public.timeslots (day, start_time) VALUES
(1,'00:00'),(1,'01:00'),(1,'02:00'),(1,'03:00'),(1,'04:00'),(1,'05:00'),
(1,'06:00'),(1,'07:00'),(1,'08:00'),(1,'09:00'),(1,'10:00'),(1,'11:00'),
(1,'12:00'),(1,'13:00'),(1,'14:00'),(1,'15:00'),(1,'16:00'),(1,'17:00'),
(1,'18:00'),(1,'19:00'),(1,'20:00'),(1,'21:00'),(1,'22:00'),(1,'23:00'),
(2,'00:00'),(2,'01:00'),(2,'02:00'),(2,'03:00'),(2,'04:00'),(2,'05:00'),
(2,'06:00'),(2,'07:00'),(2,'08:00'),(2,'09:00'),(2,'10:00'),(2,'11:00'),
(2,'12:00'),(2,'13:00'),(2,'14:00'),(2,'15:00'),(2,'16:00'),(2,'17:00'),
(2,'18:00'),(2,'19:00'),(2,'20:00'),(2,'21:00'),(2,'22:00'),(2,'23:00'),
(3,'00:00'),(3,'01:00'),(3,'02:00'),(3,'03:00'),(3,'04:00'),(3,'05:00'),
(3,'06:00'),(3,'07:00'),(3,'08:00'),(3,'09:00'),(3,'10:00'),(3,'11:00'),
(3,'12:00'),(3,'13:00'),(3,'14:00'),(3,'15:00'),(3,'16:00'),(3,'17:00'),
(3,'18:00'),(3,'19:00'),(3,'20:00'),(3,'21:00'),(3,'22:00'),(3,'23:00'),
(4,'00:00'),(4,'01:00'),(4,'02:00'),(4,'03:00'),(4,'04:00'),(4,'05:00'),
(4,'06:00'),(4,'07:00'),(4,'08:00'),(4,'09:00'),(4,'10:00'),(4,'11:00'),
(4,'12:00'),(4,'13:00'),(4,'14:00'),(4,'15:00'),(4,'16:00'),(4,'17:00'),
(4,'18:00'),(4,'19:00'),(4,'20:00'),(4,'21:00'),(4,'22:00'),(4,'23:00'),
(5,'00:00'),(5,'01:00'),(5,'02:00'),(5,'03:00'),(5,'04:00'),(5,'05:00'),
(5,'06:00'),(5,'07:00'),(5,'08:00'),(5,'09:00'),(5,'10:00'),(5,'11:00'),
(5,'12:00'),(5,'13:00'),(5,'14:00'),(5,'15:00'),(5,'16:00'),(5,'17:00'),
(5,'18:00'),(5,'19:00'),(5,'20:00'),(5,'21:00'),(5,'22:00'),(5,'23:00'),
(6,'00:00'),(6,'01:00'),(6,'02:00'),(6,'03:00'),(6,'04:00'),(6,'05:00'),
(6,'06:00'),(6,'07:00'),(6,'08:00'),(6,'09:00'),(6,'10:00'),(6,'11:00'),
(6,'12:00'),(6,'13:00'),(6,'14:00'),(6,'15:00'),(6,'16:00'),(6,'17:00'),
(6,'18:00'),(6,'19:00'),(6,'20:00'),(6,'21:00'),(6,'22:00'),(6,'23:00'),
(7,'00:00'),(7,'01:00'),(7,'02:00'),(7,'03:00'),(7,'04:00'),(7,'05:00'),
(7,'06:00'),(7,'07:00'),(7,'08:00'),(7,'09:00'),(7,'10:00'),(7,'11:00'),
(7,'12:00'),(7,'13:00'),(7,'14:00'),(7,'15:00'),(7,'16:00'),(7,'17:00'),
(7,'18:00'),(7,'19:00'),(7,'20:00'),(7,'21:00'),(7,'22:00'),(7,'23:00');


-- ── Seed Data: default admin account ─────────────────────────
-- Password: admin123
-- Change this before going to production!

INSERT INTO public.hmsuser (user_role, user_username, user_password, user_name)
VALUES ('AD', 'admin', 'admin123', 'Admin User');
