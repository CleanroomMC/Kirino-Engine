- KirinoCore
- ChunkProviderClient#getLoadedChunks
- ChunkProviderClient
  ```java
  public java.util.function.BiConsumer<Integer, Integer> loadChunkCallback = null;
  public java.util.function.BiConsumer<Integer, Integer> unloadChunkCallback = null;

  public void unloadChunk(int x, int z)
  {
      ...
      if (unloadChunkCallback != null)
      {
          unloadChunkCallback.accept(x, z);
      }
  }

  public Chunk loadChunk(int chunkX, int chunkZ)
  {
      ...
      if (loadChunkCallback != null)
      {
          loadChunkCallback.accept(chunkX, chunkZ);
      }
      return chunk;
  }
  ```
- ChunkPos
  ```java
  public static int getX(long key)
  {
      return (int) (key & 0xFFFFFFFFL);
  }

  public static int getZ(long key)
  {
      return (int) ((key >>> 32) & 0xFFFFFFFFL);
  }
  ```
- FMLClientHandler
  ```java
  public void beginMinecraftLoading(Minecraft minecraft, List<IResourcePack> resourcePackList, IReloadableResourceManager resourceManager, MetadataSerializer metaSerializer)
  {
      ...
      com.cleanroommc.kirino.KirinoCore.init();
  }
  
  public void finishMinecraftLoading()
  {
      ...
      com.cleanroommc.kirino.KirinoCore.postInit();
  }
  ```
- EntityRenderer
  ```java
  private void setupCameraTransform(float partialTicks, int pass)
  {
      this.farPlaneDistance = Math.max(16f, (float)((this.mc.gameSettings.renderDistanceChunks - 1) * 16));
      ...
  }
  ```
