--
-- PostgreSQL database dump
--

\restrict hpfmZVACEhsrcsUTibKN1Q1r9ae2eAIq6MbEEEymwryLTS2pkdVkvIck2hJcHvV

-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.audit_logs (
    id character varying(36) NOT NULL,
    user_id character varying(36),
    action character varying(100) NOT NULL,
    resource character varying(100) NOT NULL,
    changes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.audit_logs OWNER TO p2p_user;

--
-- Name: bank_accounts; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.bank_accounts (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    bank_name character varying(100) NOT NULL,
    account_number character varying(50) NOT NULL,
    account_holder character varying(255) NOT NULL,
    account_type character varying(20) NOT NULL,
    currency character varying(10) NOT NULL,
    is_primary boolean DEFAULT false,
    is_verified boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.bank_accounts OWNER TO p2p_user;

--
-- Name: complaints; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.complaints (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    type character varying(50) NOT NULL,
    description text NOT NULL,
    status character varying(20) DEFAULT 'pending'::character varying NOT NULL,
    admin_note text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.complaints OWNER TO p2p_user;

--
-- Name: currencies; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.currencies (
    id character varying(36) NOT NULL,
    code character varying(10) NOT NULL,
    name character varying(100) NOT NULL,
    symbol character varying(10) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.currencies OWNER TO p2p_user;

--
-- Name: disputes; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.disputes (
    id character varying(36) NOT NULL,
    transaction_id character varying(36) NOT NULL,
    initiator_id character varying(36) NOT NULL,
    reason character varying(255) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'open'::character varying,
    resolved_by character varying(36),
    resolution character varying(20),
    resolution_note text,
    resolved_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.disputes OWNER TO p2p_user;

--
-- Name: exchange_rates; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.exchange_rates (
    id character varying(36) NOT NULL,
    from_currency character varying(10) NOT NULL,
    to_currency character varying(10) NOT NULL,
    rate double precision NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.exchange_rates OWNER TO p2p_user;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.notifications (
    id character varying(36) NOT NULL,
    user_id character varying(36) NOT NULL,
    type character varying(50) NOT NULL,
    title character varying(255) NOT NULL,
    body text NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    resource_id character varying(36),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.notifications OWNER TO p2p_user;

--
-- Name: offers; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.offers (
    id character varying(36) NOT NULL,
    vendor_id character varying(36) NOT NULL,
    from_currency character varying(10) NOT NULL,
    to_currency character varying(10) NOT NULL,
    amount double precision NOT NULL,
    available_amount double precision NOT NULL,
    price_per_unit double precision NOT NULL,
    offer_type character varying(20) NOT NULL,
    status character varying(20) DEFAULT 'active'::character varying,
    min_transaction double precision DEFAULT 0,
    max_transaction double precision,
    payment_methods text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.offers OWNER TO p2p_user;

--
-- Name: ratings; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.ratings (
    id character varying(36) NOT NULL,
    transaction_id character varying(36) NOT NULL,
    rater_id character varying(36) NOT NULL,
    ratee_id character varying(36) NOT NULL,
    score integer NOT NULL,
    comment text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.ratings OWNER TO p2p_user;

--
-- Name: transactions; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.transactions (
    id character varying(36) NOT NULL,
    offer_id character varying(36) NOT NULL,
    buyer_id character varying(36) NOT NULL,
    vendor_id character varying(36) NOT NULL,
    amount_from double precision NOT NULL,
    amount_to double precision NOT NULL,
    exchange_rate double precision NOT NULL,
    status character varying(20) DEFAULT 'pending'::character varying,
    buyer_payment_account text,
    vendor_payment_account text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    vendor_voucher_url text
);


ALTER TABLE public.transactions OWNER TO p2p_user;

--
-- Name: users; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.users (
    id character varying(36) NOT NULL,
    email character varying(120) NOT NULL,
    password_hash character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    dni character varying(20),
    phone character varying(20),
    avatar_url character varying(500),
    role character varying(20) DEFAULT 'buyer'::character varying,
    kyc_verified boolean DEFAULT false,
    rating double precision DEFAULT 0.0,
    total_transactions integer DEFAULT 0,
    is_active boolean DEFAULT true,
    is_banned boolean DEFAULT false,
    ban_reason text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fcm_token text
);


ALTER TABLE public.users OWNER TO p2p_user;

--
-- Name: vouchers; Type: TABLE; Schema: public; Owner: p2p_user
--

CREATE TABLE public.vouchers (
    id character varying(36) NOT NULL,
    transaction_id character varying(36) NOT NULL,
    sender_id character varying(36) NOT NULL,
    image_url character varying(500) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'pending'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.vouchers OWNER TO p2p_user;

--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: bank_accounts bank_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.bank_accounts
    ADD CONSTRAINT bank_accounts_pkey PRIMARY KEY (id);


--
-- Name: complaints complaints_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.complaints
    ADD CONSTRAINT complaints_pkey PRIMARY KEY (id);


--
-- Name: currencies currencies_code_key; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.currencies
    ADD CONSTRAINT currencies_code_key UNIQUE (code);


--
-- Name: currencies currencies_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.currencies
    ADD CONSTRAINT currencies_pkey PRIMARY KEY (id);


--
-- Name: disputes disputes_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.disputes
    ADD CONSTRAINT disputes_pkey PRIMARY KEY (id);


--
-- Name: exchange_rates exchange_rates_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.exchange_rates
    ADD CONSTRAINT exchange_rates_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: offers offers_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.offers
    ADD CONSTRAINT offers_pkey PRIMARY KEY (id);


--
-- Name: ratings ratings_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_pkey PRIMARY KEY (id);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);


--
-- Name: users users_dni_key; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_dni_key UNIQUE (dni);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: vouchers vouchers_pkey; Type: CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.vouchers
    ADD CONSTRAINT vouchers_pkey PRIMARY KEY (id);


--
-- Name: idx_bank_accounts_user_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_bank_accounts_user_id ON public.bank_accounts USING btree (user_id);


--
-- Name: idx_complaints_status; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_complaints_status ON public.complaints USING btree (status);


--
-- Name: idx_complaints_user_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_complaints_user_id ON public.complaints USING btree (user_id);


--
-- Name: idx_disputes_status; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_disputes_status ON public.disputes USING btree (status);


--
-- Name: idx_disputes_transaction_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_disputes_transaction_id ON public.disputes USING btree (transaction_id);


--
-- Name: idx_offers_status; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_offers_status ON public.offers USING btree (status);


--
-- Name: idx_offers_vendor_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_offers_vendor_id ON public.offers USING btree (vendor_id);


--
-- Name: idx_ratings_rater_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_ratings_rater_id ON public.ratings USING btree (rater_id);


--
-- Name: idx_transactions_buyer_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_transactions_buyer_id ON public.transactions USING btree (buyer_id);


--
-- Name: idx_transactions_status; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_transactions_status ON public.transactions USING btree (status);


--
-- Name: idx_transactions_vendor_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_transactions_vendor_id ON public.transactions USING btree (vendor_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: ix_notifications_is_read; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX ix_notifications_is_read ON public.notifications USING btree (is_read);


--
-- Name: ix_notifications_user_id; Type: INDEX; Schema: public; Owner: p2p_user
--

CREATE INDEX ix_notifications_user_id ON public.notifications USING btree (user_id);


--
-- Name: audit_logs audit_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: bank_accounts bank_accounts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.bank_accounts
    ADD CONSTRAINT bank_accounts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: complaints complaints_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.complaints
    ADD CONSTRAINT complaints_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: disputes disputes_initiator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.disputes
    ADD CONSTRAINT disputes_initiator_id_fkey FOREIGN KEY (initiator_id) REFERENCES public.users(id);


--
-- Name: disputes disputes_resolved_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.disputes
    ADD CONSTRAINT disputes_resolved_by_fkey FOREIGN KEY (resolved_by) REFERENCES public.users(id);


--
-- Name: disputes disputes_transaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.disputes
    ADD CONSTRAINT disputes_transaction_id_fkey FOREIGN KEY (transaction_id) REFERENCES public.transactions(id);


--
-- Name: notifications notifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: offers offers_vendor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.offers
    ADD CONSTRAINT offers_vendor_id_fkey FOREIGN KEY (vendor_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: ratings ratings_ratee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_ratee_id_fkey FOREIGN KEY (ratee_id) REFERENCES public.users(id);


--
-- Name: ratings ratings_rater_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_rater_id_fkey FOREIGN KEY (rater_id) REFERENCES public.users(id);


--
-- Name: ratings ratings_transaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.ratings
    ADD CONSTRAINT ratings_transaction_id_fkey FOREIGN KEY (transaction_id) REFERENCES public.transactions(id);


--
-- Name: transactions transactions_buyer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_buyer_id_fkey FOREIGN KEY (buyer_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: transactions transactions_offer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_offer_id_fkey FOREIGN KEY (offer_id) REFERENCES public.offers(id);


--
-- Name: transactions transactions_vendor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_vendor_id_fkey FOREIGN KEY (vendor_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: vouchers vouchers_sender_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.vouchers
    ADD CONSTRAINT vouchers_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: vouchers vouchers_transaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: p2p_user
--

ALTER TABLE ONLY public.vouchers
    ADD CONSTRAINT vouchers_transaction_id_fkey FOREIGN KEY (transaction_id) REFERENCES public.transactions(id);


--
-- PostgreSQL database dump complete
--

\unrestrict hpfmZVACEhsrcsUTibKN1Q1r9ae2eAIq6MbEEEymwryLTS2pkdVkvIck2hJcHvV

