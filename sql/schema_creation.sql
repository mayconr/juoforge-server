CREATE TABLE serial_counters (
 entity_type VARCHAR(32) PRIMARY KEY,
 next_serial  INT NOT NULL,
 updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO serial_counters (entity_type, next_serial) VALUES
   ('MOBILE', 0x00000001),
   ('ITEM',   0x40000000);
--Tipo	Base
-- MOBILE	0x00000000
-- ITEM	0x20000000
--VIRTUAL	0x40000000
--Reservado	0x60000000
--
CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  username   VARCHAR(32) NOT NULL UNIQUE,
  password   VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE mobiles (
 id UUID PRIMARY KEY,

 serial_id INT NOT NULL,

 type CHAR(1) NOT NULL, -- 'P' = Player | 'N' = NPC

 name VARCHAR(32) NOT NULL,
 display_name VARCHAR(64),

 model_id INT NOT NULL,
 hue INT NOT NULL,

 race SMALLINT NOT NULL,
 gender SMALLINT NOT NULL,
 notoriety SMALLINT,
 status SMALLINT,

 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

 CONSTRAINT ck_mobile_type
     CHECK (type IN ('P', 'N')),

 CONSTRAINT uk_character_serial
     UNIQUE (serial_id)
);
CREATE UNIQUE INDEX idx_mobiles_serial
    ON mobiles(serial_id);

ALTER TABLE mobiles
    ADD CONSTRAINT chk_mobile_serial_range
        CHECK (serial_id BETWEEN 1 AND 1073741823);

CREATE TABLE mobile_player (
   mobile_id UUID PRIMARY KEY
       REFERENCES mobiles(id)
           ON DELETE CASCADE,

   account_id UUID NOT NULL
       REFERENCES accounts(id)
           ON DELETE CASCADE
);

CREATE TABLE mobile_npc (
    mobile_id UUID PRIMARY KEY
        REFERENCES mobiles(id)
            ON DELETE CASCADE,

    ai VARCHAR(64) NOT NULL,
    behavior_profile VARCHAR(64) NOT NULL,

    roles JSONB NOT NULL DEFAULT '[]'::jsonb
);

CREATE TABLE mobile_attributes (
   mobile_id UUID PRIMARY KEY,

   strength      INT NOT NULL,
   dexterity     INT NOT NULL,
   intelligence  INT NOT NULL,

   stat_cap      SMALLINT NOT NULL,

   followers     INT NOT NULL,
   max_followers INT NOT NULL,

   luck           INT NOT NULL DEFAULT 0,
   tithing_points INT NOT NULL DEFAULT 0,

   CONSTRAINT fk_mobile_attributes_mobile
       FOREIGN KEY (mobile_id)
           REFERENCES mobiles(id)
           ON DELETE CASCADE,

   CONSTRAINT chk_attributes_non_negative
       CHECK (
           strength >= 0 AND
           dexterity >= 0 AND
           intelligence >= 0 AND
           stat_cap >= 0 AND
           followers >= 0 AND
           max_followers >= 0 AND
           luck >= 0 AND
           tithing_points >= 0
           )
);

CREATE TABLE mobile_vitals (
   mobile_id UUID PRIMARY KEY,

   max_hitpoints INT NOT NULL,
   max_stamina   INT NOT NULL,
   max_mana      INT NOT NULL,

   CONSTRAINT fk_mobile_vitals_mobile
       FOREIGN KEY (mobile_id)
           REFERENCES mobiles(id)
           ON DELETE CASCADE,

   CONSTRAINT chk_vitals_non_negative
       CHECK (
           max_hitpoints >= 0 AND
           max_stamina >= 0 AND
           max_mana >= 0
           )
);

CREATE TABLE mobile_runtime (
    mobile_id UUID PRIMARY KEY,

    x         INT NOT NULL,
    y         INT NOT NULL,
    z         INT NOT NULL,

    direction SMALLINT NOT NULL,
    running   BOOLEAN NOT NULL,

    hitpoints INT NOT NULL,
    stamina   INT NOT NULL,
    mana      INT NOT NULL,

    attr JSONB NOT NULL DEFAULT '{}',

    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_mobile_runtime_mobile
        FOREIGN KEY (mobile_id)
            REFERENCES mobiles(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_mobile_runtime_valid
        CHECK (
            x >= 0 AND
            y >= 0 AND
            hitpoints >= 0 AND
            stamina >= 0 AND
            mana >= 0
            )
);

CREATE TABLE mobile_skills (
   mobile_id UUID NOT NULL,
   skill_id SMALLINT NOT NULL,

   skill_base DOUBLE PRECISION NOT NULL DEFAULT 0.0,
   skill_cap  DOUBLE PRECISION NOT NULL DEFAULT 100.0,
   skill_lock SMALLINT NOT NULL DEFAULT 0,

   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

   PRIMARY KEY (mobile_id, skill_id),

   CONSTRAINT fk_mobile_skills_mobile
       FOREIGN KEY (mobile_id)
           REFERENCES mobiles(id)
           ON DELETE CASCADE
);

CREATE OR REPLACE VIEW v_account_mobiles_login AS
SELECT
    m.id AS mobile_id,
    m.serial_id,
    mp.account_id,
    m.name AS mobile_name
FROM mobiles m
JOIN mobile_player mp ON mp.mobile_id = m.id;

CREATE OR REPLACE VIEW v_mobile_full AS
SELECT
    m.id AS mobile_id,
    m.serial_id,
    mp.account_id,
    m.type,
    m.name,
    m.display_name,
    m.model_id,
    m.hue,
    m.race,
    m.gender,
    m.notoriety,
    m.status,
    m.created_at,

    r.x,
    r.y,
    r.z,
    r.direction,
    r.running,
    r.hitpoints,
    r.stamina,
    r.mana,
    r.attr,
    r.updated_at,

    a.strength,
    a.dexterity,
    a.intelligence,
    a.stat_cap,
    a.followers,
    a.max_followers,
    a.luck,
    a.tithing_points,

    v.max_hitpoints,
    v.max_stamina,
    v.max_mana,

    -- NPC specific (NULL para players)
    n.ai,
    n.behavior_profile,
    n.roles

FROM mobiles m
JOIN mobile_runtime r      ON r.mobile_id = m.id
JOIN mobile_attributes a   ON a.mobile_id = m.id
JOIN mobile_vitals v       ON v.mobile_id = m.id
LEFT JOIN mobile_player mp ON mp.mobile_id = m.id
LEFT JOIN mobile_npc n     ON n.mobile_id = m.id;

CREATE TABLE items (
   id UUID PRIMARY KEY,
   serial_id INT NOT NULL,

-- ownership forte (ao deletar o mobile, deleta os itens dele)
   owner_serial_id int,

-- hierarquia forte (ao deletar o container, deleta o conteúdo)
   container_serial_id int,

   name VARCHAR(64) NOT NULL,
   display_name VARCHAR(64) NOT NULL,

   model_id INT NOT NULL,
   hue INT NOT NULL,
   layer SMALLINT,

   unit_weight INT NOT NULL,
   amount INT NOT NULL DEFAULT 1,
   container_gump_id int,
   corpse_id INT,

   flags JSONB NOT NULL DEFAULT '{}',

   created_at TIMESTAMP NOT NULL DEFAULT NOW(),

   CONSTRAINT uk_item_serial UNIQUE (serial_id),

   CONSTRAINT fk_items_owner
       FOREIGN KEY (owner_serial_id)
           REFERENCES mobiles(serial_id)
           ON DELETE CASCADE,

   CONSTRAINT fk_items_parent
       FOREIGN KEY (container_serial_id)
           REFERENCES items(serial_id)
           ON DELETE CASCADE
);

CREATE INDEX idx_items_name ON items (name);
CREATE INDEX idx_items_owner_serial_id ON items(owner_serial_id);
CREATE INDEX idx_items_container_serial_id  ON items(container_serial_id);

CREATE TABLE item_state (
    item_id UUID PRIMARY KEY,

    x INT,
    y INT,
    z INT,

    attr JSONB NOT NULL DEFAULT '{}',

    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_item_state_item
        FOREIGN KEY (item_id)
            REFERENCES items(id)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED
);

CREATE OR REPLACE VIEW v_item_full AS
SELECT
    i.id AS item_id,
    i.serial_id,
    i.name,
    i.display_name,
    i.model_id,
    i.hue,
    i.layer,
    i.unit_weight,
    i.amount,
    i.flags,
    i.corpse_id,
    i.container_gump_id,

    i.owner_serial_id,
    i.container_serial_id,

    COALESCE(s.x, 0) AS x,
    COALESCE(s.y, 0) AS y,
    COALESCE(s.z, 0) AS z,

    s.attr,
    s.updated_at,

    CASE
        WHEN i.flags ? 'CONTAINER' THEN 'C'
        ELSE 'O'
        END AS type
FROM items i
JOIN item_state s ON s.item_id = i.id;
