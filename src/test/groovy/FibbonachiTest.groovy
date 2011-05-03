import jsr166y.ForkJoinPool

import org.mbte.groovypp.concurrent.AsyncFjTaskWithArg
import org.mbte.groovypp.concurrent.AsyncFjTask
import java.util.concurrent.ConcurrentHashMap
import org.mbte.groovypp.concurrent.ForkJoinCategory

@Typed class FibbonachiTest extends GroovyShellTestCase implements ForkJoinCategory {

    void testAsync () {
        ForkJoinPool fjPool = []
        def res = fjPool.invokeTask(20) { Integer arg ->
            if(arg <= 1 ) {
                return complete(1)
            }

            def f1 = fork(arg-1)
            def f2 = fork(arg-2)
            return {
               Integer r1 = f1.join(),
                       r2 = f2.join()
               r1 + r2
            }
        }
        println res
    }

    void testSync () {
        ForkJoinPool fjPool = []
        def res = fjPool.invokeTask(20) { Integer arg ->
            if(arg <= 1 ) {
                return complete(1)
            }

            def f2 = fork(arg-2)

            Integer r1 = invoke(arg-1),
                    r2 = f2.join()

            complete(r1 + r2)
        }
        println res
    }

    void testMapReduce () {
        ForkJoinPool fjPool = []
        def res = fjPool.invokeMap(2..<15){ arg ->
            if(arg <= 1 ) {
                return complete(1)
            }

            def f1 = fork(arg-1)
            def f2 = fork(arg-2)

            return {
                Integer r1 = f1.join(),
                        r2 = f2.join()
                r1 + r2
            }
        }
        assert res == [2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610]
    }

    void testMapReduce2 () {
        ForkJoinPool fjPool = []
        def res = fjPool.submitMap(2..<15){ arg ->
            if(arg <= 1 ) {
                return complete(1)
            }

            def f1 = fork(arg-1)
            def f2 = fork(arg-2)

            return {
                Integer r1 = f1.join(),
                        r2 = f2.join()
                r1 + r2
            }
        }.join()
        assert res == [2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610]
    }
}
