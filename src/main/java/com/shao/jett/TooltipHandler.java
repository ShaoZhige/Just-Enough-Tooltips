package com.shao.jett;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
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
}
