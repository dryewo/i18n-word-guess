
DROP VIEW game_steps;
DROP TABLE games;
DROP TABLE steps;

CREATE TABLE games
(
  id serial NOT NULL,
  word character varying NOT NULL,
  creation_date timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT games_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

CREATE TABLE steps
(
  id serial NOT NULL,
  game_id integer,
  session character varying,
  guess character varying,
  mask character varying,
  status character varying,
  creation_date timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT steps_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

CREATE OR REPLACE VIEW game_steps AS 
 SELECT g.word, s.*
   FROM games g,
    steps s
  WHERE g.id = s.game_id;
