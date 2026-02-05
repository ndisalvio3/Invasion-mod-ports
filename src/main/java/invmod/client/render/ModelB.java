package invmod.client.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;

public class ModelB extends LegacyModelBase
{
  private LegacyModelPart batHead;
  private LegacyModelPart batBody;
  private LegacyModelPart batRightWing;
  private LegacyModelPart batLeftWing;
  private LegacyModelPart batOuterRightWing;
  private LegacyModelPart batOuterLeftWing;

  public ModelB()
  {
    this.textureWidth = 64;
    this.textureHeight = 64;
    this.batHead = new LegacyModelPart(this, 0, 0);
    this.batHead.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6);
    LegacyModelPart modelrenderer = new LegacyModelPart(this, 24, 0);
    modelrenderer.addBox(-4.0F, -6.0F, -2.0F, 3, 4, 1);
    this.batHead.addChild(modelrenderer);
    LegacyModelPart modelrenderer1 = new LegacyModelPart(this, 24, 0);
    modelrenderer1.mirror = true;
    modelrenderer1.addBox(1.0F, -6.0F, -2.0F, 3, 4, 1);
    this.batHead.addChild(modelrenderer1);
    this.batBody = new LegacyModelPart(this, 0, 16);
    this.batBody.addBox(-3.0F, 4.0F, -3.0F, 6, 12, 6);
    this.batBody.setTextureOffset(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10, 6, 1);
    this.batRightWing = new LegacyModelPart(this, 42, 0);
    this.batRightWing.addBox(-12.0F, 1.0F, 1.5F, 10, 16, 1);
    this.batOuterRightWing = new LegacyModelPart(this, 24, 16);
    this.batOuterRightWing.setRotationPoint(-12.0F, 1.0F, 1.5F);
    this.batOuterRightWing.addBox(-8.0F, 1.0F, 0.0F, 8, 12, 1);
    this.batLeftWing = new LegacyModelPart(this, 42, 0);
    this.batLeftWing.mirror = true;
    this.batLeftWing.addBox(2.0F, 1.0F, 1.5F, 10, 16, 1);
    this.batOuterLeftWing = new LegacyModelPart(this, 24, 16);
    this.batOuterLeftWing.mirror = true;
    this.batOuterLeftWing.setRotationPoint(12.0F, 1.0F, 1.5F);
    this.batOuterLeftWing.addBox(0.0F, 1.0F, 0.0F, 8, 12, 1);
    this.batBody.addChild(this.batRightWing);
    this.batBody.addChild(this.batLeftWing);
    this.batRightWing.addChild(this.batOuterRightWing);
    this.batLeftWing.addChild(this.batOuterLeftWing);
  }

  public int getBatSize()
  {
    return 36;
  }

  public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
  {
    this.batHead.rotateAngleX = (par6 / 57.295776F);
    this.batHead.rotateAngleY = (par5 / 57.295776F);
    this.batHead.rotateAngleZ = 0.0F;
    this.batHead.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.batRightWing.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.batLeftWing.setRotationPoint(0.0F, 0.0F, 0.0F);
    this.batBody.rotateAngleX = (0.7853982F + Mth.cos(par4 * 0.1F) * 0.15F);
    this.batBody.rotateAngleY = 0.0F;
    this.batRightWing.rotateAngleY = (Mth.cos(par4 * 1.3F) * 3.141593F * 0.25F);
    this.batLeftWing.rotateAngleY = (-this.batRightWing.rotateAngleY);
    this.batOuterRightWing.rotateAngleY = (this.batRightWing.rotateAngleY * 0.5F);
    this.batOuterLeftWing.rotateAngleY = (-this.batRightWing.rotateAngleY * 0.5F);

    this.batHead.render(par7);
    this.batBody.render(par7);
  }
}