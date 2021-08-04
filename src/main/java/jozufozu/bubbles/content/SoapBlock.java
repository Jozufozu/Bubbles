package jozufozu.bubbles.content;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;

public class SoapBlock extends Block implements ISafeBlock {
    public static final Material SOAP = new Material(MaterialColor.COLOR_PINK, false, true, true, true, false, false, PushReaction.NORMAL);

    public SoapBlock() {
        super(AbstractBlock.Properties.of(SOAP).friction(0.99f));
    }
}
