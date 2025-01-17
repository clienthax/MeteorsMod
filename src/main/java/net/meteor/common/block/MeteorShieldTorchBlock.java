package net.meteor.common.block;

import net.meteor.common.IMeteorShield;
import net.meteor.common.MeteorBlocks;
import net.meteor.common.MeteorsMod;
import net.meteor.common.climate.HandlerMeteor;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeteorShieldTorchBlock extends BlockTorch {

    private final boolean isActive;

    // TODO - investigate whether we need the active differential
    public MeteorShieldTorchBlock(boolean active) {
        super();
        setTickRandomly(true);
        this.isActive = active;
        this.setHardness(0.0F).setLightLevel(0.5F);
        this.setSoundType(SoundType.WOOD);
    }

    @Override
    public int tickRate(World world) {
        return 2;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        checkArea(worldIn, pos, state);
    }

    private void checkArea(World world, BlockPos pos, IBlockState state) {
        final HandlerMeteor handlerMeteor = MeteorsMod.proxy.metHandlers.get(world.provider.getDimension());
        boolean isSafeChunk = handlerMeteor.getShieldManager().getClosestShieldInRange(pos.getX(), pos.getZ()) != null;
        if (this.isActive) {
            if (!isSafeChunk) {
                world.setBlockState(pos, MeteorBlocks.METEOR_SHIELD_TORCH_INACTIVE.getDefaultState().withProperty(FACING, state.getValue(FACING)), Constants.BlockFlags.DEFAULT);
            }
        } else if (isSafeChunk) {
            world.setBlockState(pos, MeteorBlocks.METEOR_SHIELD_TORCH.getDefaultState().withProperty(FACING, state.getValue(FACING)), Constants.BlockFlags.DEFAULT);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(MeteorBlocks.METEOR_SHIELD_TORCH);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (this.isActive) {
            checkArea(worldIn, pos, state);
        }
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing,
        float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            checkArea(world, pos, state);
            HandlerMeteor meteorHandler = MeteorsMod.proxy.metHandlers.get(world.provider.getDimension());
            List<IMeteorShield> shields = meteorHandler.getShieldManager().getShieldsInRange(pos.getX(), pos.getZ());
            if (!shields.isEmpty()) {
                player.sendMessage(new TextComponentString(I18n.translateToLocal("ProtectionTorch.landOwnership")));
                List owners = new ArrayList();
                for (IMeteorShield oPair : shields) {
                    if (!owners.contains(oPair.getOwner())) {
                        owners.add(oPair.getOwner());
                    }
                }

                for (Object owner : owners) {
                    player.sendMessage(new TextComponentString("    - " + (String) owner));
                }
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.LIGHT_PURPLE + I18n.translateToLocal("ProtectionTorch.usage"));
    }


}