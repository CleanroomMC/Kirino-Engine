# JMH report 20261106-165936 (full)

- JDK: 25.0.4.1+1-LTS, Windows 10, 28 CPUs
- Commit: 97194331 (dirty working tree)
- Arguments: -prof gc
- Threshold: 5.0% outside the confidence intervals

| Benchmark | Baseline | Current | Change | Verdict | Baseline run |
| --- | ---: | ---: | ---: | --- | --- |
| EntityCommandBufferContentionBenchmark.enqueueCreate | - | 2958.935 ± 1256.730 ops/ms | - | NEW | - |
| EntityCommandBufferContentionBenchmark.enqueueCreate / gc.alloc.rate.norm | - | 1011.773 ± 44.268 B/op | - | NEW | - |
| EntityCommandBufferContentionBenchmark.enqueueDestroy | - | 10191.274 ± 8732.198 ops/ms | - | NEW | - |
| EntityCommandBufferContentionBenchmark.enqueueDestroy / gc.alloc.rate.norm | - | 66.080 ± 6.665 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=100000] | - | 37304.074 ± 4933.184 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=100000] / gc.alloc.rate.norm | - | 342381632988.800 ± 67.491 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=200000] | - | 143855.053 ± 5123.298 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=200000] / gc.alloc.rate.norm | - | 1362365334617.600 ± 55.106 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=1000000] | - | 6377.598 ± 137.610 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=1000000] / gc.alloc.rate.norm | - | 81431179681.600 ± 55.106 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=100000] | - | 68.302 ± 1.421 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=100000] / gc.alloc.rate.norm | - | 944067872.943 ± 9.475 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=EVERY_OTHER_THEN_REST] | - | 32.826 ± 1.622 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=EVERY_OTHER_THEN_REST] / gc.alloc.rate.norm | - | 24233616.248 ± 25.702 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=FORWARD] | - | 36.387 ± 0.752 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=FORWARD] / gc.alloc.rate.norm | - | 24553623.426 ± 20.327 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=REVERSE] | - | 0.764 ± 0.031 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=REVERSE] / gc.alloc.rate.norm | - | 31481736.274 ± 260.838 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=SHUFFLED] | - | 29.083 ± 0.844 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=SHUFFLED] / gc.alloc.rate.norm | - | 24930318.581 ± 18.258 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=EVERY_OTHER_THEN_REST] | - | 4346.927 ± 112.694 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=EVERY_OTHER_THEN_REST] / gc.alloc.rate.norm | - | 956709004.800 ± 67.491 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=FORWARD] | - | 4948.414 ± 178.627 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=FORWARD] / gc.alloc.rate.norm | - | 956709028.800 ± 122.448 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=REVERSE] | - | 52.753 ± 2.565 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=REVERSE] / gc.alloc.rate.norm | - | 1751504295.948 ± 21.539 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=SHUFFLED] | - | 4491.592 ± 38.020 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=SHUFFLED] / gc.alloc.rate.norm | - | 963196974.400 ± 138.452 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=EVERY_OTHER_THEN_REST] | - | 857.689 ± 31.547 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=EVERY_OTHER_THEN_REST] / gc.alloc.rate.norm | - | 281069466.000 ± 37.333 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=FORWARD] | - | 983.891 ± 34.855 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=FORWARD] / gc.alloc.rate.norm | - | 281070049.067 ± 32.145 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=REVERSE] | - | 13.924 ± 0.385 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=REVERSE] / gc.alloc.rate.norm | - | 477243485.441 ± 2.189 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=SHUFFLED] | - | 752.805 ± 35.746 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=SHUFFLED] / gc.alloc.rate.norm | - | 283765422.800 ± 22.187 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=0] | - | 0.000 ± 0.000 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=0] / gc.alloc.rate.norm | - | 88.000 ± 0.000 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=50] | - | 0.265 ± 0.014 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=50] / gc.alloc.rate.norm | - | 2124360.623 ± 0.032 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=90] | - | 0.462 ± 0.005 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=90] / gc.alloc.rate.norm | - | 3404361.086 ± 0.014 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=1] | - | 0.006 ± 0.001 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=1] / gc.alloc.rate.norm | - | 2072.014 ± 0.002 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=8] | - | 0.014 ± 0.000 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=8] / gc.alloc.rate.norm | - | 80.033 ± 0.001 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=MESHLET] | - | 124.790 ± 5.280 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=MESHLET] / gc.alloc.rate.norm | - | 174949745.229 ± 24893.412 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=TINY] | - | 3.052 ± 0.534 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=TINY] / gc.alloc.rate.norm | - | 3236011.316 ± 550.917 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=MESHLET] | - | 2603.905 ± 1176.735 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=MESHLET] / gc.alloc.rate.norm | - | 1751365427.200 ± 87.402 B/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=TINY] | - | 400.941 ± 48.133 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=TINY] / gc.alloc.rate.norm | - | 32880582.800 ± 9.588 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=12500] | - | 968.114 ± 117.096 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=12500] / gc.alloc.rate.norm | - | 1092862753.467 ± 24181928.215 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=16] | - | 0.914 ± 0.010 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=16] / gc.alloc.rate.norm | - | 1293874.119 ± 730.734 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=2500] | - | 144.604 ± 2.674 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=2500] / gc.alloc.rate.norm | - | 220190139.131 ± 9820654.561 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=256] | - | 15.288 ± 0.554 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=256] / gc.alloc.rate.norm | - | 22523317.394 ± 1044194.507 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=25000] | - | 2788.137 ± 23.632 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=25000] / gc.alloc.rate.norm | - | 22070653094.400 ± 23.369 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=2500] | - | 103.025 ± 0.370 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=2500] / gc.alloc.rate.norm | - | 301140975.520 ± 36.375 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=256] | - | 8.772 ± 0.648 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=256] / gc.alloc.rate.norm | - | 12148414.955 ± 9793.513 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=FORWARD] | - | 65.702 ± 0.590 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=FORWARD] / gc.alloc.rate.norm | - | 427502819.467 ± 1.700 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=REVERSE] | - | 70.577 ± 1.514 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=REVERSE] / gc.alloc.rate.norm | - | 627047066.400 ± 2.082 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=SHUFFLED] | - | 70.566 ± 1.278 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=SHUFFLED] / gc.alloc.rate.norm | - | 443627163.733 ± 1.275 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=FORWARD] | - | 2069.175 ± 1562.265 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=FORWARD] / gc.alloc.rate.norm | - | 23336138713.600 ± 32.654 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=REVERSE] | - | 2469.881 ± 17.426 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=REVERSE] / gc.alloc.rate.norm | - | 44389316364.800 ± 12703.232 B/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=SHUFFLED] | - | 2113.255 ± 412.653 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=SHUFFLED] / gc.alloc.rate.norm | - | 23923189230.400 ± 23.369 B/op | - | NEW | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=16384] | - | 51.492 ± 8.399 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=16384] / gc.alloc.rate.norm | - | 690959473.137 ± 46.145 B/op | - | NEW | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=4096] | - | 2.842 ± 0.445 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=4096] / gc.alloc.rate.norm | - | 43628836.013 ± 3.097 B/op | - | NEW | - |
| HeapPoolGrowthBenchmark.geometric [entities=16384] | - | 0.972 ± 0.088 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.geometric [entities=16384] / gc.alloc.rate.norm | - | 14852886.879 ± 0.862 B/op | - | NEW | - |
| HeapPoolGrowthBenchmark.geometric [entities=4096] | - | 0.282 ± 0.009 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.geometric [entities=4096] / gc.alloc.rate.norm | - | 4374801.985 ± 0.060 B/op | - | NEW | - |
| MeshletEcsBenchmark.flattenComponent | - | 27.876 ± 0.765 us/op | - | NEW | - |
| MeshletEcsBenchmark.flattenComponent / gc.alloc.rate.norm | - | 31856.197 ± 0.005 B/op | - | NEW | - |
| MeshletEcsBenchmark.reconstructComponent | - | 20.368 ± 0.958 us/op | - | NEW | - |
| MeshletEcsBenchmark.reconstructComponent / gc.alloc.rate.norm | - | 46472.143 ± 0.007 B/op | - | NEW | - |
| EntityCommandBufferContentionBenchmark.enqueueCreate / gc.alloc.rate | - | 2824.564 ± 1060.437 MB/sec | - | INFO | - |
| EntityCommandBufferContentionBenchmark.enqueueCreate / gc.count | - | 13.000 counts | - | INFO | - |
| EntityCommandBufferContentionBenchmark.enqueueCreate / gc.time | - | 1342.000 ms | - | INFO | - |
| EntityCommandBufferContentionBenchmark.enqueueDestroy / gc.alloc.rate | - | 620.564 ± 578.736 MB/sec | - | INFO | - |
| EntityCommandBufferContentionBenchmark.enqueueDestroy / gc.count | - | 6.000 counts | - | INFO | - |
| EntityCommandBufferContentionBenchmark.enqueueDestroy / gc.time | - | 532.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=100000] / gc.alloc.rate | - | 8758.484 ± 1146.967 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=100000] / gc.count | - | 362.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=100000] / gc.time | - | 6249.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=200000] / gc.alloc.rate | - | 9030.908 ± 320.925 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=200000] / gc.count | - | 1601.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=200000] / gc.time | - | 48336.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=1000000] / gc.alloc.rate | - | 11951.009 ± 249.612 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=1000000] / gc.count | - | 295.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=1000000] / gc.time | - | 2038.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=100000] / gc.alloc.rate | - | 11321.896 ± 189.659 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=100000] / gc.count | - | 36.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=100000] / gc.time | - | 271.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=EVERY_OTHER_THEN_REST] / gc.alloc.rate | - | 613.796 ± 157.643 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=EVERY_OTHER_THEN_REST] / gc.count | - | 2.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=EVERY_OTHER_THEN_REST] / gc.time | - | 7.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=FORWARD] / gc.alloc.rate | - | 567.759 ± 120.981 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=FORWARD] / gc.count | - | 1.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=FORWARD] / gc.time | - | 4.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=REVERSE] / gc.alloc.rate | - | 6955.478 ± 142.994 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=REVERSE] / gc.count | - | 21.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=REVERSE] / gc.time | - | 37.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=SHUFFLED] / gc.alloc.rate | - | 699.482 ± 152.932 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=SHUFFLED] / gc.count | - | 2.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=SHUFFLED] / gc.time | - | 6.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=EVERY_OTHER_THEN_REST] / gc.alloc.rate | - | 200.296 ± 10.734 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=EVERY_OTHER_THEN_REST] / gc.count | - | 0.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=FORWARD] / gc.alloc.rate | - | 176.664 ± 15.161 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=FORWARD] / gc.count | - | 1.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=FORWARD] / gc.time | - | 13.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=REVERSE] / gc.alloc.rate | - | 12677.391 ± 575.844 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=REVERSE] / gc.count | - | 41.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=REVERSE] / gc.time | - | 331.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=SHUFFLED] / gc.alloc.rate | - | 194.913 ± 13.511 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=SHUFFLED] / gc.count | - | 1.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=SHUFFLED] / gc.time | - | 13.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=EVERY_OTHER_THEN_REST] / gc.alloc.rate | - | 292.432 ± 35.871 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=EVERY_OTHER_THEN_REST] / gc.count | - | 2.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=EVERY_OTHER_THEN_REST] / gc.time | - | 13.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=FORWARD] / gc.alloc.rate | - | 258.030 ± 18.153 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=FORWARD] / gc.count | - | 1.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=FORWARD] / gc.time | - | 7.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=REVERSE] / gc.alloc.rate | - | 11352.405 ± 255.492 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=REVERSE] / gc.count | - | 35.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=REVERSE] / gc.time | - | 158.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=SHUFFLED] / gc.alloc.rate | - | 332.663 ± 28.791 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=SHUFFLED] / gc.count | - | 2.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=SHUFFLED] / gc.time | - | 13.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=0] / gc.alloc.rate | - | 13439.503 ± 1067.278 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=0] / gc.count | - | 41.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=0] / gc.time | - | 62.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=50] / gc.alloc.rate | - | 7639.198 ± 406.926 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=50] / gc.count | - | 23.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=50] / gc.time | - | 45.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=90] / gc.alloc.rate | - | 7020.213 ± 72.645 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=90] / gc.count | - | 21.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=90] / gc.time | - | 43.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=1] / gc.alloc.rate | - | 328.019 ± 39.353 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=1] / gc.count | - | 1.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=1] / gc.time | - | 3.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=8] / gc.alloc.rate | - | 5.374 ± 0.053 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=8] / gc.count | - | 0.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=MESHLET] / gc.alloc.rate | - | 1336.664 ± 56.418 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=MESHLET] / gc.count | - | 4.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=MESHLET] / gc.time | - | 13.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=TINY] / gc.alloc.rate | - | 1003.193 ± 168.065 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=TINY] / gc.count | - | 3.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=TINY] / gc.time | - | 8.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=MESHLET] / gc.alloc.rate | - | 648.589 ± 299.364 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=MESHLET] / gc.count | - | 4.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=MESHLET] / gc.time | - | 53.000 ms | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=TINY] / gc.alloc.rate | - | 78.181 ± 9.685 MB/sec | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=TINY] / gc.count | - | 1.000 counts | - | INFO | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=TINY] / gc.time | - | 4.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=12500] / gc.alloc.rate | - | 1079.257 ± 133.933 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=12500] / gc.count | - | 15.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=12500] / gc.time | - | 187.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=16] / gc.alloc.rate | - | 1344.659 ± 15.094 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=16] / gc.count | - | 15.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=16] / gc.time | - | 196.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=2500] / gc.alloc.rate | - | 1446.407 ± 52.196 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=2500] / gc.count | - | 18.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=2500] / gc.time | - | 218.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=256] / gc.alloc.rate | - | 1399.048 ± 68.446 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=256] / gc.count | - | 17.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=256] / gc.time | - | 199.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=25000] / gc.alloc.rate | - | 7541.272 ± 64.063 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=25000] / gc.count | - | 120.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=25000] / gc.time | - | 719.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=2500] / gc.alloc.rate | - | 2777.872 ± 10.277 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=2500] / gc.count | - | 32.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=2500] / gc.time | - | 54.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=256] / gc.alloc.rate | - | 1317.544 ± 95.775 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=256] / gc.count | - | 15.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=256] / gc.time | - | 33.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=FORWARD] / gc.alloc.rate | - | 2393.559 ± 10.126 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=FORWARD] / gc.count | - | 27.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=FORWARD] / gc.time | - | 52.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=REVERSE] / gc.alloc.rate | - | 3405.901 ± 19.661 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=REVERSE] / gc.count | - | 40.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=REVERSE] / gc.time | - | 72.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=SHUFFLED] / gc.alloc.rate | - | 2417.769 ± 17.872 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=SHUFFLED] / gc.count | - | 28.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=SHUFFLED] / gc.time | - | 52.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=FORWARD] / gc.alloc.rate | - | 4782.175 ± 1625.424 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=FORWARD] / gc.count | - | 128.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=FORWARD] / gc.time | - | 822.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=REVERSE] / gc.alloc.rate | - | 8059.741 ± 40.728 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=REVERSE] / gc.count | - | 242.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=REVERSE] / gc.time | - | 1472.000 ms | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=SHUFFLED] / gc.alloc.rate | - | 4604.225 ± 478.173 MB/sec | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=SHUFFLED] / gc.count | - | 132.000 counts | - | INFO | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=SHUFFLED] / gc.time | - | 817.000 ms | - | INFO | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=16384] / gc.alloc.rate | - | 12813.509 ± 2130.358 MB/sec | - | INFO | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=16384] / gc.count | - | 222.000 counts | - | INFO | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=16384] / gc.time | - | 167.000 ms | - | INFO | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=4096] / gc.alloc.rate | - | 14654.534 ± 2179.215 MB/sec | - | INFO | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=4096] / gc.count | - | 242.000 counts | - | INFO | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=4096] / gc.time | - | 166.000 ms | - | INFO | - |
| HeapPoolGrowthBenchmark.geometric [entities=16384] / gc.alloc.rate | - | 14573.745 ± 1280.090 MB/sec | - | INFO | - |
| HeapPoolGrowthBenchmark.geometric [entities=16384] / gc.count | - | 245.000 counts | - | INFO | - |
| HeapPoolGrowthBenchmark.geometric [entities=16384] / gc.time | - | 190.000 ms | - | INFO | - |
| HeapPoolGrowthBenchmark.geometric [entities=4096] / gc.alloc.rate | - | 14817.618 ± 452.491 MB/sec | - | INFO | - |
| HeapPoolGrowthBenchmark.geometric [entities=4096] / gc.count | - | 244.000 counts | - | INFO | - |
| HeapPoolGrowthBenchmark.geometric [entities=4096] / gc.time | - | 161.000 ms | - | INFO | - |
| MeshletEcsBenchmark.flattenComponent / gc.alloc.rate | - | 1089.673 ± 30.001 MB/sec | - | INFO | - |
| MeshletEcsBenchmark.flattenComponent / gc.count | - | 17.000 counts | - | INFO | - |
| MeshletEcsBenchmark.flattenComponent / gc.time | - | 14.000 ms | - | INFO | - |
| MeshletEcsBenchmark.reconstructComponent / gc.alloc.rate | - | 2175.802 ± 102.478 MB/sec | - | INFO | - |
| MeshletEcsBenchmark.reconstructComponent / gc.count | - | 35.000 counts | - | INFO | - |
| MeshletEcsBenchmark.reconstructComponent / gc.time | - | 27.000 ms | - | INFO | - |

Positive change means slower execution, lower throughput or more bytes allocated per operation.
Allocation per operation is compared using the same threshold as timing. Allocation rate, GC count and GC time are informational.
GC metrics may include invocation setup allocations. They do not measure retained heap or prove a memory leak.
