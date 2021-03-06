package fathertoast.specialai.ai.elite;

import java.util.UUID;

import fathertoast.specialai.ai.*;
import fathertoast.specialai.config.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public
class EntityAILeap extends EntityAIBase implements IEliteAI
{
	// The weight of this AI pattern.
	private int WEIGHT;
	
	// The owner of this AI.
	protected EntityLiving theEntity;
	
	EntityAILeap( ) { }
	
	private
	EntityAILeap( EntityLiving entity )
	{
		this.theEntity = entity;
		this.setMutexBits( AIHandler.BIT_SWIMMING );
	}
	
	// Returns the string name of this AI for use in Properties.
	@Override
	public
	String getName( )
	{
		return "leap";
	}
	
	// Gets/sets the weight as defined in Properties.
	@Override
	public
	int getWeight( )
	{
		return this.WEIGHT;
	}
	
	@Override
	public
	void setWeight( int weight )
	{
		this.WEIGHT = weight;
	}
	
	// Adds a copy of this AI to the given entity.
	@Override
	public
	void addTo( EntityLiving entity, NBTTagCompound aiTag )
	{
		entity.tasks.addTask( 0, new EntityAILeap( entity ) );
	}
	
	// Saves this AI to the tag with its default value.
	@Override
	public
	void save( NBTTagCompound aiTag )
	{
		aiTag.setByte( this.getName( ), (byte) 1 );
	}
	
	// Returns true if a copy of this AI is saved to the tag.
	@Override
	public
	boolean isSaved( NBTTagCompound aiTag )
	{
		return aiTag.getByte( this.getName( ) ) > 0;
	}
	
	// Initializes any one-time effects on the entity.
	@Override
	public
	void initialize( EntityLiving entity )
	{
		ItemStack held = entity.getHeldItemMainhand( );
		if( !held.isEmpty( ) && held.getItem( ) instanceof ItemBow ) {
			entity.setHeldItem( EnumHand.MAIN_HAND, new ItemStack( Items.GOLDEN_SWORD ) );
		}
		
		entity.getEntityAttribute( SharedMonsterAttributes.MOVEMENT_SPEED ).applyModifier( new AttributeModifier(
			UUID.randomUUID( ), "Leaper speed boost",
			Config.get( ).ELITE_AI.LEAP_SPEED_BOOST, 1
		) );
	}
	
	// Returns whether the AI should begin execution.
	@Override
	public
	boolean shouldExecute( )
	{
		if( !this.theEntity.onGround || this.theEntity.isRiding( ) || this.theEntity.getRNG( ).nextInt( 5 ) != 0 )
			return false;
		EntityLivingBase target = this.theEntity.getAttackTarget( );
		if( target != null ) {
			double distance = this.theEntity.getDistanceSq( target );
			return distance <= 36.0 && distance >= 4.0;
		}
		return false;
	}
	
	// Called once when the AI begins execution.
	@Override
	public
	void startExecuting( )
	{
		EntityLivingBase target = this.theEntity.getAttackTarget( );
		if( target == null )
			return;
		
		this.theEntity.getLookHelper( ).setLookPositionWithEntity( target, 180.0F, 0.0F );
		double dX = target.posX - this.theEntity.posX;
		double dZ = target.posZ - this.theEntity.posZ;
		double dH = Math.sqrt( dX * dX + dZ * dZ );
		this.theEntity.motionX = dX / dH * 0.8 + this.theEntity.motionX * 0.2;
		this.theEntity.motionZ = dZ / dH * 0.8 + this.theEntity.motionZ * 0.2;
		this.theEntity.motionY = 0.4;
	}
	
	// Returns whether an in-progress EntityAIBase should continue executing
	@Override
	public
	boolean shouldContinueExecuting( )
	{
		return false;
	}
}
