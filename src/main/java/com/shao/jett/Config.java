package com.shao.jett;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

/**
 * JETT 模组配置
 * <p>
 * 配置文件路径：config/jett-common.toml
 */
public class Config {

    public final ConfigValue<List<? extends String>> itemWhitelist;

    /** 要隐藏的属性修饰符列表，填属性注册名 path（如 "generic.attack_damage"） */
    public final ConfigValue<List<? extends String>> hiddenAttributes;

    public final ConfigValue<Boolean> hideSlotHeaderMainhand;
    public final ConfigValue<Boolean> hideSlotHeaderOffhand;
    public final ConfigValue<Boolean> hideSlotHeaderHead;
    public final ConfigValue<Boolean> hideSlotHeaderChest;
    public final ConfigValue<Boolean> hideSlotHeaderLegs;
    public final ConfigValue<Boolean> hideSlotHeaderFeet;

    /** 槽位标题行的物品白名单 */
    public final ConfigValue<List<? extends String>> slotHeaderWhitelist;

    /** 自定义 tooltip：格式 "modid:item_name=Tooltip text" */
    public final ConfigValue<List<? extends String>> customTooltips;

    Config(ForgeConfigSpec.Builder builder) {
        builder.comment("JETT (Just Enough Tooltips) Configuration",
                         "",
                         "NOTE: Hot-reload only applies to certain config changes.",
                         "Please restart the game whenever possible.",
                         "\u6CE8\u610F\uFF1A\u70ED\u91CD\u8F7D\u4EC5\u5BF9\u90E8\u5206\u914D\u7F6E\u9879\u751F\u6548\uFF0C",
                         "\u8BF7\u5C3D\u91CF\u91CD\u542F\u6E38\u620F\u3002")
               .push("general");

        itemWhitelist = builder
                .comment("Items listed here will keep their green attribute tooltips.",
                         "Format: modid:item_name, one per line.")
                .defineList("itemWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        hiddenAttributes = builder
                .comment("Attribute modifier types to hide from tooltips.",
                         "Each entry is the registry path of an attribute.",
                         "",
                         "Vanilla attributes you can add:",
                         "  generic.attack_damage        - Attack Damage",
                         "                                 \u653B\u51FB\u4F24\u5BB3",
                         "  generic.attack_speed         - Attack Speed",
                         "                                 \u653B\u51FB\u901F\u5EA6",
                         "  generic.armor                - Armor",
                         "                                 \u62A4\u7532\u503C",
                         "  generic.armor_toughness      - Armor Toughness",
                         "                                 \u76D4\u7532\u97E7\u6027",
                         "  generic.knockback_resistance - Knockback Resistance",
                         "                                 \u51FB\u9000\u6297\u6027",
                         "  generic.max_health           - Max Health",
                         "                                 \u6700\u5927\u751F\u547D\u503C",
                         "  generic.movement_speed       - Movement Speed",
                         "                                 \u79FB\u52A8\u901F\u5EA6",
                         "  generic.attack_knockback     - Attack Knockback",
                         "                                 \u653B\u51FB\u51FB\u9000",
                         "  generic.luck                 - Luck",
                         "                                 \u5E78\u8FD0",
                         "",
                         "Example: [\"generic.attack_damage\", \"generic.armor\"]",
                         "Set to [] to show all attribute tooltips.")
                .defineList("hiddenAttributes", Lists::newArrayList,
                        obj -> obj instanceof String);

        builder.pop();

        builder.comment("Hide equipment slot header lines (all disabled by default)")
               .push("slotHeaders");

        hideSlotHeaderMainhand = builder
                .comment("Hide \"When in main hand:\"")
                .define("hideMainhand", false);
        hideSlotHeaderOffhand = builder
                .comment("Hide \"When in off hand:\"")
                .define("hideOffhand", false);
        hideSlotHeaderHead = builder
                .comment("Hide \"When on head:\"")
                .define("hideHead", false);
        hideSlotHeaderChest = builder
                .comment("Hide \"When on body:\"")
                .define("hideChest", false);
        hideSlotHeaderLegs = builder
                .comment("Hide \"When on legs:\"")
                .define("hideLegs", false);
        hideSlotHeaderFeet = builder
                .comment("Hide \"When on feet:\"")
                .define("hideFeet", false);

        slotHeaderWhitelist = builder
                .comment("Items listed here will keep their slot header lines.",
                         "Format: modid:item_name, one per line.")
                .defineList("slotHeaderWhitelist", Lists::newArrayList,
                        obj -> obj instanceof String);

        builder.pop();

        builder.comment("Custom tooltips to add to items",
                         "Format: \"modid:item_name=Your text here\"",
                         "\u683C\u5F0F\uFF1A\"modid:item_name=\u4F60\u7684\u6587\u672C\"",
                         "Example / \u793A\u4F8B\uFF1A",
                         "  [\"minecraft:iron_sword=Made of iron\",",
                         "   \"minecraft:diamond_sword=Made of diamond\"]")
               .push("customTooltips");

        customTooltips = builder
                .comment("Add custom text lines to item tooltips.",
                         "Each entry: \"modid:item_name=Tooltip text\"",
                         "\u6BCF\u6761\u683C\u5F0F\uFF1A\"modid:item_name=\u81EA\u5B9A\u4E49\u6587\u672C\"")
                .defineList("customTooltips", Lists::newArrayList,
                        obj -> obj instanceof String);

        builder.pop();
    }
}
