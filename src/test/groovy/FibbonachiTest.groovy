import jsr166y.ForkJoinPool
import jsr166y.ForkJoinTask

import jsr166y.Phaser

@Typed class FibbonachiTest extends GroovyTestCase {
    void testAsync () {
        ForkJoinPool fjPool = []
        def res = fjPool.invokeTask(42) { Integer arg ->
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
        def res = fjPool.invokeTask(42) { Integer arg ->
            if(arg <= 1 ) {
                return complete(1)
            }

            def f2 = fork(arg-2)

            Integer r1 = runHere(arg-1),
                    r2 = f2.join()

            complete(r1 + r2)
        }
        println res
    }

    abstract static class AsyncResultMerger<V> {
        abstract V merge()
    }

    static abstract class AsyncTask<V> extends ForkJoinTask<V> {
        protected AsyncTask parentTask

        private static class TaskPhaser<V> extends Phaser {
            AsyncResultMerger<V> merger
            AsyncTask<V>         task

            protected boolean onAdvance(int phase, int registeredParties) {
                def merged = merger?.merge()
                if(!task.done)
                    task.complete(merged)
                merger = null
                task = null
                return true
            }

            String toString () {
                "${super.toString()} $task"
            }
        }

        TaskPhaser<V> phaser

        private V rawResult

        final V getRawResult() {
            rawResult
        }

        protected final void setRawResult(V value) {
            rawResult = value
        }

        final void complete(V value) {
            super.complete(value)
            if(completedNormally) {
                parentTask?.phaser?.arrive()
            }
        }

        void completeExceptionally(Throwable ex) {
            super.completeExceptionally(ex)
            parentTask?.phaser?.arrive()
        }

        protected final boolean exec() {
            (phaser = [task:this]).register()
            phaser[merger: split()].arrive()
            phaser.terminated
        }

        protected abstract AsyncResultMerger<V> split()

        public <R> AsyncTask<R> submitSubtask(AsyncTask<R> task) {
            phaser.register()
            pool.submit task[parentTask:this]
        }

        public <R> R runSubtask(AsyncTask<R> task) {
            phaser.register()
            task.exec()
            task.join()
        }

        AsyncTask<V> clone() {
            AsyncTask<V> clone = super.clone()
            clone.clean()
        }

        AsyncTask clean() {
            parentTask    = null
            phaser    = null
            rawResult = null
            this
        }
    }

    static abstract class AsyncTaskWithArg<V,A> extends AsyncTask<V>  implements Cloneable {
        A argument

        AsyncTaskWithArg<V,A> clone() {
            AsyncTaskWithArg<V,A> clone = super.clone()
            clone.argument = null
            clone
        }

        protected final AsyncResultMerger<V> split() {
            doSplit(argument)
        }

        protected final AsyncTaskWithArg<V,A> fork(A arg) {
            submitSubtask( clone()[argument: arg] )
        }

        protected final V runHere(A arg) {
            runSubtask(clone()[argument: arg])
        }

        abstract AsyncResultMerger<V> doSplit(A argument)

        String toString () {
            "${this.class.simpleName}($argument) ${parentTask ? '<- ' + parentTask : ''}"
        }
    }

    static <V,A> V invokeTask(ForkJoinPool pool, A argument, AsyncTaskWithArg<V,A> task) {
        pool.invoke task[argument:argument]
    }
}
