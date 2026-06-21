--
-- PostgreSQL database dump
--

\restrict sRTDGShcsay8I5FaPMLhGhRys0dYAEYbW0buVwKbChOZ8DRAA1BSsBqlak09WYV

-- Dumped from database version 18.4 (Debian 18.4-1.pgdg13+1)
-- Dumped by pg_dump version 18.4 (Debian 18.4-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE ONLY public.schedule DROP CONSTRAINT schedule_slot_id_fkey;
ALTER TABLE ONLY public.schedule DROP CONSTRAINT schedule_doctor_id_fkey;
ALTER TABLE ONLY public.appointment DROP CONSTRAINT appointment_record_id_fkey;
ALTER TABLE ONLY public.appointment DROP CONSTRAINT appointment_doctor_id_fkey;
DROP TRIGGER set_user_id ON public.hmsuser;
ALTER TABLE ONLY public.timeslots DROP CONSTRAINT timeslots_pkey;
ALTER TABLE ONLY public.schedule DROP CONSTRAINT schedule_pkey;
ALTER TABLE ONLY public.patientrecords DROP CONSTRAINT patientrecords_pkey;
ALTER TABLE ONLY public.hmsuser DROP CONSTRAINT hmsuser_user_username_key;
ALTER TABLE ONLY public.hmsuser DROP CONSTRAINT hmsuser_pkey;
ALTER TABLE ONLY public.doctor DROP CONSTRAINT doctor_pkey;
ALTER TABLE ONLY public.appointment DROP CONSTRAINT appointment_pkey;
ALTER TABLE public.timeslots ALTER COLUMN slot_id DROP DEFAULT;
ALTER TABLE public.patientrecords ALTER COLUMN record_id DROP DEFAULT;
ALTER TABLE public.doctor ALTER COLUMN doctor_id DROP DEFAULT;
ALTER TABLE public.appointment ALTER COLUMN appointment_id DROP DEFAULT;
DROP SEQUENCE public.timeslots_slot_id_seq;
DROP TABLE public.timeslots;
DROP TABLE public.schedule;
DROP VIEW public.receptionistinfo;
DROP SEQUENCE public.rc_user_id_seq;
DROP SEQUENCE public.patientrecords_record_id_seq;
DROP SEQUENCE public.dr_user_id_seq;
DROP VIEW public.doctorinfo;
DROP SEQUENCE public.doctor_doctor_id_seq;
DROP TABLE public.doctor;
DROP VIEW public.authinfo;
DROP SEQUENCE public.appointment_appointment_id_seq;
DROP VIEW public.admininfo;
DROP SEQUENCE public.ad_user_id_seq;
DROP VIEW public.activeappointments;
DROP TABLE public.patientrecords;
DROP TABLE public.hmsuser;
DROP TABLE public.appointment;
DROP FUNCTION public.set_new_id();
--
-- Name: set_new_id(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.set_new_id() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
 IF NEW.user_role = 'AD' THEN
  NEW.user_id := nextval('ad_user_id_seq');
 ELSIF NEW.user_role = 'DR' THEN
  NEW.user_id := nextval('DR_user_id_seq');
 ELSIF NEW.user_role = 'RC' THEN
  NEW.user_id := nextval('rc_user_id_seq');
 ELSE
  RAISE EXCEPTION 'user role invalid';
 END IF;

 RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: appointment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment (
    appointment_id integer NOT NULL,
    doctor_id integer NOT NULL,
    record_id integer NOT NULL,
    appointment_time timestamp without time zone NOT NULL,
    diagnosis text,
    prescription text,
    status boolean DEFAULT true NOT NULL
);


--
-- Name: hmsuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hmsuser (
    user_role character varying(2) NOT NULL,
    user_id integer NOT NULL,
    user_username character varying(32) NOT NULL,
    user_password character varying(32) NOT NULL,
    user_name text NOT NULL,
    CONSTRAINT alphanumname CHECK ((user_name ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$'::text)),
    CONSTRAINT alphanumpass CHECK (((user_password)::text ~ '^[A-Za-z0-9]+$'::text)),
    CONSTRAINT alphanumuser CHECK (((user_username)::text ~ '^[A-Za-z0-9]+$'::text)),
    CONSTRAINT idchk CHECK ((user_id <= 999999)),
    CONSTRAINT rolechk CHECK (((user_role)::text = ANY ((ARRAY['AD'::character varying, 'DR'::character varying, 'RC'::character varying])::text[])))
);


--
-- Name: patientrecords; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.patientrecords (
    record_id integer NOT NULL,
    patient_name text NOT NULL,
    patient_gender character(1) DEFAULT 'X'::bpchar NOT NULL,
    patient_age smallint NOT NULL,
    CONSTRAINT agechk CHECK (((patient_age >= 0) AND (patient_age <= 255))),
    CONSTRAINT alphanumname CHECK ((patient_name ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$'::text)),
    CONSTRAINT genderchk CHECK ((patient_gender = ANY (ARRAY['M'::bpchar, 'F'::bpchar, 'X'::bpchar])))
);


--
-- Name: activeappointments; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.activeappointments AS
 SELECT a.appointment_id,
    a.doctor_id,
    u.user_name AS doctor_name,
    a.record_id,
    r.patient_name,
    a.appointment_time
   FROM ((public.appointment a
     LEFT JOIN public.hmsuser u ON (((u.user_id = a.doctor_id) AND ((u.user_role)::text = 'DR'::text))))
     LEFT JOIN public.patientrecords r ON ((a.record_id = r.record_id)))
  WHERE (a.status = true);


--
-- Name: ad_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ad_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: admininfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.admininfo AS
 SELECT user_id AS admin_id,
    user_username AS admin_username,
    user_password AS admin_password,
    user_name AS admin_name
   FROM public.hmsuser u
  WHERE ((user_role)::text = 'AD'::text);


--
-- Name: appointment_appointment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.appointment_appointment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: appointment_appointment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.appointment_appointment_id_seq OWNED BY public.appointment.appointment_id;


--
-- Name: authinfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.authinfo AS
 SELECT user_role,
    user_id,
    user_username AS username,
    user_password AS password
   FROM public.hmsuser u;


--
-- Name: doctor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.doctor (
    doctor_id integer NOT NULL,
    doctor_specialisation text
);


--
-- Name: doctor_doctor_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.doctor_doctor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: doctor_doctor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.doctor_doctor_id_seq OWNED BY public.doctor.doctor_id;


--
-- Name: doctorinfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.doctorinfo AS
 SELECT u.user_id AS doctor_id,
    u.user_username AS doctor_username,
    u.user_password AS doctor_password,
    u.user_name AS doctor_name,
    d.doctor_specialisation AS specialisation
   FROM (public.hmsuser u
     JOIN public.doctor d ON (((u.user_id = d.doctor_id) AND ((u.user_role)::text = 'DR'::text))));


--
-- Name: dr_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.dr_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patientrecords_record_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.patientrecords_record_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: patientrecords_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.patientrecords_record_id_seq OWNED BY public.patientrecords.record_id;


--
-- Name: rc_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rc_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: receptionistinfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.receptionistinfo AS
 SELECT user_id AS receptionist_id,
    user_username AS receptionist_username,
    user_password AS receptionist_password,
    user_name AS receptionist_name
   FROM public.hmsuser u
  WHERE ((user_role)::text = 'RC'::text);


--
-- Name: schedule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schedule (
    doctor_id integer NOT NULL,
    slot_id integer NOT NULL,
    availability boolean NOT NULL
);


--
-- Name: timeslots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.timeslots (
    slot_id integer NOT NULL,
    day smallint NOT NULL,
    start_time time without time zone NOT NULL
);


--
-- Name: timeslots_slot_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.timeslots_slot_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: timeslots_slot_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.timeslots_slot_id_seq OWNED BY public.timeslots.slot_id;


--
-- Name: appointment appointment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment ALTER COLUMN appointment_id SET DEFAULT nextval('public.appointment_appointment_id_seq'::regclass);


--
-- Name: doctor doctor_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.doctor ALTER COLUMN doctor_id SET DEFAULT nextval('public.doctor_doctor_id_seq'::regclass);


--
-- Name: patientrecords record_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patientrecords ALTER COLUMN record_id SET DEFAULT nextval('public.patientrecords_record_id_seq'::regclass);


--
-- Name: timeslots slot_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timeslots ALTER COLUMN slot_id SET DEFAULT nextval('public.timeslots_slot_id_seq'::regclass);


--
-- Data for Name: appointment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: doctor; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: hmsuser; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.hmsuser (user_role, user_id, user_username, user_password, user_name) VALUES ('AD', 1, 'nizam1up', 'Zxcv123', 'Nizam Bin Nazri');
INSERT INTO public.hmsuser (user_role, user_id, user_username, user_password, user_name) VALUES ('AD', 2, 'adam2up', 'Asdf123', 'Muhammad Adam Iqbal Bin Hafiz');
INSERT INTO public.hmsuser (user_role, user_id, user_username, user_password, user_name) VALUES ('AD', 3, 'tufayl3up', 'Zxcv456', 'Muhammad Tufayl Aiman Bin Sulaiman');
INSERT INTO public.hmsuser (user_role, user_id, user_username, user_password, user_name) VALUES ('AD', 4, 'nadirah4up', 'Asdf456', 'Nur Nadirah Binti Dominggo Yusuf');
INSERT INTO public.hmsuser (user_role, user_id, user_username, user_password, user_name) VALUES ('DR', 1, 'hateRichards', 'DOOM', 'Victor von Doom');
INSERT INTO public.hmsuser (user_role, user_id, user_username, user_password, user_name) VALUES ('RC', 1, 'futureSpengler', 'Ecto1', 'Janine Melnitz');


--
-- Data for Name: patientrecords; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: schedule; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: timeslots; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (1, 1, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (2, 1, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (3, 1, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (4, 1, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (5, 1, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (6, 1, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (7, 1, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (8, 1, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (9, 1, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (10, 1, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (11, 1, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (12, 1, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (13, 1, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (14, 1, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (15, 1, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (16, 1, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (17, 1, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (18, 1, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (19, 1, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (20, 1, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (21, 1, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (22, 1, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (23, 1, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (24, 1, '23:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (25, 2, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (26, 2, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (27, 2, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (28, 2, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (29, 2, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (30, 2, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (31, 2, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (32, 2, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (33, 2, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (34, 2, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (35, 2, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (36, 2, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (37, 2, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (38, 2, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (39, 2, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (40, 2, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (41, 2, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (42, 2, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (43, 2, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (44, 2, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (45, 2, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (46, 2, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (47, 2, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (48, 2, '23:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (49, 3, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (50, 3, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (51, 3, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (52, 3, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (53, 3, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (54, 3, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (55, 3, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (56, 3, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (57, 3, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (58, 3, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (59, 3, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (60, 3, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (61, 3, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (62, 3, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (63, 3, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (64, 3, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (65, 3, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (66, 3, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (67, 3, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (68, 3, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (69, 3, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (70, 3, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (71, 3, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (72, 3, '23:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (73, 4, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (74, 4, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (75, 4, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (76, 4, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (77, 4, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (78, 4, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (79, 4, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (80, 4, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (81, 4, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (82, 4, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (83, 4, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (84, 4, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (85, 4, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (86, 4, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (87, 4, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (88, 4, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (89, 4, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (90, 4, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (91, 4, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (92, 4, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (93, 4, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (94, 4, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (95, 4, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (96, 4, '23:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (97, 5, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (98, 5, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (99, 5, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (100, 5, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (101, 5, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (102, 5, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (103, 5, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (104, 5, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (105, 5, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (106, 5, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (107, 5, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (108, 5, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (109, 5, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (110, 5, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (111, 5, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (112, 5, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (113, 5, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (114, 5, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (115, 5, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (116, 5, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (117, 5, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (118, 5, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (119, 5, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (120, 5, '23:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (121, 6, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (122, 6, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (123, 6, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (124, 6, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (125, 6, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (126, 6, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (127, 6, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (128, 6, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (129, 6, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (130, 6, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (131, 6, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (132, 6, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (133, 6, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (134, 6, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (135, 6, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (136, 6, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (137, 6, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (138, 6, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (139, 6, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (140, 6, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (141, 6, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (142, 6, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (143, 6, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (144, 6, '23:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (145, 7, '00:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (146, 7, '01:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (147, 7, '02:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (148, 7, '03:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (149, 7, '04:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (150, 7, '05:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (151, 7, '06:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (152, 7, '07:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (153, 7, '08:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (154, 7, '09:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (155, 7, '10:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (156, 7, '11:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (157, 7, '12:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (158, 7, '13:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (159, 7, '14:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (160, 7, '15:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (161, 7, '16:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (162, 7, '17:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (163, 7, '18:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (164, 7, '19:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (165, 7, '20:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (166, 7, '21:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (167, 7, '22:00:00');
INSERT INTO public.timeslots (slot_id, day, start_time) VALUES (168, 7, '23:00:00');


--
-- Name: ad_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.ad_user_id_seq', 4, true);


--
-- Name: appointment_appointment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.appointment_appointment_id_seq', 1, false);


--
-- Name: doctor_doctor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.doctor_doctor_id_seq', 1, false);


--
-- Name: dr_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.dr_user_id_seq', 1, true);


--
-- Name: patientrecords_record_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.patientrecords_record_id_seq', 1, false);


--
-- Name: rc_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rc_user_id_seq', 1, true);


--
-- Name: timeslots_slot_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.timeslots_slot_id_seq', 168, true);


--
-- Name: appointment appointment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT appointment_pkey PRIMARY KEY (appointment_id);


--
-- Name: doctor doctor_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.doctor
    ADD CONSTRAINT doctor_pkey PRIMARY KEY (doctor_id);


--
-- Name: hmsuser hmsuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hmsuser
    ADD CONSTRAINT hmsuser_pkey PRIMARY KEY (user_role, user_id);


--
-- Name: hmsuser hmsuser_user_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hmsuser
    ADD CONSTRAINT hmsuser_user_username_key UNIQUE (user_username);


--
-- Name: patientrecords patientrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.patientrecords
    ADD CONSTRAINT patientrecords_pkey PRIMARY KEY (record_id);


--
-- Name: schedule schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule
    ADD CONSTRAINT schedule_pkey PRIMARY KEY (doctor_id, slot_id);


--
-- Name: timeslots timeslots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.timeslots
    ADD CONSTRAINT timeslots_pkey PRIMARY KEY (slot_id);


--
-- Name: hmsuser set_user_id; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER set_user_id BEFORE INSERT ON public.hmsuser FOR EACH ROW EXECUTE FUNCTION public.set_new_id();


--
-- Name: appointment appointment_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT appointment_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(doctor_id);


--
-- Name: appointment appointment_record_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT appointment_record_id_fkey FOREIGN KEY (record_id) REFERENCES public.patientrecords(record_id);


--
-- Name: schedule schedule_doctor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule
    ADD CONSTRAINT schedule_doctor_id_fkey FOREIGN KEY (doctor_id) REFERENCES public.doctor(doctor_id);


--
-- Name: schedule schedule_slot_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule
    ADD CONSTRAINT schedule_slot_id_fkey FOREIGN KEY (slot_id) REFERENCES public.timeslots(slot_id);


--
-- PostgreSQL database dump complete
--

\unrestrict sRTDGShcsay8I5FaPMLhGhRys0dYAEYbW0buVwKbChOZ8DRAA1BSsBqlak09WYV

