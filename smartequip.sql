--
-- PostgreSQL database dump
--

\restrict 6vWz0rcUh2NNAo5VbCoL9Fl9KhAwteghQEOKRRTHHxYv2qg0jKSfaZv3BchHCfj

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

ALTER TABLE ONLY public.sysuser DROP CONSTRAINT sysuser_user_role_fkey;
ALTER TABLE ONLY public.sysuser DROP CONSTRAINT sysuser_currrental_fkey;
ALTER TABLE ONLY public.roleadjustments DROP CONSTRAINT roleadjustments_role_fkey;
ALTER TABLE ONLY public.roleadjustments DROP CONSTRAINT roleadjustments_pricing_id_fkey;
ALTER TABLE ONLY public.rental DROP CONSTRAINT rental_equipment_id_fkey;
ALTER TABLE ONLY public.equipment DROP CONSTRAINT equipment_category_id_fkey;
ALTER TABLE ONLY public.categoryadjustments DROP CONSTRAINT categoryadjustments_penalty_id_fkey;
ALTER TABLE ONLY public.categoryadjustments DROP CONSTRAINT categoryadjustments_category_id_fkey;
DROP TRIGGER rentreturn ON public.rental;
DROP TRIGGER rentout ON public.rental;
ALTER TABLE ONLY public.sysuser DROP CONSTRAINT sysuser_user_username_key;
ALTER TABLE ONLY public.sysuser DROP CONSTRAINT sysuser_pkey;
ALTER TABLE ONLY public.rolepricing DROP CONSTRAINT rolepricing_pkey;
ALTER TABLE ONLY public.roleadjustments DROP CONSTRAINT roleadjustments_pkey;
ALTER TABLE ONLY public.role DROP CONSTRAINT role_pkey;
ALTER TABLE ONLY public.rental DROP CONSTRAINT rental_pkey;
ALTER TABLE ONLY public.penaltypricing DROP CONSTRAINT penaltypricing_pkey;
ALTER TABLE ONLY public.equipment DROP CONSTRAINT equipment_pkey;
ALTER TABLE ONLY public.equipment DROP CONSTRAINT equipment_equipment_name_key;
ALTER TABLE ONLY public.categoryadjustments DROP CONSTRAINT categoryadjustments_pkey;
ALTER TABLE ONLY public.category DROP CONSTRAINT category_pkey;
ALTER TABLE ONLY public.category DROP CONSTRAINT category_category_name_key;
ALTER TABLE ONLY public.administrator DROP CONSTRAINT administrator_admin_username_key;
ALTER TABLE ONLY public.administrator DROP CONSTRAINT administrator_admin_password_key;
ALTER TABLE public.sysuser ALTER COLUMN user_id DROP DEFAULT;
ALTER TABLE public.rolepricing ALTER COLUMN pricing_id DROP DEFAULT;
ALTER TABLE public.role ALTER COLUMN role_id DROP DEFAULT;
ALTER TABLE public.rental ALTER COLUMN rental_id DROP DEFAULT;
ALTER TABLE public.penaltypricing ALTER COLUMN penalty_id DROP DEFAULT;
ALTER TABLE public.equipment ALTER COLUMN equipment_id DROP DEFAULT;
ALTER TABLE public.category ALTER COLUMN category_id DROP DEFAULT;
ALTER TABLE public.administrator ALTER COLUMN admin_id DROP DEFAULT;
DROP SEQUENCE public.sysuser_user_id_seq;
DROP VIEW public.rolepricinginfo;
DROP SEQUENCE public.rolepricing_pricing_id_seq;
DROP TABLE public.rolepricing;
DROP TABLE public.roleadjustments;
DROP SEQUENCE public.role_role_id_seq;
DROP SEQUENCE public.rental_rental_id_seq;
DROP SEQUENCE public.penaltypricing_penalty_id_seq;
DROP VIEW public.equipmentinfo;
DROP SEQUENCE public.equipment_equipment_id_seq;
DROP VIEW public.currentrentals;
DROP TABLE public.sysuser;
DROP TABLE public.role;
DROP TABLE public.rental;
DROP TABLE public.equipment;
DROP VIEW public.categorypenaltyinfo;
DROP TABLE public.penaltypricing;
DROP TABLE public.categoryadjustments;
DROP SEQUENCE public.category_category_id_seq;
DROP TABLE public.category;
DROP SEQUENCE public.administrator_admin_id_seq;
DROP TABLE public.administrator;
DROP FUNCTION public.update_availability();
DROP FUNCTION public.decrement_availability();
--
-- Name: decrement_availability(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.decrement_availability() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE equipment SET num_available = num_available-1 WHERE equipment_id = NEW.equipment_id;
	RETURN NEW;
END;
$$;


--
-- Name: update_availability(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_availability() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF NEW.time_end IS DISTINCT FROM OLD.time_end THEN
		UPDATE equipment SET num_available = num_available+1 WHERE equipment_id =	NEW.equipment_id;
		UPDATE sysUser SET currRental = NULL WHERE currRental = NEW.rental_id;
	END IF;
	RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: administrator; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.administrator (
    admin_id integer NOT NULL,
    admin_username character varying(32) NOT NULL,
    admin_password character varying(32) NOT NULL,
    CONSTRAINT alphanumpass CHECK (((admin_password)::text ~ '^[A-Za-z0-9]+$'::text)),
    CONSTRAINT alphanumuser CHECK (((admin_username)::text ~ '^[A-Za-z0-9]+$'::text))
);


--
-- Name: administrator_admin_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.administrator_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: administrator_admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.administrator_admin_id_seq OWNED BY public.administrator.admin_id;


--
-- Name: category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.category (
    category_id integer NOT NULL,
    category_name character varying(32) NOT NULL,
    max_rent_duration integer NOT NULL,
    CONSTRAINT alphanumname CHECK (((category_name)::text ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$'::text))
);


--
-- Name: category_category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.category_category_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.category_category_id_seq OWNED BY public.category.category_id;


--
-- Name: categoryadjustments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categoryadjustments (
    category_id integer NOT NULL,
    penalty_id integer NOT NULL
);


--
-- Name: penaltypricing; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penaltypricing (
    penalty_id integer NOT NULL,
    penalty_name character varying(32) NOT NULL,
    operator character varying(3) NOT NULL,
    price_adjustment numeric(8,2) NOT NULL,
    CONSTRAINT alphanumname CHECK (((penalty_name)::text ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$'::text)),
    CONSTRAINT operatorchk CHECK (((operator)::text = ANY ((ARRAY['ADD'::character varying, 'MUL'::character varying])::text[])))
);


--
-- Name: categorypenaltyinfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.categorypenaltyinfo AS
 SELECT c.category_name,
    p.penalty_name AS penalty,
    p.operator AS modifier,
    p.price_adjustment AS adjustment
   FROM ((public.categoryadjustments a
     JOIN public.category c ON ((a.category_id = c.category_id)))
     JOIN public.penaltypricing p ON ((a.penalty_id = p.penalty_id)));


--
-- Name: equipment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.equipment (
    equipment_id integer NOT NULL,
    category_id integer NOT NULL,
    equipment_name character varying(32) NOT NULL,
    daily_rental numeric(8,2) NOT NULL,
    num_available integer NOT NULL,
    CONSTRAINT alphanumname CHECK (((equipment_name)::text ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$'::text))
);


--
-- Name: rental; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rental (
    rental_id integer NOT NULL,
    equipment_id integer NOT NULL,
    time_start date NOT NULL,
    time_end date
);


--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
    role_id integer NOT NULL,
    role_name character varying(2) NOT NULL
);


--
-- Name: sysuser; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sysuser (
    user_id integer NOT NULL,
    user_role integer NOT NULL,
    user_username character varying(32) NOT NULL,
    user_password character varying(32) NOT NULL,
    user_name text NOT NULL,
    currrental integer,
    CONSTRAINT alphaname CHECK ((user_name ~ '^[A-Za-z]+( [A-Za-z]+)*$'::text)),
    CONSTRAINT alphanumpass CHECK (((user_password)::text ~ '^[A-Za-z0-9]+$'::text)),
    CONSTRAINT alphanumuser CHECK (((user_username)::text ~ '^[A-Za-z0-9]+$'::text))
);


--
-- Name: currentrentals; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.currentrentals AS
 SELECT r.rental_id,
    r.time_start AS day_rented,
    u.user_id,
    role.role_name AS role,
    u.user_name AS name,
    e.equipment_id,
    e.equipment_name AS equipment,
    c.category_name AS equipment_category,
    c.max_rent_duration AS max_days_allowed
   FROM ((((public.rental r
     JOIN public.sysuser u ON (((u.currrental = r.rental_id) AND (u.currrental IS NOT NULL) AND (r.time_end IS NULL))))
     JOIN public.equipment e ON ((e.equipment_id = r.equipment_id)))
     JOIN public.role ON ((u.user_role = role.role_id)))
     JOIN public.category c ON ((e.category_id = c.category_id)));


--
-- Name: equipment_equipment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.equipment_equipment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: equipment_equipment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.equipment_equipment_id_seq OWNED BY public.equipment.equipment_id;


--
-- Name: equipmentinfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.equipmentinfo AS
 SELECT c.category_name,
    c.max_rent_duration AS max_days_allowed,
    e.equipment_id,
    e.equipment_name AS equipment,
    e.daily_rental AS daily_rental_rate,
    e.num_available AS number_available
   FROM (public.category c
     JOIN public.equipment e ON ((e.category_id = c.category_id)));


--
-- Name: penaltypricing_penalty_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.penaltypricing_penalty_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: penaltypricing_penalty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.penaltypricing_penalty_id_seq OWNED BY public.penaltypricing.penalty_id;


--
-- Name: rental_rental_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rental_rental_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rental_rental_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rental_rental_id_seq OWNED BY public.rental.rental_id;


--
-- Name: role_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_role_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_role_id_seq OWNED BY public.role.role_id;


--
-- Name: roleadjustments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roleadjustments (
    role integer NOT NULL,
    pricing_id integer NOT NULL
);


--
-- Name: rolepricing; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rolepricing (
    pricing_id integer NOT NULL,
    pricing_name character varying(32) NOT NULL,
    pricing_type character varying(2) NOT NULL,
    operator character varying(3) NOT NULL,
    price_adjustment numeric(8,2) NOT NULL,
    CONSTRAINT alphanumname CHECK (((pricing_name)::text ~ '^[A-Za-z0-9]+( [A-Za-z0-9]+)*$'::text)),
    CONSTRAINT operatorchk CHECK (((operator)::text = ANY ((ARRAY['SUB'::character varying, 'MUL'::character varying])::text[]))),
    CONSTRAINT typechk CHECK (((pricing_type)::text = ANY ((ARRAY['DC'::character varying, 'PR'::character varying])::text[])))
);


--
-- Name: rolepricing_pricing_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rolepricing_pricing_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rolepricing_pricing_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rolepricing_pricing_id_seq OWNED BY public.rolepricing.pricing_id;


--
-- Name: rolepricinginfo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.rolepricinginfo AS
 SELECT r.role_name AS role,
    p.pricing_name AS name,
    p.pricing_type AS type,
    p.operator AS modifier,
    p.price_adjustment AS adjustment
   FROM ((public.role r
     LEFT JOIN public.roleadjustments a ON ((r.role_id = a.role)))
     LEFT JOIN public.rolepricing p ON ((a.pricing_id = p.pricing_id)));


--
-- Name: sysuser_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sysuser_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sysuser_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sysuser_user_id_seq OWNED BY public.sysuser.user_id;


--
-- Name: administrator admin_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.administrator ALTER COLUMN admin_id SET DEFAULT nextval('public.administrator_admin_id_seq'::regclass);


--
-- Name: category category_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category ALTER COLUMN category_id SET DEFAULT nextval('public.category_category_id_seq'::regclass);


--
-- Name: equipment equipment_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment ALTER COLUMN equipment_id SET DEFAULT nextval('public.equipment_equipment_id_seq'::regclass);


--
-- Name: penaltypricing penalty_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penaltypricing ALTER COLUMN penalty_id SET DEFAULT nextval('public.penaltypricing_penalty_id_seq'::regclass);


--
-- Name: rental rental_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rental ALTER COLUMN rental_id SET DEFAULT nextval('public.rental_rental_id_seq'::regclass);


--
-- Name: role role_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role ALTER COLUMN role_id SET DEFAULT nextval('public.role_role_id_seq'::regclass);


--
-- Name: rolepricing pricing_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rolepricing ALTER COLUMN pricing_id SET DEFAULT nextval('public.rolepricing_pricing_id_seq'::regclass);


--
-- Name: sysuser user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sysuser ALTER COLUMN user_id SET DEFAULT nextval('public.sysuser_user_id_seq'::regclass);


--
-- Data for Name: administrator; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.administrator (admin_id, admin_username, admin_password) VALUES (1, 'nizam1up', 'Zxcv123');
INSERT INTO public.administrator (admin_id, admin_username, admin_password) VALUES (2, 'adam2up', 'Asdf123');
INSERT INTO public.administrator (admin_id, admin_username, admin_password) VALUES (3, 'tufayl3up', 'Zxcv456');
INSERT INTO public.administrator (admin_id, admin_username, admin_password) VALUES (4, 'nadirah4up', 'Asdf456');


--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.category (category_id, category_name, max_rent_duration) VALUES (1, 'Media Equipment', 7);
INSERT INTO public.category (category_id, category_name, max_rent_duration) VALUES (2, 'Laboratory Equipment', 2);


--
-- Data for Name: categoryadjustments; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.categoryadjustments (category_id, penalty_id) VALUES (1, 1);
INSERT INTO public.categoryadjustments (category_id, penalty_id) VALUES (1, 2);
INSERT INTO public.categoryadjustments (category_id, penalty_id) VALUES (1, 3);
INSERT INTO public.categoryadjustments (category_id, penalty_id) VALUES (2, 1);
INSERT INTO public.categoryadjustments (category_id, penalty_id) VALUES (2, 3);


--
-- Data for Name: equipment; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.equipment (equipment_id, category_id, equipment_name, daily_rental, num_available) VALUES (1, 1, 'Canon XA60 4K Camcorder', 20.00, 5);
INSERT INTO public.equipment (equipment_id, category_id, equipment_name, daily_rental, num_available) VALUES (2, 2, 'Bunsen Burner', 5.00, 50);


--
-- Data for Name: penaltypricing; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.penaltypricing (penalty_id, penalty_name, operator, price_adjustment) VALUES (1, 'Late', 'ADD', 50.00);
INSERT INTO public.penaltypricing (penalty_id, penalty_name, operator, price_adjustment) VALUES (2, 'Damage', 'MUL', 10.00);
INSERT INTO public.penaltypricing (penalty_id, penalty_name, operator, price_adjustment) VALUES (3, 'Broken', 'MUL', 120.00);


--
-- Data for Name: rental; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.role (role_id, role_name) VALUES (1, 'SD');
INSERT INTO public.role (role_id, role_name) VALUES (2, 'LC');


--
-- Data for Name: roleadjustments; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.roleadjustments (role, pricing_id) VALUES (1, 1);
INSERT INTO public.roleadjustments (role, pricing_id) VALUES (2, 2);


--
-- Data for Name: rolepricing; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.rolepricing (pricing_id, pricing_name, pricing_type, operator, price_adjustment) VALUES (1, 'Student Discount', 'DC', 'MUL', 0.80);
INSERT INTO public.rolepricing (pricing_id, pricing_name, pricing_type, operator, price_adjustment) VALUES (2, 'Gratuity', 'PR', 'SUB', 5.00);


--
-- Data for Name: sysuser; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.sysuser (user_id, user_role, user_username, user_password, user_name, currrental) VALUES (1, 1, 'mortesa', 'necrofencer', 'Darcy Graves', NULL);


--
-- Name: administrator_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.administrator_admin_id_seq', 4, true);


--
-- Name: category_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.category_category_id_seq', 2, true);


--
-- Name: equipment_equipment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.equipment_equipment_id_seq', 2, true);


--
-- Name: penaltypricing_penalty_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.penaltypricing_penalty_id_seq', 3, true);


--
-- Name: rental_rental_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rental_rental_id_seq', 1, false);


--
-- Name: role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_role_id_seq', 2, true);


--
-- Name: rolepricing_pricing_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.rolepricing_pricing_id_seq', 2, true);


--
-- Name: sysuser_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sysuser_user_id_seq', 1, true);


--
-- Name: administrator administrator_admin_password_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.administrator
    ADD CONSTRAINT administrator_admin_password_key UNIQUE (admin_password);


--
-- Name: administrator administrator_admin_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.administrator
    ADD CONSTRAINT administrator_admin_username_key UNIQUE (admin_username);


--
-- Name: category category_category_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_category_name_key UNIQUE (category_name);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);


--
-- Name: categoryadjustments categoryadjustments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categoryadjustments
    ADD CONSTRAINT categoryadjustments_pkey PRIMARY KEY (category_id, penalty_id);


--
-- Name: equipment equipment_equipment_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT equipment_equipment_name_key UNIQUE (equipment_name);


--
-- Name: equipment equipment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT equipment_pkey PRIMARY KEY (equipment_id);


--
-- Name: penaltypricing penaltypricing_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penaltypricing
    ADD CONSTRAINT penaltypricing_pkey PRIMARY KEY (penalty_id);


--
-- Name: rental rental_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rental
    ADD CONSTRAINT rental_pkey PRIMARY KEY (rental_id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: roleadjustments roleadjustments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roleadjustments
    ADD CONSTRAINT roleadjustments_pkey PRIMARY KEY (role, pricing_id);


--
-- Name: rolepricing rolepricing_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rolepricing
    ADD CONSTRAINT rolepricing_pkey PRIMARY KEY (pricing_id);


--
-- Name: sysuser sysuser_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sysuser
    ADD CONSTRAINT sysuser_pkey PRIMARY KEY (user_id);


--
-- Name: sysuser sysuser_user_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sysuser
    ADD CONSTRAINT sysuser_user_username_key UNIQUE (user_username);


--
-- Name: rental rentout; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER rentout AFTER INSERT ON public.rental FOR EACH ROW EXECUTE FUNCTION public.decrement_availability();


--
-- Name: rental rentreturn; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER rentreturn AFTER UPDATE ON public.rental FOR EACH ROW EXECUTE FUNCTION public.update_availability();


--
-- Name: categoryadjustments categoryadjustments_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categoryadjustments
    ADD CONSTRAINT categoryadjustments_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(category_id);


--
-- Name: categoryadjustments categoryadjustments_penalty_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categoryadjustments
    ADD CONSTRAINT categoryadjustments_penalty_id_fkey FOREIGN KEY (penalty_id) REFERENCES public.penaltypricing(penalty_id);


--
-- Name: equipment equipment_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT equipment_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(category_id);


--
-- Name: rental rental_equipment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rental
    ADD CONSTRAINT rental_equipment_id_fkey FOREIGN KEY (equipment_id) REFERENCES public.equipment(equipment_id);


--
-- Name: roleadjustments roleadjustments_pricing_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roleadjustments
    ADD CONSTRAINT roleadjustments_pricing_id_fkey FOREIGN KEY (pricing_id) REFERENCES public.rolepricing(pricing_id);


--
-- Name: roleadjustments roleadjustments_role_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roleadjustments
    ADD CONSTRAINT roleadjustments_role_fkey FOREIGN KEY (role) REFERENCES public.role(role_id);


--
-- Name: sysuser sysuser_currrental_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sysuser
    ADD CONSTRAINT sysuser_currrental_fkey FOREIGN KEY (currrental) REFERENCES public.rental(rental_id);


--
-- Name: sysuser sysuser_user_role_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sysuser
    ADD CONSTRAINT sysuser_user_role_fkey FOREIGN KEY (user_role) REFERENCES public.role(role_id);


--
-- PostgreSQL database dump complete
--

\unrestrict 6vWz0rcUh2NNAo5VbCoL9Fl9KhAwteghQEOKRRTHHxYv2qg0jKSfaZv3BchHCfj

