package jozufozu.bubbles.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class SoapBlock extends Block implements ISafeBlock {
    public SoapBlock() {
        super(AbstractBlock.Properties.create(Material.ROCK));
    }
}
