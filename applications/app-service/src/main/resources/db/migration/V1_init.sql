CREATE TABLE public.application (
	application_id uuid DEFAULT gen_random_uuid() NOT NULL,
	loan_amount numeric(15, 2) NOT NULL,
	term_months int4 NOT NULL,
	email_user varchar(150) NOT NULL,
	loan_type_id int4 NOT NULL,
	application_status_id int4 NOT NULL,
	CONSTRAINT application_pkey PRIMARY KEY (application_id)
);

ALTER TABLE public.application ADD CONSTRAINT fk_application_loan_type FOREIGN KEY (loan_type_id) REFERENCES public.loan_type(loan_type_id) ON DELETE CASCADE;
ALTER TABLE public.application ADD CONSTRAINT fk_application_status FOREIGN KEY (application_status_id) REFERENCES public.status(application_status_id) ON DELETE CASCADE;

CREATE TABLE public.loan_type (
	loan_type_id serial4 NOT NULL,
	"name" varchar(100) NOT NULL,
	min_amount numeric(15, 2) NOT NULL,
	max_amount numeric(15, 2) NOT NULL,
	interest_rate numeric(5, 2) NOT NULL,
	automatic_validation bool DEFAULT false NULL,
	CONSTRAINT loan_type_pkey PRIMARY KEY (loan_type_id)
);

CREATE TABLE public.status (
	application_status_id int4 DEFAULT nextval('status_status_id_seq'::regclass) NOT NULL,
	"name" varchar(100) NOT NULL,
	description text NULL,
	CONSTRAINT status_pkey PRIMARY KEY (application_status_id)
);

INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('bb91329a-24ce-49ae-9ae4-56db66a801f5'::uuid, 1500000.50, 24, 'carlos.ramirez@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('424aee4a-df82-494e-8ac5-b85de33fe9e9'::uuid, 1500000.50, 24, 'cars.lop@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('3e06cfa4-8cc6-42f7-ba5a-33ee56eb74a7'::uuid, 1500000.50, 24, 'cars.lop@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('4a901c47-3d4e-43dd-96df-769f19363fe0'::uuid, 150000.00, 24, 'cars.lop@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('d255966d-ef08-46fb-8e5b-be2dda9eaed9'::uuid, 150000.00, 24, 'cars.lop@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('a97dc9c7-ac95-42ef-8f41-22828c7b8f41'::uuid, 150000.00, 24, 'cars.lop@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('50ccb636-e105-424d-a66b-1f8874248f48'::uuid, 150000.00, 24, 'cars.lop@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('fd160b59-7479-4c9c-990a-9d3589bfc59c'::uuid, 1500000.00, 24, 'lopez237@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('780fcd11-a169-402d-b07b-b0d9d7448bb9'::uuid, 1500000.00, 24, 'lopez237@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('96674a59-9d57-4ba7-9500-16747349fe09'::uuid, 1500000.00, 24, 'lopez237@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('bc37fa5b-a2e0-4c26-a32d-10515d0a6463'::uuid, 1500000.00, 24, 'lopez237@example.org', 1, 1);
INSERT INTO public.application
(application_id, loan_amount, term_months, email_user, loan_type_id, application_status_id)
VALUES('c0bfc109-4f65-4618-8480-077a3aabf82d'::uuid, 1500000.00, 24, 'lopez237@example.org', 1, 1);

INSERT INTO public.loan_type
(loan_type_id, "name", min_amount, max_amount, interest_rate, automatic_validation)
VALUES(1, 'EXPRESS', 1000000.00, 5000000.00, 12.00, false);
INSERT INTO public.loan_type
(loan_type_id, "name", min_amount, max_amount, interest_rate, automatic_validation)
VALUES(2, 'ROTATIVO', 1000000.00, 15000000.00, 18.00, false);
INSERT INTO public.loan_type
(loan_type_id, "name", min_amount, max_amount, interest_rate, automatic_validation)
VALUES(3, 'VIVIENDA', 30000000.00, 200000000.00, 9.00, false);

INSERT INTO public.status
(application_status_id, "name", description)
VALUES(2, 'Rejected', 'Solicitud de prestamo rechazada');
INSERT INTO public.status
(application_status_id, "name", description)
VALUES(3, 'Manual review', 'Solicitud de prestamos en revision manual');
INSERT INTO public.status
(application_status_id, "name", description)
VALUES(4, 'Approved', 'Solicitud de prestamo aprobada');
INSERT INTO public.status
(application_status_id, "name", description)
VALUES(1, 'Pending review', 'Estado inicial de la solicitud de prestamo');