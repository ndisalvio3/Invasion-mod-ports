package invmod.client.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class ModelThrower extends LegacyModelBase
{
  public LegacyModelPart bipedHead;
  public LegacyModelPart bipedBody;
  public LegacyModelPart bipedBody2;
  public LegacyModelPart bipedRightArm;
  public LegacyModelPart bipedLeftArm;
  public LegacyModelPart bipedRightLeg;
  public LegacyModelPart bipedLeftLeg;
  public boolean heldItemLeft;
  public boolean heldItemRight;
  public boolean isSneak;

  public ModelThrower()
  {
    this(0.0F);
  }

  public ModelThrower(float f)
  {
    this(f, 0.0F);
  }

  public ModelThrower(float f, float f1)
  {
    this.heldItemLeft = false;
    this.heldItemRight = false;
    this.isSneak = false;

    this.bipedHead = new LegacyModelPart(this, 16, 14);
    this.bipedHead.addBox(-2.0F, -2.0F, -2.0F, 4, 2, 4, 0.0F);
    this.bipedHead.setRotationPoint(0.0F, 16.0F, 4.0F);
    this.bipedHead.rotateAngleX = 0.0F;
    this.bipedHead.rotateAngleY = 0.0F;
    this.bipedHead.rotateAngleZ = 0.0F;
    this.bipedHead.mirror = false;
    this.bipedBody = new LegacyModelPart(this, 0, 1);
    this.bipedBody.addBox(-7.0F, 2.0F, -4.0F, 12, 4, 9, 0.0F);
    this.bipedBody.setRotationPoint(-0.4F, 16.0F, 3.0F);
    this.bipedBody.rotateAngleX = 0.0F;
    this.bipedBody.rotateAngleY = 0.0F;
    this.bipedBody.rotateAngleZ = 0.0F;
    this.bipedBody.mirror = false;
    this.bipedRightArm = new LegacyModelPart(this, 39, 22);
    this.bipedRightArm.addBox(-3.0F, 0.0F, -1.466667F, 3, 7, 3, 0.0F);
    this.bipedRightArm.setRotationPoint(-6.566667F, 16.0F, 5.0F);
    this.bipedRightArm.rotateAngleX = 0.0F;
    this.bipedRightArm.rotateAngleY = 0.0F;
    this.bipedRightArm.rotateAngleZ = 0.0F;
    this.bipedRightArm.mirror = false;
    this.bipedLeftArm = new LegacyModelPart(this, 40, 16);
    this.bipedLeftArm.addBox(0.0F, 0.0F, -1.0F, 2, 4, 2, 0.0F);
    this.bipedLeftArm.setRotationPoint(5.0F, 16.0F, 5.0F);
    this.bipedLeftArm.rotateAngleX = 0.0F;
    this.bipedLeftArm.rotateAngleY = 0.0F;
    this.bipedLeftArm.rotateAngleZ = 0.0F;
    this.bipedLeftArm.mirror = false;
    this.bipedRightLeg = new LegacyModelPart(this, 0, 14);
    this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F);
    this.bipedRightLeg.setRotationPoint(-4.066667F, 22.0F, 4.0F);
    this.bipedRightLeg.rotateAngleX = 0.0F;
    this.bipedRightLeg.rotateAngleY = 0.0F;
    this.bipedRightLeg.rotateAngleZ = 0.0F;
    this.bipedRightLeg.mirror = false;
    this.bipedLeftLeg = new LegacyModelPart(this, 0, 14);
    this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F);
    this.bipedLeftLeg.setRotationPoint(3.0F, 22.0F, 4.0F);
    this.bipedLeftLeg.rotateAngleX = 0.0F;
    this.bipedLeftLeg.rotateAngleY = 0.0F;
    this.bipedLeftLeg.rotateAngleZ = 0.0F;
    this.bipedLeftLeg.mirror = false;
    this.bipedBody2 = new LegacyModelPart(this, 0, 23);
    this.bipedBody2.addBox(-3.666667F, 0.0F, 0.0F, 12, 2, 7, 0.0F);
    this.bipedBody2.setRotationPoint(-3.0F, 16.0F, 0.0F);
    this.bipedBody2.rotateAngleX = 0.0F;
    this.bipedBody2.rotateAngleY = 0.0F;
    this.bipedBody2.rotateAngleZ = 0.0F;
    this.bipedBody2.mirror = false;
  }

  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    this.bipedHead.render(f5);
    this.bipedBody.render(f5);
    this.bipedBody2.render(f5);
    this.bipedRightArm.render(f5);
    this.bipedLeftArm.render(f5);
    this.bipedRightLeg.render(f5);
    this.bipedLeftLeg.render(f5);
  }

  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    this.bipedHead.rotateAngleY = (f3 / 57.29578F);
    this.bipedHead.rotateAngleX = (f4 / 57.29578F);
    this.bipedRightArm.rotateAngleX = (Mth.cos(f * 0.6662F + 3.141593F) * 2.0F * f1 * 0.5F);
    this.bipedLeftArm.rotateAngleX = (Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F);
    this.bipedRightArm.rotateAngleZ = 0.0F;
    this.bipedLeftArm.rotateAngleZ = 0.0F;
    this.bipedRightLeg.rotateAngleX = (Mth.cos(f * 0.6662F) * 1.4F * f1);
    this.bipedLeftLeg.rotateAngleX = (Mth.cos(f * 0.6662F + 3.141593F) * 1.4F * f1);
    this.bipedRightLeg.rotateAngleY = 0.0F;
    this.bipedLeftLeg.rotateAngleY = 0.0F;
    if (this.isRiding)
    {
      this.bipedRightArm.rotateAngleX += -0.6283185F;
      this.bipedLeftArm.rotateAngleX += -0.6283185F;
      this.bipedRightLeg.rotateAngleX = -1.256637F;
      this.bipedLeftLeg.rotateAngleX = -1.256637F;
      this.bipedRightLeg.rotateAngleY = 0.314159F;
      this.bipedLeftLeg.rotateAngleY = -0.314159F;
    }
    if (this.heldItemLeft)
    {
      this.bipedLeftArm.rotateAngleX = (this.bipedLeftArm.rotateAngleX * 0.5F - 0.314159F);
    }
    if (this.heldItemRight)
    {
      this.bipedRightArm.rotateAngleX = (this.bipedRightArm.rotateAngleX * 0.5F - 0.314159F);
    }
    this.bipedRightArm.rotateAngleY = 0.0F;
    this.bipedLeftArm.rotateAngleY = 0.0F;

    this.bipedRightArm.rotateAngleZ += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
    this.bipedLeftArm.rotateAngleZ -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
    this.bipedRightArm.rotateAngleX += Mth.sin(f2 * 0.067F) * 0.05F;
    this.bipedLeftArm.rotateAngleX -= Mth.sin(f2 * 0.067F) * 0.05F;
  }
}