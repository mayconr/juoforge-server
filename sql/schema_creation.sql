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
    -- identidade
     serial_id INT PRIMARY KEY,

    -- tipo
     type CHAR(1) NOT NULL, -- 'P' | 'N'

    -- base
     name VARCHAR(32) NOT NULL,
     display_name VARCHAR(64),

     model_id INT NOT NULL,
     hue INT NOT NULL,

     race SMALLINT NOT NULL,
     gender SMALLINT NOT NULL,
     notoriety SMALLINT,
     status SMALLINT,

     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- player only
     account_id UUID,

    -- npc only
     roles JSONB DEFAULT '[]'::jsonb,
     behavior jsonb,

    -- attributes
     strength INT NOT NULL,
     dexterity INT NOT NULL,
     intelligence INT NOT NULL,

     stat_cap SMALLINT NOT NULL,

     followers INT NOT NULL,
     max_followers INT NOT NULL,

     luck INT NOT NULL DEFAULT 0,
     tithing_points INT NOT NULL DEFAULT 0,

    -- vitals (base)
     max_hitpoints INT NOT NULL,
     max_stamina INT NOT NULL,
     max_mana INT NOT NULL,

    -- runtime
     x INT NOT NULL,
     y INT NOT NULL,
     z INT NOT NULL,

     direction SMALLINT NOT NULL,
     running BOOLEAN NOT NULL,

     hitpoints INT NOT NULL,
     stamina INT NOT NULL,
     mana INT NOT NULL,

     alive BOOLEAN not null,

     runtime_attr JSONB NOT NULL DEFAULT '{}',

     updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- constraints
     CONSTRAINT fk_mobile_account
         FOREIGN KEY (account_id)
             REFERENCES accounts(id)
             ON DELETE CASCADE,

     CONSTRAINT ck_mobile_type
         CHECK (type IN ('P', 'N')),

     CONSTRAINT chk_mobile_serial_range
         CHECK (serial_id BETWEEN 1 AND 1073741823),

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
             ),

     CONSTRAINT chk_vitals_non_negative
         CHECK (
             max_hitpoints >= 0 AND
             max_stamina >= 0 AND
             max_mana >= 0
             ),

     CONSTRAINT chk_mobile_runtime_valid
         CHECK (
             x >= 0 AND
             y >= 0 AND
             hitpoints >= 0 AND
             stamina >= 0 AND
             mana >= 0
             ),

    -- coerência tipo → campos
     CONSTRAINT chk_player_fields
         CHECK (
             (type = 'P' AND account_id IS NOT NULL)
                 OR
             (type = 'N' AND account_id IS NULL)
             ),

     CONSTRAINT chk_npc_fields
         CHECK (
             (type = 'N' AND behavior IS NOT NULL)
                 OR
             (type = 'P')
             )
);

CREATE TABLE mobile_skills (
       serial_id INT NOT NULL,
       skill_id SMALLINT NOT NULL,

       skill_base DOUBLE PRECISION NOT NULL DEFAULT 0.0,
       skill_cap  DOUBLE PRECISION NOT NULL DEFAULT 100.0,
       skill_lock SMALLINT NOT NULL DEFAULT 0,

       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

       PRIMARY KEY (serial_id, skill_id),

       CONSTRAINT fk_mobile_skills_mobile
           FOREIGN KEY (serial_id)
               REFERENCES mobiles(serial_id)
               ON DELETE CASCADE,

       CONSTRAINT chk_skill_values
           CHECK (
               skill_base >= 0 AND
               skill_cap >= 0 AND
               skill_base <= skill_cap
               ),

       CONSTRAINT chk_skill_lock
           CHECK (skill_lock IN (0, 1, 2)) -- 0=up, 1=down, 2=locked (padrão UO)
);

CREATE TABLE items (
    -- identidade
   serial_id INT PRIMARY KEY,

-- relação
   location_type INT NOT NULL,
   owner_serial_id INT,
   container_serial_id INT,

-- definição
   name VARCHAR(64) NOT NULL,
   display_name VARCHAR(64) NOT NULL,

   model_id INT NOT NULL,
   hue INT NOT NULL,
   layer SMALLINT,
   movable BOOLEAN,

   unit_weight INT NOT NULL,
   amount INT NOT NULL DEFAULT 1,

   container_gump_id INT,
   corpse_id INT,

   flags JSONB NOT NULL DEFAULT '{}',

-- estado (ex item_state)
   x INT,
   y INT,
   z INT,

   attr JSONB NOT NULL DEFAULT '{}',

   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
   created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

-- constraints
   CONSTRAINT fk_items_owner
       FOREIGN KEY (owner_serial_id)
           REFERENCES mobiles(serial_id)
           ON DELETE CASCADE,

   CONSTRAINT fk_items_container
       FOREIGN KEY (container_serial_id)
           REFERENCES items(serial_id)
           ON DELETE CASCADE,

   CONSTRAINT chk_item_amount
       CHECK (amount > 0),

   CONSTRAINT chk_item_weight
       CHECK (unit_weight >= 0),

   CONSTRAINT chk_item_position
       CHECK (
           (x IS NULL AND y IS NULL AND z IS NULL)
               OR
           (x >= 0 AND y >= 0)
           ),

   CONSTRAINT chk_item_location_rules
       CHECK (
       -- location_type = 1 → ambos NULL
       (location_type = 1 AND owner_serial_id IS NULL AND container_serial_id IS NULL)

       OR

       -- location_type = 2 → container != NULL e owner NULL
       (location_type = 2 AND container_serial_id IS NOT NULL AND owner_serial_id IS NULL)

       OR

       -- location_type = 3 → owner != NULL e container NULL
       (location_type = 3 AND owner_serial_id IS NOT NULL AND container_serial_id IS NULL)

       OR

       -- location_type = 4 → ambos NULL
       (location_type = 4 AND owner_serial_id IS NULL AND container_serial_id IS NULL)
       )
);
