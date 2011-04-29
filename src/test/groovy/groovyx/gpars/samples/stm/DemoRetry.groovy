// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.samples.stm

import groovyx.gpars.stm.GParsStm
import org.multiverse.api.AtomicBlock
import org.multiverse.api.PropagationLevel
import static org.multiverse.api.StmUtils.newIntRef

final AtomicBlock block = GParsStm.createAtomicBlock(maxRetries: 3000, familyName: 'Custom', PropagationLevel: PropagationLevel.Requires, interruptible: false)

def counter = newIntRef(0)
final int max = 100
Thread.start {
    while (counter.atomicGet() < max) {
        counter.atomicIncrementAndGet(1)
        sleep 10
    }
}
assert max + 1 == GParsStm.atomicWithInt(block) {tx ->
    if (counter.get() == max) return counter.get() + 1
    tx.retry()
}
