# JMH report 20261106-155210 (full)

- JDK: 25.0.4.1+1-LTS, Windows 10, 28 CPUs
- Commit: 97194331 (dirty working tree)
- Arguments: (none)
- Threshold: 5.0% outside the confidence intervals

| Benchmark | Baseline | Current | Change | Verdict | Baseline run |
| --- | ---: | ---: | ---: | --- | --- |
| EntityCommandBufferContentionBenchmark.enqueueCreate | - | 3020.273 ± 1210.770 ops/ms | - | NEW | - |
| EntityCommandBufferContentionBenchmark.enqueueDestroy | - | 9793.408 ± 9464.284 ops/ms | - | NEW | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=100000] | - | 35446.729 ± 3866.271 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createHugeMeshletBatch [entities=200000] | - | 141328.754 ± 20043.971 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=1000000] | - | 5966.405 ± 406.742 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.createTinyBatch [entities=100000] | - | 62.924 ± 2.346 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=EVERY_OTHER_THEN_REST] | - | 32.746 ± 0.920 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=FORWARD] | - | 35.823 ± 0.695 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=REVERSE] | - | 0.698 ± 0.044 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=10000,order=SHUFFLED] | - | 28.489 ± 0.352 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=EVERY_OTHER_THEN_REST] | - | 4352.362 ± 165.970 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=FORWARD] | - | 4946.417 ± 146.534 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=REVERSE] | - | 52.773 ± 1.404 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=100000,order=SHUFFLED] | - | 4617.087 ± 181.876 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=EVERY_OTHER_THEN_REST] | - | 866.661 ± 12.778 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=FORWARD] | - | 980.015 ± 29.834 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=REVERSE] | - | 14.064 ± 0.770 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.destroyTiny [entities=50000,order=SHUFFLED] | - | 752.376 ± 18.193 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=0] | - | 0.000 ± 0.000 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=50] | - | 0.268 ± 0.018 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.getArrayRangeOnFragmentedPool [holes=90] | - | 0.470 ± 0.009 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=1] | - | 0.006 ± 0.001 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.startQueryAcross255Archetypes [mustHaveParts=8] | - | 0.014 ± 0.000 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=MESHLET] | - | 132.804 ± 3.180 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=2000,payload=TINY] | - | 3.033 ± 0.319 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=MESHLET] | - | 2742.731 ± 1400.350 ms/op | - | NEW | - |
| EntityFlushAbsurdBenchmark.toggleTagOnEveryEntity [entities=20000,payload=TINY] | - | 423.734 ± 53.639 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=12500] | - | 964.311 ± 175.123 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=16] | - | 0.917 ± 0.009 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=2500] | - | 146.374 ± 2.095 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.churnFrame [changed=256] | - | 15.047 ± 0.298 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=25000] | - | 2908.984 ± 112.169 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=2500] | - | 106.004 ± 0.781 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.createMeshlets [meshlets=256] | - | 9.360 ± 0.134 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=FORWARD] | - | 66.604 ± 1.445 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=REVERSE] | - | 72.072 ± 1.472 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=2500,order=SHUFFLED] | - | 71.105 ± 2.343 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=FORWARD] | - | 2589.803 ± 1348.963 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=REVERSE] | - | 3007.965 ± 1135.740 ms/op | - | NEW | - |
| EntityFlushRealisticBenchmark.destroyAllMeshlets [meshlets=25000,order=SHUFFLED] | - | 3773.286 ± 2644.951 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=16384] | - | 46.730 ± 5.056 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.fixedStep [entities=4096] | - | 2.836 ± 0.145 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.geometric [entities=16384] | - | 1.017 ± 0.082 ms/op | - | NEW | - |
| HeapPoolGrowthBenchmark.geometric [entities=4096] | - | 0.297 ± 0.041 ms/op | - | NEW | - |
| MeshletEcsBenchmark.flattenComponent | - | 28.019 ± 0.676 us/op | - | NEW | - |
| MeshletEcsBenchmark.reconstructComponent | - | 20.830 ± 2.910 us/op | - | NEW | - |

Change is positive when the benchmark got slower (or its throughput dropped).
