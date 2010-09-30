// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.dataflow

import spock.lang.Specification

//todo test priorities, guards, selector + guards, prioritySelector + guards
//todo make sure the list of requests gets cleaned up
class AltSelectTest extends Specification {
    def "selecting from three df variables"() {
        given:
        def a = new DataFlowVariable()
        def b = new DataFlowVariable()
        def c = new DataFlowVariable()
        def select = DataFlow.select(a, b, c)
        when:
        b << 10
        then:
        select.select() == [1, 10] as SelectResult
    }

    def "selecting from three df variables with value being bound is a separate thread"() {
        given:
        def a = new DataFlowVariable()
        def b = new DataFlowVariable()
        def c = new DataFlowVariable()
        def select = DataFlow.select(a, b, c)
        when:
        Thread.start {
            sleep 3000
            b << 10
        }
        and:
        def res1 = select.select()
        c << 20
        def res2 = select.select()

        then:
        res1 == [1, 10] as SelectResult
        res2 == [2, 20] as SelectResult
    }

    def "selecting from three df variables with a value being bound prior to selector creation"() {
        given:
        def a = new DataFlowVariable()
        def b = new DataFlowVariable()
        def c = new DataFlowVariable()
        c << 20
        when:
        def select = DataFlow.select(a, b, c)
        then:
        select() == [2, 20] as SelectResult
    }

    def "selecting from three df streams"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        def select = DataFlow.select(a, b, c)
        when:
        b << 10
        then:
        select() == [1, 10] as SelectResult
    }

    def "selecting from three df streams with value being bound is a separate thread"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        def select = DataFlow.select(a, b, c)
        when:
        Thread.start {
            sleep 3000
            b << 10
        }
        and:
        def res1 = select.select()
        c << 20
        def res2 = select.select()

        then:
        res1 == [1, 10] as SelectResult
        res2 == [2, 20] as SelectResult
    }

    def "selecting from three df streams with a value being bound prior to selector creation"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        c << 20
        when:
        def select = DataFlow.select(a, b, c)
        then:
        select() == [2, 20] as SelectResult
    }

    def "selecting preserves order within a single stream"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        def select = DataFlow.select(a, b, c)
        when:
        b << 10
        b << 20
        b << 30
        then:
        select() == [1, 10] as SelectResult
        select() == [1, 20] as SelectResult
        select() == [1, 30] as SelectResult
    }

    def "selecting across streams"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        def select = DataFlow.select(a, b, c)
        when:
        b << 10
        a << 20
        b << 30
        c << 40

        def possibleResults = [
                [1, 10] as SelectResult,
                [0, 20] as SelectResult,
                [1, 30] as SelectResult,
                [2, 40] as SelectResult
        ]
        def res1 = select()
        def res2 = select()
        def res3 = select()
        def res4 = select()
        then:
        res1 in possibleResults
        res2 in possibleResults - [res1]
        res3 in possibleResults - [res1, res2]
        res4 in possibleResults - [res1, res2, res3]
    }

    def "select with quards"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        c << 20
        a << 30
        when:
        def select = DataFlow.select(a, b, c)
        b << 5
        b << 10
        then:
        select([true, false, false]) == [0, 30] as SelectResult
        select([true, false, true]) == [2, 20] as SelectResult
        select([true, true, false]) == [1, 5] as SelectResult
        select([true, true, true]) == [1, 10] as SelectResult
    }

    def "priority select"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        c << 20
        a << 30
        when:
        def select = DataFlow.select(a, b, c)
        b << 5
        b << 10
        then:
        select.prioritySelect() == [0, 30] as SelectResult
        select.prioritySelect() == [1, 5] as SelectResult
        select.prioritySelect() == [1, 10] as SelectResult
        select.prioritySelect() == [2, 20] as SelectResult
    }

    def "priority select with quards"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        c << 20
        a << 30
        when:
        def select = DataFlow.select(a, b, c)
        b << 5
        b << 10
        then:
        select.prioritySelect([true, true, false]) == [0, 30] as SelectResult
        select.prioritySelect([true, false, true]) == [2, 20] as SelectResult
        select.prioritySelect([true, true, false]) == [1, 5] as SelectResult
        select.prioritySelect([true, true, false]) == [1, 10] as SelectResult
    }

    def "priority select from one value"() {
        given:
        def a = new DataFlowStream()
        def b = new DataFlowStream()
        def c = new DataFlowStream()
        c << 20
        when:
        def select = DataFlow.select(a, b, c)
        then:
        select.prioritySelect() == [2, 20] as SelectResult
    }
}
