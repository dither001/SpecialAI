package fathertoast.specialai.ai;

import fathertoast.specialai.config.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntitySlime;

import java.util.Collections;
import java.util.List;

public
class EntityAIRider extends EntityAIBase
{
	// The owner of this AI.
	protected EntityLiving theEntity;
	
	// True if this mob is small.
	private boolean      isSmall;
	// The mob the host wants to mount.
	private EntityLiving target;
	// Ticks until the entity will search for a mount.
	private int          checkTime;
	// Ticks until the entity gives up.
	private int          giveUpDelay;
	
	public
	EntityAIRider( EntityLiving entity, boolean small )
	{
		this.theEntity = entity;
		this.isSmall = small;
		this.setMutexBits( AIHandler.BIT_MOVEMENT | AIHandler.BIT_FACING );
	}
	
	// Returns whether the AI should begin execution.
	@Override
	public
	boolean shouldExecute( )
	{
		if( !this.theEntity.isRiding( ) && ++this.checkTime > 50 ) {
			this.checkTime = 0;
			return this.findNearbyMount( );
		}
		return false;
	}
	
	// Returns whether an in-progress EntityAIBase should continue executing.
	@Override
	public
	boolean shouldContinueExecuting( )
	{
		return !this.theEntity.isRiding( ) && this.target != null && !this.target.isBeingRidden( ) && this.target.isEntityAlive( ) && !this.theEntity.getNavigator( ).noPath( );
	}
	
	// Determine if this AI task is interruptible by a higher priority task.
	@Override
	public
	boolean isInterruptible( )
	{
		return false;
	}
	
	// Execute a one shot task or start executing a continuous task.
	@Override
	public
	void startExecuting( )
	{
		this.theEntity.getNavigator( ).tryMoveToEntityLiving( this.target, 1.3 );
	}
	
	// Called every tick while this AI is executing.
	@Override
	public
	void updateTask( )
	{
		this.theEntity.getLookHelper( ).setLookPositionWithEntity( this.target, 30.0F, 30.0F );
		
		double range = this.theEntity.width * 2.0F * this.theEntity.width * 2.0F + this.target.width;
		if( !this.target.isBeingRidden( ) && this.theEntity.getDistanceSq( this.target.posX, this.target.getEntityBoundingBox( ).minY, this.target.posZ ) <= range ) {
			this.theEntity.startRiding( this.target, true );
			this.target = null;
		}
		else if( ++this.giveUpDelay > 400 ) {
			this.theEntity.getNavigator( ).clearPath( );
		}
		else {
			if( this.theEntity.getNavigator( ).noPath( ) ) {
				this.theEntity.getNavigator( ).tryMoveToEntityLiving( this.target, 1.3 );
			}
		}
	}
	
	// Resets the task.
	@Override
	public
	void resetTask( )
	{
		this.theEntity.getNavigator( ).clearPath( );
		this.giveUpDelay = 0;
		this.target = null;
	}
	
	// Searches for a nearby mount and targets it. Returns true if one is found.
	private
	boolean findNearbyMount( )
	{
		List list = this.theEntity.world.getEntitiesWithinAABBExcludingEntity( this.theEntity, this.theEntity.getEntityBoundingBox( ).expand( 16.0, 8.0, 16.0 ) );
		Collections.shuffle( list );
		for( Object entity : list ) {
			if( entity instanceof EntityLiving && this.isValidTarget( (EntityLiving) entity ) ) {
				this.target = (EntityLiving) entity;
				return true;
			}
		}
		return false;
	}
	
	// Returns true if the entity can be mounted by the host.
	private
	boolean isValidTarget( EntityLiving entity )
	{
		if( this.isSmall || this.theEntity.isChild( ) || this.theEntity instanceof EntitySlime && ((EntitySlime) this.theEntity).isSmallSlime( ) )
			return Config.get( ).JOCKEYS.MOUNT_LIST_SMALL.contains( entity ) || entity.isChild( ) && Config.get( ).JOCKEYS.MOUNT_LIST.contains( entity );
		return !entity.isChild( ) && Config.get( ).JOCKEYS.MOUNT_LIST.contains( entity );
	}
}
