package com.shao.jett;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // 物品名称隐藏
        removeItemName(event, stack);

        if (isWhitelisted(stack)) {
            addCustomTooltips(event, stack);
            return;
        }

        // 槽位标题行隐藏（先执行，独立于属性列表）
        removeSlotHeaders(event, stack);

        // 根据 hiddenAttributes 配置收集要匹配的属性翻译名
        Set<String> hiddenNames = collectHiddenAttributeNames(stack);
        if (!hiddenNames.isEmpty()) {
            // 文本匹配移除（属性名始终在行尾，用 endsWith 避免 "Armor" 误匹配 "Armor Toughness"）
            event.getToolTip().removeIf(line -> {
                String text = line.getString();
                for (String name : hiddenNames) {
                    if (text.endsWith(name)) return true;
                }
                return false;
            });
        }

        // 添加自定义 tooltip
        addCustomTooltips(event, stack);

        // 正则表达式删除（仿 CRT，最后执行以覆盖前面所有结果）
        removeByRegex(event, stack);
    }

    /** 移除 tooltip 第一行（物品名称） */
    private void removeItemName(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null || event.getToolTip().isEmpty()) return;

        String itemName = rl.toString();
        Config config = JETT.CONFIG;

        // 全局隐藏模式
        if (config.hideAllItemNames.get()) {
            if (!config.itemNameWhitelist.get().contains(itemName)) {
                event.getToolTip().remove(0);
            }
            return;
        }

        // 单物品隐藏模式
        if (config.hideItemNames.get().contains(itemName)) {
            event.getToolTip().remove(0);
        }
    }

    private void removeSlotHeaders(ItemTooltipEvent event, ItemStack stack) {
        // 槽位标题白名单检查
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl != null && JETT.CONFIG.slotHeaderWhitelist.get().contains(rl.toString())) return;

        Config config = JETT.CONFIG;
        event.getToolTip().removeIf(line -> {
            if (!(line instanceof TranslationTextComponent)) return false;
            String key = ((TranslationTextComponent) line).getKey();
            if (key.equals("item.modifiers.mainhand") && config.hideSlotHeaderMainhand.get()) return true;
            if (key.equals("item.modifiers.offhand")  && config.hideSlotHeaderOffhand.get())  return true;
            if (key.equals("item.modifiers.head")     && config.hideSlotHeaderHead.get())     return true;
            if (key.equals("item.modifiers.chest")    && config.hideSlotHeaderChest.get())    return true;
            if (key.equals("item.modifiers.legs")     && config.hideSlotHeaderLegs.get())     return true;
            if (key.equals("item.modifiers.feet")     && config.hideSlotHeaderFeet.get())     return true;
            return false;
        });
    }

    private boolean isWhitelisted(ItemStack stack) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return rl != null && JETT.CONFIG.itemWhitelist.get().contains(rl.toString());
    }

    /**
     * 遍历物品所有槽位的属性修饰符，找出 registry path 在 hiddenAttributes
     * 配置列表中的属性，返回其翻译后名称（如 "攻击伤害"）。
     */
    private Set<String> collectHiddenAttributeNames(ItemStack stack) {
        List<? extends String> hidden = JETT.CONFIG.hiddenAttributes.get();
        if (hidden.isEmpty()) return new HashSet<>();

        Set<String> names = new HashSet<>();
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
            for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                ResourceLocation rl = entry.getKey().getRegistryName();
                if (rl != null && hidden.contains(rl.getPath())) {
                    String key = "attribute.name." + rl.getPath();
                    names.add(new TranslationTextComponent(key).getString());
                }
            }
        }
        return names;
    }

    /** 解析配置中的自定义 tooltip，匹配物品注册名后追加到 tooltip 末尾 */
    private void addCustomTooltips(ItemTooltipEvent event, ItemStack stack) {
        ResourceLocation itemRl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRl == null) return;

        String itemName = itemRl.toString();
        List<? extends String> entries = JETT.CONFIG.customTooltips.get();

        for (String entry : entries) {
            int eq = entry.indexOf('=');
            if (eq <= 0 || eq >= entry.length() - 1) continue; // 无效格式

            String key = entry.substring(0, eq);
            if (!key.equals(itemName)) continue;

            String text = entry.substring(eq + 1);
            event.getToolTip().add(new StringTextComponent(text));
        }
    }

    /**
     * 正则表达式删除 tooltip 行。
     *
     * 仿 CraftTweaker 的 ActionRemoveRegexTooltip 实现。
     *
     * CRT 中的原始调用链：
     *   1. ZenScript: item.removeTooltip("regex")
     *   2. → IIngredient.removeTooltip()                [api/item/IIngredient.java]
     *   3. → ActionRemoveRegexTooltip 构造，注册 ITooltipFunction
     *      [impl/actions/items/tooltips/ActionRemoveRegexTooltip.java]
     *   4. TooltipFunction 存入全局 Map
     *      → ActionTooltipBase.getTooltip()
     *      [impl/actions/items/tooltips/ActionTooltipBase.java]
     *   5. ItemTooltipEvent 触发时回调
     *      → CTClientEventHandler.handleTooltips()
     *      [impl/events/CTClientEventHandler.java]
     *
     * CRT 核心过滤逻辑（ActionRemoveRegexTooltip 第 20-29 行）：
     *   遍历 tooltip → MCTextComponent.getFormattedText() = ITextComponent.getString()
     *   → regex.matcher(text).find() → 匹配的行丢弃，不匹配的保留
     *   → tooltip.clear() + tooltip.addAll(filtered)
     *
     * JETT 复刻版：支持三种模式 — 全局正则、按物品正则、白名单排除。
     * 执行顺序：先检查白名单 → 有白名单则跳过全局正则 → 仍执行按物品正则。
     */
    private void removeByRegex(ItemTooltipEvent event, ItemStack stack) {
        Config config = JETT.CONFIG;
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String itemName = rl != null ? rl.toString() : "";

        // 收集所有要应用的正则
        List<Pattern> compiled = new ArrayList<>();

        // 按物品正则：仅当物品名匹配时才添加对应正则
        for (String entry : config.perItemRegexRemove.get()) {
            int eq = entry.indexOf('=');
            if (eq <= 0 || eq >= entry.length() - 1) continue;
            if (entry.substring(0, eq).equals(itemName)) {
                compiled.add(Pattern.compile(entry.substring(eq + 1)));
            }
        }

        // 全局正则：仅当物品不在白名单中时才添加
        if (!config.regexRemoveWhitelist.get().contains(itemName)) {
            for (String pattern : config.regexRemove.get()) {
                compiled.add(Pattern.compile(pattern));
            }
        }

        if (compiled.isEmpty()) return;

        // 仿 CRT 过滤逻辑
        List<ITextComponent> kept = new ArrayList<>();
        for (ITextComponent line : event.getToolTip()) {
            String text = line.getString();
            boolean matched = false;
            for (Pattern p : compiled) {
                if (p.matcher(text).find()) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                kept.add(line);
            }
        }

        event.getToolTip().clear();
        event.getToolTip().addAll(kept);
    }
}
