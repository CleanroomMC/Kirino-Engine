package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.cleanroommc.kirino.ecs.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.jspecify.annotations.NonNull;

/**
 * Query creation + checking 64 archetypes, ns per full pass &mdash; first run:
 *
 * <table class="striped">
 *   <caption>Benchmark: Query creation + checking 64 archetypes, ns per full pass &mdash; first run</caption>
 *   <thead>
 *     <tr>
 *       <th scope="col">Collection</th>
 *       <th scope="col" style="text-align:right">1 condition</th>
 *       <th scope="col" style="text-align:right">4+2 conditions</th>
 *       <th scope="col" style="text-align:right">4+2 repeated &times;4</th>
 *       <th scope="col" style="text-align:right">16+4 conditions</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>ArrayList</td>
 *       <td style="text-align:right">156</td>
 *       <td style="text-align:right">601</td>
 *       <td style="text-align:right">1664</td>
 *       <td style="text-align:right">1973</td>
 *     </tr>
 *     <tr>
 *       <td>ObjectArrayList</td>
 *       <td style="text-align:right">150</td>
 *       <td style="text-align:right"><b>391</b></td>
 *       <td style="text-align:right">1027</td>
 *       <td style="text-align:right"><b>1685</b></td>
 *     </tr>
 *     <tr>
 *       <td>ReferenceArrayList</td>
 *       <td style="text-align:right">154</td>
 *       <td style="text-align:right">523</td>
 *       <td style="text-align:right">1103</td>
 *       <td style="text-align:right">1777</td>
 *     </tr>
 *     <tr>
 *       <td>ReferenceArraySet</td>
 *       <td style="text-align:right"><b>141</b></td>
 *       <td style="text-align:right">413</td>
 *       <td style="text-align:right"><b>442</b></td>
 *       <td style="text-align:right">1697</td>
 *     </tr>
 *     <tr>
 *       <td>ReferenceOpenHashSet</td>
 *       <td style="text-align:right">339</td>
 *       <td style="text-align:right">811</td>
 *       <td style="text-align:right">857</td>
 *       <td style="text-align:right">3007</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public final class EntityQuery {
    final ReferenceSet<Class<? extends CleanComponent>> mustHave;
    final ReferenceSet<Class<? extends CleanComponent>> mustNotHave;

    private EntityQuery() {
        mustHave = new ReferenceArraySet<>();
        mustNotHave = new ReferenceArraySet<>();
    }

    @NonNull
    static EntityQuery query() {
        return new EntityQuery();
    }

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li><code>component</code> is valid and registered</li>
     * </ul>
     *
     * @param component The component class
     * @return The query object
     */
    @CanIgnoreReturnValue
    @NonNull
    public EntityQuery with(@NonNull Class<? extends CleanComponent> component) {
        mustHave.add(component);
        return this;
    }

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li><code>component</code> is valid and registered</li>
     * </ul>
     *
     * @param component The component class
     * @return The query object
     */
    @CanIgnoreReturnValue
    @NonNull
    public EntityQuery without(@NonNull Class<? extends CleanComponent> component) {
        mustNotHave.add(component);
        return this;
    }
}
