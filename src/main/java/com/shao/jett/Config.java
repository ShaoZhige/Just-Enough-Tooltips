package com.shao.jett;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

/**
 * JETT (Just Enough Tooltips) configuration.
 * <p>
 * Config file: {@code config/jett-common.toml}
 * <p>
 * Features:
 * <ul>
 *   <li>Hide attribute modifiers from tooltips</li>
 *   <li>Merge multi-modifier attributes into a single line</li>
 *   <li>Hide equipment slot headers ("When in main hand:", etc.)</li>
 *   <li>Global tooltip text displayed on all items (with blacklist)</li>
 *   <li>Per-item custom tooltip text (with position control)</li>
 *   <li>Hide item display names</li>
 *   <li>Regex-based tooltip line removal</li>
 * </ul>
 *
 * @author Shao_Zhige
 */
public class Config {

    // ======================== [general] ========================

    /** Items completely excluded from JETT processing. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> itemWhitelist;

    /** Attribute registry paths to hide from tooltips (e.g. "generic.attack_damage") */
    public final ConfigValue<List<? extends String>> hiddenAttributes;

    /** Merge multiple modifiers of the same attribute into a single line showing the total */
    public final ConfigValue<Boolean> mergeModifiers;

    /** Items whose display names should be hidden. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> hideItemNames;

    /** Hide ALL item display names (use itemNameWhitelist to exclude specific items) */
    public final ConfigValue<Boolean> hideAllItemNames;

    /** Items to keep their names when hideAllItemNames is enabled */
    public final ConfigValue<List<? extends String>> itemNameWhitelist;

    /** Enable verbose debug logging in the game log */
    public final ConfigValue<Boolean> debug;

    // ======================== [globalTooltip] ========================

    /** Text displayed on every item's tooltip. Supports "N:text" position prefix */
    public final ConfigValue<String> globalTooltip;

    /** Items excluded from the global tooltip. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> globalTooltipBlacklist;

    // ======================== [slotHeaders] ========================

    public final ConfigValue<Boolean> hideSlotHeaderMainhand;
    public final ConfigValue<Boolean> hideSlotHeaderOffhand;
    public final ConfigValue<Boolean> hideSlotHeaderHead;
    public final ConfigValue<Boolean> hideSlotHeaderChest;
    public final ConfigValue<Boolean> hideSlotHeaderLegs;
    public final ConfigValue<Boolean> hideSlotHeaderFeet;
    public final ConfigValue<Boolean> hideFireworkFlight;
    public final ConfigValue<Boolean> hidePotionEffectHeader;

    /** Items that keep their slot header lines. Format: "modid:item_name" */
    public final ConfigValue<List<? extends String>> slotHeaderWhitelist;

    // ======================== [customTooltips] ========================

    /** Per-item custom tooltip lines. Format: "key=[N:]text" (key supports comma-separated multi-item) */
    public final ConfigValue<List<? extends String>> customTooltips;

    // ======================== [regexRemove] ========================

    /** Regex patterns to remove matching tooltip lines (applied to all items) */
    public final ConfigValue<List<? extends String>> regexRemove;

    /** Items excluded from global regex removal */
    public final ConfigValue<List<? extends String>> regexRemoveWhitelist;

    /** Per-item regex removal. Format: "modid:item_name=regex pattern" */
    public final ConfigValue<List<? extends String>> perItemRegexRemove;

    // ================================================================

    Config(ForgeConfigSpec.Builder builder) {
        builder.comment(
                "",
                "JETT (Just Enough Tooltips) Configuration",
                "",
                "A client-side mod that gives you full control over item tooltips.",
                "All features are optional — only enable what you need.",
                "",
                "All settings support hot-reloading — edit the config and switch back",
                "to the game to see changes immediately. No restart required.",
                "",
                "\u7eaf\u5ba2\u6237\u7aef\u6a21\u7ec4\uff0c\u6240\u6709\u529f\u80fd\u5747\u53ef\u72ec\u7acb\u5f00\u5173\u3002",
                "\u5168\u90e8\u8bbe\u7f6e\u652f\u6301\u70ed\u91cd\u8f7d\uff0c\u4fee\u6539\u914d\u7f6e\u540e\u5207\u56de\u6e38\u620f\u5373\u53ef\u751f\u6548\uff0c\u65e0\u9700\u91cd\u542f\u3002")
               .push("general");

        itemWhitelist = builder
                .comment(
                        "Items that JETT will NOT modify at all (keep everything as-is).",
                        "Format: \"modid:item_name\", one per line.",
                        "\u5b8c\u5168\u4e0d\u53d7\u5f71\u54cd\u7684\u7269\u54c1\u767d\u540d\u5355\u3002\u683c\u5f0f\uff1amodid:item_name")
                .defineList("itemWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        hiddenAttributes = builder
                .comment(
                        "Attribute types to hide from tooltips.",
                        "Each entry is the registry path of an attribute.",
                        "Vanilla attributes:",
                        "  generic.attack_damage         - Attack Damage (\u653b\u51fb\u4f24\u5bb3)",
                        "  generic.attack_speed          - Attack Speed (\u653b\u51fb\u901f\u5ea6)",
                        "  generic.armor                 - Armor (\u62a4\u7532\u503c)",
                        "  generic.armor_toughness       - Armor Toughness (\u76d4\u7532\u97e7\u6027)",
                        "  generic.knockback_resistance  - Knockback Resistance (\u51fb\u9000\u6297\u6027)",
                        "  generic.max_health            - Max Health (\u6700\u5927\u751f\u547d\u503c)",
                        "  generic.movement_speed        - Movement Speed (\u79fb\u52a8\u901f\u5ea6)",
                        "  generic.attack_knockback      - Attack Knockback (\u653b\u51fb\u51fb\u9000)",
                        "  generic.luck                  - Luck (\u5e78\u8fd0)",
                        "",
                        "Mod-added attributes use the same format (e.g. \"epicfight.stun\" etc.)",
                        "\u5176\u4ed6\u6a21\u7ec4\u7684\u5c5e\u6027\u540c\u6837\u9002\u7528\uff0c\u586b\u6ce8\u518c\u540d\u5373\u53ef\u3002",
                        "",
                        "Example: [\"generic.attack_damage\", \"generic.armor\"]",
                        "Empty list = show everything. \u7a7a\u5217\u8868 = \u5168\u90e8\u663e\u793a\u3002")
                .defineList("hiddenAttributes", Lists::newArrayList, obj -> obj instanceof String);

        mergeModifiers = builder
                .comment(
                        "Merge multiple modifiers of the same attribute into one line.",
                        "Inspired by NeoForge's enableMergedAttributeTooltips.",
                        "\u5c06\u540c\u5c5e\u6027\u7684\u591a\u4e2a\u4fee\u9970\u7b26\u5408\u5e76\u4e3a\u4e00\u884c\u663e\u793a\u603b\u503c\u3002",
                        "\u7075\u611f\u6765\u81ea NeoForge\u3002",
                        "  false = show each modifier separately (default)",
                        "  true  = merge into a single line")
                .define("mergeModifiers", false);

        hideItemNames = builder
                .comment(
                        "Hide the display name of specific items.",
                        "Format: \"modid:item_name\", one per line.",
                        "Use hideAllItemNames=true to hide ALL item names instead.",
                        "\u9690\u85cf\u6307\u5b9a\u7269\u54c1\u7684\u540d\u79f0\u3002\u683c\u5f0f\uff1amodid:item_name")
                .defineList("hideItemNames", Lists::newArrayList, obj -> obj instanceof String);

        hideAllItemNames = builder
                .comment(
                        "Hide ALL item display names.",
                        "Use itemNameWhitelist to keep names on specific items.",
                        "\u9690\u85cf\u6240\u6709\u7269\u54c1\u540d\u79f0\uff0c\u914d\u5408 itemNameWhitelist \u6392\u9664\u7279\u5b9a\u7269\u54c1\u3002")
                .define("hideAllItemNames", false);

        itemNameWhitelist = builder
                .comment(
                        "Items that keep their names when hideAllItemNames is enabled.",
                        "Format: \"modid:item_name\", one per line.",
                        "\u5728\u5168\u5c40\u9690\u85cf\u540d\u79f0\u65f6\u4fdd\u7559\u6307\u5b9a\u7269\u54c1\u7684\u540d\u79f0\u3002")
                .defineList("itemNameWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        debug = builder
                .comment(
                        "Enable verbose debug logging.",
                        "When enabled, the complete tooltip processing pipeline is logged.",
                        "Zero performance cost when disabled. \u96f6\u6027\u80fd\u5f00\u9500\u3002")
                .define("debug", false);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "",
                "Global tooltip text displayed on every item.",
                "",
                "Color codes (copy the \u00a7 symbol directly):",
                "  \u00a70=black  \u00a71=dark_blue  \u00a72=dark_green  \u00a73=dark_aqua",
                "  \u00a74=dark_red  \u00a75=dark_purple  \u00a76=gold  \u00a77=gray",
                "  \u00a78=dark_gray  \u00a79=blue  \u00a7a=green  \u00a7b=aqua",
                "  \u00a7c=red  \u00a7d=light_purple  \u00a7e=yellow  \u00a7f=white",
                "  Style: \u00a7l=bold  \u00a7o=italic  \u00a7n=underline  \u00a7m=strikethrough  \u00a7k=obfuscated  \u00a7r=reset",
                "",
                "Format:",
                "  \"\"        = disabled",
                "  \"text\"    = append to end of every tooltip",
                "  \"N:text\"  = insert at line N (0 = first line)",
                "",
                "Examples:",
                "  \u00a77\u00a7oRight-click to use         -> appended",
                "  0:\u00a7e\u00a7l\u2605 Server Item \u2605        -> inserted at line 0",
                "",
                "\u5168\u5c40\u63d0\u793a\uff1a\u5bf9\u6240\u6709\u7269\u54c1\u663e\u793a\u7684\u81ea\u5b9a\u4e49\u6587\u5b57\uff08\u9ed1\u540d\u5355\u9664\u5916\uff09\u3002",
                "\u683c\u5f0f: \"text\"\uff08\u8ffd\u52a0\uff09\u6216 \"N:text\"\uff08\u63d2\u5165\u7b2cN\u884c\uff09\u3002\u7a7a=\u7981\u7528\u3002")
               .push("globalTooltip");

        globalTooltip = builder
                .comment(
                        "Text displayed on every item's tooltip.",
                        "Empty string = disabled.",
                        "\u5168\u5c40\u63d0\u793a\u6587\u672c\u3002\u7a7a\u5b57\u7b26\u4e32 = \u7981\u7528\u3002")
                .define("globalTooltip", "");

        globalTooltipBlacklist = builder
                .comment(
                        "Items excluded from the global tooltip.",
                        "Format: \"modid:item_name\", one per line.",
                        "Example: [\"minecraft:air\", \"minecraft:barrier\"]",
                        "\u5168\u5c40\u63d0\u793a\u9ed1\u540d\u5355\u3002\u683c\u5f0f\uff1amodid:item_name")
                .defineList("globalTooltipBlacklist", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "Hide equipment slot header lines.",
                "All disabled by default — enable only what you need.",
                "",
                "Examples of slot headers:",
                "  \"When in main hand:\"  (item.modifiers.mainhand)",
                "  \"When in off hand:\"   (item.modifiers.offhand)",
                "  \"When on head:\"       (item.modifiers.head)",
                "  \"When on body:\"       (item.modifiers.chest)",
                "  \"When on legs:\"       (item.modifiers.legs)",
                "  \"When on feet:\"       (item.modifiers.feet)",
                "",
                "\u9690\u85cf\u88c5\u5907\u69fd\u4f4d\u6807\u9898\u884c\uff08\u5982\"\u5728\u4e3b\u624b\u65f6\uff1a\"\u7b49\uff09\u3002\u5168\u90e8\u9ed8\u8ba4\u5173\u95ed\u3002")
               .push("slotHeaders");

        hideSlotHeaderMainhand = builder
                .comment("Hide the main hand slot header.")
                .define("hideMainhand", false);
        hideSlotHeaderOffhand = builder
                .comment("Hide the off hand slot header.")
                .define("hideOffhand", false);
        hideSlotHeaderHead = builder
                .comment("Hide the head slot header.")
                .define("hideHead", false);
        hideSlotHeaderChest = builder
                .comment("Hide the chest slot header.")
                .define("hideChest", false);
        hideSlotHeaderLegs = builder
                .comment("Hide the legs slot header.")
                .define("hideLegs", false);
        hideSlotHeaderFeet = builder
                .comment("Hide the feet slot header.")
                .define("hideFeet", false);

        hideFireworkFlight = builder
                .comment("Hide firework flight duration line (\"Flight Duration: N\").",
                        "\u9690\u85cf\u70df\u82b1\u7684 \"\u98de\u884c\u65f6\u95f4\uff1aN\" \u884c\u3002")
                .define("hideFireworkFlight", false);
        hidePotionEffectHeader = builder
                .comment("Hide tipped arrow / potion effect header line (\"When Applied:\").",
                        "\u9690\u85cf\u836f\u7bad\u7684 \"\u5f53\u751f\u6548\u540e\uff1a\" \u6807\u9898\u884c\u3002")
                .define("hidePotionEffectHeader", false);

        slotHeaderWhitelist = builder
                .comment(
                        "Items that keep their slot headers when hideSlot* is enabled.",
                        "Format: \"modid:item_name\", one per line.",
                        "\u69fd\u4f4d\u6807\u9898\u767d\u540d\u5355\u3002\u683c\u5f0f\uff1amodid:item_name")
                .defineList("slotHeaderWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "",
                "Per-item custom tooltip text.",
                "",
                "Color codes (copy the \u00a7 symbol directly):",
                "  \u00a70=black  \u00a71=dark_blue  \u00a72=dark_green  \u00a73=dark_aqua",
                "  \u00a74=dark_red  \u00a75=dark_purple  \u00a76=gold  \u00a77=gray",
                "  \u00a78=dark_gray  \u00a79=blue  \u00a7a=green  \u00a7b=aqua",
                "  \u00a7c=red  \u00a7d=light_purple  \u00a7e=yellow  \u00a7f=white",
                "  Style: \u00a7l=bold  \u00a7o=italic  \u00a7n=underline  \u00a7m=strikethrough  \u00a7k=obfuscated  \u00a7r=reset",
                "  Note: put color codes BEFORE style codes (e.g. \u00a7b\u00a7o works, \u00a7o\u00a7b may not)",
                "",
                "Format: \"key=[N:]text\"",
                "  key   = single item: \"modid:item_name\"",
                "         OR comma-separated: \"modid:a,modid:b,modid:c\"",
                "  N     = optional 0-based line number (omit to append at end)",
                "  text  = display text (supports \u00a7 color/format codes)",
                "",
                "Examples:",
                "  \"minecraft:diamond_sword=1:\u00a7bRare\"",
                "  \"minecraft:diamond_sword=\u00a7b\u00a7o\u94bb\u77f3\u5236\u54c1\"",
                "  \"minecraft:diamond,emerald=0:\u00a7a\u2605 Gem \u2605\"",
                "  \"minecraft:diamond_sword,netherite_sword=\u00a7cLegendary\"",
                "",
                "\u6309\u7269\u54c1\u7684\u81ea\u5b9a\u4e49\u63d0\u793a\u6587\u5b57\u3002",
                "\u683c\u5f0f: \"key=[N:]text\"\uff0ckey\u652f\u6301\u9017\u53f7\u5206\u9694\u591a\u7269\u54c1\u3002")
               .push("customTooltips");

        customTooltips = builder
                .comment(
                        "Add custom text lines to item tooltips.",
                        "Each entry matches one or more items and inserts/appends text.",
                        "\u6dfb\u52a0\u81ea\u5b9a\u4e49 tooltip \u6587\u5b57\u884c\u3002")
                .defineList("customTooltips", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();

        // ---------------------------------------------------------

        builder.comment(
                "",
                "Regex-based tooltip line removal.",
                "Uses Java regex patterns. Matcher.find() matches anywhere in the line.",
                "This runs LAST — it can delete lines added by customTooltips too.",
                "",
                "\u57fa\u4e8e\u6b63\u5219\u8868\u8fbe\u5f0f\u7684 tooltip \u884c\u5220\u9664\u3002\u6700\u540e\u6267\u884c\uff0c\u53ef\u8986\u76d6\u524d\u9762\u6240\u6709\u7ed3\u679c\u3002")
               .push("regexRemove");

        regexRemove = builder
                .comment(
                        "Regex patterns applied to ALL tooltips.",
                        "Any tooltip line containing a match is removed.",
                        "Uses Matcher.find() — matches anywhere in the line.",
                        "Example: \"Attack Damage\" removes lines with that text.",
                        "\u5168\u5c40\u6b63\u5219\uff0c\u5339\u914d\u4efb\u610f\u7269\u54c1\u3002")
                .defineList("regexRemove", Lists::newArrayList, obj -> obj instanceof String);

        regexRemoveWhitelist = builder
                .comment(
                        "Items excluded from global regex removal.",
                        "Per-item regex (perItemRegexRemove) still applies.",
                        "Format: \"modid:item_name\", one per line.",
                        "\u5168\u5c40\u6b63\u5219\u767d\u540d\u5355\u3002\u683c\u5f0f\uff1amodid:item_name")
                .defineList("regexRemoveWhitelist", Lists::newArrayList, obj -> obj instanceof String);

        perItemRegexRemove = builder
                .comment(
                        "Per-item regex removal.",
                        "Format: \"modid:item_name=regex pattern\"",
                        "Example: [\"minecraft:diamond_sword=Attack\"]",
                        "\u4ecd\u4f1a\u6267\u884c\u6309\u7269\u54c1\u6b63\u5219\u3002",
                        "\u6309\u7269\u54c1\u7684\u6b63\u5219\u5220\u9664\u3002\u683c\u5f0f: \"modid:item_name=regex\"")
                .defineList("perItemRegexRemove", Lists::newArrayList, obj -> obj instanceof String);

        builder.pop();
    }
}
