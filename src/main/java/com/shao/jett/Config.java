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

    /** 合并多修饰符为一行（灵感来自 NeoForge enableMergedAttributeTooltips） */
    public final ConfigValue<Boolean> mergeModifiers;

    public final ConfigValue<Boolean> debug;

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

    /** 要隐藏物品名称的物品列表 */
    public final ConfigValue<List<? extends String>> hideItemNames;

    /** 隐藏所有物品名称（可选白名单排除） */
    public final ConfigValue<Boolean> hideAllItemNames;

    /** 隐藏所有名称时的白名单 */
    public final ConfigValue<List<? extends String>> itemNameWhitelist;

    /** 正则表达式删除模式列表（仿 CRT ActionRemoveRegexTooltip） */
    public final ConfigValue<List<? extends String>> regexRemove;

    /** 正则删除的白名单（这些物品不参与全局正则删除） */
    public final ConfigValue<List<? extends String>> regexRemoveWhitelist;

    /** 按物品的单独正则删除：格式 "modid:item_name=regex pattern" */
    public final ConfigValue<List<? extends String>> perItemRegexRemove;

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

        mergeModifiers = builder
                .comment("Merge multiple attribute modifiers of the same type",
                         "into a single tooltip line showing the total value.",
                         "Inspired by NeoForge's enableMergedAttributeTooltips.",
                         "\u5C06\u540C\u4E00\u5C5E\u6027\u7684\u591A\u4E2A\u4FEE\u9970\u7B26\u5408\u5E76\u4E3A\u4E00\u884C\uFF0C",
                         "\u663E\u793A\u603B\u503C\u3002\u7075\u611F\u6765\u81EA NeoForge\u3002")
                .define("mergeModifiers", false);

        debug = builder
                .comment("Enable debug output in the game log.",
                         "\u5F00\u542F\u8C03\u8BD5\u65E5\u5FD7\u8F93\u51FA\u3002")
                .define("debug", false);

        hideItemNames = builder
                .comment("Items whose display names should be hidden from tooltips.",
                         "Format: modid:item_name, one per line.",
                         "\u9690\u85CF\u6307\u5B9A\u7269\u54C1\u7684\u540D\u79F0\uFF0C\u683C\u5F0F\uFF1Amodid:item_name")
                .defineList("hideItemNames", Lists::newArrayList,
                        obj -> obj instanceof String);

        hideAllItemNames = builder
                .comment("Hide ALL item display names from tooltips.",
                         "Set to true to remove names from every item.",
                         "Use itemNameWhitelist to exclude specific items.",
                         "\u9690\u85CF\u6240\u6709\u7269\u54C1\u540D\u79F0\uFF0C\u914D\u5408\u767D\u540D\u5355\u6392\u9664")
                .define("hideAllItemNames", false);

        itemNameWhitelist = builder
                .comment("Items to keep their names when hideAllItemNames is true.",
                         "Format: modid:item_name, one per line.",
                         "\u5728\u5168\u5C40\u9690\u85CF\u540D\u79F0\u65F6\u4FDD\u7559\u6307\u5B9A\u7269\u54C1\u7684\u540D\u79F0")
                .defineList("itemNameWhitelist", Lists::newArrayList,
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
                         "",
                         "Supports vanilla color/format codes (\u00A7).",
                         "When using both, the color code MUST come before",
                         "the format code, or the format will be ignored.",
                         "\u652F\u6301\u539F\u7248\u989C\u8272/\u683C\u5F0F\u4EE3\u7801\uFF08\u00A7\uFF09\u3002",
                         "\u989C\u8272\u4EE3\u7801\u5FC5\u987B\u5728\u683C\u5F0F\u4EE3\u7801\u524D\u9762\uFF0C\u5426\u5219\u683C\u5F0F\u4F1A\u5931\u6548\u3002",
                         "",
                         "Optional position prefix: \"N:text\" inserts at line N.",
                         "\u53EF\u9009\u4F4D\u7F6E\u524D\u7F00\uFF1A\u201CN:\u6587\u672C\u201D\u5728\u7B2CN\u884C\u63D2\u5165\uFF0C\u65E0\u524D\u7F00\u5219\u8FFD\u52A0\u5230\u672B\u5C3E\u3002",
                         "",
                         "Example / \u793A\u4F8B\uFF1A",
                         "  customTooltips = [\"minecraft:diamond_sword=1:\u00A7bRare\",",
                         "                     \"minecraft:diamond_sword=\u00A7b\u00A7o\u94BB\u77F3\u5236\u54C1\"]")
               .push("customTooltips");

        customTooltips = builder
                .comment("Add custom text lines to item tooltips.",
                         "Format: \"modid:item_name=[N:]Tooltip text\"",
                         "N is an optional 0-based line number. If omitted, appends to end.",
                         "\u683C\u5F0F\uFF1A\"modid:item_name=[N:]\u81EA\u5B9A\u4E49\u6587\u672C\"",
                         "N \u4E3A\u53EF\u9009\u884C\u53F7\uFF080\u5F00\u59CB\uFF09\uFF0C\u4E0D\u586B\u5219\u8FFD\u52A0\u5230\u672B\u5C3E\u3002")
                .defineList("customTooltips", Lists::newArrayList,
                        obj -> obj instanceof String);

        builder.pop();

        builder.comment("Regex-based tooltip line removal (like CRT removeTooltip)",
                         "Each entry is a Java regex pattern. Any tooltip line whose",
                         "text matches the pattern will be removed.",
                         "",
                         "Example / \u793A\u4F8B\uFF1A",
                         "  regexRemove = [\"Attack Damage\", \"\u653B\u51FB\u4F24\u5BB3\", \"\u5728.*\u65F6\uFF1A\"]")
               .push("regexRemove");

        regexRemove = builder
                .comment("Regex patterns to remove matching tooltip lines.",
                         "Example: \"Attack Damage\" removes lines containing that text.",
                         "Uses Matcher.find() — matches anywhere in the line, not just start/end.",
                         "Matcher.find() \u5339\u914D\u884C\u4E2D\u4EFB\u610F\u4F4D\u7F6E\uFF0C\u4E0D\u662F\u5F00\u5934")
                .defineList("regexRemove", Lists::newArrayList,
                        obj -> obj instanceof String);

        regexRemoveWhitelist = builder
                .comment("Items excluded from global regex removal.",
                         "Format: modid:item_name, one per line.",
                         "\u5168\u5C40\u6B63\u5219\u5220\u9664\u7684\u767D\u540D\u5355\uFF0C\u683C\u5F0F\uFF1Amodid:item_name")
                .defineList("regexRemoveWhitelist", Lists::newArrayList,
                        obj -> obj instanceof String);

        perItemRegexRemove = builder
                .comment("Per-item regex removal.",
                         "Format: \"modid:item_name=regex pattern\"",
                         "\u6309\u7269\u54C1\u7684\u5355\u72EC\u6B63\u5219\u5220\u9664\uFF0C",
                         "Example / \u793A\u4F8B\uFF1A",
                         "  perItemRegexRemove = [\"minecraft:diamond_sword=Attack\"]")
                .defineList("perItemRegexRemove", Lists::newArrayList,
                        obj -> obj instanceof String);

        builder.pop();
    }
}
