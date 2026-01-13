
INSERT INTO accounts (
    id,
    username,
    password
) VALUES (
     '550e8400-e29b-41d4-a716-446655440010',
     'admin',
     'admin'
 );

INSERT INTO mobiles (
    id,
    serial_id,
    account_id,
    name,
    model_id,
    hue,
    race,
    gender,
    notoriety,
    status
) VALUES (
     '79b42e27-5b54-4b5f-9890-eaf1ffda93bb',
     100001,
     '550e8400-e29b-41d4-a716-446655440010',
     'TestKnight',
     400,
     0,
     0, -- HUMAN
     0, -- MALE
     0, -- INNOCENT
     0  -- NORMAL
 );

INSERT INTO mobile_attributes (
    mobile_id,
    strength,
    dexterity,
    intelligence,
    stat_cap,
    followers,
    max_followers,
    luck,
    tithing_points
) VALUES (
     '79b42e27-5b54-4b5f-9890-eaf1ffda93bb',
     50,
     50,
     50,
     225,
     0,
     5,
     0,
     0
);

INSERT INTO mobile_vitals (
    mobile_id,
    max_hitpoints,
    max_stamina,
    max_mana
) VALUES (
 '79b42e27-5b54-4b5f-9890-eaf1ffda93bb',
 100,
 100,
 50
);

INSERT INTO mobile_runtime (
    mobile_id,
    x,
    y,
    z,
    direction,
    running,
    hitpoints,
    stamina,
    mana
) VALUES (
     '79b42e27-5b54-4b5f-9890-eaf1ffda93bb', -- mesmo UUID existente em mobiles.id
     1325,   -- x
     1624,   -- y
     0,      -- z
     2,      -- direction (ex: EAST)
     false,  -- running
     50,     -- hitpoints atual
     50,     -- stamina atual
     30      -- mana atual
 );


-- backpack
INSERT INTO items (
    id,
    serial_id,
    model_id,
    hue,
    layer,
    unit_weight,
    amount,
    properties
) VALUES (
     gen_random_uuid(),
     2000001,                 -- exemplo de serial global do shard
     0x0E75,                  -- backpack
     0,                       -- hue padrão
     NULL,                    -- layer (não equipada)
     3,                       -- peso unitário típico de backpack
     1,
     jsonb_build_object(
             'gumpId', 0x003C
     )
 );

INSERT INTO item_state (
    item_id,
    owner_mobile_id,
    parent_item_id,
    x,
    y,
    z,
    map,
    equipped
)
SELECT
    id,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    FALSE
FROM items
WHERE serial_id = 2000001;