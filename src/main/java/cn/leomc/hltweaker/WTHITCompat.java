package cn.leomc.hltweaker;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import squeek.wthitharvestability.helpers.StringHelper;

@OnlyIn(Dist.CLIENT)
public class WTHITCompat {
    public static void apply() {
        StringHelper.TIER_NAME_GETTERS.add(t -> t instanceof HLTTier hltTier ? Utils.getTierName(hltTier).getString() : null);
    }
}
