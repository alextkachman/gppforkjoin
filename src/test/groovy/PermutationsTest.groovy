/*
 * Copyright 2009-2010 MBTE Sweden AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import jsr166y.ForkJoinPool
import org.mbte.groovypp.concurrent.ForkJoinCategory
import jsr166y.ForkJoinTask
import groovypp.channels.ExecutingChannel
import java.util.concurrent.Executors

@Typed
@Use(ForkJoinCategory)
class PermutationsTest extends GroovyTestCase {
    void testPermutations() {
        ForkJoinPool fjPool = []
        fjPool.invokeTask((0..8) as LinkedHashSet) { set ->
            if(set.size() == 1) {
                complete([set])
            }
            else {
                List<Pair<Object,ForkJoinTask<List<LinkedHashSet>>>> results = []
                for(el in set) {
                    LinkedHashSet cloned = set.clone ()
                    cloned.remove(el)
                    results << [el, fork(cloned)]
                }

                {  ->
                    List<LinkedHashSet> result = []
                    for(pair in results) {
                        def el = pair.first
                        def elResults = pair.second.get()
                        for(elResult in elResults) {
                            elResult << el
                            result << elResult
                        }
                    }
                    result
                }
            }
        }.each {
            println it
        }
    }

    void testPermutationsNoStoring() {
        ForkJoinPool fjPool = []
        fjPool.invokeTask([(0..50) as LinkedHashSet, [] as LinkedHashSet] as Pair) { set ->
            if(set.first.size() == 0) {
                println set.second
            }
            else {
                for(el in set.first) {
                    LinkedHashSet clonedSet    = set.first.clone ()
                    clonedSet.remove(el)

                    LinkedHashSet clonedResult = set.second.clone ()
                    clonedResult.add(el)

                    fork((Pair)[clonedSet, clonedResult])
                }
            }
        }
    }
}
