package com.github.mayconr.juoserver.game.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate.BaseDamage;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate.Material;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate.Weapon;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.ItemMaterial;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.WeaponStyle;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemMapper;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Conversion of {@link ItemTemplate} into {@link UOItemData} and {@link UOItem}, and back to data. */
class ItemTemplateToItemConversionTest {
    private static final int SERIAL = 0x40000123;
    private static final Material TEMPLATE_MATERIAL = new Material(11, 22, 33, 44);
    private static final ItemMaterial ITEM_MATERIAL = new ItemMaterial(11, 22, 33, 44);

    @Nested
    class TemplateToData {

        @Test
        void copiesEveryTemplateFieldToItsDataField() {
            var template = fullTemplate();

            var data = template.toData(SERIAL);

            assertAll(
                    () -> assertEquals("iron_ore", data.getTemplate()),
                    () -> assertEquals(SERIAL, data.getSerialId()),
                    () -> assertEquals(6585, data.getModelId()),
                    () -> assertEquals("iron_ore", data.getName()),
                    () -> assertEquals("iron ore", data.getDisplayName()),
                    () -> assertEquals(Layer.ONE_HANDED, data.getLayer()),
                    () -> assertEquals(0x96D, data.getHue()),
                    () -> assertFalse(data.isMovable()),
                    () -> assertEquals(7, data.getUnitWeight()),
                    () -> assertEquals(List.of(ItemFlag.WEAPON), data.getFlags()),
                    () -> assertEquals(0x3C, data.getContainerGumpId()),
                    () -> assertEquals(ITEM_MATERIAL, data.getMaterial()),
                    () -> assertEquals("iron_ingot", data.getPersistentAttrMap().get("mining.smelting.ingot")),
                    () -> assertEquals(5, data.getPersistentAttrMap().get("mining.minSkill"))
            );
        }

        @Test
        void initializesInstanceStateIndependentlyOfTheTemplate() {
            var data = fullTemplate().toData(SERIAL);

            assertAll(
                    () -> assertNotNull(data.getId()),
                    () -> assertEquals(0, data.getX()),
                    () -> assertEquals(0, data.getY()),
                    () -> assertEquals(0, data.getZ()),
                    () -> assertEquals(1, data.getAmount()),
                    () -> assertFalse(data.isHidden()),
                    () -> assertEquals(Direction.NORTH, data.getDirection()),
                    () -> assertEquals(ItemLocationType.ORPHAN, data.getLocationType()),
                    () -> assertNull(data.getOwnerSerialId()),
                    () -> assertNull(data.getContainerSerialId()),
                    () -> assertEquals(0, data.getCorpseId()),
                    () -> assertTrue(data.getEquippedItems().isEmpty())
            );
        }

        @Test
        void eachCallCreatesADistinctIdentity() {
            var template = fullTemplate();

            var ids = IntStream.range(0, 100)
                    .mapToObj(index -> template.toData(SERIAL + index).getId())
                    .collect(HashSet<UUID>::new, HashSet::add, HashSet::addAll);

            assertEquals(100, ids.size());
        }

        @Test
        void eachCallCreatesIndependentData() {
            var template = fullTemplate();

            var first = template.toData(SERIAL);
            var second = template.toData(SERIAL + 1);

            assertNotSame(first, second);
            assertNotSame(first.getPersistentAttrMap(), second.getPersistentAttrMap());
            assertNotSame(first.getEquippedItems(), second.getEquippedItems());
        }

        @Test
        void attributesAreCopiedSoItemChangesDoNotLeakIntoTheTemplateOrOtherItems() {
            var template = fullTemplate();
            var first = template.toData(SERIAL);
            var second = template.toData(SERIAL + 1);

            first.getPersistentAttrMap().set("mining.minSkill", 99);
            first.getPersistentAttrMap().set("crafted.by", "player");
            first.getPersistentAttrMap().remove("mining.smelting.ingot");

            assertEquals(5, template.attr().get("mining.minSkill"));
            assertEquals("iron_ingot", template.attr().get("mining.smelting.ingot"));
            assertFalse(template.attr().containsKey("crafted.by"));
            assertEquals(5, second.getPersistentAttrMap().get("mining.minSkill"));
            assertEquals("iron_ingot", second.getPersistentAttrMap().get("mining.smelting.ingot"));
            assertFalse(second.getPersistentAttrMap().contains("crafted.by"));
        }

        @Test
        void attributesStayWritableEvenThoughTheTemplateAttributesAreImmutable() {
            var data = fullTemplate().toData(SERIAL);

            data.getPersistentAttrMap().set("new.key", "value");

            assertEquals("value", data.getPersistentAttrMap().get("new.key"));
        }

        @Test
        void templateAttributesAreIsolatedFromTheMapUsedToBuildTheTemplate() {
            var source = new HashMap<String, Object>(Map.of("key", "original"));
            var template = template(source, List.of(), null);

            source.put("key", "changed");
            source.put("extra", "added");

            var data = template.toData(SERIAL);
            assertEquals("original", data.getPersistentAttrMap().get("key"));
            assertFalse(data.getPersistentAttrMap().contains("extra"));
        }

        @Test
        void flagsChangedOnAnItemDoNotLeakIntoTheTemplateOrOtherItems() {
            var template = template(Map.of(), new ArrayList<>(List.of(ItemFlag.WEAPON)), null);
            var first = new UOItem(template.toData(SERIAL), template);

            first.getFlags().add(ItemFlag.MOUNT);

            assertEquals(List.of(ItemFlag.WEAPON), template.flags());
            assertEquals(List.of(ItemFlag.WEAPON), template.toData(SERIAL + 1).getFlags());
        }

        @Test
        void templateFlagsAreIsolatedFromTheListUsedToBuildTheTemplate() {
            var source = new ArrayList<>(List.of(ItemFlag.WEAPON));
            var template = template(Map.of(), source, null);

            source.add(ItemFlag.MOUNT);

            assertEquals(List.of(ItemFlag.WEAPON), template.toData(SERIAL).getFlags());
        }

        @Test
        void eachDataReceivesItsOwnMutableFlagsWhileTheTemplateFlagsAreImmutable() {
            var template = template(Map.of(), List.of(ItemFlag.WEAPON), null);
            var first = template.toData(SERIAL);
            var second = template.toData(SERIAL + 1);

            first.getFlags().add(ItemFlag.MOUNT);

            assertNotSame(first.getFlags(), second.getFlags());
            assertEquals(List.of(ItemFlag.WEAPON), second.getFlags());
            assertThrows(UnsupportedOperationException.class, () -> template.flags().add(ItemFlag.MOUNT));
        }

        @Test
        void missingOptionalCollectionsBecomeEmpty() {
            var template = template(null, null, null);

            var data = template.toData(SERIAL);

            assertTrue(data.getFlags().isEmpty());
            assertNotNull(data.getPersistentAttrMap());
            assertFalse(data.getPersistentAttrMap().contains("any"));
        }
    }

    @Nested
    class MaterialConversion {

        @Test
        void mapsEachComponentToTheSameNamedComponent() {
            var material = new Material(1, 2, 3, 4).toItemMaterial();

            assertAll(
                    () -> assertEquals(1, material.density()),
                    () -> assertEquals(2, material.hardness()),
                    () -> assertEquals(3, material.toughness()),
                    () -> assertEquals(4, material.magicAffinity())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 100, Integer.MAX_VALUE})
        void preservesBoundaryValuesExactly(int value) {
            var material = new Material(value, value, value, value).toItemMaterial();

            assertEquals(new ItemMaterial(value, value, value, value), material);
        }

        @Test
        void eachComponentIsIndependentOfTheOthers() {
            var materials = List.of(
                    new Material(9, 0, 0, 0),
                    new Material(0, 9, 0, 0),
                    new Material(0, 0, 9, 0),
                    new Material(0, 0, 0, 9)
            );

            var converted = materials.stream().map(Material::toItemMaterial).toList();

            assertEquals(List.of(
                    new ItemMaterial(9, 0, 0, 0),
                    new ItemMaterial(0, 9, 0, 0),
                    new ItemMaterial(0, 0, 9, 0),
                    new ItemMaterial(0, 0, 0, 9)
            ), converted);
        }

        @Test
        void templateWithoutMaterialCreatesItemsWithoutMaterial() {
            var template = template(Map.of(), List.of(), null);

            var data = template.toData(SERIAL);

            assertNull(data.getMaterial());
            assertNull(new UOItem(data, template).getMaterial());
        }

        @Test
        void itemMaterialIsNotTheTemplateMaterialType() {
            var data = fullTemplate().toData(SERIAL);

            assertInstanceOf(ItemMaterial.class, data.getMaterial());
            assertNotEquals(TEMPLATE_MATERIAL, data.getMaterial());
        }
    }

    @Nested
    class DataToItem {

        @Test
        void exposesEveryDataFieldThroughTheItem() {
            var template = fullTemplate();
            var data = template.toData(SERIAL);

            var item = new UOItem(data, template);

            assertAll(
                    () -> assertEquals(data.getId(), item.getId()),
                    () -> assertSame(template, item.getTemplate()),
                    () -> assertEquals(SERIAL, item.getSerialId()),
                    () -> assertEquals(6585, item.getModelId()),
                    () -> assertEquals(0, item.getX()),
                    () -> assertEquals(0, item.getY()),
                    () -> assertEquals(0, item.getZ()),
                    () -> assertEquals("iron_ore", item.getName()),
                    () -> assertEquals("iron ore", item.getDisplayName()),
                    () -> assertEquals(Layer.ONE_HANDED, item.getLayer()),
                    () -> assertEquals(1, item.getAmount()),
                    () -> assertEquals(0x96D, item.getHue()),
                    () -> assertEquals(7, item.getUnitWeight()),
                    () -> assertEquals(ITEM_MATERIAL, item.getMaterial()),
                    () -> assertFalse(item.isMovable()),
                    () -> assertFalse(item.isHidden()),
                    () -> assertEquals(Direction.NORTH, item.getDirection()),
                    () -> assertEquals(List.of(ItemFlag.WEAPON), item.getFlags()),
                    () -> assertInstanceOf(OrphanLocation.class, item.getCurrentLocation()),
                    () -> assertNull(item.getPreviousLocation()),
                    () -> assertEquals("iron_ingot", item.persistentAttributes().get("mining.smelting.ingot")),
                    () -> assertFalse(item.runtimeAttributes().contains("mining.smelting.ingot"))
            );
        }

        @Test
        void derivesWeightFromTheTemplateUnitWeight() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);

            item.setAmount(5);

            assertEquals(35, item.getWeight());
        }

        @Test
        void keepsTemplateOnlyPropertiesAvailableThroughTheTemplate() {
            var template = fullTemplate();

            var item = new UOItem(template.toData(SERIAL), template);

            assertAll(
                    () -> assertEquals("ORE", item.getTemplate().stockType()),
                    () -> assertEquals(10, item.getTemplate().basePrice()),
                    () -> assertEquals("mining.smelting", item.getTemplate().script()),
                    () -> assertEquals("MATERIAL", item.getTemplate().family()),
                    () -> assertEquals("mount", item.getTemplate().mountName()),
                    () -> assertEquals(weapon(), item.getTemplate().weapon()),
                    () -> assertEquals(TEMPLATE_MATERIAL, item.getTemplate().material())
            );
        }
    }

    @Nested
    class RoundTrip {

        @Test
        void itemDataEqualsTheDataItWasCreatedFrom() {
            var template = template(Map.of("key", "value"), List.of(ItemFlag.WEAPON), TEMPLATE_MATERIAL);
            var data = template.toData(SERIAL);

            var item = new UOItem(data, template);

            assertEquals(data, item.toData());
        }

        @Test
        void itemWithoutMaterialRoundTripsToEqualData() {
            var template = template(Map.of(), List.of(), null);
            var data = template.toData(SERIAL);

            assertEquals(data, new UOItem(data, template).toData());
        }

        @Test
        void repeatedRoundTripsAreStable() {
            var template = template(Map.of("key", "value"), List.of(ItemFlag.WEAPON), TEMPLATE_MATERIAL);
            var original = template.toData(SERIAL);

            var data = original;
            for (int round = 0; round < 5; round++) {
                data = new UOItem(data, template).toData();
            }

            assertEquals(original, data);
        }

        @Test
        void restoredItemMatchesTheOriginalItemField() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);

            var restored = new UOItem(item.toData(), template);

            assertItemsMatch(item, restored);
        }

        @Test
        void eachToDataCallReturnsANewDataInstance() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);

            var first = item.toData();
            var second = item.toData();

            assertNotSame(first, second);
            assertEquals(first, second);
        }

        @Test
        void changesToReturnedDataDoNotAffectTheItem() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);
            var data = item.toData();

            data.setAmount(50);
            data.setHue(1);
            data.setMaterial(new ItemMaterial(0, 0, 0, 0));
            data.setUnitWeight(0);

            assertEquals(1, item.getAmount());
            assertEquals(0x96D, item.getHue());
            assertEquals(ITEM_MATERIAL, item.getMaterial());
            assertEquals(7, item.getUnitWeight());
        }
    }

    @Nested
    class MutatedItemToData {

        @Test
        void writesEveryMutableFieldBackToData() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);
            var material = new ItemMaterial(5, 6, 7, 8);

            item.setModelId(0x1BF2);
            item.setLocation(100, 200);
            item.setZ(-5);
            item.setName("renamed");
            item.setDisplayName("Renamed");
            item.setLayer(Layer.TWO_HANDED);
            item.setAmount(42);
            item.setHue(0x455);
            item.setUnitWeight(3);
            item.setMaterial(material);
            item.setMovable(true);
            item.setHidden(true);
            item.setDirection(Direction.SOUTH);
            item.setFlags(List.of(ItemFlag.WEARABLE));
            item.persistentAttributes().set("crafted.by", "player");

            var data = item.toData();

            assertAll(
                    () -> assertEquals(SERIAL, data.getSerialId()),
                    () -> assertEquals(0x1BF2, data.getModelId()),
                    () -> assertEquals(100, data.getX()),
                    () -> assertEquals(200, data.getY()),
                    () -> assertEquals(-5, data.getZ()),
                    () -> assertEquals("renamed", data.getName()),
                    () -> assertEquals("Renamed", data.getDisplayName()),
                    () -> assertEquals(Layer.TWO_HANDED, data.getLayer()),
                    () -> assertEquals(42, data.getAmount()),
                    () -> assertEquals(0x455, data.getHue()),
                    () -> assertEquals(3, data.getUnitWeight()),
                    () -> assertEquals(material, data.getMaterial()),
                    () -> assertTrue(data.isMovable()),
                    () -> assertTrue(data.isHidden()),
                    () -> assertEquals(Direction.SOUTH, data.getDirection()),
                    () -> assertEquals(List.of(ItemFlag.WEARABLE), data.getFlags()),
                    () -> assertEquals("player", data.getPersistentAttrMap().get("crafted.by")),
                    () -> assertEquals("iron_ore", data.getTemplate())
            );
        }

        @Test
        void removedMaterialIsWrittenAsAbsent() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);

            item.setMaterial(null);

            assertNull(item.toData().getMaterial());
            assertNull(new UOItem(item.toData(), template).getMaterial());
        }

        @Test
        void runtimeAttributesAreNeverWrittenToData() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);

            item.runtimeAttributes().set("runtime.only", true);

            assertFalse(item.toData().getPersistentAttrMap().contains("runtime.only"));
        }

        @Test
        void itemWithoutTemplateWritesNoTemplateName() {
            var data = fullTemplate().toData(SERIAL);

            var item = new UOItem(data, null);

            assertNull(item.toData().getTemplate());
        }
    }

    @Nested
    class Locations {

        @Test
        void containerLocationRoundTrips() {
            var restored = afterMoving(ItemLocation.container(0x40000999));

            assertEquals(ItemLocationType.CONTAINER, restored.data().getLocationType());
            assertEquals(0x40000999, restored.data().getContainerSerialId());
            assertNull(restored.data().getOwnerSerialId());
            assertEquals(new ContainerLocation(0x40000999), restored.item().getCurrentLocation());
        }

        @Test
        void equippedLocationRoundTrips() {
            var restored = afterMoving(ItemLocation.equipped(0x00000042));

            assertEquals(ItemLocationType.EQUIPPED, restored.data().getLocationType());
            assertEquals(0x00000042, restored.data().getOwnerSerialId());
            assertNull(restored.data().getContainerSerialId());
            assertEquals(new EquippedLocation(0x00000042), restored.item().getCurrentLocation());
        }

        @Test
        void groundLocationRoundTrips() {
            var restored = afterMoving(ItemLocation.ground());

            assertEquals(ItemLocationType.GROUND, restored.data().getLocationType());
            assertNull(restored.data().getOwnerSerialId());
            assertNull(restored.data().getContainerSerialId());
            assertInstanceOf(GroundLocation.class, restored.item().getCurrentLocation());
        }

        @Test
        void orphanLocationRoundTrips() {
            var restored = afterMoving(ItemLocation.orphan());

            assertEquals(ItemLocationType.ORPHAN, restored.data().getLocationType());
            assertNull(restored.data().getOwnerSerialId());
            assertNull(restored.data().getContainerSerialId());
            assertInstanceOf(OrphanLocation.class, restored.item().getCurrentLocation());
        }

        @Test
        void movingKeepsThePreviousLocationOnTheItemOnly() {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);

            item.setCurrentLocation(ItemLocation.ground());

            assertInstanceOf(OrphanLocation.class, item.getPreviousLocation());
            assertNull(new UOItem(item.toData(), template).getPreviousLocation());
        }

        private Restored afterMoving(ItemLocation location) {
            var template = fullTemplate();
            var item = new UOItem(template.toData(SERIAL), template);
            item.setCurrentLocation(location);
            var data = item.toData();
            return new Restored(data, new UOItem(data, template));
        }
    }

    @Nested
    class SpecializedItems {
        private final ItemMapper mapper = new ItemMapper(new InMemoryTemplateRegistry<>(
                List.of(containerTemplate(), corpseTemplate(), fullTemplate()), ItemTemplate::name
        ));

        @Test
        void plainTemplateLoadsAsAPlainItem() {
            var item = mapper.mapToItem(fullTemplate().toData(SERIAL));

            assertEquals(UOItem.class, item.getClass());
            assertEquals(fullTemplate(), item.getTemplate());
        }

        @Test
        void containerTemplateLoadsAsAContainerKeepingItsGump() {
            var data = containerTemplate().toData(SERIAL);

            var container = assertInstanceOf(UOContainer.class, mapper.mapToItem(data));

            assertEquals(0x3C, container.getContainerGumpId());
            assertEquals(data, container.toData());
            assertEquals(ITEM_MATERIAL, container.getMaterial());
        }

        @Test
        void corpseTemplateLoadsAsACorpseKeepingItsState() {
            var data = corpseTemplate().toData(SERIAL);
            data.setCorpseId(0x190);
            data.getEquippedItems().put(Layer.HEAD, 0x40000555);

            var corpse = assertInstanceOf(UOCorpse.class, mapper.mapToItem(data));

            assertEquals(0x190, corpse.getCorpseId());
            assertEquals(Map.of(Layer.HEAD, 0x40000555), corpse.getEquippedItems());
            assertEquals(data, corpse.toData());
        }

        @Test
        void storedDataKeepsMaterialThroughTheStorageMapper() {
            var data = fullTemplate().toData(SERIAL);

            var item = mapper.mapToItem(data);

            assertEquals(ITEM_MATERIAL, item.getMaterial());
            assertEquals(ITEM_MATERIAL, mapper.mapToData(item).getMaterial());
        }

        @Test
        void dataWithoutTemplateStillLoadsItsMaterial() {
            var data = fullTemplate().toData(SERIAL);
            data.setTemplate(null);

            var item = mapper.mapToItem(data);

            assertNull(item.getTemplate());
            assertEquals(ITEM_MATERIAL, item.getMaterial());
        }
    }

    @Nested
    class FromJson {
        private final ObjectMapper json = new ObjectMapper();

        @Test
        void jsonTemplateWithMaterialProducesAnItemWithEqualMaterial() throws Exception {
            var template = json.readValue("""
                    {
                      "name": "iron_ore",
                      "script": "mining.smelting",
                      "displayName": "iron ore",
                      "modelId": 6585,
                      "stockType": "ORE",
                      "unitWeight": 1,
                      "basePrice": 10,
                      "material": {
                        "density": 70,
                        "hardness": 60,
                        "toughness": 65,
                        "magicAffinity": 10
                      },
                      "attr": {
                        "mining.smelting.ingot": "iron_ingot",
                        "mining.minSkill": 0,
                        "mining.occurrence": "COMMON"
                      }
                    }
                    """, ItemTemplate.class);

            var item = new UOItem(template.toData(SERIAL), template);

            assertAll(
                    () -> assertEquals(new Material(70, 60, 65, 10), template.material()),
                    () -> assertEquals(new ItemMaterial(70, 60, 65, 10), item.getMaterial()),
                    () -> assertEquals("iron ore", item.getDisplayName()),
                    () -> assertEquals(6585, item.getModelId()),
                    () -> assertEquals(1, item.getUnitWeight()),
                    () -> assertTrue(item.isMovable()),
                    () -> assertEquals("iron_ingot", item.persistentAttributes().get("mining.smelting.ingot")),
                    () -> assertEquals(0, item.persistentAttributes().get("mining.minSkill")),
                    () -> assertEquals("COMMON", item.persistentAttributes().get("mining.occurrence"))
            );
        }

        @Test
        void jsonTemplateWithoutMaterialProducesAnItemWithoutMaterial() throws Exception {
            var template = json.readValue("{\"name\":\"copper_ore\",\"modelId\":6585}", ItemTemplate.class);

            assertNull(template.material());
            assertNull(new UOItem(template.toData(SERIAL), template).getMaterial());
        }

        @Test
        void explicitNullMaterialIsTheSameAsMissingMaterial() throws Exception {
            var template = json.readValue("{\"name\":\"ore\",\"material\":null}", ItemTemplate.class);

            assertNull(template.toData(SERIAL).getMaterial());
        }

        @Test
        void partialMaterialDefaultsMissingComponentsToZero() throws Exception {
            var template = json.readValue("{\"name\":\"ore\",\"material\":{\"density\":70}}", ItemTemplate.class);

            assertEquals(new ItemMaterial(70, 0, 0, 0), template.toData(SERIAL).getMaterial());
        }

        @Test
        void explicitlyImmovableJsonTemplateCreatesImmovableItems() throws Exception {
            var template = json.readValue("{\"name\":\"anvil\",\"movable\":false}", ItemTemplate.class);

            assertFalse(new UOItem(template.toData(SERIAL), template).isMovable());
        }

        @Test
        void templateSurvivesJsonRoundTripWithItsMaterial() throws Exception {
            var template = fullTemplate();

            var restored = json.readValue(json.writeValueAsString(template), ItemTemplate.class);

            assertEquals(template, restored);
            assertEquals(template.toData(SERIAL).getMaterial(), restored.toData(SERIAL).getMaterial());
        }
    }

    private record Restored(UOItemData data, UOItem item) {}

    private static void assertItemsMatch(UOItem expected, UOItem actual) {
        assertAll(
                () -> assertEquals(expected.getId(), actual.getId()),
                () -> assertSame(expected.getTemplate(), actual.getTemplate()),
                () -> assertEquals(expected.getSerialId(), actual.getSerialId()),
                () -> assertEquals(expected.getModelId(), actual.getModelId()),
                () -> assertEquals(expected.getX(), actual.getX()),
                () -> assertEquals(expected.getY(), actual.getY()),
                () -> assertEquals(expected.getZ(), actual.getZ()),
                () -> assertEquals(expected.getName(), actual.getName()),
                () -> assertEquals(expected.getDisplayName(), actual.getDisplayName()),
                () -> assertEquals(expected.getLayer(), actual.getLayer()),
                () -> assertEquals(expected.getAmount(), actual.getAmount()),
                () -> assertEquals(expected.getHue(), actual.getHue()),
                () -> assertEquals(expected.getUnitWeight(), actual.getUnitWeight()),
                () -> assertEquals(expected.getMaterial(), actual.getMaterial()),
                () -> assertEquals(expected.isMovable(), actual.isMovable()),
                () -> assertEquals(expected.isHidden(), actual.isHidden()),
                () -> assertEquals(expected.getDirection(), actual.getDirection()),
                () -> assertEquals(expected.getFlags(), actual.getFlags()),
                () -> assertEquals(expected.getCurrentLocation(), actual.getCurrentLocation())
        );
    }

    /** A template with a distinct, non-default value in every field, so swapped or dropped fields fail. */
    private static ItemTemplate fullTemplate() {
        return new ItemTemplate(
                "iron_ore",
                "iron ore",
                Layer.ONE_HANDED,
                6585,
                false,
                0x96D,
                "ORE",
                10,
                List.of(ItemFlag.WEAPON),
                Map.of("mining.smelting.ingot", "iron_ingot", "mining.minSkill", 5),
                0x3C,
                "mount",
                weapon(),
                "mining.smelting",
                7,
                "MATERIAL",
                TEMPLATE_MATERIAL
        );
    }

    private static ItemTemplate containerTemplate() {
        return new ItemTemplate(
                "backpack", "backpack", Layer.BACKPACK, 0x0E75, true, 0, null, 0,
                List.of(ItemFlag.CONTAINER), Map.of(), 0x3C, null, null, null, 3, "OTHER", TEMPLATE_MATERIAL
        );
    }

    private static ItemTemplate corpseTemplate() {
        return new ItemTemplate(
                "corpse", "corpse", null, 0x2006, false, 0, null, 0,
                List.of(ItemFlag.CONTAINER, ItemFlag.CORPSE), Map.of(), 0x09, null, null, null, 0, "OTHER", null
        );
    }

    private static ItemTemplate template(Map<String, Object> attr, List<ItemFlag> flags, Material material) {
        return new ItemTemplate(
                "iron_ore", "iron ore", null, 6585, true, 0, "ORE", 10,
                flags, attr, 0, null, null, null, 1, "OTHER", material
        );
    }

    private static Weapon weapon() {
        return new Weapon(WeaponStyle.values()[0], new BaseDamage(3, 9), 1);
    }
}
