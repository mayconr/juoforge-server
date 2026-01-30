CREATE TABLE serial_counters (
 entity_type VARCHAR(32) PRIMARY KEY,
 next_serial  INT NOT NULL,
 updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO serial_counters (entity_type, next_serial) VALUES
   ('MOBILE', 1),
   ('ITEM',   0x40000000);

CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  username   VARCHAR(32) NOT NULL UNIQUE,
  password   VARCHAR(255) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE mobiles (
    id UUID PRIMARY KEY,

    serial_id INT NOT NULL,

    account_id UUID,

    name VARCHAR(32) NOT NULL,
    display_name VARCHAR(64),

    model_id INT NOT NULL,
    hue INT NOT NULL,

    race SMALLINT NOT NULL,
    gender SMALLINT NOT NULL,
    notoriety SMALLINT,
    status SMALLINT,

    mount_item_name VARCHAR(64),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_mobiles_account
     FOREIGN KEY (account_id)
         REFERENCES accounts(id)
         ON DELETE CASCADE,

    CONSTRAINT uk_character_serial
     UNIQUE (serial_id)
);
CREATE UNIQUE INDEX idx_mobiles_serial
    ON mobiles(serial_id);

CREATE INDEX idx_mobiles_account
    ON mobiles(account_id);

CREATE UNIQUE INDEX idx_players_unique_name
    ON mobiles (LOWER(name))
    WHERE account_id IS NOT NULL;

ALTER TABLE mobiles
    ADD CONSTRAINT chk_mobile_serial_range
        CHECK (serial_id BETWEEN 1 AND 1073741823);

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
      id UUID PRIMARY KEY,
      mobile_id UUID NOT NULL,
      skill_id INT NOT NULL,
      skill_base DOUBLE PRECISION NOT NULL DEFAULT 0.0,
      skill_cap DOUBLE PRECISION NOT NULL DEFAULT 100.0,
      skill_lock INT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      CONSTRAINT uq_mobile_skill UNIQUE (mobile_id, skill_id)
);


CREATE VIEW v_account_mobiles_login as
SELECT id AS mobile_id,
       serial_id,
       account_id,
       name AS mobile_name
FROM mobiles m;

CREATE VIEW v_mobile_full AS
SELECT
    m.id AS mobile_id,
    m.serial_id,
    m.account_id,
    m.name,
    m.display_name,
    m.model_id,
    m.hue,
    m.race,
    m.gender,
    m.notoriety,
    m.status,
    m.mount_item_name,
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
    v.max_mana

FROM mobiles m
 JOIN mobile_runtime r
      ON r.mobile_id = m.id
 JOIN mobile_attributes a
      ON a.mobile_id = m.id
 JOIN mobile_vitals v
      ON v.mobile_id = m.id;

CREATE TABLE items (
   id UUID PRIMARY KEY,
   serial_id INT NOT NULL,

   name VARCHAR(64) NOT NULL,
   display_name VARCHAR(64) NOT NULL,

   type INT NOT NULL,
   model_id INT NOT NULL,
   hue INT NOT NULL,
   layer SMALLINT,

   unit_weight INT NOT NULL,
   amount INT NOT NULL DEFAULT 1,

   created_at TIMESTAMP NOT NULL DEFAULT NOW(),

   CONSTRAINT uk_item_serial UNIQUE (serial_id)
);

CREATE INDEX idx_items_name ON items (name);

CREATE TABLE item_state (
    item_id UUID PRIMARY KEY,

    owner_mobile_id UUID,
    parent_item_id UUID,

    x INT,
    y INT,
    z INT,

    attr JSONB NOT NULL DEFAULT '{}',

    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_item_state_item
        FOREIGN KEY (item_id)
            REFERENCES items(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_item_state_owner
        FOREIGN KEY (owner_mobile_id)
            REFERENCES mobiles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_item_state_parent
        FOREIGN KEY (parent_item_id)
            REFERENCES items(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_item_state_equipped_mobile
    ON item_state (owner_mobile_id);

CREATE VIEW v_item_full AS
SELECT i.id AS item_id,
       i.serial_id,
       i.name,
       i.display_name,
       i.type,
       i.model_id,
       i.hue,
       i.layer,
       i.unit_weight,
       i.amount,
       s.owner_mobile_id,
       s.parent_item_id,
       s.x,
       s.y,
       s.z,
       s.attr,
       s.updated_at
FROM item_state s
         JOIN items i ON i.id = s.item_id;
