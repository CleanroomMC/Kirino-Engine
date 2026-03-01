- `Minecraft#shutdownMinecraftApplet`
  ```java
  try
  {
      LOGGER.info("Stopping!");
      try
      {
          this.loadWorld(null);
      }
      catch (Throwable throwable)
      {
      }
  
      this.soundHandler.unloadSounds();
  +   com.cleanroommc.kirino.gl.GLResourceManager.disposeAll();
  }
  ```

- `RenderGlobal#notifyBlockUpdate` (see `KirinoClientCore#RenderGlobal$notifyBlockUpdate`)
  ```java
  @Override
  public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags)
  {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1, (flags & 8) != 0);
  +   com.cleanroommc.kirino.KirinoClientCore.RenderGlobal$notifyBlockUpdate(i, j, k, oldState, newState);
  }
  ```

- `RenderGlobal#notifyLightSet` (see `KirinoClientCore#RenderGlobal$notifyLightUpdate`)
  ```java
  @Override
  public void notifyLightSet(BlockPos pos)
  {
      this.setLightUpdates.add(pos.toImmutable());
  +   com.cleanroommc.kirino.KirinoClientCore.RenderGlobal$notifyLightUpdate(pos.getX(), pos.getY(), pos.getZ());
  }
  ```
  
- `ChunkProviderClient`
  ```java
  ...
      private final World world;

  +   private java.util.function.BiConsumer<Integer, Integer> loadChunkCallback = null;
  +   private java.util.function.BiConsumer<Integer, Integer> unloadChunkCallback = null;

      public ChunkProviderClient(World worldIn)
  ...
  ```
  ```java
  public void unloadChunk(int x, int z)
  {
      Chunk chunk = this.provideChunk(x, z);

      if (!chunk.isEmpty())
      {
          chunk.onUnload();
      }

      this.loadedChunks.remove(ChunkPos.asLong(x, z));
  +   if (unloadChunkCallback != null)
  +   {
  +       unloadChunkCallback.accept(x, z);
  +   }
  }
  ```
  ```java
  public Chunk loadChunk(int chunkX, int chunkZ)
  {
      Chunk chunk = new Chunk(this.world, chunkX, chunkZ);
      this.loadedChunks.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
      chunk.markLoaded(true);
  +   if (loadChunkCallback != null)
  +   {
  +       loadChunkCallback.accept(chunkX, chunkZ);
  +   }
      return chunk;
  }
  ```

- `ChunkPos`
  ```java
  ...
  +   public static int getX(long key)
  +   {
  +       return (int) (key & 0xFFFFFFFFL);
  +   }
  
  +   public static int getZ(long key)
  +   {
  +       return (int) ((key >>> 32) & 0xFFFFFFFFL);
  +   }
  ...
  ```

- `EntityRenderer#updateCameraAndRender`
  ```java
  ...
  -   this.renderWorld(partialTicks, System.nanoTime() + l);
      
  +   if (com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnable()
  +           && com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnableRenderDelegate()
  +           && !com.cleanroommc.kirino.KirinoClientCore.isRenderUnsupported())
  +   {
  +       com.cleanroommc.kirino.KirinoClientCore.EntityRenderer$renderWorld(System.nanoTime() + l);
  +   }
  +   else
  +   {
  +       this.renderWorld(partialTicks, System.nanoTime() + l);
  +   }
  ...
  ```

- `EntityRenderer#renderWorldPass`
  ```java
  private void renderWorldPass(int pass, float partialTicks, long finishTimeNano)
  {
  +   if (com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnable()
  +           && (!com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnableRenderDelegate()
  +           || com.cleanroommc.kirino.KirinoClientCore.isRenderUnsupported()))
  +   {
  +       if (com.cleanroommc.kirino.KirinoCommonCore.KIRINO_ENGINE.nextExpectedPhase() == com.cleanroommc.kirino.engine.FramePhase.PREPARE)
  +       {
  +           com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.PREPARE);
  +       }
  +       com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.PRE_UPDATE);
  +   }
  ...
  ```
  ```java
  ...
      if (entity.posY + (double)entity.getEyeHeight() < 128.0)
      {
          this.renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
      }

  +   if (com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnable()
  +           && (!com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnableRenderDelegate()
  +           || com.cleanroommc.kirino.KirinoClientCore.isRenderUnsupported()))
  +   {
  +       com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.UPDATE);
  +       com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.RENDER_OPAQUE);
  +   }

      this.mc.profiler.endStartSection("prepareterrain");
  ...
  ```
  ```java
  ...
      GlStateManager.shadeModel(7425);

  +   if (com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnable()
  +           && (!com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnableRenderDelegate()
  +           || com.cleanroommc.kirino.KirinoClientCore.isRenderUnsupported()))
  +   {
  +       com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.RENDER_TRANSPARENT);
  +   }

      this.mc.profiler.endStartSection("translucent");
  ...
  ```
  ```java
  ...
  +   if (com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnable()
  +           && (!com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB.isEnableRenderDelegate()
  +           || com.cleanroommc.kirino.KirinoClientCore.isRenderUnsupported()))
  +   {
  +       com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.POST_UPDATE);
  +       com.cleanroommc.kirino.KirinoClientCore.runHeadlessly(com.cleanroommc.kirino.engine.FramePhase.RENDER_OVERLAY);
  +   }
  }
  ```


